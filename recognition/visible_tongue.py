"""Experimental tongue-protrusion evidence, independent of jaw/teeth signals.

Looks for connected reddish/pink tissue extending below the lower lip. This
conservative image heuristic is not a trained semantic tongue classifier.
"""
import numpy as np
from visual_input import valid_region_input
from visible_teeth import INNER_LIP


OUTER_LIP = (61, 185, 40, 39, 37, 0, 267, 269, 270, 409, 291,
             375, 321, 405, 314, 17, 84, 181, 91, 146)


def measure_tongue(frame, landmarks, debug=False):
    import cv2
    if not valid_region_input(frame, landmarks, tuple(set(OUTER_LIP + INNER_LIP + (205, 425)))):
        return {'score': None, 'state': 'invalid tongue image/landmarks'}
    height, width = frame.shape[:2]
    point = lambda i: np.asarray((landmarks[i].x * width, landmarks[i].y * height), dtype=float)
    left, right = point(61), point(291)
    span = float(np.linalg.norm(right - left))
    if span < 24 or not np.isfinite(span):
        return {'score': None, 'state': 'mouth region too small / invalid'}
    across = (right - left) / span
    down = np.asarray((-across[1], across[0]))
    if down[1] < 0:
        down = -down
    origin = (left + right) / 2
    lower_outer = float(np.dot(point(17) - origin, down))
    lower_inner = float(np.dot(point(14) - origin, down))
    top = min(lower_inner - span * .05, lower_outer)
    bottom = lower_outer + span * .65
    corners = [origin + across * x + down * y for x in (-span * .48, span * .48) for y in (top, bottom)]
    x1, x2 = max(0, int(min(p[0] for p in corners))), min(width, int(max(p[0] for p in corners)) + 1)
    y1, y2 = max(0, int(min(p[1] for p in corners))), min(height, int(max(p[1] for p in corners)) + 1)
    if x2 - x1 < 8 or y2 - y1 < 8:
        return {'score': None, 'state': 'tongue search region off frame'}
    roi = frame[y1:y2, x1:x2]
    hsv = cv2.cvtColor(roi, cv2.COLOR_BGR2HSV)
    lab = cv2.cvtColor(roi, cv2.COLOR_BGR2LAB)
    reference = []
    for index in (205, 425):
        cx, cy = point(index).astype(int)
        radius = max(2, int(span * .07))
        patch = frame[max(0, cy-radius):min(height, cy+radius+1), max(0, cx-radius):min(width, cx+radius+1)]
        if patch.size:
            reference.append(float(np.median(cv2.cvtColor(patch, cv2.COLOR_BGR2LAB)[:, :, 1])))
    if not reference:
        return {'score': None, 'state': 'skin reference missing'}
    skin_red = float(np.median(reference))
    yy, xx = np.mgrid[y1:y2, x1:x2]
    dx, dy = xx - origin[0], yy - origin[1]
    local_x, local_y = dx * across[0] + dy * across[1], dx * down[0] + dy * down[1]
    b, g, r = [roi[:, :, i].astype(float) for i in range(3)]
    reddish = (((hsv[:, :, 0] < 24) | (hsv[:, :, 0] > 165)) & (hsv[:, :, 1] >= 40)
               & (hsv[:, :, 2] >= 65) & (r > g * 1.08) & (r > b * 1.03)
               & (lab[:, :, 1].astype(float) >= skin_red + 7))
    region = (np.abs(local_x) <= span * .40) & (local_y >= top) & (local_y <= bottom)
    # Colour alone cannot distinguish lip tissue from tongue tissue. Exclude
    # the landmark-defined lip ring before measuring any candidate extension.
    outer_mask = np.zeros(roi.shape[:2], dtype=np.uint8)
    inner_mask = np.zeros_like(outer_mask)
    for contour, target in ((OUTER_LIP, outer_mask), (INNER_LIP, inner_mask)):
        polygon = np.round([point(i) - (x1, y1) for i in contour]).astype(np.int32)
        cv2.fillPoly(target, [polygon], 1)
    lip_ring = (outer_mask > 0) & (inner_mask == 0)
    excluded_lip_pixels = int(np.count_nonzero(reddish & region & lip_ring))
    mask = (reddish & region & ~lip_ring).astype(np.uint8)
    mask = cv2.morphologyEx(mask, cv2.MORPH_CLOSE, np.ones((3, 3), dtype=np.uint8))
    mask[~region | lip_ring] = 0
    count, labels, stats, _ = cv2.connectedComponentsWithStats(mask, 8)
    extension_boundary = lower_outer + max(2, span * .035)
    best = 0.0
    attached = 0
    extended_total = int(np.count_nonzero((mask > 0) & (local_y > extension_boundary)))
    for index in range(1, count):
        component = labels == index
        # A detached pink patch on the chin is not evidence of a tongue.
        near_mouth = component & (local_y <= lower_outer + span * .04)
        if np.any(near_mouth):
            attached += 1
        extended = component & (local_y > extension_boundary)
        extended_pixels = int(np.count_nonzero(extended))
        if not np.any(near_mouth) or extended_pixels < max(6, span * span * .012):
            continue
        if stats[index, cv2.CC_STAT_WIDTH] < span * .09:
            continue
        extent = float(np.max(local_y[extended]) - lower_outer) / span
        area = extended_pixels / (span * span)
        best = max(best, min(1.0, extent / .25) * min(1.0, area / .06))
    candidate_pixels = int(np.count_nonzero(mask))
    if candidate_pixels == 0:
        reason = 'no pink/red tissue passed colour filter'
    elif extended_total < max(6, span * span * .012):
        reason = 'insufficient tissue beyond estimated lower lip'
    elif attached == 0:
        reason = 'tissue detached from estimated mouth'
    elif best == 0:
        reason = 'candidate too small or narrow'
    else:
        reason = 'protruding-tongue candidate found (experimental)'
    result = {'score': float(best), 'state': reason, 'skin_red_reference': skin_red,
              'required_red_contrast': 7, 'mouth_span_pixels': span,
              'candidate_pixels': candidate_pixels, 'extended_pixels': extended_total,
              'excluded_lip_pixels': excluded_lip_pixels,
              'attached_components': attached, 'lower_lip_offset_pixels': lower_outer}
    if debug:
        result['_overlay'] = {'box': (x1, y1, x2, y2), 'mask': mask,
                              'line': [origin + across * x + down * extension_boundary
                                       for x in (-span * .45, span * .45)]}
    return result


def draw_tongue_debug(frame, overlay):
    import cv2
    if not overlay:
        return frame
    output = frame.copy()
    x1, y1, x2, y2 = overlay['box']
    roi = output[y1:y2, x1:x2]
    selected = overlay['mask'] > 0
    roi[selected] = (.45 * roi[selected] + .55 * np.array([40, 240, 40])).astype(np.uint8)
    cv2.rectangle(output, (x1, y1), (x2-1, y2-1), (255, 190, 40), 1)
    start, end = [tuple(np.round(p).astype(int)) for p in overlay['line']]
    cv2.line(output, start, end, (0, 230, 255), 1)
    return output
