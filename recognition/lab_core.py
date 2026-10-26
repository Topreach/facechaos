"""Standalone facial lab: measurements, personal calibration and labelled trials.

No network, game actions, camera capture, or image persistence in this module.
"""
import math
from collections import deque
from dataclasses import dataclass


@dataclass(frozen=True)
class Section:
    key: str
    title: str
    prompt: str
    kind: str = 'blendshape'
    dwell: float = .07


SECTIONS = (
    Section('mouth_open', 'Jaw drop (mouth opening)', 'Lower your jaw to open your mouth comfortably; teeth need not show'),
    Section('mouth_close', 'Quick mouth closure', 'Start with your mouth open, then close it quickly and gently', 'motion', .025),
    Section('blink', 'Deliberate eye blink', 'Close BOTH eyes gently for about half a second, then reopen', 'temporal', .25),
    Section('brows_up', 'Eye widening', 'Open both eyes wider as if surprised; avoid straining'),
    Section('smile', 'Closed-lip smile', 'Lift both mouth corners while keeping your lips together'),
    Section('teeth', 'Teeth baring', 'Pull lips back and apart to expose upper AND lower teeth comfortably', 'visual'),
    Section('cheek_puff', 'Cheek puff', 'Keep lips closed, fill BOTH cheeks gently with air, then release', 'appearance'),
    Section('tongue_out', 'Tongue protrusion', 'Extend your tongue visibly past your lips, then retract; no wide jaw required', 'visual'),
    Section('wink_left', 'Left eye wink', 'Close your own LEFT eye while keeping the right eye open', 'experimental', .15),
    Section('wink_right', 'Right eye wink', 'Close your own RIGHT eye while keeping the left eye open', 'experimental', .15),
    Section('face_scrunch', 'Face scrunch', 'Gently wrinkle your nose and draw cheeks and brows inward, then relax'),
    Section('head_tilt', 'Head tilt', 'Lean an ear toward the same-side shoulder, then return upright', 'pose'),
    Section('head_nod', 'Head nod', 'Gently move your head down and back up once, like saying yes', 'motion', .025),
    Section('head_shake', 'Head shake', 'Gently turn your head left and right, then return to centre, like saying no', 'motion', .025),
)
BY_KEY = {s.key: s for s in SECTIONS}
KEYS = tuple(BY_KEY)


def clamp(value):
    if not isinstance(value, (float, int)) or isinstance(value, bool) or not math.isfinite(value):
        return 0.0
    return max(0.0, min(1.0, float(value)))


def quantile(values, fraction):
    if not values:
        raise ValueError('No samples')
    ordered = sorted(values)
    return ordered[int((len(ordered) - 1) * fraction)]


def suppression_reason(key, values, controls=None):
    if values is None:
        return None
    scrunch = values.get('face_scrunch', 0.0)
    if (key in ('blink', 'wink_left', 'wink_right')
            and isinstance(scrunch, (int, float)) and math.isfinite(scrunch) and scrunch >= .35):
        return 'face scrunch owns eye closure'
    tongue = values.get('tongue_out')
    config = (controls or {}).get('tongue_out')
    if (key in ('mouth_open', 'mouth_close') and config
            and isinstance(tongue, (int, float)) and math.isfinite(tongue)
            and tongue >= config['neutral'] + (config['active'] - config['neutral']) * config['sensitivity']):
        return 'tongue gesture owns mouth command'
    teeth = values.get('teeth')
    config = (controls or {}).get('teeth')
    if (key == 'smile' and config
            and isinstance(teeth, (int, float)) and not isinstance(teeth, bool)
            and math.isfinite(teeth) and 0 <= teeth <= 1
            and teeth >= config['neutral'] + (config['active'] - config['neutral']) * config['sensitivity']):
        return 'visible teeth owns smile command'
    return None


def prepare_detection_values(values, appearance):
    if values is None:
        return None
    ready = dict(values)
    # Native cheekPuff is a live diagnostic, not a calibrated personal detector.
    if 'cheek_puff' not in appearance:
        ready['cheek_puff'] = None
    return ready


class FeatureEngine:
    def __init__(self):
        self.reset()

    def reset(self):
        self.history = deque()
        self.pose_zero = None

    def reset_motion(self):
        self.history.clear()

    def extract(self, scores, pose, now):
        def v(name):
            return clamp(scores.get(name, 0.0))
        def pair(name):
            return (v(name + 'Left') + v(name + 'Right')) / 2
        left, right = v('eyeBlinkLeft'), v('eyeBlinkRight')
        jaw = v('jawOpen')
        values = dict.fromkeys(KEYS, 0.0)
        values.update(mouth_open=jaw, blink=min(left, right),
                      brows_up=max(v('browInnerUp'), pair('browOuterUp')), smile=pair('mouthSmile'),
                      cheek_puff=v('cheekPuff') if 'cheekPuff' in scores else None,
                      wink_left=left * (1 - right) if right < .35 else 0.0,
                      wink_right=right * (1 - left) if left < .35 else 0.0)
        # A scrunch is a multi-region movement, not an emotional classification.
        regions = [pair('browDown'), pair('noseSneer'), pair('cheekSquint'),
                   pair('eyeSquint'), pair('mouthPress')]
        # Smiling commonly raises cheek/eye-squint together; require a separate
        # brow/nose/lip-press anchor as well as a second active region.
        anchor = max(regions[0], regions[1], regions[4])
        values['face_scrunch'] = min(anchor, sorted(regions, reverse=True)[1])
        if (pose is not None and len(pose) == 3
                and all(isinstance(v, (int, float)) and math.isfinite(v) for v in pose)):
            if self.pose_zero is None:
                self.pose_zero = pose
            delta = tuple(((angle - zero + 180) % 360) - 180
                          for angle, zero in zip(pose, self.pose_zero))
            values['head_tilt'] = clamp(abs(delta[2]) / 35)
        else:
            delta = None
            for key in ('head_tilt', 'head_nod', 'head_shake'):
                values[key] = None
        self.history.append((now, jaw, delta))
        while self.history and now - self.history[0][0] > 1.2:
            self.history.popleft()
        # Require an observable quick close from a previously open mouth.
        recent_jaw = [(now - item[0], item[1]) for item in self.history
                      if .035 <= now - item[0] <= .25]
        if jaw < .15:
            changes = [previous - jaw for dt, previous in recent_jaw
                       if previous > .25 and previous - jaw > .30 and (previous - jaw) / dt > 2.0]
            if changes:
                values['mouth_close'] = clamp(max(changes) / .7)
        for key, axis in [('head_nod', 0), ('head_shake', 1)]:
            # Never infer movement across a period with missing head-pose data.
            points = []
            for item in reversed(self.history):
                if item[2] is None:
                    break
                points.append(item[2][axis])
            points.reverse()
            if delta is not None and len(points) >= 5:
                # Excursion followed by at least 4 degrees of return, not a held pose.
                peak, trough = max(points), min(points)
                peak_index, trough_index = points.index(peak), points.index(trough)
                returned_from_peak = (0 < peak_index < len(points) - 1
                                      and peak - points[0] >= 4 and peak - points[-1] >= 4)
                returned_from_trough = (0 < trough_index < len(points) - 1
                                        and points[0] - trough >= 4 and points[-1] - trough >= 4)
                if key == 'head_shake' and not (peak >= 8 and trough <= -8 and abs(points[-1]) <= 10):
                    # A single look to one side and back is not a left/right shake.
                    continue
                if (returned_from_peak or returned_from_trough) and peak - trough >= 10:
                    values[key] = clamp((peak - trough) / 45)
        return values


def calibrate_section(neutral, repetitions):
    if len(neutral) < 20 or len(repetitions) != 3 or any(len(r) < 12 for r in repetitions):
        raise ValueError('Need neutral samples and three tracked repetitions')
    all_samples = neutral + [v for repetition in repetitions for v in repetition]
    if any(not isinstance(v, (int, float)) or not math.isfinite(v) or not 0 <= v <= 1
           for v in all_samples):
        raise ValueError('Invalid measurements')
    low = quantile(neutral, .95)
    peaks = [quantile(r, .90) for r in repetitions]
    high = min(peaks)
    if high - low < .08:
        raise ValueError('Overlaps neutral or one repetition failed. Retry or leave disabled.')
    return {'neutral': low, 'active': high, 'sensitivity': .55}


class LabDetector:
    def __init__(self):
        self.controls = {}
        self.reset()

    def reset(self):
        self.filtered = {}
        self.armed = set()
        self.on_since, self.off_since = {}, {}
        self.last_time = None
        self.tongue_until = float('-inf')
        self.teeth_until = float('-inf')
        self.suppressed = {}

    def update(self, values, now):
        if values is None:
            self.reset()
            return [], {}
        dt = max(0, now - self.last_time) if self.last_time is not None else 0
        if dt > .4:
            self.reset()
        self.last_time = now
        self.suppressed = {}
        if suppression_reason('mouth_open', values, self.controls):
            self.tongue_until = now + .25
        if suppression_reason('smile', values, self.controls):
            self.teeth_until = now + .25
        alpha = 1 - math.exp(-dt / .035) if dt else 1
        events, meters = [], {}
        for key, config in self.controls.items():
            raw = values.get(key)
            reason = suppression_reason(key, values, self.controls)
            if key in ('mouth_open', 'mouth_close') and now < self.tongue_until:
                reason = 'tongue gesture / return to neutral'
            if key == 'smile' and now < self.teeth_until:
                reason = 'visible teeth / return to neutral'
            if reason:
                self.suppressed[key] = reason
            if (reason or not isinstance(raw, (int, float)) or isinstance(raw, bool)
                    or not math.isfinite(raw) or not 0 <= raw <= 1):
                self.armed.discard(key)
                self.on_since.pop(key, None)
                self.off_since.pop(key, None)
                self.filtered.pop(key, None)
                continue
            value = self.filtered.get(key, raw)
            value += alpha * (raw - value)
            self.filtered[key] = value
            amount = (value - config['neutral']) / (config['active'] - config['neutral'])
            meters[key] = clamp(amount)
            on, off = config['sensitivity'], max(.05, config['sensitivity'] - .2)
            if amount <= off:
                self.on_since.pop(key, None)
                if now - self.off_since.setdefault(key, now) >= .15:
                    self.armed.add(key)
            else:
                self.off_since.pop(key, None)
                if amount >= on and key in self.armed:
                    if now - self.on_since.setdefault(key, now) >= BY_KEY[key].dwell:
                        events.append(key)
                        self.armed.discard(key)
                        self.on_since.pop(key, None)
                else:
                    self.on_since.pop(key, None)
        return events, meters


class TrialBook:
    """Prompt-window success and neutral false events; no invented latency/precision."""
    def __init__(self):
        self.results = {key: {'attempts': 0, 'detected': 0, 'missed': 0, 'duplicates': 0,
                              'cross_activations': {}, 'neutral_events': 0} for key in KEYS}
        self.neutral_seconds = 0.0
        self.invalid_trials = 0

    def trial(self, target, events, valid=True):
        if not valid:
            self.invalid_trials += 1
            return
        result = self.results[target]
        result['attempts'] += 1
        count = events.count(target)
        result['detected' if count else 'missed'] += 1
        result['duplicates'] += max(0, count - 1)
        for key in events:
            if key != target:
                result['cross_activations'][key] = result['cross_activations'].get(key, 0) + 1

    def neutral(self, dt, events):
        self.neutral_seconds += max(0, dt)
        for key in events:
            self.results[key]['neutral_events'] += 1

    def reset_control(self, key):
        self.results[key] = {'attempts': 0, 'detected': 0, 'missed': 0, 'duplicates': 0,
                             'cross_activations': {}, 'neutral_events': 0}
        # A changed detector invalidates the previous shared neutral exposure.
        self.neutral_seconds = 0
        for result in self.results.values():
            result['neutral_events'] = 0

    def report(self):
        details = {}
        for key, result in self.results.items():
            details[key] = dict(result)
            details[key]['prompt_window_detection_rate'] = (
                result['detected'] / result['attempts'] if result['attempts'] else None)
            details[key]['false_events_per_10_min'] = (
                result['neutral_events'] * 600 / self.neutral_seconds if self.neutral_seconds else None)
            enough = result['attempts'] >= 50 and self.neutral_seconds >= 600
            meets = (enough and details[key]['prompt_window_detection_rate'] >= .95
                     and details[key]['false_events_per_10_min'] < 2 and result['duplicates'] == 0)
            details[key]['screening'] = 'candidate: review cross-activations' if meets else (
                'needs improvement' if enough else 'insufficient trials')
        return {'results': details, 'neutral_tracked_seconds': self.neutral_seconds,
                'invalid_trials': self.invalid_trials,
                'limits': 'Prompt-window rates are not frame-labelled precision/recall or end-to-end latency. '
                          'Cross-activations may be real co-occurring movements; review them. '
                          'Passing screening is not permission to enable game actions.'}
