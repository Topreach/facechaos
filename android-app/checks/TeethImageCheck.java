package com.facechaos.mobile;
/** Controlled pixel negatives/positives; not a claim of demographic recognition accuracy. */
public final class TeethImageCheck {
 static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
 static float evidence(float slope,boolean both,int color){
  int width=100,height=90;int[] pixels=new int[width*height];java.util.Arrays.fill(pixels,0xFF301010);
  float[] xs={20,80,80,20},ys={25-30*slope,25+30*slope,55+30*slope,55-30*slope};
  for(int y=0;y<height;y++)for(int x=0;x<width;x++){float relative=y+.5f-(40+slope*(x+.5f-50));if(x>=20&&x<80&&(relative>=-12&&relative<-4||both&&relative>=4&&relative<12))pixels[y*width+x]=color;}
  return TeethEvidence.measure(pixels,width,height,xs,ys);
 }
 public static void main(String[] args){
  for(float slope:new float[]{-.5f,-.25f,0,.25f,.5f}){
   require(evidence(slope,false,0xFFF2EEE0)>.6f,"A visible upper row must count at head tilt: "+slope);
   require(evidence(slope,true,0xFFF2EEE0)>.8f,"Visible ivory upper/lower rows must survive head tilt: "+slope);
   require(evidence(slope,true,0xFF858278)>.8f,"Dim ivory teeth must not be rejected by a fixed 145 brightness cutoff: "+slope);
   require(evidence(slope,false,0xFF858278)>.6f,"Dim upper-row teeth must count: "+slope);
   require(evidence(slope,true,0xFFE9A4A2)==0,"Pink lips/tongue must not become teeth: "+slope);
   require(evidence(slope,true,0xFF301010)==0,"Dark open mouth must not become teeth: "+slope);
  }
  int[] small=new int[12*12];require(Float.isNaN(TeethEvidence.measure(small,12,12,new float[]{1,10,10,1},new float[]{1,1,10,10})),"Insufficient tooth detail must be reported as unavailable");
  int[] asymmetric=new int[100*90];java.util.Arrays.fill(asymmetric,0xFF301010);
  float[] ax={20,50,80,50},ay={35,25,35,55};
  for(int y=25;y<40;y++)for(int x=20;x<80;x++)asymmetric[y*100+x]=0xFFF2EEE0;
  require(TeethEvidence.measure(asymmetric,100,90,ax,ay)>.6f,"Visible upper teeth count with asymmetric jaw opening");
  java.util.Arrays.fill(asymmetric,0xFFF2EEE0);require(TeethEvidence.measure(asymmetric,100,90,ax,ay)>.8f,"An asymmetric open mouth can show both tooth rows");
  java.util.Arrays.fill(asymmetric,0xFF777777);require(TeethEvidence.measure(asymmetric,100,90,ax,ay)==0,"A uniformly grey interior must not activate exposure adaptation");
  java.util.Arrays.fill(asymmetric,0xFF301010);for(int x=30;x<70;x++)asymmetric[35*100+x]=0xFFF2EEE0;
  require(TeethEvidence.measure(asymmetric,100,90,ax,ay)==0,"One bright highlight line must not count as teeth");
  java.util.Arrays.fill(asymmetric,0xFF301010);for(int y=34;y<37;y++)for(int x=48;x<51;x++)asymmetric[y*100+x]=0xFFF2EEE0;
  require(TeethEvidence.measure(asymmetric,100,90,ax,ay)==0,"A tiny bright spot must not count as teeth");
  System.out.println("PASS: one/both tooth rows in bright/dim exposure and pink/dark negatives at five head tilts, plus insufficient image detail");
 }
}
