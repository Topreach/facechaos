import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / 'recognition'))
from lab_core import LabDetector
from lab_profile import validate_profile, FEATURES


class TeethSmilePriorityTest(unittest.TestCase):
    def setUp(self):
        self.detector = LabDetector()
        self.config = {'neutral': 0, 'active': 1, 'sensitivity': .55}
        self.detector.controls = {k: dict(self.config) for k in ('smile', 'teeth', 'mouth_open')}
        self.now = 0
        self.feed(0, 0, 0)

    def feed(self, smile, teeth, mouth, frames=25):
        events = []
        for _ in range(frames):
            self.now += .02
            found, _ = self.detector.update({'smile': smile, 'teeth': teeth, 'mouth_open': mouth}, self.now)
            events.extend(found)
        return events

    def test_closed_lip_smile(self):
        self.assertEqual(self.feed(.9, 0, 0), ['smile'])

    def test_teeth_wins_even_when_smile_coefficient_is_high(self):
        self.assertEqual(self.feed(.9, .9, 0), ['teeth'])
        self.assertIn('smile', self.detector.suppressed)
        # Withdrawing teeth while still grinning must not produce a second action.
        self.assertEqual(self.feed(.9, 0, 0, 40), [])
        self.feed(0, 0, 0)
        self.assertEqual(self.feed(.9, 0, 0), ['smile'])

    def test_covered_teeth_open_mouth(self):
        self.assertEqual(self.feed(0, 0, .9), ['mouth_open'])

    def test_invalid_teeth_does_not_claim_priority(self):
        self.assertEqual(self.feed(.9, None, 0), ['smile'])

    def test_priority_independent_of_control_iteration_order(self):
        self.detector.controls = dict(reversed(list(self.detector.controls.items())))
        self.assertEqual(self.feed(.9, .9, 0), ['teeth'])

    def test_previous_calibration_requires_new_smile_and_teeth_poses(self):
        data = {'version': 1, 'features': FEATURES, 'camera': 0, 'detector_revision': 3,
                'controls': self.detector.controls, 'appearance': {}, 'pose_zero': None}
        controls, _, _ = validate_profile(data, 0)
        self.assertEqual(set(controls), {'mouth_open'})
        data['detector_revision'] = 4
        controls, _, _ = validate_profile(data, 0)
        self.assertEqual(set(controls), set(self.detector.controls))


if __name__ == '__main__':
    unittest.main()
