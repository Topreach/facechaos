"""Temporary synthetic camera source for Godot integration tests; no real camera."""
import sys
import time
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / 'recognition'))
from camera_preview import CameraPreview
from lab_game_bridge import LabGameBridge

preview = CameraPreview(port=0)
sender = LabGameBridge(port=int(sys.argv[1]))
preview.update(np.full((240, 320, 3), (60, 120, 180), dtype=np.uint8))
try:
    for _ in range(50):
        preview.update(np.full((240, 320, 3), (60, 120, 180), dtype=np.uint8))
        sender.publish(True, {'mouth_open': {}}, [], {'mouth_open': .3},
                       preview=preview.metadata(), avatar={'blink_left': .9, 'blink_right': 0, 'jaw': .3})
        time.sleep(.05)
finally:
    sender.close()
    preview.close()
