"""Opt-in, token-protected loopback preview. Latest JPEG only; no disk writes."""
import secrets
import threading
import time
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer


class CameraPreview:
    def __init__(self, port=47778):
        self.token = secrets.token_hex(16)
        self._jpeg = b''
        self._updated = 0.0
        self._lock = threading.Lock()
        owner = self

        class Handler(BaseHTTPRequestHandler):
            def do_GET(self):
                if self.path != '/frame/' + owner.token:
                    self.send_error(404)
                    return
                with owner._lock:
                    jpeg = owner._jpeg if time.monotonic() - owner._updated < 1.0 else b''
                self.send_response(200 if jpeg else 204)
                self.send_header('Content-Type', 'image/jpeg')
                self.send_header('Cache-Control', 'no-store')
                self.send_header('Content-Length', str(len(jpeg)))
                self.end_headers()
                if jpeg:
                    try:
                        self.wfile.write(jpeg)
                    except (BrokenPipeError, ConnectionResetError):
                        pass

            def log_message(self, *args):
                pass

        self.server = ThreadingHTTPServer(('127.0.0.1', port), Handler)
        self.server.daemon_threads = True
        self.port = self.server.server_port
        self.worker = threading.Thread(target=self.server.serve_forever,
                                       kwargs={'poll_interval': .1}, daemon=True)
        self.worker.start()

    def metadata(self):
        return {'port': self.port, 'token': self.token}

    def update(self, mirrored_frame):
        import cv2
        height, width = mirrored_frame.shape[:2]
        scale = min(1.0, 640 / width, 480 / height)
        small = cv2.resize(mirrored_frame, (int(width * scale), int(height * scale)))
        ok, encoded = cv2.imencode('.jpg', small, [cv2.IMWRITE_JPEG_QUALITY, 72])
        if ok and encoded.nbytes <= 350_000:
            with self._lock:
                self._jpeg = encoded.tobytes()
                self._updated = time.monotonic()

    def clear(self):
        with self._lock:
            self._jpeg = b''
            self._updated = 0

    def close(self):
        self.clear()
        self.server.shutdown()
        self.server.server_close()
        self.worker.join(timeout=.5)
