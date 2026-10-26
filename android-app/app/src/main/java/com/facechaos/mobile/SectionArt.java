package com.facechaos.mobile;
import android.graphics.*;

/** Original miniature landscapes behind game objects; the centre of the selfie stays clear. */
final class SectionArt {
 private final Paint p=new Paint(3);private final Path path=new Path();
 private static final int[] SKY={0xFFD3ECCD,0xFF95C7A0,0xFFFFD39A,0xFFCCEFFF,0xFFB6A29A,0xFF765875,0xFF454974,0xFF775591,0xFFA9E6E8,0xFF8794B5,0xFFFFD8C0};
 private static final int[] FLOOR={0xFF619B73,0xFF336E58,0xFFC38E59,0xFF76AFCD,0xFF70616C,0xFF653B48,0xFF292F57,0xFF493B69,0xFF478E9B,0xFF465F78,0xFFAF7662};
 static int sky(MiniGame g){return SKY[GameSections.at(g.mode,g.stage).biome];}
 static int floor(MiniGame g){return FLOOR[GameSections.at(g.mode,g.stage).biome];}
 static int light(MiniGame g){int c=sky(g);return Color.rgb((Color.red(c)+255)/2,(Color.green(c)+255)/2,(Color.blue(c)+255)/2);}
 private void box(Canvas c,float x,float y,float w,float h,int color,float r){p.setShader(null);p.setColor(color);c.drawRoundRect(x,y,x+w,y+h,r,r,p);}
 private void tri(Canvas c,float x,float y,float w,float h,int color){p.setColor(color);path.reset();path.moveTo(x,y+h);path.lineTo(x+w/2,y);path.lineTo(x+w,y+h);path.close();c.drawPath(path,p);}
 void scene(Canvas c,MiniGame g,float height,float controls){
  if(!GameSections.applies(g.mode))return;
  float bottom=height-Math.max(110,controls)+44,top=Math.max(height*.56f,bottom-246);
  if(g.mode==12||g.mode==13)top=Math.max(height*.58f,height-Math.max(118,controls)+4-245);
  background(c,g,top,bottom);
 }
 void title(Canvas c,MiniGame g,float y){
  box(c,55,y,280,22,0xE02C444B,11);p.setColor(0xFFFFE1A0);p.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));p.setTextAlign(Paint.Align.CENTER);p.setTextSize(12);c.drawText(GameSections.at(g.mode,g.stage).name,195,y+15,p);p.setTextAlign(Paint.Align.LEFT);
 }
 void background(Canvas c,MiniGame g,float top,float bottom){
  int biome=GameSections.at(g.mode,g.stage).biome;float h=bottom-top;
  p.setShader(new LinearGradient(0,top,0,bottom,sky(g),floor(g),Shader.TileMode.CLAMP));c.drawRoundRect(10,top,380,bottom,18,18,p);p.setShader(null);
  c.save();c.clipRect(10,top,380,bottom);
  float scroll=g.elapsed*(g.mode==12||g.mode==13?13:4);
  for(int i=-1;i<7;i++){
   float x=i*81-scroll%81,y=top+h*.45f;
   switch(biome){
    case GameSections.MEADOW:
     p.setColor(0xFFB2D7A9);c.drawOval(x-15,top+h*.5f,x+125,bottom+50,p);box(c,x+37,y+17,7,h*.55f,0xFF8B8B58,3);p.setColor(0xFF81B981);c.drawCircle(x+40,y+5,23,p);break;
    case GameSections.JUNGLE:
     box(c,x+28,top+12,12,h,0xFF497759,5);p.setColor(0xFF5A9769);c.drawOval(x-5,top-15,x+79,top+48,p);box(c,x+43,bottom-66,27,60,0xFF7B9472,4);box(c,x+47,bottom-55,19,4,0xFFC2C99D,2);break;
    case GameSections.DESERT:
     p.setColor(0xFFE7B271);c.drawOval(x-35,top+h*.4f,x+140,bottom+70,p);box(c,x+31,bottom-67,8,62,0xFF7C986E,4);box(c,x+17,bottom-52,22,7,0xFF7C986E,3);box(c,x+16,bottom-70,7,24,0xFF7C986E,3);break;
    case GameSections.FROST:
     tri(c,x-12,top+20,130,h,0xFF99C8DB);tri(c,x+13,top+20,80,h*.4f,0xFFECFBFF);box(c,x-4,bottom-22,76,29,0xFFBAE7EF,8);break;
    case GameSections.CLOCKWORK:
     box(c,x+8,top+42,54,h,0xFF8B7D7D,5);box(c,x+17,top+55,9,29,0xFFB0C1BD,3);gear(c,x+38,bottom-38,19,g.elapsed);break;
    case GameSections.EMBER:
     tri(c,x-15,top+30,132,h,0xFF4E3B53);tri(c,x+38,top+30,26,24,0xFFFFC172);box(c,x-4,bottom-23,84,26,0xFFE67848,5);box(c,x+6,bottom-14,39,4,0xFFFFCE78,2);star(c,x+26,top+35+(float)Math.sin(g.elapsed+i)*14,2,0xFFFFCD77);break;
    case GameSections.SPACE:
     for(int n=0;n<5;n++)star(c,x+18+n*13,top+16+(i*29+n*37+190)%Math.max(30,(int)(h-34)),n==0?3:1.5f,0xFFD5D9FF);
     if(i==3){p.setColor(0xFF9187B9);c.drawCircle(x+27,top+h*.35f,26,p);p.setColor(0xFFA9A2CE);c.drawOval(x-12,top+h*.35f-5,x+67,top+h*.35f+5,p);}break;
    case GameSections.NEON:case GameSections.CITY:
     float roof=top+22+(i*31+100)%65;box(c,x+5,roof,53,bottom-roof,biome==7?0xFF534674:0xFF566D87,4);for(int row=0;row<5;row++)for(int col=0;col<3;col++)box(c,x+13+col*12,roof+12+row*20,5,9,(row+col+i)%2==0?0xFFFFD784:0xFF9BDDD9,1);break;
    case GameSections.COAST:
     p.setColor(0xFF7CC3D0);c.drawOval(x-10,top+h*.55f,x+132,bottom+50,p);box(c,x+34,bottom-73,7,67,0xFF9F8F66,3);p.setColor(0xFF548F79);c.drawOval(x+6,bottom-87,x+69,bottom-66,p);c.drawOval(x+20,bottom-103,x+48,bottom-55,p);break;
    case GameSections.CANDY:
     box(c,x+18,bottom-84,49,74,0xFFCB9390,8);tri(c,x+9,bottom-111,66,35,0xFFFFE5B6);box(c,x+35,bottom-48,16,38,0xFFAC7585,6);for(int n=0;n<3;n++)star(c,x+26+n*15,bottom-71,3,0xFFFFE8BA);break;
   }
  }
  // Each kind of game gets its own functional-looking setting, in addition to the biome.
  if(g.mode==2){for(int i=0;i<8;i++){float x=28+i*47;p.setColor(0xFF799C73);p.setStrokeWidth(2);c.drawLine(x,bottom-7,x,bottom-30,p);for(int petal=0;petal<5;petal++){double a=petal*Math.PI*2/5;p.setColor(new int[]{0xFFFFD1DC,0xFFFFE396,0xFFD8C8F6,0xFFECFAFF,0xFFE4CEFF}[g.stage-1]);c.drawCircle(x+(float)Math.cos(a)*5,bottom-33+(float)Math.sin(a)*5,4,p);}}}
  if(g.mode==4){p.setColor(0x44FFFFFF);p.setStrokeWidth(1);for(int i=0;i<5;i++)c.drawLine(20,bottom-40-i*9,370,bottom-40-i*9,p);}
  if(g.mode==6){for(int i=0;i<9;i++)box(c,15+i*40,top,40,15,i%2==0?light(g):floor(g),2);}
  if(g.mode==7){for(int i=0;i<5;i++)box(c,22+i*74,bottom-18,54,18+i%3*8,light(g),4);}
  if(g.mode==11){for(int i=0;i<11;i++){float x=25+i*33,y=top+25+(float)Math.sin(i*1.7+g.elapsed*.6)*18;star(c,x,y,2+(i%3),0xD9FFF5A8);}}
  box(c,10,bottom-6,370,9,floor(g),0);c.restore();
 }
 void hurdle(Canvas c,MiniGame g,float x,float ground,float width){
  int biome=GameSections.at(g.mode,g.stage).biome;float height=g.jumpRequired();
  switch(biome){
   case GameSections.JUNGLE:for(int i=0;i<4;i++)tri(c,x-width/2+i*width/4,ground-height,width/4+3,height,0xFF8A6954);break;
   case GameSections.FROST:for(int i=0;i<3;i++)tri(c,x-width/2+i*width/3,ground-height,width/3+4,height,0xFFBCEEF7);break;
   case GameSections.CLOCKWORK:box(c,x-width*.4f,ground-height,width*.8f,height,0xFF907466,5);gear(c,x,ground-height/2,Math.min(22,height*.4f),g.elapsed);break;
   case GameSections.EMBER:box(c,x-width*.4f,ground-height+6,width*.8f,height-6,0xFF79505A,5);for(int i=0;i<4;i++)tri(c,x-width/2+i*width/4,ground-height-5,width/4,height*.5f,0xFFFFB45C);break;
   default:for(int i=0;i<3;i++)tri(c,x-width/2+i*width/3,ground-height,width/3+3,height,0xFFE7B171);break;
  }
 }
 void projectile(Canvas c,MiniGame g,float x,float y){int biome=GameSections.at(g.mode,g.stage).biome;if(biome==GameSections.CLOCKWORK)gear(c,x,y,22,g.elapsed*2);else star(c,x,y,25,biome==GameSections.FROST?0xFFC6F1FF:biome==GameSections.EMBER?0xFFFFB25B:0xFFFFE6A6);}
 void planet(Canvas c,MiniGame g,float x,float y,float radius){p.setShader(new RadialGradient(x-radius*.3f,y-radius*.3f,radius*1.6f,light(g),floor(g),Shader.TileMode.CLAMP));c.drawCircle(x,y,radius,p);p.setShader(null);p.setColor(0x4470718B);c.drawCircle(x-radius*.22f,y-radius*.23f,radius*.23f,p);c.drawCircle(x+radius*.3f,y+radius*.35f,radius*.16f,p);if(g.stage==3){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(5);p.setColor(0xFFE7C48C);c.drawOval(x-radius*1.35f,y-radius*.2f,x+radius*1.35f,y+radius*.2f,p);p.setStyle(Paint.Style.FILL);}}
 void star(Canvas c,float x,float y,float r,int color){p.setColor(color);path.reset();for(int i=0;i<10;i++){double a=-Math.PI/2+i*Math.PI/5;float v=i%2==0?r:r*.45f;if(i==0)path.moveTo(x+(float)Math.cos(a)*v,y+(float)Math.sin(a)*v);else path.lineTo(x+(float)Math.cos(a)*v,y+(float)Math.sin(a)*v);}path.close();c.drawPath(path,p);}
 private void gear(Canvas c,float x,float y,float r,float time){c.save();c.rotate(time*35,x,y);for(int i=0;i<8;i++){c.save();c.rotate(i*45,x,y);box(c,x-3,y-r-2,6,8,0xFFD6B784,1);c.restore();}p.setColor(0xFFD6B784);c.drawCircle(x,y,r*.8f,p);p.setColor(0xFF77616A);c.drawCircle(x,y,r*.25f,p);c.restore();}
}
