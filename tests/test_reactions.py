import json
import sys
import tempfile
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / 'recognition'))
from reactions import (FEATURE_VERSION, PacketBuilder, ReactionDetector, calibrate,
                       save_profile, validate_profile)


class ReactionsTest(unittest.TestCase):
    def setUp(self):
        self.config = calibrate([.05] * 30, [.8] * 30)
        self.detector = ReactionDetector({'mouth_open': self.config})
        self.clock = 0

    def pose(self, value, frames=20):
        events = []
        for _ in range(frames):
            self.clock += .033
            found, _ = self.detector.update(
                {'mouth_open': value, 'brows_up': .02, 'smile': .02}, self.clock)
            events.extend(found)
        return events

    def test_neutral_rearm_and_hold(self):
        self.assertEqual(self.pose(.8), [])
        self.pose(.05)
        self.assertEqual(self.pose(.8), ['mouth_open'])
        self.assertEqual(self.pose(.8), [])
        self.pose(.05)
        self.assertEqual(self.pose(.8), ['mouth_open'])

    def test_tracking_loss_requires_neutral(self):
        self.pose(.05)
        self.detector.update(None, self.clock)
        self.assertEqual(self.pose(.8), [])

    def test_short_noise_and_invalid_values(self):
        self.pose(.05)
        self.assertEqual(self.pose(.8, 1), [])
        self.assertEqual(self.pose(.05), [])
        self.assertEqual(self.detector.update({'mouth_open': float('nan')}, 10), ([], {}))

    def test_calibration_rejects_overlap(self):
        with self.assertRaises(ValueError):
            calibrate([.1] * 30, [.15] * 30)
        with self.assertRaises(ValueError):
            calibrate([.1], [.9])

    def test_profile_validation_and_roundtrip(self):
        data = {'version': 1, 'features': FEATURE_VERSION, 'camera': 0,
                'controls': {'mouth_open': self.config}}
        with tempfile.TemporaryDirectory() as folder:
            target = Path(folder) / 'profile.json'
            save_profile(target, data)
            self.assertEqual(validate_profile(json.loads(target.read_text()), 0), data)
            with self.assertRaises(ValueError):
                validate_profile(data, 1)
        self.config['active'] = float('nan')
        with self.assertRaises(ValueError):
            validate_profile(data, 0)

    def test_event_retransmission_and_loss(self):
        wire = PacketBuilder()
        def packet(events, tracking='tracked'):
            return json.loads(wire.packet(tracking, True, ['mouth_open'], events, {}))
        first = packet(['mouth_open'])
        self.assertEqual(packet([])['events'], first['events'])
        self.assertEqual(packet([])['events'], first['events'])
        self.assertEqual(packet([])['events'], [])
        packet(['mouth_open'])
        self.assertEqual(packet([], 'lost')['events'], [])
        self.assertEqual(packet([])['events'], [])


if __name__ == '__main__':
    unittest.main()
