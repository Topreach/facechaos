"""Experimental visible-teeth evidence from the inner-lip image region.

Independent of jaw-opening/smile scores. Bright low-chroma mouth pixels are
evidence, not a universal tooth detector; glare/dentures/lighting need trials.
"""
import numpy as np
from visual_input import valid_region_input

INNER_LIP = (78, 191, 80, 81, 82, 13, 312, 311, 310, 415, 308,
             324, 318, 402, 317, 14, 87, 178, 88, 95)


def measure_teeth(frame, landmarks):
    import cv2
    if not valid_region_input(frame, landmarks, INNER_LIP):
        return {'score': None, 'state': 'invalid mouth image/landmarks'}
    height, width = frame.shape[:2]
    polygon = np.asarray([(round(landmarks[i].x * width), round(landmarks[i].y * height))
                          for i in INNER_LIP], dtype=np.int32)
    x, y, w, h = cv2.boundingRect(polygon)
    x1, y1, x2, y2 = max(0, x), max(0, y), min(width, x + w), min(height, y + h)
    if x2 - x1 < 16:
        return {'score': None, 'state': 'mouth region too small / off frame'}
    if y2 - y1 < 2:
        return {'score': 0.0, 'state': 'lips closed / no visible inner-mouth area'}
    mask = np.zeros((y2 - y1, x2 - x1), dtype=np.uint8)
    cv2.fillPoly(mask, [polygon - np.array([x1, y1])], 255)
    area = int(np.count_nonzero(mask))
    if area < 16:
        return {'score': 0.0, 'state': 'lips closed / insufficient inner-mouth area'}
    roi = frame[y1:y2, x1:x2]
    hsv = cv2.cvtColor(roi, cv2.COLOR_BGR2HSV)
    b, g, r = [roi[:, :, i].astype(float) for i in range(3)]
    # Pale pink tongue/lip pixels may also be bright and low saturation. Reject
    # red excess relative to the yellow/blue difference; retain white/ivory teeth.
    pink = (r - g) > (.65 * np.maximum(0, g - b) + 12)
    # Excludes red tongue/lips and the surrounding skin outside the inner-lip mask.
    candidates = ((hsv[:, :, 2] >= 145) & (hsv[:, :, 1] <= 105) & ~pink & (mask > 0)).astype(np.uint8)
    candidates = cv2.morphologyEx(candidates, cv2.MORPH_OPEN, np.ones((2, 2), dtype=np.uint8))
    candidates[mask == 0] = 0
    count, _labels, stats, _centers = cv2.connectedComponentsWithStats(candidates, 8)
    component_sizes = stats[1:, cv2.CC_STAT_AREA] if count > 1 else []
    pixels = sum(int(size) for size in component_sizes if size >= max(3, area * .01))
    fraction = pixels / area
    score = max(0.0, min(1.0, (fraction - .02) / .30))
    return {'score': score, 'state': 'direct visible-teeth candidate (experimental)',
            'bright_inner_mouth_fraction': fraction, 'mouth_pixels': area}
