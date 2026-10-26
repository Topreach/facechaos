import math
import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / 'recognition'))
from lab_core import FeatureEngine, LabDetector, TrialBook, calibrate_section, KEYS


class FeatureTest(unittest.TestCase):
    def test_missing_cheek_signal_is_unavailable_not_zero(self):
        self.assertIsNone(FeatureEngine().extract({}, None, 0)['cheek_puff'])
        self.assertEqual(FeatureEngine().extract({'cheekPuff': .7}, None, 0)['cheek_puff'], .7)

    def test_scrunch_detected_without_smile_or_open_mouth(self):
        engine = FeatureEngine()
        scores = {key: .85 for key in ('browDownLeft', 'browDownRight',
                                       'noseSneerLeft', 'noseSneerRight', 'cheekSquintLeft', 'cheekSquintRight')}
        values = engine.extract(scores, (0, 0, 0), 0)
        self.assertGreater(values['face_scrunch'], .8)
        self.assertEqual(values['mouth_open'], 0)
        self.assertEqual(values['smile'], 0)

    def test_blink_not_classed_as_wink_or_scrunch(self):
        values = FeatureEngine().extract({'eyeBlinkLeft': 1, 'eyeBlinkRight': 1}, None, 0)
        self.assertEqual(values['blink'], 1)
        self.assertEqual(values['wink_left'], 0)
        self.assertEqual(values['wink_right'], 0)
        self.assertEqual(values['face_scrunch'], 0)

    def test_wink_sides_without_removed_frown(self):
        values = FeatureEngine().extract({'eyeBlinkLeft': .9, 'eyeBlinkRight': .1,
                                          'mouthFrownLeft': .8, 'mouthFrownRight': .8}, None, 0)
        self.assertGreater(values['wink_left'], .8)
        self.assertLess(values['wink_right'], .02)
        self.assertNotIn('frown', values)

    def test_quick_close_needs_preceding_open(self):
        engine = FeatureEngine()
        self.assertEqual(engine.extract({}, None, 0)['mouth_close'], 0)
        engine.extract({'jawOpen': .8}, None, .1)
        self.assertGreater(engine.extract({'jawOpen': .02}, None, .2)['mouth_close'], .9)
        self.assertEqual(engine.extract({'jawOpen': .02}, None, 1)['mouth_close'], 0)

    def test_nod_needs_reversal_and_pose_survives_tracking_gap(self):
        engine = FeatureEngine()
        for i, pitch in enumerate([0, 3, 6, 10, 15, 20]):
            values = engine.extract({}, (pitch, 0, 0), i * .1)
            self.assertEqual(values['head_nod'], 0)
        for i, pitch in enumerate([15, 10, 3, 0]):
            values = engine.extract({}, (pitch, 0, 0), .6 + i * .1)
        self.assertGreater(values['head_nod'], .4)
        engine.reset_motion()
        self.assertEqual(engine.pose_zero, (0, 0, 0))
        self.assertEqual(engine.extract({}, (0, 0, 0), 2)['head_nod'], 0)

    def test_teeth_and_tongue_not_inferred_from_jaw(self):
        values = FeatureEngine().extract({'jawOpen': 1, 'mouthSmileLeft': 1}, None, 0)
        self.assertEqual(values['teeth'], 0)
        self.assertEqual(values['tongue_out'], 0)


class DetectorTest(unittest.TestCase):
    def test_calibration_requires_all_repetitions(self):
        self.assertRaises(ValueError, calibrate_section, [.1] * 30, [[.9] * 30] * 2)
        self.assertRaises(ValueError, calibrate_section, [.1] * 30, [[.9] * 30, [.9] * 30, [.12] * 30])
        self.assertRaises(ValueError, calibrate_section, [.1] * 30, [[math.nan] * 30] * 3)

    def test_deliberate_blink_duration_and_rearm(self):
        detector = LabDetector()
        detector.controls['blink'] = calibrate_section([0.0] * 30, [[.9] * 30] * 3)
        now = 0
        def feed(value, count):
            nonlocal now
            found = []
            for _ in range(count):
                now += .02
                events, _ = detector.update({'blink': value}, now)
                found.extend(events)
            return found
        feed(0, 20)
        self.assertEqual(feed(.9, 5), [])
        self.assertEqual(feed(0, 20), [])
        self.assertEqual(feed(.9, 30), ['blink'])
        self.assertEqual(feed(.9, 30), [])
        detector.update(None, now)
        self.assertEqual(feed(.9, 30), [])

    def test_independent_multi_region_reactions(self):
        detector = LabDetector()
        detector.controls = {key: calibrate_section([0] * 30, [[.9] * 30] * 3)
                             for key in ('face_scrunch', 'smile')}
        for i in range(20):
            detector.update({'face_scrunch': 0, 'smile': 0}, i * .03)
        found = []
        for i in range(20):
            events, _ = detector.update({'face_scrunch': .9, 'smile': .9}, .6 + i * .03)
            found.extend(events)
        self.assertCountEqual(found, ['face_scrunch', 'smile'])


class EvidenceTest(unittest.TestCase):
    def test_cross_activations_misses_and_invalid_trials(self):
        book = TrialBook()
        book.trial('face_scrunch', ['face_scrunch', 'face_scrunch', 'smile'])
        book.trial('face_scrunch', [])
        book.trial('face_scrunch', [], valid=False)
        report = book.report()
        result = report['results']['face_scrunch']
        self.assertEqual(result['prompt_window_detection_rate'], .5)
        self.assertEqual(result['duplicates'], 1)
        self.assertEqual(result['cross_activations'], {'smile': 1})
        self.assertEqual(report['invalid_trials'], 1)
        self.assertEqual(result['screening'], 'insufficient trials')

    def test_neutral_exposure_required_and_false_event_rate(self):
        book = TrialBook()
        for _ in range(50):
            book.trial('smile', ['smile'])
        self.assertEqual(book.report()['results']['smile']['screening'], 'insufficient trials')
        book.neutral(600, ['smile', 'smile'])
        self.assertEqual(book.report()['results']['smile']['false_events_per_10_min'], 2)
        self.assertEqual(book.report()['results']['smile']['screening'], 'needs improvement')

    def test_screening_is_not_game_approval(self):
        book = TrialBook()
        for _ in range(50):
            book.trial('smile', ['smile'])
        book.neutral(600, [])
        report = book.report()
        self.assertIn('candidate', report['results']['smile']['screening'])
        self.assertIn('not permission', report['limits'])


class AppearanceTest(unittest.TestCase):
    def test_cheek_descriptor_tracks_shape_and_rejects_small_faces(self):
        import numpy as np
        from types import SimpleNamespace
        from lab_appearance import cheek_descriptor
        frame = np.full((480, 640, 3), 100, dtype=np.uint8)
        points = [SimpleNamespace(x=.5, y=.5) for _ in range(478)]
        for index, x, y in ((33, .3, .4), (263, .7, .4), (205, .35, .6), (425, .65, .6)):
            points[index] = SimpleNamespace(x=x, y=y)
        baseline = cheek_descriptor(frame, points)
        self.assertEqual(baseline.shape, (217,))
        self.assertTrue(np.isfinite(baseline).all())
        points[205].x = .30
        points[425].x = .70
        changed = cheek_descriptor(frame, points)
        self.assertFalse(np.allclose(baseline[-9:], changed[-9:]))
        points[33].x, points[263].x = .49, .51
        self.assertIsNone(cheek_descriptor(frame, points))

    def test_learns_separate_appearances_and_rejects_unknown(self):
        import numpy as np
        from lab_appearance import AppearanceModel
        model = AppearanceModel()
        positives = [[np.array([.8, .1, .2])] * 15 for _ in range(3)]
        negatives = [[np.array([.1, .8, .2])] * 15, [np.array([.2, .1, .8])] * 15]
        negative, positive = model.fit(positives, negatives)
        self.assertGreater(min(positive[0]), .9)
        self.assertLess(max(negative), .1)
        self.assertEqual(model.score(np.array([10, 10, 10])), 0)
        self.assertIsNone(model.score(None))
        self.assertEqual(model.explain(None)['state'], 'region missing / face too small')
        self.assertEqual(model.explain(np.array([10, 10, 10]))['state'], 'outside calibrated examples')
        self.assertEqual(model.explain(np.array([float('nan'), 0, 0]))['state'], 'invalid region measurements')
        self.assertEqual(model.explain(np.array([1, 2]))['state'], 'invalid region measurements')

    def test_rejects_identical_positive_and_negative_examples(self):
        import numpy as np
        from lab_appearance import AppearanceModel
        values = [np.array([.5, .5])] * 15
        with self.assertRaises(ValueError):
            AppearanceModel().fit([values] * 3, [values] * 2)


if __name__ == '__main__':
    unittest.main()
