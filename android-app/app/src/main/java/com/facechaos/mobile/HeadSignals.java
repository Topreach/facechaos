package com.facechaos.mobile;

/** Screen-relative head displacement, independently calibrated from facial expressions. */
final class HeadSignals {
 int samples,lane;
 float center,width,offset,sumX,sumWidth;
 boolean ready;
 String status="Centre your head and hold still";
 void reset(){lost();}
 void lost(){samples=0;sumX=sumWidth=0;ready=false;lane=0;offset=0;status="No head tracked - move into view";}
 void accept(float noseX,float faceWidth){accept(noseX,faceWidth,1);}
 void accept(float noseX,float faceWidth,float sensitivity){
  if(!Float.isFinite(noseX)||!Float.isFinite(faceWidth)||faceWidth<.10f||noseX<.04f||noseX>.96f){lost();return;}
  if(!ready){
   if(noseX<.22f||noseX>.78f){samples=0;sumX=sumWidth=0;status="Move your head toward the middle of the selfie";return;}
   if(samples>0&&Math.abs(noseX-sumX/samples)>faceWidth*.18f){samples=0;sumX=sumWidth=0;}
   sumX+=noseX;sumWidth+=faceWidth;samples++;status="Head calibration "+samples+" / 8 - look centred";
   if(samples>=8){center=sumX/8;width=sumWidth/8;ready=true;lane=0;status="Head ready - centre lane";}return;
  }
  width=width*.85f+faceWidth*.15f;
  offset=offset*.15f+Math.max(-1,Math.min(1,(noseX-center)/width))*.85f;
  float scale=Math.max(.7f,Math.min(1.3f,sensitivity));
  if(offset<-.18f*scale)lane=-1;else if(offset>.18f*scale)lane=1;else if(Math.abs(offset)<.08f*scale)lane=0;
  status=lane<0?"Head left - left lane":lane>0?"Head right - right lane":"Head centred - middle lane";
 }
}
