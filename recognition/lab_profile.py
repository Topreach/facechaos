"""Explicit local calibration persistence. No video, images or executable formats."""
import json
import math
from pathlib import Path

from lab_core import BY_KEY
from lab_appearance import AppearanceModel

FEATURES = 'lab-v4-visible-teeth-tongue'
PREVIOUS_FEATURES = 'lab-v3-visible-teeth'
LEGACY_FEATURES = 'lab-v2-mouth104-cheeks217'
APPEARANCE_DIMENSIONS = {'cheek_puff': 217}
DETECTOR_REVISION = 4
RECALIBRATE_FROM_REVISION_1 = ('face_scrunch', 'wink_left', 'wink_right', 'mouth_close', 'head_shake')


def validate_profile(data, camera):
    if (not isinstance(data, dict) or data.get('version') != 1
            or data.get('features') not in (FEATURES, PREVIOUS_FEATURES, LEGACY_FEATURES) or type(data.get('camera')) is not int
            or data['camera'] != camera):
        raise ValueError('Calibration belongs to a different camera or lab version')
    controls, models = data.get('controls'), data.get('appearance')
    revision = data.get('detector_revision', 1)
    if type(revision) is not int or revision not in (1, 2, 3, DETECTOR_REVISION):
        raise ValueError('Unsupported facial detector revision')
    if isinstance(controls, dict):
        controls = dict(controls)
        # The separate eye-wide row was removed; preserve the renamed brows_up calibration.
        controls.pop('eyes_wide', None)
        controls.pop('frown', None)
        # The player now trains a closed-lip smile and a broad exposed-teeth pose.
        if revision < 4:
            controls.pop('smile', None)
            controls.pop('teeth', None)
        if revision < 2:
            for key in RECALIBRATE_FROM_REVISION_1:
                controls.pop(key, None)
    if data.get('features') == LEGACY_FEATURES and isinstance(controls, dict) and isinstance(models, dict):
        controls, models = dict(controls), dict(models)
        # Preserve other working controls; old classifier thresholds do not describe visible-pixel scores.
        controls.pop('teeth', None)
        models.pop('teeth', None)
    if data.get('features') in (LEGACY_FEATURES, PREVIOUS_FEATURES) and isinstance(controls, dict) and isinstance(models, dict):
        controls, models = dict(controls), dict(models)
        controls.pop('tongue_out', None)
        models.pop('tongue_out', None)
    if (not isinstance(controls, dict) or not isinstance(models, dict)
            or set(controls) - set(BY_KEY) or set(models) - set(APPEARANCE_DIMENSIONS)):
        raise ValueError('Unknown or missing calibration sections')
    clean_controls = {}
    for key, config in controls.items():
        if not isinstance(config, dict):
            raise ValueError('Invalid threshold settings')
        numbers = [config.get(k) for k in ('neutral', 'active', 'sensitivity')]
        if any(type(v) not in (int, float) or not math.isfinite(v) for v in numbers):
            raise ValueError('Non-numeric calibration threshold')
        low, high, threshold = numbers
        if not (0 <= low < high <= 1 and high - low >= .08 and .2 <= threshold <= .85):
            raise ValueError('Calibration range is invalid')
        clean_controls[key] = dict(zip(('neutral', 'active', 'sensitivity'), numbers))
    required = set(controls) & set(APPEARANCE_DIMENSIONS)
    if required != set(models):
        raise ValueError('Personal appearance examples are missing or inconsistent')
    clean_models = {key: AppearanceModel.from_dict(value, APPEARANCE_DIMENSIONS[key])
                    for key, value in models.items()}
    pose = data.get('pose_zero')
    if pose is not None and (not isinstance(pose, list) or len(pose) != 3
                            or any(type(v) not in (float, int) or not math.isfinite(v)
                                   or abs(v) > 180 for v in pose)):
        raise ValueError('Invalid head-pose origin')
    return clean_controls, clean_models, tuple(pose) if pose is not None else None


def save_profile(path, camera, controls, models, pose_zero):
    data = {'version': 1, 'features': FEATURES, 'camera': camera,
            'detector_revision': DETECTOR_REVISION,
            'controls': controls, 'appearance': {key: model.to_dict() for key, model in models.items()},
            'pose_zero': list(pose_zero) if pose_zero is not None else None,
            'privacy': 'Derived personal calibration/templates only. No camera images or video.'}
    validate_profile(data, camera)
    path = Path(path)
    path.parent.mkdir(parents=True, exist_ok=True)
    temporary = path.with_suffix(path.suffix + '.tmp')
    try:
        temporary.write_text(json.dumps(data, indent=2, allow_nan=False), encoding='utf-8')
        temporary.replace(path)
    finally:
        temporary.unlink(missing_ok=True)


def load_profile(path, camera):
    path = Path(path)
    if path.stat().st_size > 1024 * 1024:
        raise ValueError('Calibration file is too large')
    return validate_profile(json.loads(path.read_text(encoding='utf-8')), camera)
