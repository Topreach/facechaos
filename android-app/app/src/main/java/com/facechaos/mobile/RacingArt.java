package com.facechaos.mobile;

import android.graphics.*;
/** Original vector sports cars and a miniature neon coastal road over the selfie. */
final class RacingArt {
 private final Paint p=new Paint(3);
 private final Path path=new Path();
 private int roadShade=0xFF142C42,railShade=0xFFFFCB9D;
 void tutorial(Canvas c,float w,float h,int mode){
  c.save();c.scale(w/390f,h/220f);c.drawColor(mode==12?0xFFE5F6F8:0xFFEBEEFF);
  p.setColor(0xFFD2E7ED);c.drawCircle(338,30,110,p);road(c,25,210,0,mode==12?2:3);
  if(mode==12){car(c,145,203,55,0xFF24BDD0,false,"YOU");car(c,254,130,41,0xFFF77978,false,"CPU");label(c,"SOFT",50,49,10,0xFF426E7B);label(c,"STRONG",333,49,10,0xFF426E7B);meter(c,28,59,72,.28f,0xFF62C7CA);meter(c,290,59,72,.86f,0xFFF5B35B);}
  else{car(c,93,203,51,0xFF26BFC7,false,"YOU");car(c,195,145,43,0xFFF69D64,true,"");car(c,278,115,35,0xFFE8799A,true,"");arrow(c,98,67,-1,0xFF31BBA8);label(c,"MOVE YOUR HEAD",195,25,12,0xFF486188);}
  c.restore();
 }
 private void road(Canvas c,float top,float bottom,float scroll,int lanes){
  p.setShader(new LinearGradient(0,top,0,bottom,0xFF31465D,roadShade,Shader.TileMode.CLAMP));path.reset();path.moveTo(149,top);path.lineTo(241,top);path.lineTo(378,bottom);path.lineTo(12,bottom);path.close();c.drawPath(path,p);p.setShader(null);
  for(int edge:new int[]{-1,1}){
   p.setColor(edge<0?0xFFA8EFE0:railShade);p.setStrokeWidth(4);c.drawLine(195+edge*46,top,195+edge*183,bottom,p);
   for(int i=0;i<9;i++){float t=(i/9f+scroll%1)%1,y=top+(bottom-top)*t*t,half=46+137*t*t;float x=195+edge*(half+10);p.setColor(i%2==0?0xFFFFDA8C:0xFFB9DCCC);c.drawCircle(x,y,1.5f+3*t,p);}
  }
  for(int lane=1;lane<lanes;lane++)for(int i=0;i<8;i++){
   float a=(i/8f+scroll%1)%1,b=Math.min(1,a+.065f),offset=-1+2*lane/(float)lanes;
   float ya=top+(bottom-top)*a*a,yb=top+(bottom-top)*b*b;
   p.setColor(0xBEE2EBE5);p.setStrokeWidth(1+2*a);c.drawLine(195+offset*(46+137*a*a),ya,195+offset*(46+137*b*b),yb,p);
  }
 }
 private void car(Canvas c,float x,float bottom,float width,int color,boolean incoming,String tag){
  float h=width*1.66f,cy=bottom-h/2;
  p.setColor(0x66071425);c.drawOval(x-width*.62f,bottom-h*.12f,x+width*.62f,bottom+width*.12f,p);c.save();if(incoming)c.rotate(180,x,cy);
  for(int side:new int[]{-1,1})for(float y:new float[]{bottom-h*.73f,bottom-h*.22f}){p.setColor(0xFF142134);c.drawRoundRect(x+side*width*.46f-width*.1f,y-width*.14f,x+side*width*.46f+width*.1f,y+width*.14f,3,3,p);}
  p.setShader(new LinearGradient(x-width/2,0,x+width/2,0,light(color),color,Shader.TileMode.CLAMP));path.reset();path.moveTo(x-width*.25f,bottom-h);path.quadTo(x-width*.46f,bottom-h,x-width*.46f,bottom-h*.78f);path.lineTo(x-width*.36f,bottom-h*.48f);path.quadTo(x-width*.51f,bottom-h*.24f,x-width*.46f,bottom-h*.10f);path.quadTo(x-width*.42f,bottom,x-width*.27f,bottom);path.lineTo(x+width*.27f,bottom);path.quadTo(x+width*.42f,bottom,x+width*.46f,bottom-h*.10f);path.quadTo(x+width*.51f,bottom-h*.24f,x+width*.36f,bottom-h*.48f);path.lineTo(x+width*.46f,bottom-h*.78f);path.quadTo(x+width*.46f,bottom-h,x+width*.25f,bottom-h);path.close();c.drawPath(path,p);p.setShader(null);
  p.setColor(0x66465F75);c.drawRoundRect(x-width*.32f,bottom-h*.70f,x+width*.32f,bottom-h*.25f,width*.14f,width*.14f,p);
  p.setShader(new LinearGradient(0,bottom-h*.73f,0,bottom-h*.46f,0xFFE6FCFF,0xFF39758E,Shader.TileMode.CLAMP));c.drawRoundRect(x-width*.3f,bottom-h*.74f,x+width*.3f,bottom-h*.47f,width*.11f,width*.11f,p);p.setShader(null);
  p.setColor(0xFF9FDDE7);c.drawRoundRect(x-width*.27f,bottom-h*.34f,x+width*.27f,bottom-h*.19f,4,4,p);
  p.setColor(0xCCF7FFFF);for(int side:new int[]{-1,1})c.drawRoundRect(x+side*width*.27f-width*.095f,bottom-h*.94f,x+side*width*.27f+width*.095f,bottom-h*.84f,3,3,p);
  p.setColor(0xFFFFB2B0);for(int side:new int[]{-1,1})c.drawRoundRect(x+side*width*.28f-width*.08f,bottom-h*.1f,x+side*width*.28f+width*.08f,bottom-h*.04f,2,2,p);
  p.setColor(0xFFE4FFF9);c.drawRoundRect(x-width*.09f,bottom-h*.99f,x+width*.09f,bottom-h*.77f,2,2,p);c.drawRoundRect(x-width*.11f,bottom-h*.12f,x+width*.11f,bottom-h*.02f,2,2,p);
  p.setColor(0xFF264153);c.drawRoundRect(x-width*.48f,bottom-h*.09f,x+width*.48f,bottom-h*.025f,3,3,p);
  c.restore();if(!tag.isEmpty()){p.setColor(0xD5284E60);c.drawRoundRect(x-width*.42f,bottom+3,x+width*.42f,bottom+17,6,6,p);label(c,tag,x,bottom+13,8,Color.WHITE);}
 }
 private int light(int c){return Color.rgb(Math.min(255,Color.red(c)+45),Math.min(255,Color.green(c)+35),Math.min(255,Color.blue(c)+30));}
 void scene(Canvas c,MiniGame g,float height,float safeTop,float controls){
  int tone=SectionArt.floor(g);roadShade=Color.rgb(Color.red(tone)/2,Color.green(tone)/2,Color.blue(tone)/2);railShade=SectionArt.light(g);
  RacingGame r=g.racing;float bottom=height-Math.max(118,controls)+4,top=Math.max(height*.58f,bottom-245);
  road(c,top,bottom,r.time*(g.mode==12?Math.max(.3f,r.playerSpeed/100):.85f),g.mode==12?2:3);
  if(g.mode==12){
   float gap=r.rivalDistance-r.playerDistance;float rivalBottom=Math.max(top+45,Math.min(bottom-12,bottom-50-gap*1.4f));
   float depth=(rivalBottom-top)/(bottom-top);car(c,195+61*depth,rivalBottom,30+26*depth,0xFFEF797C,false,"CPU");car(c,139,bottom-15,58,0xFF27C4CE,false,"YOU");
   if(Math.max(r.playerDistance,r.rivalDistance)>RacingGame.FINISH*.8f){float t=Math.min(1,(Math.max(r.playerDistance,r.rivalDistance)/RacingGame.FINISH-.8f)/.2f),y=top+10+t*(bottom-top-15);for(int i=0;i<14;i++)for(int row=0;row<2;row++){p.setColor((i+row)%2==0?Color.WHITE:0xFF20364A);c.drawRect(55+i*20,y+row*7,75+i*20,y+7+row*7,p);}}
   label(c,(int)r.playerSpeed+" km/h",195,bottom+35,16,Color.WHITE);
  }else{
   float t=r.trafficProgress(),y=top+25+(bottom-top-27)*t*t,width=21+40*t;
   if(r.recovery<=0)for(int lane=-1;lane<=1;lane++)if(r.blocked(lane))car(c,195+lane*(35+83*t*t),y,width,lane==0?0xFFF5A566:0xFFEA7CA6,true,"");
   car(c,195+r.steerPosition*118,bottom-12,57,0xFF2BC9C8,false,"YOU");
   if(g.flash>0&&!g.earnedCoin&&r.crashes>0){p.setColor(0x77FFAD95);c.drawCircle(195+r.steerPosition*118,bottom-60,43,p);}
   label(c,"Cleared "+r.dodged+"/"+r.trafficRequired()+" - wave "+Math.min(r.trafficGoal(),r.rows+1)+"/"+r.trafficGoal(),195,bottom+34,12,Color.WHITE);
  }
  if(!g.finished){
   float y=safeTop+82;p.setColor(0xF5EDF7FF);c.drawRoundRect(20,y,370,y+88,20,20,p);
   if(g.mode==12){
    boolean leading=r.playerDistance>=r.rivalDistance;
    car(c,62,y+65,27,0xFF28BDC8,false,"");
    String title=g.paused?"Paused":g.microphoneMode&&!g.micReady?"Stay quiet to set sound level":g.countdown>0?"Ready in "+(int)Math.ceil(g.countdown):leading?"You're ahead - keep your rhythm":"Catch the CPU - add sound power";
    if(g.earnedCoin&&g.flash>0&&g.countdown<=0&&!g.paused)title="Checkpoint! +1 coin";
    else if(!g.microphoneMode&&g.countdown<=0&&!g.paused)title=leading?"You're ahead - hold your pace":"Hold to accelerate past the CPU";
    label(c,title,223,y+24,13,0xFF315974);
    label(c,"YOU",99,y+41,8,0xFF50778A);label(c,"CPU",99,y+54,8,0xFF50778A);meter(c,116,y+35,211,r.playerDistance/RacingGame.FINISH,0xFF39BFB7);meter(c,116,y+48,211,r.rivalDistance/RacingGame.FINISH,0xFFEC8A8A);
    label(c,g.microphoneMode?"Sound power "+(int)(g.soundStrength*100)+"% - no shouting":"Hold: accelerate  /  release: coast",223,y+75,10,0xFF607E8C);
   }else{
    int clear=r.safeLane();arrow(c,70,y+43,clear,0xFF36ACAE);
    String title=g.paused?"Paused":g.countdown>0?"Ready in "+(int)Math.ceil(g.countdown):g.faceBlocked()?"Centre your head to calibrate":g.headGrace>0?"Choose a lane - traffic resumes":g.flash>0&&!g.earnedCoin&&r.crashes>0?"Traffic hit - keep going":g.earnedCoin&&g.flash>0?"Good dodge! +1 coin":clear<0?"Clear lane: LEFT":clear>0?"Clear lane: RIGHT":"Clear lane: MIDDLE";
    label(c,title,224,y+34,17,0xFF3F567E);label(c,g.faceMode?"Move your head toward the clear lane":"Tap the left, middle or right of screen",224,y+59,10,0xFF697F9B);
   }
  }
 }
 private void arrow(Canvas c,float x,float y,int direction,int color){
  p.setColor(color);p.setStrokeWidth(5);p.setStrokeCap(Paint.Cap.ROUND);float dx=direction==0?0:direction*19,dy=direction==0?-17:0;c.drawLine(x-dx*.5f,y-dy*.5f,x+dx,y+dy,p);c.drawLine(x+dx,y+dy,x+dx-(direction==0?9:direction*10),y+dy+(direction==0?10:-9),p);c.drawLine(x+dx,y+dy,x+dx+(direction==0?9:-direction*10),y+dy+9,p);p.setStrokeCap(Paint.Cap.BUTT);
 }
 private void meter(Canvas c,float x,float y,float width,float value,int color){p.setColor(0xFFD5E3EC);c.drawRoundRect(x,y,x+width,y+6,3,3,p);p.setColor(color);c.drawRoundRect(x,y,x+width*Math.max(0,Math.min(1,value)),y+6,3,3,p);}
 private void label(Canvas c,String text,float x,float y,float size,int color){p.setShader(null);p.setColor(color);p.setTextAlign(Paint.Align.CENTER);p.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));p.setTextSize(size);while(p.measureText(text)>260&&size>8)p.setTextSize(--size);c.drawText(text,x,y,p);p.setTextAlign(Paint.Align.LEFT);}
}
