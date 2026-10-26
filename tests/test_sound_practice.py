import queue
import sys
import unittest
from pathlib import Path
from unittest.mock import Mock

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / 'recognition'))
from sound_practice import SoundPractice, SOUNDS


class SoundPracticeTest(unittest.TestCase):
    def test_inactive_input_clears_stale_level(self):
        practice = SoundPractice.__new__(SoundPractice)
        practice.stream = Mock(active=False)
        practice.messages = queue.Queue()
        practice.messages.put((.5, .7, ''))
        practice.level, practice.status = Mock(), Mock()
        practice.closed = True
        practice.poll()
        self.assertIsNone(practice.stream)
        practice.level.set.assert_called_with(0)
        self.assertIn('stopped or disconnected', practice.status.set.call_args.args[0])

    def test_invalid_audio_cannot_make_a_nonfinite_meter(self):
        practice = SoundPractice.__new__(SoundPractice)
        practice.messages = queue.Queue()
        practice.receive(np.array([[float('nan')]]), 1, None, '')
        self.assertEqual(practice.messages.get_nowait(), (0, 0, 'invalid or empty audio input'))

    def test_failed_stop_still_attempts_abort_and_close(self):
        practice = SoundPractice.__new__(SoundPractice)
        stream = practice.stream = Mock()
        stream.stop.side_effect = RuntimeError('device unplugged')
        practice.messages = queue.Queue()
        practice.level, practice.status = Mock(), Mock()
        practice.stop()
        stream.abort.assert_called_once()
        stream.close.assert_called_once()
        self.assertIsNone(practice.stream)

    def test_callback_keeps_scalars_only_and_bounds_queue(self):
        practice = SoundPractice.__new__(SoundPractice)
        practice.messages = queue.Queue(maxsize=2)
        data = np.full((128, 1), .25, dtype=np.float32)
        for _ in range(10):
            practice.receive(data, 128, None, '')
        self.assertEqual(practice.messages.qsize(), 2)
        rms, peak, status = practice.messages.get_nowait()
        self.assertEqual((rms, peak, status), (.25, .25, ''))

    def test_stop_releases_device_and_discards_pending_levels(self):
        practice = SoundPractice.__new__(SoundPractice)
        stream = practice.stream = Mock()
        practice.messages = queue.Queue()
        practice.messages.put((.2, .3, ''))
        practice.level, practice.status = Mock(), Mock()
        practice.stop()
        stream.stop.assert_called_once()
        stream.close.assert_called_once()
        self.assertIsNone(practice.stream)
        self.assertTrue(practice.messages.empty())
        practice.level.set.assert_called_with(0)

    def test_silence_has_zero_level_and_is_not_recognition(self):
        practice = SoundPractice.__new__(SoundPractice)
        practice.messages = queue.Queue()
        practice.messages.put((0, 0, ''))
        practice.stream, practice.level, practice.status = Mock(), Mock(), Mock()
        practice.closed = True
        practice.poll()
        practice.level.set.assert_called_with(0)
        self.assertIn('no sound classification', practice.status.set.call_args.args[0])


if __name__ == '__main__':
    unittest.main()
