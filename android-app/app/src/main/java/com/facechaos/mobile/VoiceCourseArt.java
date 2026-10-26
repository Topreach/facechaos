package com.facechaos.mobile;
import android.graphics.*;

/** Original scenery and collision-aligned obstacles, confined below the selfie face. */
final class VoiceCourseArt {
 private final Paint p=new Paint(3);
 private final Path path=new Path();
 static final int[] SKY={0xFFD5F2D4,0xFF83C8AB,0xFFD8BA9F,0xFFC5EDFF,0xFF644977};
 static final int[] FLOOR={0xFF54966D,0xFF245F50,0xFF6A5156,0xFF6EADCE,0xFF432D50};
 private void box(Canvas c,float x,float y,float w,float h,int color,float radius){if(h<=0)return;p.setShader(null);p.setColor(color);c.drawRoundRect(x,y,x+w,y+h,radius,radius,p);}
 private void triangle(Canvas c,float x,float y,float w,float h,int color){p.setColor(color);path.reset();path.moveTo(x,y+h);path.lineTo(x+w/2,y);path.lineTo(x+w,y+h);path.close();c.drawPath(path,p);}
 void backdrop(Canvas c,MiniGame g,float top,float bottom){
  int world=g.stage-1;
  p.setShader(new LinearGradient(0,top,0,bottom,SKY[world],FLOOR[world],Shader.TileMode.CLAMP));c.drawRoundRect(10,top-14,380,bottom+45,18,18,p);p.setShader(null);
  c.save();c.clipRect(10,top-14,380,bottom+45);
  for(int i=-1;i<7;i++){
   float x=i*83-(g.elapsed*(8+world*2))%83;
   if(world==0){p.setColor(0xFFB5DCB3);c.drawOval(x-10,bottom-28,x+112,bottom+65,p);box(c,x+44,bottom-38,8,40,0xFF7E9160,3);p.setColor(0xFF71AE7A);c.drawCircle(x+48,bottom-43,20,p);}
   else if(world==1){box(c,x+25,top+20,13,bottom-top,0xFF497E66,5);p.setColor(0xFF5A9D77);c.drawOval(x-5,top+4,x+72,top+60,p);box(c,x+52,bottom-52,31,82,0xFF638B73,5);box(c,x+57,bottom-41,21,5,0xFF92AC86,2);}
   else if(world==2){triangle(c,x,top+60,125,bottom-top,0xFFA48178);box(c,x+33,bottom-67,35,85,0xFF806761,5);gear(c,x+50,bottom-70,16,0xFFB49B81,g.elapsed*.2f);}
   else if(world==3){triangle(c,x,top+20,120,bottom-top+20,0xFF91C5DF);triangle(c,x+22,top+20,76,72,0xFFEAFBFF);box(c,x,bottom-3,72,28,0xFFB5E7EE,8);}
   else{triangle(c,x,top+35,130,bottom-top+40,0xFF49364E);box(c,x,bottom-3,83,35,0xFFEE6D3C,6);box(c,x+8,bottom+5,42,4,0xFFFFC55F,2);p.setColor(0xFFFFB45B);c.drawCircle(x+26,top+25+(float)Math.sin(g.elapsed+i)*12,2,p);}
  }
  box(c,10,bottom+20,370,24,FLOOR[world],0);box(c,10,bottom+20,370,4,world==4?0xFFFFAC49:world==3?0xFFE8FAFF:0xFFB7D697,0);c.restore();
 }
 void obstacle(Canvas c,MiniGame g,float x,float top,float travel,float bottom){
  float gapTop=top+(g.corridor()-g.voiceTolerance())*travel-VoiceCourse.BODY_RADIUS,gapBottom=top+(g.corridor()+g.voiceTolerance())*travel+VoiceCourse.BODY_RADIUS;
  int kind=VoiceCourse.obstacle(g.stage,g.encounter);
  if(kind<2)return;
  c.save();c.clipRect(10,top-14,380,bottom+19);
  if(kind==2||kind==3){
   box(c,x-13,top-14,26,gapTop-top+14,0xFF31664D,9);box(c,x-17,gapBottom,34,bottom+20-gapBottom,0xFF526C45,7);
   for(float y=top;y<gapTop-5;y+=16){p.setColor(0xFF6EBF69);c.drawOval(x-27,y,x+2,y+10,p);c.drawOval(x-1,y+8,x+26,y+18,p);}
   for(int i=0;i<4;i++)triangle(c,x-25+i*13,gapBottom-5,14,22,0xFFCF866E);
  }else if(kind==4||kind==5){
   box(c,x-15,top-14,30,gapTop-top+14,0xFF735D5D,5);box(c,x-15,gapBottom,30,bottom+20-gapBottom,0xFF735D5D,5);
   box(c,x-24,gapTop-12,48,12,0xFFC99B61,4);box(c,x-24,gapBottom,48,12,0xFFC99B61,4);
   gear(c,x,gapTop-24,17,0xFFE6C38C,g.elapsed);gear(c,x,gapBottom+27,17,0xFFE6C38C,-g.elapsed);
   if(kind==5){p.setColor(0xFF6F7B89);p.setStrokeWidth(2);for(int i=0;i<3;i++)c.drawLine(x-50,gapTop+14+i*14,x-27,gapTop+14+i*14,p);}
  }else if(kind==6||kind==7){
   box(c,x-18,top-14,36,gapTop-top+14,0xFF79BFD8,5);box(c,x-18,gapBottom,36,bottom+20-gapBottom,0xFF79BFD8,5);
   for(int i=0;i<3;i++){triangle(c,x-24+i*17,gapBottom-7,18,30,0xFFCBF3FF);c.save();c.rotate(180,x-14+i*17,gapTop);triangle(c,x-23+i*17,gapTop,18,27,0xFFE3FAFF);c.restore();}
   box(c,x-10,top-10,4,Math.max(0,gapTop-top-20),0xFFD7F8FF,2);
  }else{
   box(c,x-16,top-14,32,gapTop-top+14,0xFF574150,5);box(c,x-16,gapBottom,32,bottom+20-gapBottom,0xFF574150,5);
   box(c,x-21,gapTop-7,42,7,0xFFFF9A42,3);box(c,x-21,gapBottom,42,7,0xFFFF9A42,3);
   for(int i=0;i<4;i++){float flame=10+4*(float)Math.sin(g.elapsed*7+i);triangle(c,x-25+i*13,gapBottom-5,13,flame,0xFFFFC85B);}
   if(kind==9){gear(c,x,gapTop-22,15,0xFFFF9A42,g.elapsed);gear(c,x,gapBottom+23,15,0xFFFF9A42,-g.elapsed);}
  }
  // Physical opening includes the avatar body radius around the model's safe centre band.
  p.setColor(0xFFFFE89C);p.setStrokeWidth(2);c.drawLine(x-24,gapTop,x+24,gapTop,p);c.drawLine(x-24,gapBottom,x+24,gapBottom,p);c.restore();
 }
 private void gear(Canvas c,float x,float y,float r,int color,float angle){c.save();c.rotate(angle*45,x,y);for(int i=0;i<8;i++){c.save();c.rotate(i*45,x,y);box(c,x-3,y-r-3,6,9,color,1);c.restore();}p.setColor(color);c.drawCircle(x,y,r*.8f,p);p.setColor(0xFF66545E);c.drawCircle(x,y,r*.28f,p);c.restore();}
}
