package com.facechaos.mobile;

/** Experimental image evidence inside inner lips. Never substitutes jaw/smile scores for teeth. */
final class TeethEvidence {
 static float measure(int[] pixels,int width,int height,float[] xs,float[] ys){
  if(xs.length<3||xs.length!=ys.length||pixels.length!=width*height)return Float.NaN;
  float minX=width,minY=height,maxX=0,maxY=0;
  for(int i=0;i<xs.length;i++){if(!Float.isFinite(xs[i])||!Float.isFinite(ys[i])||xs[i]<0||ys[i]<0||xs[i]>=width||ys[i]>=height)return Float.NaN;minX=Math.min(minX,xs[i]);minY=Math.min(minY,ys[i]);maxX=Math.max(maxX,xs[i]);maxY=Math.max(maxY,ys[i]);}
  if(maxX-minX<16)return Float.NaN;
  if(maxY-minY<2)return 0;
  // Measure coherent tooth bands along the mouth axis, including a tilted head.
  float leftY=0,rightY=0;int leftCount=0,rightCount=0;
  for(int i=0;i<xs.length;i++){if(xs[i]<=minX+.5f){leftY+=ys[i];leftCount++;}if(xs[i]>=maxX-.5f){rightY+=ys[i];rightCount++;}}
  leftY/=leftCount;rightY/=rightCount;
  float slope=(rightY-leftY)/(maxX-minX),minNormal=Float.POSITIVE_INFINITY,maxNormal=Float.NEGATIVE_INFINITY;
  for(int i=0;i<xs.length;i++){float normal=ys[i]-slope*xs[i];minNormal=Math.min(minNormal,normal);maxNormal=Math.max(maxNormal,normal);}
  // Jaw opening is asymmetric around the corners. Use the midpoint between the
  // upper/lower inner lips along that axis, rather than the corner height itself.
  if(maxNormal-minNormal<Math.max(2,(maxX-minX)*.04f))return 0;
  int[] toothBands=new int[(int)Math.ceil(maxNormal-minNormal)+2];
  int area=0,toothPixels=0,candidates=0;
  boolean[] mask=new boolean[pixels.length];int[] brightness=new int[256],neutralBrightness=new int[256];
  for(int y=(int)minY;y<=(int)maxY;y++)for(int x=(int)minX;x<=(int)maxX;x++){
   boolean inside=false;for(int i=0,j=xs.length-1;i<xs.length;j=i++)if((ys[i]>y+.5f)!=(ys[j]>y+.5f)&&(x+.5f<(xs[j]-xs[i])*(y+.5f-ys[i])/(ys[j]-ys[i])+xs[i]))inside=!inside;
   if(!inside)continue;area++;mask[y*width+x]=true;
   int rgb=pixels[y*width+x],level=maxColor(rgb);brightness[level]++;
   if(pale(rgb,80)){neutralBrightness[level]++;candidates++;}
  }
  if(area<16)return 0;
  int floor=145;
  if(candidates>=4){
   int light=percentile(neutralBrightness,candidates,.90f),dark=percentile(brightness,area,.10f);
   // Exposure may put visible ivory teeth below the old absolute 145 cutoff.
   // Require local contrast before adapting, so a flat grey mouth is not teeth.
   if(light<180&&light-dark>=35)floor=Math.max(90,Math.min(145,Math.round(light*.8f)));
  }
  for(int y=(int)minY;y<=(int)maxY;y++)for(int x=(int)minX;x<=(int)maxX;x++){
   if(!mask[y*width+x])continue;
   boolean pale=pale(pixels[y*width+x],floor);
   // Reject isolated bright specks; an adjacent mouth pixel must also look pale.
   if(pale&&x+1<width)pale=mask[y*width+x+1]&&pale(pixels[y*width+x+1],floor);
   if(pale){toothPixels++;int band=(int)Math.floor(y+.5f-slope*(x+.5f)-minNormal);if(band>=0&&band<toothBands.length)toothBands[band]++;}
  }
  // Showing either tooth row is sufficient. Require a broad coherent band on
  // at least two image rows so a lone highlight is not a tooth display.
  int widest=0,supported=0;for(int count:toothBands){widest=Math.max(widest,count);if(count>=Math.max(4,(maxX-minX)*.20f))supported++;}
  if(supported<2)return 0;
  float fraction=toothPixels/(float)area,coverage=widest/(maxX-minX);
  return Math.max(0,Math.min(1,Math.min((fraction-.015f)/.08f,(coverage-.10f)/.45f)));
 }
 private static int maxColor(int rgb){return Math.max(rgb>>16&255,Math.max(rgb>>8&255,rgb&255));}
 private static boolean pale(int rgb,int floor){int r=rgb>>16&255,g=rgb>>8&255,b=rgb&255,max=Math.max(r,Math.max(g,b)),min=Math.min(r,Math.min(g,b));return max>=floor&&(max-min)*255<=max*105&&r-g<=.65f*Math.max(0,g-b)+12;}
 private static int percentile(int[] histogram,int count,float fraction){int target=Math.max(1,(int)Math.ceil(count*fraction)),sum=0;for(int i=0;i<histogram.length;i++){sum+=histogram[i];if(sum>=target)return i;}return 255;}
}
