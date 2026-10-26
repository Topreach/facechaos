"""Synthetic channel-isolation fixtures, not recordings of human expressions.

Visual scores and the trained-cheek score are supplied as fixtures here. Their
image/appearance algorithms have separate tests; this matrix checks routing,
temporal logic and command cross-activation, not real-world recognition accuracy.
"""
from lab_core import KEYS, FeatureEngine, LabDetector, prepare_detection_values


def scenario_events(target):
    engine, detector = FeatureEngine(), LabDetector()
    detector.controls = {key: {'neutral': 0.0, 'active': .8, 'sensitivity': .55} for key in KEYS}
    now = 0.0

    def tick(scores, pose=(0, 0, 0), visual=None):
        nonlocal now
        now += .03
        values = engine.extract(scores, pose, now)
        values.update(teeth=0.0, tongue_out=0.0, cheek_puff=0.0)
        values.update(visual or {})
        return detector.update(prepare_detection_values(values, {'cheek_puff': object()}), now)[0]

    for _ in range(20):
        tick({})
    if target == 'mouth_close':
        # A close requires a preceding opening. The genuine opening is not
        # misreported as an unexpected command during the close's action window.
        for _ in range(10):
            tick({'jawOpen': .9})
    results = []
    for index in range(40):
        scores, pose, visual = {}, (0, 0, 0), {}
        if target == 'mouth_open':
            scores['jawOpen'] = .9
        elif target == 'mouth_close':
            scores['jawOpen'] = .01
        elif target == 'blink':
            scores.update(eyeBlinkLeft=.95, eyeBlinkRight=.95)
        elif target == 'brows_up':
            scores['browInnerUp'] = .9
        elif target == 'smile':
            scores.update(mouthSmileLeft=.9, mouthSmileRight=.9, cheekSquintLeft=.85,
                          cheekSquintRight=.85, eyeSquintLeft=.75, eyeSquintRight=.75)
        elif target in ('teeth', 'tongue_out', 'cheek_puff'):
            visual[target] = .9
            if target == 'tongue_out':
                # Tongue protrusion may also raise jawOpen: only the tongue
                # command should be accepted when the tongue detector is enabled.
                scores['jawOpen'] = .9
        elif target == 'wink_left':
            scores.update(eyeBlinkLeft=.95, eyeBlinkRight=.05)
        elif target == 'wink_right':
            scores.update(eyeBlinkLeft=.05, eyeBlinkRight=.95)
        elif target == 'face_scrunch':
            scores.update(browDownLeft=.9, browDownRight=.9, noseSneerLeft=.9,
                          noseSneerRight=.9, cheekSquintLeft=.85, cheekSquintRight=.85,
                          eyeSquintLeft=.9, eyeSquintRight=.9, eyeBlinkLeft=.9, eyeBlinkRight=.9)
        elif target == 'head_tilt':
            pose = (0, 0, 25)
        elif target == 'head_nod':
            waveform = [0, 5, 10, 18, 25, 25, 18, 10, 5, 0]
            pose = (waveform[index] if index < len(waveform) else 0, 0, 0)
        elif target == 'head_shake':
            waveform = [0, 8, 16, 25, 25, 12, 0, -12, -25, -25, -12, 0]
            pose = (0, waveform[index] if index < len(waveform) else 0, 0)
        results.extend(tick(scores, pose, visual))
    return results


def crosscheck_matrix():
    results = {}
    for key in KEYS:
        observed = scenario_events(key)
        unexpected = [event for event in observed if event != key]
        results[key] = {'expected': [key], 'observed': observed, 'unexpected': unexpected,
                        'passed': observed.count(key) == 1 and not unexpected}
    return results
