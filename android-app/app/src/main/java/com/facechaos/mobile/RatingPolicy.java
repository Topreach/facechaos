package com.facechaos.mobile;
/** Optional local feedback, never a substitute for a public store review. */
final class RatingPolicy {
 static final long COOLDOWN=7L*24*60*60*1000;
 static boolean due(int completed,long lastPrompt,boolean rated,boolean disabled,long now){return completed>=3&&!rated&&!disabled&&(lastPrompt==0||now>=lastPrompt&&now-lastPrompt>=COOLDOWN);}
 static boolean valid(float stars){return Float.isFinite(stars)&&stars>=1&&stars<=5&&stars==(int)stars;}
}
