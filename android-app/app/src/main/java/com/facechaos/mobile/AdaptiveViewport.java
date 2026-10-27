package com.facechaos.mobile;

/** Keep the original phone canvas while fitting a tall game into wide windows. */
final class AdaptiveViewport {
    static int overlayWidth(int width,int height,float density) {
        if(width<=0||height<=0)return 1;
        if(width<=height&&width/density<600)return width;
        return Math.max(1,Math.min(width,Math.min(Math.round(height*9f/16),Math.round(560*density))));
    }
    static int controlsWidth(int width,float density) {
        return Math.max(1,Math.min(width,Math.round(560*density)));
    }
}
