import io
import sys
import threading
import unittest
import wave
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / 'recognition'))
from lab_tones import EventTones, synthesize


class ToneTest(unittest.TestCase):
    def test_generated_chord_is_short_valid_audio(self):
        data = synthesize(['mouth_open', 'smile', 'brows_up'])
        with wave.open(io.BytesIO(data)) as audio:
            self.assertEqual(audio.getnchannels(), 1)
            self.assertEqual(audio.getsampwidth(), 2)
            self.assertAlmostEqual(audio.getnframes() / audio.getframerate(), .14, places=3)
        self.assertIsNone(synthesize(['frown', 'unknown']))

    def test_worker_does_not_block_ui_and_mute_rejects_new_notes(self):
        entered, release = threading.Event(), threading.Event()
        def player(_data):
            entered.set()
            release.wait(1)
        tones = EventTones(backend=player)
        try:
            self.assertTrue(tones.play(['smile']))
            self.assertTrue(entered.wait(.5))
            self.assertFalse(tones.play(['smile']))
            tones.enabled = False
            self.assertFalse(tones.play(['teeth']))
        finally:
            release.set()
            tones.close()
        self.assertFalse(tones.play(['smile']))


if __name__ == '__main__':
    unittest.main()
