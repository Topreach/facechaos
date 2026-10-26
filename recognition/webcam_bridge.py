"""Local camera preview and calibration; no frames saved or transmitted."""
import argparse
import json
import socket
import time
from pathlib import Path
from camera_capture import dark_frame, open_camera
from reactions import (FEATURE_VERSION, REACTIONS, PacketBuilder, ReactionDetector,
                       calibrate, save_profile, validate_profile)

ROOT = Path(__file__).resolve().parent
PROMPTS = {'neutral': 'Relax your face', 'mouth_open': 'Open mouth comfortably',
           'brows_up': 'Raise eyebrows', 'smile': 'Smile comfortably'}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--camera', type=int, default=0, help='Windows camera index')
    parser.add_argument('--backend', choices=('auto', 'dshow', 'msmf'), default='auto',
                        help='Auto retries Media Foundation if DirectShow returns black frames')
    parser.add_argument('--list-cameras', action='store_true', help='Probe camera indices 0-5')
    parser.add_argument('--model', type=Path, default=ROOT / 'models/face_landmarker.task')
    parser.add_argument('--profile', type=Path, help='Local calibration JSON path')
    args = parser.parse_args()
    import cv2
    import mediapipe as mp
    from mediapipe.tasks import python
    from mediapipe.tasks.python import vision
    if args.list_cameras:
        found = []
        for index in range(6):
            try:
                cap, backend_name, diagnostics = open_camera(index, args.backend)
            except RuntimeError as error:
                print(f'Camera {index}: {error}')
                continue
            try:
                if cap.isOpened() and cap.read()[0]:
                    found.append(index)
                    print(f'Camera {index}: {backend_name}; ' + '; '.join(diagnostics))
            finally:
                cap.release()
        if not found:
            print('No readable cameras found at indices 0-5. Check Windows permissions and other camera apps.')
        return
    if not args.model.is_file():
        raise RuntimeError('Model missing. Run recognition/setup.ps1 first.')
    profile_path = args.profile or ROOT / f'profiles/camera_{args.camera}.json'
    data = {'version': 1, 'features': FEATURE_VERSION, 'camera': args.camera, 'controls': {}}
    notice = 'Press C to calibrate. Keyboard remains available.'
    if profile_path.exists():
        try:
            data = validate_profile(json.loads(profile_path.read_text(encoding='utf-8')), args.camera)
            notice = 'Profile loaded. Test neutral and each expression before playing.'
        except (ValueError, OSError, TypeError) as error:
            notice = f'Profile ignored: {error}'
    detector = ReactionDetector(data['controls'])
    wire = PacketBuilder()
    cap, backend_name, diagnostics = open_camera(args.camera, args.backend)
    print('Camera diagnostics: ' + '; '.join(diagnostics), flush=True)
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    window = 'Face Chaos - local camera setup'
    capture, neutral, selected, last_action = None, [], 'mouth_open', 'None'
    options = vision.FaceLandmarkerOptions(
        base_options=python.BaseOptions(model_asset_path=str(args.model)),
        running_mode=vision.RunningMode.VIDEO, num_faces=1, output_face_blendshapes=True,
        min_face_detection_confidence=.6, min_face_presence_confidence=.6,
        min_tracking_confidence=.6)
    timestamp = 0
    try:
        if not cap.isOpened():
            raise RuntimeError(f'Camera {args.camera} unavailable. Try --list-cameras or Windows camera permissions.')
        cap.set(cv2.CAP_PROP_FRAME_WIDTH, 640)
        cap.set(cv2.CAP_PROP_FRAME_HEIGHT, 480)
        cv2.namedWindow(window)
        with vision.FaceLandmarker.create_from_options(options) as model:
            while True:
                loop_start = time.monotonic()
                ok, frame = cap.read()
                if not ok:
                    raise RuntimeError('Camera stopped returning frames. Reconnect and restart the bridge.')
                image_dark = dark_frame(frame)
                timestamp = max(timestamp + 1, int(time.monotonic() * 1000))
                result = model.detect_for_video(mp.Image(image_format=mp.ImageFormat.SRGB,
                    data=cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)), timestamp)
                values = None
                if result.face_blendshapes and not image_dark:
                    scores = {x.category_name: float(x.score) for x in result.face_blendshapes[0]}
                    values = {'mouth_open': scores.get('jawOpen', 0.0),
                              'brows_up': scores.get('browInnerUp', 0.0),
                              'smile': (scores.get('mouthSmileLeft', 0.0) + scores.get('mouthSmileRight', 0.0)) / 2}
                now = time.monotonic()
                was_capturing = capture is not None
                if capture:
                    detector.reset()
                    label, start, samples = capture
                    if values is None:
                        capture = None
                        notice = 'Face lost: sample cancelled. Press C/N or select 1/2/3 and E to retry.'
                    elif now - start < 1.5:
                        notice = f'Prepare: {PROMPTS[label]}'
                    elif now - start < 4.5:
                        samples.append(values.copy())
                        notice = f'Measuring: {PROMPTS[label]}'
                    else:
                        capture = None
                        try:
                            if label == 'neutral':
                                if len(samples) < 20:
                                    raise ValueError('Too few samples: retry neutral')
                                neutral = samples
                                notice = 'Neutral saved for session. Select 1/2/3 then E for each expression.'
                            else:
                                config = calibrate([s[label] for s in neutral], [s[label] for s in samples])
                                data['controls'][label] = config
                                save_profile(profile_path, data)
                                notice = f'Saved {label}. Return to neutral before testing.'
                        except (ValueError, OSError) as error:
                            notice = str(error)
                        detector = ReactionDetector(data['controls'])
                events, meters = detector.update(values, now) if not was_capturing else ([], {})
                if events:
                    last_action = ', '.join(events)
                enabled = list(data['controls'])
                calibrated = bool(enabled) and not was_capturing
                sock.sendto(wire.packet('tracked' if values else 'lost', calibrated,
                    enabled, events, meters), ('127.0.0.1', 47777))
                preview = cv2.flip(frame, 1)
                preview = cv2.copyMakeBorder(preview, 0, 200, 0, 0, cv2.BORDER_CONSTANT, value=(20, 25, 35))
                height = frame.shape[0]
                camera_status = 'TRACKED' if values else 'NO FACE - facial input disabled'
                if image_dark:
                    camera_status = 'BLACK/DARK IMAGE - check privacy shutter, lighting, camera apps'
                lines = [camera_status,
                         'C: new calibration  1/2/3: select  E: capture  Q: quit',
                         'N: neutral  +/-: sensitivity  D: disable selected',
                         f'Selected: {selected} | Last detected: {last_action}',
                         notice[:95], f'Camera {args.camera} / {backend_name} | Teeth unavailable: use A']
                for i, text in enumerate(lines):
                    cv2.putText(preview, text, (10, height + 23 + i * 25), cv2.FONT_HERSHEY_SIMPLEX,
                                .42, (220, 230, 240), 1, cv2.LINE_AA)
                for i, reaction in enumerate(REACTIONS):
                    y = 25 + i * 34
                    config = data['controls'].get(reaction)
                    amount = meters.get(reaction, 0.0)
                    cv2.rectangle(preview, (10, y + 5), (10 + int(180 * amount), y + 11), (60, 220, 100), -1)
                    raw = values.get(reaction, 0.0) if values else 0.0
                    label = f'{reaction}: {amount:.2f}' if config else f'{reaction}: raw {raw:.2f} - calibrate'
                    if config:
                        label += f" threshold {config['sensitivity']:.2f}"
                    cv2.putText(preview, label, (10, y), cv2.FONT_HERSHEY_SIMPLEX, .5, (255, 255, 255), 1)
                cv2.imshow(window, preview)
                key = cv2.waitKey(1) & 0xFF
                if key == ord('q') or cv2.getWindowProperty(window, cv2.WND_PROP_VISIBLE) < 1:
                    break
                if key in (ord('1'), ord('2'), ord('3')):
                    selected = REACTIONS[key - ord('1')]
                if key in (ord('c'), ord('n')):
                    data['controls'] = {}
                    neutral = []
                    save_profile(profile_path, data)
                    detector = ReactionDetector({})
                    capture = ['neutral', time.monotonic(), []]
                elif key == ord('e') and capture is None:
                    if neutral:
                        data['controls'].pop(selected, None)
                        save_profile(profile_path, data)
                        capture = [selected, time.monotonic(), []]
                    else:
                        notice = 'Capture neutral with C first.'
                elif key == ord('d'):
                    data['controls'].pop(selected, None)
                    save_profile(profile_path, data)
                    detector = ReactionDetector(data['controls'])
                elif key in (ord('+'), ord('='), ord('-')) and selected in data['controls']:
                    settings = data['controls'][selected]
                    settings['sensitivity'] = max(.2, min(.85, settings['sensitivity'] + (.05 if key == ord('-') else -.05)))
                    save_profile(profile_path, data)
                    detector.reset()
                time.sleep(max(0, 1 / 30 - (time.monotonic() - loop_start)))
    finally:
        try:
            sock.sendto(wire.packet('unavailable', False, [], [], {}), ('127.0.0.1', 47777))
        finally:
            cap.release()
            cv2.destroyAllWindows()
            sock.close()


if __name__ == '__main__':
    try:
        main()
    except (RuntimeError, OSError, ImportError, ValueError) as error:
        raise SystemExit(f'Face Chaos camera setup: {error}') from error
