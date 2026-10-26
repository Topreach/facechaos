package com.facechaos.mobile;

public final class PreviewGeometryCheck {
    public static void main(String[] args) {
        // Regression: Samsung's 270-degree front sensor is ALREADY upright at display 0.
        PreviewGeometry portrait = PreviewGeometry.calculate(1920, 1080, 1080, 2340, 270, 0);
        require(portrait.displayCorrection == 0, "Do not rotate the sensor twice");
        require(portrait.naturalWidth == 1080 && portrait.naturalHeight == 1920, "Portrait dimensions");
        for (int sensor : new int[]{0, 90, 180, 270}) {
            for (int display : new int[]{0, 90, 180, 270}) {
                for (float[] viewport : new float[][]{{1080,2340},{2340,1080},{800,800},{420,900}}) {
                    PreviewGeometry g = PreviewGeometry.calculate(1920,1080,viewport[0],viewport[1],sensor,display);
                    float rw = display % 180 == 0 ? g.naturalWidth : g.naturalHeight;
                    float rh = display % 180 == 0 ? g.naturalHeight : g.naturalWidth;
                    float renderedW = rw*g.zoom, renderedH = rh*g.zoom;
                    require(renderedW+.01 >= viewport[0] && renderedH+.01 >= viewport[1], "Fill viewport without gaps");
                    require(Math.abs(renderedW-viewport[0]) < .01 || Math.abs(renderedH-viewport[1]) < .01, "Minimum center crop");
                    require(Math.abs(renderedW/renderedH-rw/rh) < .0001, "Preserve proportions");
                    require(g.displayCorrection == -display, "Only compensate display rotation");
                }
            }
        }
        System.out.println("PASS: Samsung portrait regression and 64 sensor/display/viewport crop combinations");
    }
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
}
