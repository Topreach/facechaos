"""Fail closed for unusable per-region image measurements."""
import math
import numpy as np


def valid_region_input(frame, landmarks, indices):
    if (not isinstance(frame, np.ndarray) or frame.dtype != np.uint8 or frame.ndim != 3
            or frame.shape[2] != 3 or frame.shape[0] < 1 or frame.shape[1] < 1):
        return False
    try:
        for index in indices:
            point = landmarks[index]
            if not all(math.isfinite(v) and -.5 <= v <= 1.5 for v in (point.x, point.y)):
                return False
    except (TypeError, IndexError, AttributeError):
        return False
    return True
