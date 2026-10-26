"""Windows capture backend selection. Black frames are not proof of a working image."""
import time


def dark_frame(frame):
    # Diagnostic heuristic, not a shutter or face detector.
    return frame is None or frame.size == 0 or float(frame.mean()) < 3.0


def open_camera(index, backend='auto', cv=None, pause=time.sleep):
    if cv is None:
        import cv2 as cv
    choices = [('dshow', cv.CAP_DSHOW), ('msmf', cv.CAP_MSMF)]
    if backend != 'auto':
        choices = [item for item in choices if item[0] == backend]
    diagnostics = []
    last_readable = None
    for name, api in choices:
        cap = cv.VideoCapture(index, api)
        if not cap.isOpened():
            diagnostics.append(f'{name}: unavailable')
            cap.release()
            continue
        cap.set(cv.CAP_PROP_FRAME_WIDTH, 640)
        cap.set(cv.CAP_PROP_FRAME_HEIGHT, 480)
        readable = False
        for _ in range(12):
            ok, frame = cap.read()
            if ok and frame is not None and frame.size:
                readable = True
                if not dark_frame(frame):
                    diagnostics.append(f'{name}: image received')
                    return cap, name, diagnostics
            pause(.05)
        diagnostics.append(f'{name}: black/dark image' if readable else f'{name}: no frames')
        if readable:
            last_readable = (name, api)
        cap.release()
    if last_readable:
        name, api = last_readable
        cap = cv.VideoCapture(index, api)
        if cap.isOpened():
            cap.set(cv.CAP_PROP_FRAME_WIDTH, 640)
            cap.set(cv.CAP_PROP_FRAME_HEIGHT, 480)
            return cap, name, diagnostics
        cap.release()
    raise RuntimeError('Camera could not provide frames. ' + '; '.join(diagnostics)
                       + '. Check Windows camera permissions or try another --camera index.')
