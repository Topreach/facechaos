package com.facechaos.mobile;
public final class FaceFlexibilityCheck {
 static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
 static FaceSignals relaxed(int interval){FaceSignals f=new FaceSignals();for(int t=0;t<=900;t+=interval)f.accept(.14f,.34f,.05f,0,t,1);require(f.ready,"Personalized resting jaw/smile calibrate");return f;}
 public static void main(String[] args){
  for(int interval:new int[]{33,60,100,150})relaxed(interval);
  FaceSignals fast=new FaceSignals();for(int i=0;i<12;i++)fast.accept(.03f,.04f,0,0,i,1);require(!fast.ready,"A burst of frames cannot shortcut the timed baseline");
  FaceSignals blinking=new FaceSignals();for(int t=0;t<=900;t+=60)blinking.accept(.05f,.08f,t==300?.9f:0,0,t,1);require(blinking.ready,"A normal blink does not reset neutral calibration");
  FaceSignals f=relaxed(60);require(f.accept(.6f,.34f,.05f,0,1000,1)==-1,"Expression requires stability");require(f.accept(.6f,.34f,.05f,0,1120,1)==FaceSignals.MOUTH,"Mouth responds within 120 ms after calibration");
  for(int t=1180;t<=1360;t+=60)require(f.accept(.6f,.34f,.05f,0,t,1)==-1,"Held mouth cannot repeat");
  f.accept(.14f,.34f,.05f,0,1420,1);f.accept(.14f,.34f,.05f,0,1540,1);
  f.accept(.14f,.75f,.05f,0,1600,1);require(f.accept(.14f,.75f,.05f,0,1720,1)==FaceSignals.SMILE,"Quick neutral return rearms a distinct smile");
  f.lost();require(f.recovering,"Tracking loss requests a brief neutral recovery");f.accept(.14f,.34f,.05f,0,1800,1);f.accept(.14f,.34f,.05f,0,1920,1);require(!f.recovering&&f.armed,"Recovery keeps personal baseline and rearms after neutral");
  f.reset();require(!f.ready&&f.neutralFrames==0,"Manual recalibration clears the baseline");
  HeadSignals head=new HeadSignals();for(int i=0;i<8;i++)head.accept(.26f,.2f);require(head.ready,"A comfortable off-centre position can calibrate head steering");head.accept(.32f,.2f);require(head.lane==1,"Steering stays relative to the personal centre");
  for(float h:new float[]{693,845,920})for(int mode=0;mode<15;mode++)for(float bottom:new float[]{.35f,.48f,.58f}){
   OverlayLayout layout=new OverlayLayout(h,114,mode);for(boolean calibrating:new boolean[]{false,true})require(layout.captionTop(bottom*h,calibrating)>=bottom*h+8,"Sticker stays below detected face");require(layout.scale==1&&layout.offsetY==0,"Face-aware captions never resize or move the game");
  }
  System.out.println("PASS: natural baselines at four frame cadences, blink-tolerant timed calibration, responsive deliberate reactions, held-input rejection, recovery, off-centre steering and face-clear captions with unchanged game geometry");
 }
}
