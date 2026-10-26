import sys
import unittest
from pathlib import Path
from types import SimpleNamespace

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / 'recognition'))
from visible_teeth import INNER_LIP, measure_teeth
from lab_core import FeatureEngine


class TeethTest(unittest.TestCase):
    def scene(self, teeth=False, red=False, outside_white=False, half_height=18):
        import cv2
        frame = np.full((160, 240, 3), 240 if outside_white else 70, dtype=np.uint8)
        landmarks = [SimpleNamespace(x=.5, y=.5) for _ in range(478)]
        polygon = []
        for key, angle in zip(INNER_LIP, np.linspace(0, 2 * np.pi, len(INNER_LIP), endpoint=False)):
            x, y = int(120 + 50 * np.cos(angle)), int(80 + half_height * np.sin(angle))
            landmarks[key] = SimpleNamespace(x=x / 240, y=y / 160)
            polygon.append((x, y))
        cv2.fillPoly(frame, [np.asarray(polygon)], (20, 20, 20))
        if teeth:
            cv2.rectangle(frame, (88, 77), (152, 82), (220, 225, 230), -1)
        if red:
            cv2.rectangle(frame, (88, 77), (152, 82), (40, 50, 210), -1)
        return frame, landmarks

    def test_open_mouth_without_teeth(self):
        frame, points = self.scene()
        values = FeatureEngine().extract({'jawOpen': .9}, None, 0)
        self.assertGreater(values['mouth_open'], .8)
        self.assertEqual(measure_teeth(frame, points)['score'], 0)

    def test_visible_teeth_without_wide_jaw(self):
        frame, points = self.scene(teeth=True, half_height=5)
        values = FeatureEngine().extract({'jawOpen': .02}, None, 0)
        self.assertLess(values['mouth_open'], .1)
        self.assertGreater(measure_teeth(frame, points)['score'], .5)

    def test_visible_teeth_can_coexist_with_open_mouth(self):
        frame, points = self.scene(teeth=True)
        values = FeatureEngine().extract({'jawOpen': .9}, None, 0)
        self.assertGreater(values['mouth_open'], .8)
        self.assertGreater(measure_teeth(frame, points)['score'], .1)

    def test_red_tongue_and_surrounding_bright_skin_not_teeth(self):
        frame, points = self.scene(red=True, outside_white=True)
        self.assertEqual(measure_teeth(frame, points)['score'], 0)

    def test_pale_pink_tongue_not_classed_as_white_teeth(self):
        import cv2
        frame, points = self.scene()
        cv2.rectangle(frame, (88, 77), (152, 82), (150, 170, 220), -1)
        self.assertEqual(measure_teeth(frame, points)['score'], 0)

    def test_ivory_teeth_remain_candidate(self):
        import cv2
        frame, points = self.scene()
        cv2.rectangle(frame, (88, 77), (152, 82), (150, 200, 220), -1)
        self.assertGreater(measure_teeth(frame, points)['score'], .1)

    def test_closed_lips_and_tiny_mouth(self):
        frame, points = self.scene(half_height=0)
        self.assertEqual(measure_teeth(frame, points)['score'], 0)
        for index in INNER_LIP:
            points[index] = SimpleNamespace(x=.5, y=.5)
        self.assertIsNone(measure_teeth(frame, points)['score'])


if __name__ == '__main__':
    unittest.main()
