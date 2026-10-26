import sys
import unittest
from pathlib import Path
from types import SimpleNamespace

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / 'recognition'))
from visible_tongue import measure_tongue, OUTER_LIP
from visible_teeth import INNER_LIP, measure_teeth
from lab_core import FeatureEngine


class TongueTest(unittest.TestCase):
    def test_debug_geometry_and_report_are_separate(self):
        import json
        from visible_tongue import draw_tongue_debug
        frame, points = self.scene(tongue=True)
        original = frame.copy()
        result = measure_tongue(frame, points, debug=True)
        overlay = result.pop('_overlay')
        self.assertGreater(result['candidate_pixels'], 0)
        self.assertGreater(result['extended_pixels'], 0)
        json.dumps(result, allow_nan=False)
        rendered = draw_tongue_debug(frame, overlay)
        self.assertEqual(rendered.shape, frame.shape)
        self.assertTrue(np.array_equal(frame, original))
        self.assertFalse(np.array_equal(rendered, original))
    def scene(self, tongue=False, lip=False, detached=False, teeth=False):
        import cv2
        frame = np.full((180, 240, 3), (110, 150, 190), dtype=np.uint8)
        points = [SimpleNamespace(x=.5, y=.5) for _ in range(478)]
        for index, angle in zip(OUTER_LIP, np.linspace(np.pi, 3*np.pi, len(OUTER_LIP), endpoint=False)):
            points[index] = SimpleNamespace(x=(120+40*np.cos(angle))/240,
                                            y=(75-7*np.sin(angle))/180)
        for index, x, y in ((61, 80, 75), (291, 160, 75), (14, 120, 77),
                             (17, 120, 82), (205, 55, 80), (425, 185, 80)):
            points[index] = SimpleNamespace(x=x/240, y=y/180)
        for index, angle in zip(INNER_LIP, np.linspace(0, 2*np.pi, len(INNER_LIP), endpoint=False)):
            # Keep the tongue detector's lower-inner-lip seed near the actual mouth.
            if index != 14:
                points[index] = SimpleNamespace(x=(120+40*np.cos(angle))/240,
                                                y=(75+5*np.sin(angle))/180)
        cv2.rectangle(frame, (90, 72), (150, 81), (20, 20, 20), -1)
        if lip:
            cv2.rectangle(frame, (90, 79), (150, 83), (85, 90, 215), -1)
        if tongue:
            cv2.rectangle(frame, (108, 76), (132, 116), (85, 90, 215), -1)
        if detached:
            cv2.rectangle(frame, (108, 100), (132, 116), (85, 90, 215), -1)
        if teeth:
            cv2.rectangle(frame, (90, 73), (150, 76), (230, 230, 230), -1)
        return frame, points

    def test_protruding_tongue_without_wide_jaw_or_teeth(self):
        frame, points = self.scene(tongue=True)
        self.assertGreater(measure_tongue(frame, points)['score'], .5)
        self.assertEqual(measure_teeth(frame, points)['score'], 0)
        self.assertEqual(FeatureEngine().extract({'jawOpen': 0}, None, 0)['mouth_open'], 0)

    def test_lips_alone_are_not_protrusion(self):
        frame, points = self.scene(lip=True)
        result = measure_tongue(frame, points)
        self.assertEqual(result['score'], 0)
        self.assertGreater(result['excluded_lip_pixels'], 0)

    def test_detached_red_chin_patch_not_tongue(self):
        frame, points = self.scene(detached=True)
        self.assertEqual(measure_tongue(frame, points)['score'], 0)

    def test_teeth_without_tongue(self):
        frame, points = self.scene(teeth=True)
        self.assertEqual(measure_tongue(frame, points)['score'], 0)
        self.assertGreater(measure_teeth(frame, points)['score'], .1)

    def test_tongue_and_teeth_can_both_be_visible(self):
        frame, points = self.scene(tongue=True, teeth=True)
        self.assertGreater(measure_tongue(frame, points)['score'], .5)
        self.assertGreater(measure_teeth(frame, points)['score'], .1)

    def test_skin_and_empty_mouth_do_not_trigger(self):
        frame, points = self.scene()
        self.assertEqual(measure_tongue(frame, points)['score'], 0)

    def test_too_small_region_is_unavailable(self):
        frame, points = self.scene()
        points[291] = SimpleNamespace(x=points[61].x+.01, y=points[61].y)
        self.assertIsNone(measure_tongue(frame, points)['score'])


if __name__ == '__main__':
    unittest.main()
