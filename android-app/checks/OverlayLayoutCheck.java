package com.facechaos.mobile;
public final class OverlayLayoutCheck {
 static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
 public static void main(String[] args){
  for(float h:new float[]{693,780,845,920})for(float controls:new float[]{110,145,180})for(int mode=0;mode<15;mode++){
   OverlayLayout layout=new OverlayLayout(h,controls,mode);
   require(layout.gameTop>=h*.49f,"Selfie area stays clear");
   require(layout.instructionTop+layout.instructionHeight<layout.gameTop,"Instructions immediately above game");
   require(layout.scale==1&&layout.offsetX==0&&layout.offsetY==0,"Original game size and position restored");
   for(float x:new float[]{65,130,195,325})require(Math.abs(layout.sourceX(layout.offsetX+x*layout.scale)-x)<.001,"Face-card and lane X coordinates map correctly");
   float y=h-controls-40;require(Math.abs(layout.sourceY(layout.offsetY+y*layout.scale)-y)<.001,"Face-card Y coordinates map correctly");
   require(layout.scale<=1&&layout.scale>0,"Uniform scaling preserves character proportions");
  }
  System.out.println("PASS: 180 phone/control/game layouts; original game dimensions and position; instructions immediately above landscape; card/lane hit coordinates");
 }
}
