"""Explicit opt-in, local-only delivery of the lab's calibrated core controls."""
import math
import json
import socket

from reactions import PacketBuilder

GAME_REACTIONS = ('mouth_open', 'brows_up', 'smile', 'teeth')


class LabGameBridge:
    def __init__(self, port=47777):
        self.target = ('127.0.0.1', port)
        self.socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self.socket.setblocking(False)
        self.builder = PacketBuilder(allowed=GAME_REACTIONS)
        self.error = None
        self.closed = False

    def publish(self, tracked, controls, events, meters, busy=False, available=True, preview=None, avatar=None):
        if self.closed:
            return False
        enabled = [key for key in GAME_REACTIONS if key in controls]
        state = 'unavailable' if not available else 'tracked' if tracked else 'lost'
        ready = bool(enabled) and tracked and not busy and available
        safe_meters = {key: float(value) for key, value in meters.items()
                       if key in enabled and isinstance(value, (int, float))
                       and not isinstance(value, bool) and math.isfinite(value) and 0 <= value <= 1}
        payload = self.builder.packet(state, ready, enabled, events if ready else [],
                                      safe_meters if ready else {})
        packet = json.loads(payload)
        if preview and available:
            packet['preview'] = preview
        if tracked and available and avatar:
            packet['avatar'] = {key: float(value) for key, value in avatar.items()
                                if key in ('blink_left', 'blink_right', 'jaw', 'smile')
                                and isinstance(value, (int, float)) and not isinstance(value, bool)
                                and math.isfinite(value) and 0 <= value <= 1}
        payload = json.dumps(packet, allow_nan=False).encode('utf-8')
        try:
            self.socket.sendto(payload, self.target)
            self.error = None
            return True
        except OSError as error:
            self.error = str(error)
            return False

    def close(self):
        if not self.closed:
            self.publish(False, {}, [], {}, available=False)
            self.socket.close()
            self.closed = True
