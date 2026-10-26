import sys
import unittest
from pathlib import Path
from types import SimpleNamespace

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / 'recognition'))
from camera_capture import open_camera, dark_frame


class Frame:
    size = 10

    def __init__(self, brightness):
        self.brightness = brightness

    def mean(self):
        return self.brightness


class Capture:
    def __init__(self, brightness):
        self.brightness = brightness
        self.closed = False

    def isOpened(self):
        return self.brightness is not None

    def read(self):
        return (False, None) if self.brightness is None else (True, Frame(self.brightness))

    def set(self, *_args):
        pass

    def release(self):
        self.closed = True


class CameraTest(unittest.TestCase):
    def backend(self, brightness):
        captures = []
        def create(_index, api):
            result = Capture(brightness[api])
            captures.append(result)
            return result
        return SimpleNamespace(CAP_DSHOW=1, CAP_MSMF=2, CAP_PROP_FRAME_WIDTH=3,
                               CAP_PROP_FRAME_HEIGHT=4, VideoCapture=create), captures

    def test_fallback_from_black_directshow(self):
        cv, captures = self.backend({1: 0, 2: 80})
        cap, name, _ = open_camera(0, cv=cv, pause=lambda _: None)
        self.assertEqual(name, 'msmf')
        self.assertTrue(captures[0].closed)
        self.assertFalse(cap.closed)

    def test_black_is_diagnosed_not_called_valid_image(self):
        cv, _ = self.backend({1: 0, 2: 0})
        cap, _, messages = open_camera(0, cv=cv, pause=lambda _: None)
        self.assertTrue(dark_frame(cap.read()[1]))
        self.assertTrue(all('black/dark' in msg for msg in messages))

    def test_unavailable_cameras_release_handles(self):
        cv, captures = self.backend({1: None, 2: None})
        with self.assertRaises(RuntimeError):
            open_camera(0, cv=cv, pause=lambda _: None)
        self.assertTrue(all(cap.closed for cap in captures))


if __name__ == '__main__':
    unittest.main()
