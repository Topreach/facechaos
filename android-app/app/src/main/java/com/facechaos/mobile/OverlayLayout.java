package com.facechaos.mobile;

/** Instructions sit above the existing game landscape; game coordinates stay unchanged. */
final class OverlayLayout {
 final float captionBottom;
 final float instructionTop,instructionHeight=62,gameTop,gameBottom,sourceTop,scale=1,offsetX=0,offsetY=0;
 OverlayLayout(float height,float controls,int mode){
  captionBottom=height-Math.max(110,controls)-8;
  gameBottom=height-Math.max(110,controls)+44;
  float landscapeTop=Math.max(height*.56f,gameBottom-246);
  if(mode==12||mode==13)landscapeTop=Math.max(height*.58f,height-Math.max(118,controls)+4-245);
  gameTop=landscapeTop;sourceTop=gameTop;
  instructionTop=gameTop-instructionHeight-6;
 }
 float captionTop(float faceBottom,boolean calibrating){return Math.max(calibrating?gameTop+6:instructionTop,faceBottom<0?0:faceBottom+8);}
 float sourceX(float x){return x;}
 float sourceY(float y){return y;}
}
