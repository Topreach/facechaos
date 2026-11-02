package com.facechaos.mobile;

/** Immutable, bounded appearance settings. No facial geometry or recognition input. */
final class CameraLook {
 static final int OFF=0,NATURAL=1,SMOOTH=2;
 static final CameraLook ORIGINAL=new CameraLook(OFF,0,0);
 final int mode;
 final float intensity,warmth;
 final boolean smoothing;
 CameraLook(int mode,float intensity,float warmth){this(mode,intensity,warmth,true);}
 private CameraLook(int mode,float intensity,float warmth,boolean smoothing){this.mode=mode>=OFF&&mode<=SMOOTH?mode:OFF;this.intensity=finite(intensity,0,1,.55f);this.warmth=finite(warmth,-1,1,0);this.smoothing=smoothing;}
 CameraLook supported(boolean available){return new CameraLook(!available&&mode==SMOOTH?NATURAL:mode,intensity,warmth,available);}
 float strength(){return mode==OFF?0:Math.min(1,intensity*(mode==SMOOTH?1.15f:1));}
 float softness(){return smoothing?strength()*(mode==SMOOTH?.85f:.18f):0;}
 float[] colorMatrix(){float lift=.22f*strength(),gain=1-lift,w=.075f*warmth*strength();return new float[]{gain,0,0,0,(lift+w)*255,0,gain,0,0,lift*255,0,0,gain,0,(lift-w)*255,0,0,0,1,0};}
 private static float finite(float value,float min,float max,float fallback){return Float.isFinite(value)?Math.max(min,Math.min(max,value)):fallback;}
}
