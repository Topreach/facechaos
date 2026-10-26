import sys
import unittest
import urllib.error
import urllib.request
from pathlib import Path

import cv2
import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / 'recognition'))
from camera_preview import CameraPreview


class CameraPreviewTest(unittest.TestCase):
    def setUp(self):
        self.preview = CameraPreview(port=0)
        self.url = f'http://127.0.0.1:{self.preview.port}/frame/{self.preview.token}'

    def tearDown(self):
        self.preview.close()

    def test_empty_clear_and_expired_frames_are_not_served(self):
        with urllib.request.urlopen(self.url, timeout=1) as response:
            self.assertEqual(response.status, 204)
        self.preview.update(np.full((80, 100, 3), 100, dtype=np.uint8))
        self.preview.clear()
        with urllib.request.urlopen(self.url, timeout=1) as response:
            self.assertEqual(response.status, 204)
        self.preview.update(np.full((80, 100, 3), 100, dtype=np.uint8))
        self.preview._updated = 0
        with urllib.request.urlopen(self.url, timeout=1) as response:
            self.assertEqual(response.status, 204)

    def test_only_current_bounded_jpeg_is_served_without_cache(self):
        self.preview.update(np.full((1080, 1920, 3), 100, dtype=np.uint8))
        with urllib.request.urlopen(self.url, timeout=1) as response:
            self.assertEqual(response.headers['Cache-Control'], 'no-store')
            body = response.read()
        image = cv2.imdecode(np.frombuffer(body, dtype=np.uint8), cv2.IMREAD_COLOR)
        self.assertLessEqual(len(body), 350000)
        self.assertLessEqual(image.shape[1], 640)
        self.assertLessEqual(image.shape[0], 480)

    def test_wrong_token_is_rejected_and_only_loopback_is_bound(self):
        self.assertEqual(self.preview.server.server_address[0], '127.0.0.1')
        with self.assertRaises(urllib.error.HTTPError) as caught:
            urllib.request.urlopen(self.url + 'wrong', timeout=1)
        self.assertEqual(caught.exception.code, 404)
