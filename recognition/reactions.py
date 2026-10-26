"""Device-independent calibration, event rearming, and local protocol."""
import json
import math
import statistics
import uuid
from collections import deque
from pathlib import Path

REACTIONS = ("mouth_open", "brows_up", "smile")
FEATURE_VERSION = "blendshapes-v1"


def validate_values(values):
    return (isinstance(values, dict) and all(
        isinstance(values.get(k), (float, int)) and not isinstance(values[k], bool)
        and math.isfinite(values[k]) and 0 <= values[k] <= 1 for k in REACTIONS))


def calibrate(neutral, active):
    if len(neutral) < 20 or len(active) < 20:
        raise ValueError("Need at least 20 tracked samples for each pose")
    if any(not math.isfinite(x) or not 0 <= x <= 1 for x in neutral + active):
        raise ValueError("Invalid calibration measurements")
    baseline = sorted(neutral)[int((len(neutral) - 1) * .9)]
    peak = statistics.median(active)
    if peak - baseline < .08:
        raise ValueError("Expression overlaps neutral: retry comfortably or use keyboard")
    return {"neutral": baseline, "active": peak, "sensitivity": .55}


def validate_profile(data, camera):
    if (not isinstance(data, dict) or data.get("version") != 1
            or data.get("features") != FEATURE_VERSION or data.get("camera") != camera):
        raise ValueError("Profile does not match this camera or recognition version")
    controls = data.get("controls")
    if not isinstance(controls, dict) or set(controls) - set(REACTIONS):
        raise ValueError("Invalid controls")
    for settings in controls.values():
        if not isinstance(settings, dict):
            raise ValueError("Invalid control settings")
        numbers = [settings.get(k) for k in ("neutral", "active", "sensitivity")]
        if any(not isinstance(v, (int, float)) or isinstance(v, bool)
               or not math.isfinite(v) for v in numbers):
            raise ValueError("Invalid thresholds")
        low, high, fraction = numbers
        if not (0 <= low < high <= 1 and high - low >= .08 and .2 <= fraction <= .85):
            raise ValueError("Invalid calibrated range")
    return data


def save_profile(path, data):
    validate_profile(data, data["camera"])
    path = Path(path)
    path.parent.mkdir(parents=True, exist_ok=True)
    temp = path.with_suffix(".tmp")
    temp.write_text(json.dumps(data, indent=2), encoding="utf-8")
    temp.replace(path)


class ReactionDetector:
    def __init__(self, controls):
        self.controls = controls
        self.reset()

    def reset(self):
        self.filtered = {}
        self.armed = {key: False for key in REACTIONS}
        self.since = {}
        self.previous_time = None

    def update(self, values, now):
        if not validate_values(values):
            self.reset()
            return [], {}
        dt = 0 if self.previous_time is None else max(0, now - self.previous_time)
        self.previous_time = now
        alpha = 1 - math.exp(-dt / .035) if dt else 1
        fired, meters = [], {}
        for key, config in self.controls.items():
            value = self.filtered.get(key, values[key])
            value += alpha * (values[key] - value)
            self.filtered[key] = value
            normalized = (value - config["neutral"]) / (config["active"] - config["neutral"])
            meters[key] = max(0.0, min(1.0, normalized))
            on, off = config["sensitivity"], max(.05, config["sensitivity"] - .20)
            if normalized <= off:
                if now - self.since.setdefault((key, "release"), now) >= .12:
                    self.armed[key] = True
                self.since.pop((key, "on"), None)
            else:
                self.since.pop((key, "release"), None)
                if normalized >= on and self.armed[key]:
                    if now - self.since.setdefault((key, "on"), now) >= .045:
                        fired.append(key)
                        self.armed[key] = False
                        self.since.pop((key, "on"), None)
                else:
                    self.since.pop((key, "on"), None)
        return fired, meters


class PacketBuilder:
    def __init__(self, allowed=REACTIONS):
        self.allowed = frozenset(allowed)
        self.session = uuid.uuid4().hex
        self.sequence = 0
        self.event_id = 0
        self.repeats = deque(maxlen=12)

    def packet(self, tracking, calibrated, enabled, events, meters):
        self.repeats = deque((x for x in self.repeats if x[0]['reaction'] in enabled), maxlen=12)
        if tracking != "tracked" or not calibrated:
            self.repeats.clear()
            events = []
        for reaction in events:
            if reaction not in enabled or reaction not in self.allowed:
                continue
            self.event_id += 1
            self.repeats.append([{"id": self.event_id, "reaction": reaction}, 3])
        packet = {"version": 1, "session_id": self.session, "sequence": self.sequence,
                  "tracking": tracking, "calibrated": calibrated,
                  "enabled": list(enabled), "events": [x[0] for x in self.repeats], "meters": meters}
        self.sequence += 1
        for entry in self.repeats:
            entry[1] -= 1
        self.repeats = deque((x for x in self.repeats if x[1] > 0), maxlen=12)
        return json.dumps(packet, allow_nan=False).encode("utf-8")
