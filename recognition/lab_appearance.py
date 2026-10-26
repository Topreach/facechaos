"""Experimental personal mouth/cheek classifiers; no raw frames persisted.

Teeth/tongue cannot be confirmed from a grin/jaw blendshape. Learn per-user
positive and confounding examples using compact color/gradient histograms.
These remain experimental and must pass independent held-out trials.
"""
import numpy as np
from visual_input import valid_region_input


def mouth_descriptor(frame, landmarks):
    import cv2
    if not valid_region_input(frame, landmarks, (61, 291, 13, 14, 0, 17, 78, 308)):
        return None
    height, width = frame.shape[:2]
    points = [landmarks[i] for i in (61, 291, 13, 14, 0, 17, 78, 308)]
    xs, ys = [p.x * width for p in points], [p.y * height for p in points]
    pad_x = max(4, (max(xs) - min(xs)) * .12)
    # Include the area below the lips where a protruding tongue can appear.
    pad_y = max(8, (max(xs) - min(xs)) * .35)
    x1, x2 = max(0, int(min(xs) - pad_x)), min(width, int(max(xs) + pad_x))
    y1, y2 = max(0, int(min(ys) - pad_y)), min(height, int(max(ys) + pad_y))
    if x2 - x1 < 24 or y2 - y1 < 8:
        return None
    return region_descriptor(frame[y1:y2, x1:x2])


def region_descriptor(roi):
    import cv2
    roi = cv2.resize(roi, (64, 32))
    hsv = cv2.cvtColor(roi, cv2.COLOR_BGR2HSV)
    features = []
    # Coarse quadrant histograms, not resampled camera pixels.
    for y in (0, 16):
        for x in (0, 32):
            block = hsv[y:y + 16, x:x + 32]
            for channel, maximum in ((0, 180), (1, 256), (2, 256)):
                histogram = cv2.calcHist([block], [channel], None, [8], [0, maximum]).ravel()
                features.extend(histogram / max(1, histogram.sum()))
    gray = cv2.cvtColor(roi, cv2.COLOR_BGR2GRAY)
    gx = cv2.Sobel(gray, cv2.CV_32F, 1, 0)
    gy = cv2.Sobel(gray, cv2.CV_32F, 0, 1)
    magnitude, angles = cv2.cartToPolar(gx, gy)
    histogram, _ = np.histogram(angles, bins=8, range=(0, 2 * np.pi), weights=magnitude)
    features.extend(histogram / max(1, histogram.sum()))
    return np.asarray(features, dtype=float)


def cheek_descriptor(frame, landmarks):
    """Personal bilateral cheek appearance/shape; does not trust cheekPuff alone.

    Regions follow the cheek landmarks, with distances normalized by eye span.
    Head angle, lighting and smiles remain confounds to test independently.
    """
    if not valid_region_input(frame, landmarks, (33, 263, 205, 425, 234, 454, 93, 323,
                                                132, 361, 61, 291, 1, 187, 411)):
        return None
    height, width = frame.shape[:2]
    points = np.asarray([(p.x * width, p.y * height) for p in landmarks], dtype=float)
    eye_span = float(np.linalg.norm(points[33] - points[263]))
    if eye_span < 45:
        return None
    descriptors = []
    for center_id in (205, 425):
        cx, cy = points[center_id]
        radius = eye_span * .18
        x1, x2 = max(0, int(cx - radius)), min(width, int(cx + radius))
        y1, y2 = max(0, int(cy - radius)), min(height, int(cy + radius))
        if x2 - x1 < 12 or y2 - y1 < 12:
            return None
        descriptors.append(region_descriptor(frame[y1:y2, x1:x2]))
    shape = []
    for a, b in ((234, 454), (93, 323), (132, 361), (205, 425), (61, 291),
                 (205, 1), (425, 1), (187, 33), (411, 263)):
        shape.append(float(np.linalg.norm(points[a] - points[b])) / eye_span)
    return np.concatenate([*descriptors, np.asarray(shape)])


class AppearanceModel:
    def to_dict(self):
        return {'scale': self.scale.tolist(), 'positive': self.positive.tolist(),
                'negative': self.negative.tolist(), 'radius': float(self.radius)}

    @classmethod
    def from_dict(cls, data, dimensions=None):
        if not isinstance(data, dict):
            raise ValueError('Invalid appearance model')
        try:
            scale = np.asarray(data['scale'], dtype=float)
            positive = np.asarray(data['positive'], dtype=float)
            negative = np.asarray(data['negative'], dtype=float)
            radius = float(data['radius'])
        except (KeyError, TypeError, ValueError) as error:
            raise ValueError('Invalid appearance model arrays') from error
        if (scale.ndim != 1 or not 1 <= scale.size <= 512
                or (dimensions is not None and scale.size != dimensions)
                or positive.shape != (3, scale.size) or negative.ndim != 2
                or not 2 <= len(negative) <= 8 or negative.shape[1] != scale.size
                or not all(np.isfinite(v).all() for v in (scale, positive, negative))
                or not (scale > 0).all() or not (scale <= 100).all()
                or not np.isfinite(radius) or not 0 < radius <= 100
                or np.max(np.abs(positive)) > 100 or np.max(np.abs(negative)) > 100):
            raise ValueError('Appearance model has invalid dimensions or values')
        model = cls()
        model.scale, model.positive, model.negative, model.radius = scale, positive, negative, radius
        return model

    def fit(self, positives, negatives):
        if len(positives) != 3 or len(negatives) < 2:
            raise ValueError('Need three positive repetitions and at least two negative poses')
        if any(len(group) < 12 for group in positives + negatives):
            raise ValueError('Too few tracked appearance samples')
        all_values = np.concatenate([np.asarray(g) for g in positives + negatives])
        self.scale = np.maximum(all_values.std(axis=0), .035)
        self.positive = np.asarray([np.mean(g, axis=0) for g in positives])
        self.negative = np.asarray([np.mean(g, axis=0) for g in negatives])
        distances = [self._distance(x, self.positive) for g in positives for x in g]
        self.radius = max(.3, float(np.quantile(distances, .95)) * 3)
        # Reject unseparated examples rather than always inventing an enabled detector.
        pos_scores = [self.score(x) for g in positives for x in g]
        neg_scores = [self.score(x) for g in negatives for x in g]
        if np.quantile(pos_scores, .15) - np.quantile(neg_scores, .95) < .12:
            raise ValueError('Appearance samples overlap. Change lighting/retry or leave disabled.')
        return [float(x) for x in neg_scores], [[self.score(x) for x in g] for g in positives]

    def _distance(self, value, centers):
        return float(np.min(np.mean(((centers - value) / self.scale) ** 2, axis=1)))

    def score(self, value):
        return self.explain(value)['score']

    def explain(self, value):
        if value is None:
            return {'score': None, 'state': 'region missing / face too small'}
        value = np.asarray(value, dtype=float)
        if value.shape != self.scale.shape or not np.isfinite(value).all():
            return {'score': None, 'state': 'invalid region measurements'}
        positive = self._distance(value, self.positive)
        negative = self._distance(value, self.negative)
        if positive > self.radius:
            return {'score': 0.0, 'state': 'outside calibrated examples',
                    'positive_distance': positive, 'negative_distance': negative, 'radius': self.radius}
        return {'score': float(negative / max(1e-8, positive + negative)),
                'state': 'comparing personal examples', 'positive_distance': positive,
                'negative_distance': negative, 'radius': self.radius}
