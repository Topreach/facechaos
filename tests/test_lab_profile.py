import json
import sys
import tempfile
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / 'recognition'))
from lab_profile import save_profile, load_profile, validate_profile, FEATURES, LEGACY_FEATURES, PREVIOUS_FEATURES
from lab_appearance import AppearanceModel


class LabProfileTest(unittest.TestCase):
    def setUp(self):
        import numpy as np
        self.sample = np.full(217, .8)
        self.model = AppearanceModel()
        self.model.fit([[self.sample] * 15] * 3,
                       [[np.full(217, .1)] * 15, [np.full(217, .2)] * 15])
        self.control = {'neutral': 0.0, 'active': 1.0, 'sensitivity': .55}
        self.data = {'version': 1, 'features': FEATURES, 'camera': 0,
                     'controls': {'cheek_puff': self.control}, 'appearance': {'cheek_puff': self.model.to_dict()},
                     'pose_zero': [0, 0, 0]}

    def test_saved_templates_restore_same_detection(self):
        with tempfile.TemporaryDirectory() as folder:
            path = Path(folder) / 'calibration.json'
            save_profile(path, 0, {'cheek_puff': self.control}, {'cheek_puff': self.model}, (0, 0, 0))
            controls, models, pose = load_profile(path, 0)
            self.assertEqual(controls['cheek_puff'], self.control)
            self.assertAlmostEqual(models['cheek_puff'].score(self.sample), self.model.score(self.sample))
            self.assertEqual(pose, (0, 0, 0))
            self.assertFalse(path.with_suffix('.json.tmp').exists())
            self.assertNotIn('image', json.loads(path.read_text())['appearance']['cheek_puff'])

    def test_wrong_camera_rejected(self):
        with self.assertRaises(ValueError):
            validate_profile(self.data, 1)

    def test_old_profile_preserves_working_controls_and_drops_old_teeth_classifier(self):
        self.data['features'] = LEGACY_FEATURES
        self.data['controls']['teeth'] = dict(self.control)
        self.data['appearance']['teeth'] = self.model.to_dict()
        self.data['controls']['smile'] = dict(self.control)
        self.data['controls']['frown'] = dict(self.control)
        controls, models, _pose = validate_profile(self.data, 0)
        self.assertIn('cheek_puff', controls)
        self.assertNotIn('smile', controls)
        self.assertNotIn('teeth', controls)
        self.assertNotIn('teeth', models)
        self.assertNotIn('frown', controls)

    def test_v3_profile_drops_changed_teeth_and_old_tongue_classifier(self):
        self.data['features'] = PREVIOUS_FEATURES
        self.data['controls']['tongue_out'] = dict(self.control)
        self.data['appearance']['tongue_out'] = self.model.to_dict()
        self.data['controls']['teeth'] = dict(self.control)
        controls, models, _ = validate_profile(self.data, 0)
        self.assertNotIn('teeth', controls)
        self.assertIn('cheek_puff', models)
        self.assertNotIn('tongue_out', controls)
        self.assertNotIn('tongue_out', models)

    def test_missing_personal_templates_rejected(self):
        self.data['appearance'] = {}
        with self.assertRaises(ValueError):
            validate_profile(self.data, 0)

    def test_changed_detectors_require_recalibration_but_others_survive(self):
        self.data['controls']['face_scrunch'] = dict(self.control)
        self.data['controls']['smile'] = dict(self.control)
        controls, models, _ = validate_profile(self.data, 0)
        self.assertNotIn('face_scrunch', controls)
        self.assertNotIn('smile', controls)
        self.assertIn('cheek_puff', models)
        self.data['detector_revision'] = 2
        controls, _, _ = validate_profile(self.data, 0)
        self.assertIn('face_scrunch', controls)

    def test_invalid_threshold_rejected(self):
        self.data['controls']['cheek_puff']['active'] = float('nan')
        with self.assertRaises(ValueError):
            validate_profile(self.data, 0)

    def test_wrong_feature_dimensions_rejected(self):
        self.data['appearance']['cheek_puff']['scale'] = [1, 2]
        with self.assertRaises(ValueError):
            validate_profile(self.data, 0)

    def test_oversized_or_corrupt_file_rejected(self):
        with tempfile.TemporaryDirectory() as folder:
            path = Path(folder) / 'calibration.json'
            path.write_text('bad-json')
            with self.assertRaises(ValueError):
                load_profile(path, 0)
            path.write_bytes(b' ' * (1024 * 1024 + 1))
            with self.assertRaises(ValueError):
                load_profile(path, 0)


if __name__ == '__main__':
    unittest.main()
