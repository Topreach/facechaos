"""Explicit local runtime/camera probe. Does not save, display, or transmit frames."""
import argparse
import json
from pathlib import Path


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--camera', type=int, help='Optionally open this camera for one frame')
    args = parser.parse_args()
    import cv2
    import mediapipe as mp
    import numpy as np
    from mediapipe.tasks import python
    from mediapipe.tasks.python import vision
    options = vision.FaceLandmarkerOptions(
        base_options=python.BaseOptions(model_asset_path=str(
            Path(__file__).resolve().parents[1] / 'recognition/models/face_landmarker.task')),
        running_mode=vision.RunningMode.VIDEO, output_face_blendshapes=True, num_faces=1)
    with vision.FaceLandmarker.create_from_options(options) as model:
        result = model.detect_for_video(mp.Image(image_format=mp.ImageFormat.SRGB,
            data=np.zeros((480, 640, 3), dtype=np.uint8)), 1)
        report = {'mediapipe': mp.__version__, 'opencv': cv2.__version__,
                  'model_inference': 'passed', 'blank_frame_faces': len(result.face_landmarks)}
        if args.camera is not None:
            cap = cv2.VideoCapture(args.camera, cv2.CAP_DSHOW)
            try:
                ok, frame = cap.read() if cap.isOpened() else (False, None)
                report['camera_index'] = args.camera
                report['camera_readable'] = bool(ok)
                if ok:
                    report['camera_resolution'] = [int(frame.shape[1]), int(frame.shape[0])]
                    actual = model.detect_for_video(mp.Image(image_format=mp.ImageFormat.SRGB,
                        data=cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)), 2)
                    report['camera_inference'] = 'passed'
                    report['face_present_at_probe'] = bool(actual.face_landmarks)
            finally:
                cap.release()
    print(json.dumps(report, indent=2))


if __name__ == '__main__':
    main()
