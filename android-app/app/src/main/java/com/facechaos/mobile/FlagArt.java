package com.facechaos.mobile;
import android.graphics.*;

/** Crisp vector flags, keeping national proportions; no answer text on an unanswered flag. */
final class FlagArt {
 private final Paint p=new Paint(3);
 private void rect(Canvas c,float x,float y,float w,float h,int color){p.setColor(color);c.drawRect(x,y,x+w,y+h,p);}
 private void text(Canvas c,String text,float x,float y,float size,int color){p.setColor(color);p.setTextSize(size);p.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));c.drawText(text,x,y,p);}
 private void panel(Canvas c,float x,float y,float w,float h,int color){p.setColor(color);c.drawRoundRect(x,y,x+w,y+h,18,18,p);}
 void scene(Canvas c,MiniGame g,float height,float safe,float reserved){
  panel(c,18,safe+82,354,53,0xED173E42);text(c,g.paused?"Paused":g.countdown>0?"Get ready to name the flag":"Which country is this?",32,safe+106,18,Color.WHITE);
  text(c,"Say its name in English  /  +1 coin",32,safe+125,12,0xFFFFDF7F);
  float bottom=height-Math.max(110,reserved)+2,top=Math.max(height*.57f,bottom-235);
  float enter=g.resolved?0:Math.max(0,1-g.phase/.55f)*40;
  panel(c,18,top,354,bottom-top,0xF5FFF9E9);
  text(c,"WORLD TOUR",34,top+25,12,0xFF47776E);text(c,"FLAG "+(g.encounter+1),280,top+25,12,0xFF47776E);
  float flagHeight=Math.min(100,Math.max(60,bottom-top-109));
  flag(c,g.flags.country(g.encounter).code,195+enter,top+36+flagHeight/2,172,flagHeight);
  String line=g.resolved?g.feedback:g.quizStatus;
  text(c,fit(line,320,14),34,bottom-52,14,g.earnedCoin?0xFF247E53:0xFF173E42);
  panel(c,30,bottom-37,330,29,0xFF173E42);text(c,g.quizListening?"Listening...  Tap for other options":"Tap here: say answer or type answer",43,bottom-17,12,Color.WHITE);
  rect(c,34,top+33,322,3,0xFFDDE6DA);rect(c,34,top+33,322*Math.max(0,1-g.phase/g.flagLimit()),3,0xFFDEAF3D);
 }
 private String fit(String s,float max,float size){p.setTextSize(size);while(s.length()>1&&p.measureText(s)>max)s=s.substring(0,s.length()-2)+"…";return s;}
 void tutorial(Canvas c,float w,float h){c.drawColor(0xFFE6F2E7);panel(c,w*.06f,h*.1f,w*.88f,h*.8f,0xFFFFFAE9);flag(c,"JP",w*.3f,h*.44f,w*.32f,h*.5f);text(c,"See the flag",w*.55f,h*.35f,h*.09f,0xFF173E42);text(c,"Say: Japan",w*.55f,h*.55f,h*.09f,0xFF173E42);text(c,"Good job! +1 coin",w*.55f,h*.73f,h*.075f,0xFF247E53);}
 void flag(Canvas c,String code,float cx,float cy,float maxW,float maxH){
  float ratio=switch(code){case "NG","IE","HU"->2;case "DE","BD","BG"->5f/3;case "PL","SE","PW"->1.6f;case "BE"->13f/15;case "CH"->1;case "FI"->18f/11;case "DK"->37f/28;case "EE"->11f/7;case "NO"->22f/16;case "IS"->25f/18;default->1.5f;};
  float w=Math.min(maxW,maxH*ratio),h=w/ratio;c.save();c.translate(cx-w/2,cy-h/2);c.scale(w/300,h/200);
  int white=Color.WHITE,red=0xFFCE1126,blue=0xFF003399,green=0xFF009A44;
  rect(c,-2,-2,304,204,0xFFB2BFAF);rect(c,0,0,300,200,white);
  switch(code){
   case "FR":vertical(c,0xFF002395,white,0xFFED2939);break;
   case "IT":vertical(c,0xFF009246,white,0xFFCE2B37);break;
   case "NG":vertical(c,0xFF008751,white,0xFF008751);break;
   case "IE":vertical(c,0xFF169B62,white,0xFFFF883E);break;
   case "BE":vertical(c,0xFF000000,0xFFFDDA24,0xFFEF3340);break;
   case "RO":vertical(c,0xFF002B7F,0xFFFCD116,0xFFCE1126);break;
   case "DE":horizontal(c,Color.BLACK,0xFFDD0000,0xFFFFCE00);break;
   case "NL":horizontal(c,0xFFAE1C28,white,0xFF21468B);break;
   case "AT":horizontal(c,0xFFED2939,white,0xFFED2939);break;
   case "HU":horizontal(c,0xFFCE2939,white,0xFF477050);break;
   case "BG":horizontal(c,white,0xFF00966E,0xFFD62612);break;
   case "EE":horizontal(c,0xFF4891D9,Color.BLACK,white);break;
   case "UA":rect(c,0,0,300,100,0xFF0057B7);rect(c,0,100,300,100,0xFFFFD700);break;
   case "PL":rect(c,0,100,300,100,0xFFDC143C);break;
   case "ID":rect(c,0,0,300,100,0xFFFF0000);break;
   case "JP":p.setColor(0xFFBC002D);c.drawCircle(150,100,60,p);break;
   case "BD":rect(c,0,0,300,200,0xFF006A4E);p.setColor(0xFFF42A41);c.drawCircle(135,100,60,p);break;
   case "PW":rect(c,0,0,300,200,0xFF4AADD6);p.setColor(0xFFFFDE00);c.drawCircle(131.25f,100,60,p);break;
   case "CH":rect(c,0,0,300,200,0xFFFF0000);rect(c,56.25f,81.25f,187.5f,37.5f,white);rect(c,121.875f,37.5f,56.25f,125,white);break;
   case "SE":nordic(c,0xFF006AA7,0xFFFECC00,5,2,9,4,2,4);break;
   case "FI":nordic(c,white,0xFF003580,5,3,10,4,3,4);break;
   case "DK":nordic(c,0xFFC60C30,white,12,4,21,12,4,12);break;
   case "NO":nordic(c,0xFFBA0C2F,white,6,4,12,6,4,6);cross(c,0xFF00205B,7f/22*300,2f/22*300,7f/16*200,2f/16*200);break;
   case "IS":nordic(c,0xFF02529C,white,7,4,14,7,4,7);cross(c,0xFFDC1E35,8f/25*300,2f/25*300,8f/18*200,2f/18*200);break;
  }c.restore();
 }
 private void vertical(Canvas c,int a,int b,int d){rect(c,0,0,100,200,a);rect(c,100,0,100,200,b);rect(c,200,0,100,200,d);}
 private void horizontal(Canvas c,int a,int b,int d){rect(c,0,0,300,200f/3,a);rect(c,0,200f/3,300,200f/3,b);rect(c,0,400f/3,300,200f/3,d);}
 private void cross(Canvas c,int color,float x,float width,float y,float height){rect(c,x,0,width,200,color);rect(c,0,y,300,height,color);}
 private void nordic(Canvas c,int bg,int fg,float left,float bar,float right,float top,float middle,float bottom){rect(c,0,0,300,200,bg);cross(c,fg,left/(left+bar+right)*300,bar/(left+bar+right)*300,top/(top+middle+bottom)*200,middle/(top+middle+bottom)*200);}
}
