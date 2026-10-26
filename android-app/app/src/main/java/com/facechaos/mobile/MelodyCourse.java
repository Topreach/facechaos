package com.facechaos.mobile;
/** Authored scrolling voice course. Values are world units, never microphone thresholds. */
final class MelodyCourse {
 static final int[][] HEIGHTS={{18,24,28,20,32,24,36,28},{30,36,42,32,46,36,50,42},{40,48,52,44,56,48,60,54},{48,56,60,52,64,58,68,62},{56,62,66,60,70,64,72,68}};
 static String difficulty(int stage){return new String[]{"Simple","Difficult","Hard","Extreme","Finale"}[stage-1];}
 static int height(int stage,int encounter){return HEIGHTS[stage-1][Math.floorMod(encounter,8)];}
 static int kind(int stage,int encounter){return Math.floorMod(encounter+stage-1,Math.min(5,stage+1));}
 static String obstacle(int stage,int encounter){return new String[]{"Ember gap","Raised step","Twin fire pits","Tall tower","Drifting ledge"}[kind(stage,encounter)];}
 static float speed(int stage){return 70+(stage-1)*5;}
 static float ceilingMargin(int stage,int encounter){return stage>=2&&kind(stage,encounter)==1?44-stage*5:Float.POSITIVE_INFINITY;}
 static float drift(int stage,int encounter,float phase){return kind(stage,encounter)==4?5*(float)Math.sin((phase-2.65f)*2):0;}
 static float required(int stage,int encounter,float phase){return height(stage,encounter)+drift(stage,encounter,phase);}
 static float targetStrength(int stage,int encounter){float t=.45f,h=height(stage,encounter);float apex=(h+500*t*t)*(h+500*t*t)/(2000*t*t);return Math.max(.15f,Math.min(.8f,(apex-18)/82));}
}
