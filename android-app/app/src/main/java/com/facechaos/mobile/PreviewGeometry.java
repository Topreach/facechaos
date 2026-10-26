package com.facechaos.mobile;

/** Pure geometry, independent of Android, for the already sensor-oriented texture. */
final class PreviewGeometry {
    final float naturalWidth, naturalHeight, zoom;
    final int displayCorrection;
    private PreviewGeometry(float width, float height, float zoom, int correction) {
        naturalWidth = width; naturalHeight = height; this.zoom = zoom;
        displayCorrection = correction;
    }
    static PreviewGeometry calculate(int bufferWidth, int bufferHeight, float viewWidth,
                                     float viewHeight, int sensor, int display) {
        if (bufferWidth <= 0 || bufferHeight <= 0 || viewWidth <= 0 || viewHeight <= 0)
            throw new IllegalArgumentException("Positive dimensions required");
        boolean sensorSwaps = sensor % 180 != 0;
        float width = sensorSwaps ? bufferHeight : bufferWidth;
        float height = sensorSwaps ? bufferWidth : bufferHeight;
        boolean displaySwaps = display % 180 != 0;
        float rotatedWidth = displaySwaps ? height : width;
        float rotatedHeight = displaySwaps ? width : height;
        return new PreviewGeometry(width, height,
            Math.max(viewWidth / rotatedWidth, viewHeight / rotatedHeight), -display);
    }
}
