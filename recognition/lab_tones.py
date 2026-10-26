"""Short locally synthesized musical feedback; no microphone or downloads."""
import io
import math
import queue
import threading
import time
import wave
from array import array

NOTES = {
    'mouth_open': 261.63, 'mouth_close': 293.66, 'blink': 329.63,
    'brows_up': 392.00, 'smile': 440.00, 'teeth': 523.25,
    'cheek_puff': 587.33, 'tongue_out': 659.25, 'wink_left': 220.00,
    'wink_right': 246.94, 'face_scrunch': 349.23, 'head_tilt': 196.00,
    'head_nod': 174.61, 'head_shake': 164.81,
}


def synthesize(events, duration=.14):
    frequencies = [NOTES[key] for key in dict.fromkeys(events) if key in NOTES][:3]
    if not frequencies:
        return None
    sample_rate = 22050
    count = int(sample_rate * duration)
    samples = array('h')
    for index in range(count):
        t = index / sample_rate
        envelope = min(1, t / .012, (duration - t) / .045)
        value = sum(math.sin(2 * math.pi * frequency * t) for frequency in frequencies) / len(frequencies)
        samples.append(int(32767 * .12 * max(0, envelope) * value))
    output = io.BytesIO()
    with wave.open(output, 'wb') as audio:
        audio.setnchannels(1)
        audio.setsampwidth(2)
        audio.setframerate(sample_rate)
        audio.writeframes(samples.tobytes())
    return output.getvalue()


def windows_play(data):
    import winsound
    # Synchronous memory playback lives on the worker, never the UI thread.
    winsound.PlaySound(data, winsound.SND_MEMORY | winsound.SND_NODEFAULT)


class EventTones:
    def __init__(self, backend=None, clock=time.monotonic):
        self.enabled = True
        self.closed = False
        self.error = None
        self._backend = backend or windows_play
        self._clock = clock
        self._last = float('-inf')
        self._queue = queue.Queue(maxsize=1)
        self._worker = None

    def play(self, events):
        if self.closed or not self.enabled or self._clock() - self._last < .18:
            return False
        notes = tuple(dict.fromkeys(key for key in events if key in NOTES))[:3]
        if not notes:
            return False
        try:
            self._queue.put_nowait(notes)
        except queue.Full:
            return False
        self._last = self._clock()
        if self._worker is None:
            self._worker = threading.Thread(target=self._run, daemon=True, name='FaceChaosTones')
            self._worker.start()
        return True

    def _run(self):
        while not self.closed:
            try:
                events = self._queue.get(timeout=.1)
            except queue.Empty:
                continue
            if self.enabled and not self.closed:
                try:
                    self._backend(synthesize(events))
                except (ImportError, RuntimeError, OSError) as error:
                    self.error = str(error)
                    self.enabled = False

    def close(self):
        self.closed = True
        self.enabled = False
        if self._worker:
            self._worker.join(timeout=.3)
