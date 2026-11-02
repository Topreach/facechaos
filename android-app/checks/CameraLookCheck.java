package com.facechaos.mobile;
public final class CameraLookCheck {
 public static void main(String[] args){
  CameraLook off=new CameraLook(CameraLook.OFF,1,1);float[] identity=off.colorMatrix();if(off.strength()!=0||off.softness()!=0||identity[0]!=1||identity[4]!=0||identity[12]!=1)throw new AssertionError("Off must preserve the original");
  CameraLook zero=new CameraLook(CameraLook.SMOOTH,0,1);if(zero.strength()!=0||zero.softness()!=0||zero.colorMatrix()[4]!=0)throw new AssertionError("Zero strength must preserve the original");
  CameraLook smooth=new CameraLook(CameraLook.SMOOTH,1,1),natural=new CameraLook(CameraLook.NATURAL,1,1);if(smooth.softness()<=natural.softness()||smooth.softness()>.85f)throw new AssertionError("Bounded softening");
  CameraLook old=smooth.supported(false);if(old.mode!=CameraLook.NATURAL||old.softness()!=0||old.strength()!=1)throw new AssertionError("Old devices retain brightness without unsupported smoothing");
  CameraLook bad=new CameraLook(9,Float.NaN,Float.POSITIVE_INFINITY);if(bad.mode!=CameraLook.OFF||!Float.isFinite(bad.intensity)||!Float.isFinite(bad.warmth))throw new AssertionError("Invalid preferences");
  for(float amount:new float[]{-2,0,.35f,1,4})for(float warmth:new float[]{-4,0,4}){CameraLook look=new CameraLook(CameraLook.NATURAL,amount,warmth);if(look.intensity<0||look.intensity>1||look.warmth< -1||look.warmth>1)throw new AssertionError("Unbounded appearance");for(float value:look.colorMatrix())if(!Float.isFinite(value))throw new AssertionError("Invalid color transform");}
  System.out.println("Appearance identity, bounded intensity/warmth, softening strength and older-device fallback passed");
 }
}
