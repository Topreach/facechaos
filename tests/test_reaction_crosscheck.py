import sys
import unittest
from pathlib import Path
from types import SimpleNamespace

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / 'recognition'))
from lab_core import (KEYS, FeatureEngine, LabDetector, clamp, prepare_detection_values,
                      suppression_reason)
from facial_scenarios import crosscheck_matrix


class CrosscheckTest(unittest.TestCase):
    def test_tongue_priority_keeps_raw_jaw_and_blocks_retraction_commands(self):
        detector = LabDetector()
        detector.controls = {key: {'neutral': 0, 'active': .8, 'sensitivity': .55}
                             for key in ('mouth_open', 'mouth_close', 'tongue_out')}
        now = 0
        def feed(jaw, tongue, close=0, frames=20):
            nonlocal now
            events = []
            for _ in range(frames):
                now += .03
                raw = {'mouth_open': jaw, 'mouth_close': close, 'tongue_out': tongue}
                found, _ = detector.update(raw, now)
                self.assertEqual(raw['mouth_open'], jaw)
                events.extend(found)
            return events
        feed(0, 0)
        self.assertEqual(feed(.9, .9), ['tongue_out'])
        self.assertIn('mouth_open', detector.suppressed)
        self.assertEqual(feed(.9, 0, .9, frames=6), [])
        self.assertEqual(feed(.9, 0), [])
        feed(0, 0)
        self.assertEqual(feed(.9, 0), ['mouth_open'])

    def test_unconfigured_tongue_does_not_disable_working_mouth_input(self):
        detector = LabDetector()
        detector.controls = {'mouth_open': {'neutral': 0, 'active': .8, 'sensitivity': .55}}
        events = []
        for index in range(40):
            raw = {'mouth_open': 0 if index < 20 else .9, 'tongue_out': .9}
            found, _ = detector.update(raw, index * .03)
            events.extend(found)
        self.assertEqual(events, ['mouth_open'])

    def test_all_fourteen_channel_scenarios(self):
        matrix = crosscheck_matrix()
        self.assertEqual(len(matrix), 14)
        for key, result in matrix.items():
            with self.subTest(section=key):
                self.assertTrue(result['passed'], result)

    def test_no_hidden_removed_sections(self):
        self.assertEqual(set(FeatureEngine().extract({}, (0, 0, 0), 0)), set(KEYS))
        self.assertNotIn('frown', KEYS)
        self.assertNotIn('eyes_wide', KEYS)

    def test_smiling_eyes_do_not_become_scrunch(self):
        values = FeatureEngine().extract({'mouthSmileLeft': .9, 'mouthSmileRight': .9,
            'cheekSquintLeft': .9, 'cheekSquintRight': .9, 'eyeSquintLeft': .8, 'eyeSquintRight': .8}, None, 0)
        self.assertEqual(values['face_scrunch'], 0)
        self.assertGreater(values['smile'], .8)

    def test_partially_closed_opposite_eye_prevents_wink(self):
        values = FeatureEngine().extract({'eyeBlinkLeft': .9, 'eyeBlinkRight': .5}, None, 0)
        self.assertEqual(values['wink_left'], 0)
        self.assertEqual(values['wink_right'], 0)

    def test_scrunch_suppresses_eye_commands_not_raw_measurement(self):
        values = FeatureEngine().extract({'browDownLeft': .8, 'browDownRight': .8,
            'noseSneerLeft': .8, 'noseSneerRight': .8, 'eyeBlinkLeft': .9, 'eyeBlinkRight': .9}, None, 0)
        self.assertGreater(values['blink'], .8)
        self.assertIsNotNone(suppression_reason('blink', values))
        self.assertIsNone(suppression_reason('mouth_open', values))

    def test_slow_mouth_closing_is_not_quick_close(self):
        engine = FeatureEngine()
        for index in range(31):
            values = engine.extract({'jawOpen': .9 * (1 - index / 30)}, None, index * .04)
            self.assertEqual(values['mouth_close'], 0)

    def test_missing_pose_is_unavailable_and_not_bridged_into_motion(self):
        engine = FeatureEngine()
        engine.extract({}, (0, 0, 0), 0)
        engine.extract({}, (25, 0, 0), .1)
        values = engine.extract({}, None, .2)
        self.assertIsNone(values['head_tilt'])
        self.assertIsNone(values['head_nod'])
        for i in range(8):
            values = engine.extract({}, (0, 0, 0), .3 + i * .03)
            self.assertEqual(values['head_nod'], 0)

    def test_single_side_glance_is_not_head_shake(self):
        engine = FeatureEngine()
        for index, yaw in enumerate([0, 5, 10, 20, 25, 20, 10, 5, 0, 0]):
            values = engine.extract({}, (0, yaw, 0), index * .06)
            self.assertEqual(values['head_shake'], 0)

    def test_native_cheek_measurement_cannot_trigger_without_personal_model(self):
        values = dict.fromkeys(KEYS, 0.0)
        values['cheek_puff'] = .9
        ready = prepare_detection_values(values, {})
        self.assertIsNone(ready['cheek_puff'])
        self.assertEqual(values['cheek_puff'], .9)

    def test_nan_is_not_promoted_to_full_activation(self):
        self.assertEqual(clamp(float('nan')), 0)
        self.assertEqual(clamp(float('inf')), 0)
        engine = FeatureEngine()
        self.assertEqual(engine.extract({'jawOpen': float('nan')}, None, 0)['mouth_open'], 0)

    def test_invalid_image_landmarks_fail_closed_per_region(self):
        from visible_teeth import measure_teeth
        from visible_tongue import measure_tongue
        from lab_appearance import cheek_descriptor
        image = np.zeros((120, 160, 3), dtype=np.uint8)
        points = [SimpleNamespace(x=.5, y=.5) for _ in range(478)]
        for i in (13, 61, 205):
            points[i] = SimpleNamespace(x=float('nan'), y=.5)
        self.assertIsNone(measure_teeth(image, points)['score'])
        self.assertIsNone(measure_tongue(image, points)['score'])
        self.assertIsNone(cheek_descriptor(image, points))
        self.assertIsNone(measure_teeth(image, [])['score'])


if __name__ == '__main__':
    unittest.main()
