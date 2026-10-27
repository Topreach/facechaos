package com.facechaos.mobile;
import android.content.Context;
import android.graphics.*;
import android.os.SystemClock;
import android.view.View;
/** Original vector artwork over the selfie; no opaque game background. */
final class GameOverlay extends View {
 private final Paint p=new Paint(3);
 private int top,bottomReserved;
 private float detectedFaceBottom=-1;
 private boolean faceCheckOpen;
 void setFaceBottom(float bottom){detectedFaceBottom=bottom;invalidate();}
 void setFaceCheckOpen(boolean open){faceCheckOpen=open;invalidate();}
 private long last;
 MiniGame game=new MiniGame(0);
 private boolean saved;
 private boolean persistenceEnabled=true;
 void setPersistenceEnabled(boolean enabled){persistenceEnabled=enabled;}
 private int persistedCoins;
 private EmojiGatesRenderer emojiRenderer;
 private final InstructionPanel instructions;
 private static final int[] ACCENTS={0xFFF7CF55,0xFFFFAE80,0xFFA9E7BB,0xFF91DDF8,0xFFD5B2FF,0xFFFFBDDE,0xFFFFBDAD,0xFFAEDBD8,0xFFD6C0FF,0xFFFFD6B1,0xFFC4D8FF,0xFFDFD1F5,0xFF8DDFF0,0xFFAABCF8,0xFF8CE0BA};
 GameOverlay(Context c){super(c);instructions=new InstructionPanel(c);setContentDescription("Face Chaos game overlay with optional facial, sound and touch controls");}
 void select(int mode){select(mode,1);}
 void select(int mode,int stage){select(mode,stage,0);}
 void select(int mode,int stage,int carriedCoins){saveCoins();game=new MiniGame(mode,(int)SystemClock.elapsedRealtime(),stage);android.content.SharedPreferences records=getContext().getSharedPreferences("games",0);game.coinBalance=records.getLong("escape_coin_balance",0);game.coinsBeforeStage=game.stage>1?Math.max(0,carriedCoins):0;game.bestRunCoins=RunRecords.best(records,mode);saved=false;persistedCoins=0;last=0;invalidate();}
 void setSafeTop(int n){top=n;invalidate();}
 void setBottomReserved(int n){bottomReserved=n;invalidate();}
 void setPaused(boolean n){game.paused=n;if(n){game.cancelTouch();game.voiceActive=false;game.jumpSampling=false;game.jumpArmed=false;if(game.mode==8&&!game.resolved){game.charge=0;game.wasCharging=false;game.launchQuietTime=0;}}last=0;invalidate();}
 void restart(){select(game.mode,1);}
 boolean animalGuideAt(float x,float y){if(game.mode!=9||getWidth()==0)return false;float scale=getWidth()/390f,ground=getHeight()/scale-Math.max(110,bottomReserved/scale)+4;return x/scale>=115&&x/scale<=275&&y/scale>=ground-190&&y/scale<=ground-156;}
 int memoryActionAt(float x,float y){
  if(game.mode!=10||game.faceMode||game.paused||game.finished||game.countdown>0||game.memoryShowing()||game.resolved||getWidth()==0)return -1;
  float scale=getWidth()/390f,ground=getHeight()/scale-Math.max(110,bottomReserved/scale)+4;
  OverlayLayout layout=new OverlayLayout(getHeight()/scale,bottomReserved/scale,game.mode);
  return ExtrasArt.memoryChoiceAt(layout.sourceX(x/scale),layout.sourceY(y/scale),ground);
 }
 float headTouchFraction(float x){float scale=getWidth()/390f;OverlayLayout layout=new OverlayLayout(getHeight()/scale,bottomReserved/scale,game.mode);return Math.max(0,Math.min(1,layout.sourceX(x/scale)/390f));}
 OverlayLayout gameLayout(){float scale=Math.max(1,getWidth())/390f;return new OverlayLayout(getHeight()/scale,bottomReserved/scale,game.mode);}
 void match(){input(0);}
 void input(int action){game.input(action);saveCoins();invalidate();}
 void saveCoins(){if(!persistenceEnabled)return;android.content.SharedPreferences records=getContext().getSharedPreferences("games",0);game.bestRunCoins=RunRecords.save(records,game.mode,game.runCoins());if(game.coins>persistedCoins){persistedCoins=game.coins;records.edit().putLong("escape_coin_balance",game.coinBalance).apply();}}
 private void text(Canvas c,String s,float x,float y,float size,int color){p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(color);p.setTextSize(size);p.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));c.drawText(s,x,y,p);}
 private void card(Canvas c,float x,float y,float w,float h,int color){p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(color);c.drawRoundRect(x,y,x+w,y+h,20,20,p);}
 private void star(Canvas c,float x,float y,float r,int color){Path path=new Path();for(int i=0;i<10;i++){double a=-Math.PI/2+i*Math.PI/5;float v=i%2==0?r:r*.46f;float px=x+(float)Math.cos(a)*v,py=y+(float)Math.sin(a)*v;if(i==0)path.moveTo(px,py);else path.lineTo(px,py);}path.close();p.setColor(color);c.drawPath(path,p);}
 private void hero(Canvas c,float x,float y,int accent){p.setAlpha(255);p.setShader(new LinearGradient(x-28,y-28,x+28,y+28,0xFFFFF6C1,accent,Shader.TileMode.CLAMP));c.drawCircle(x,y,28,p);p.setShader(null);p.setColor(0xFF173E42);c.drawCircle(x-9,y-5,3,p);c.drawCircle(x+9,y-5,3,p);if(game.mode==0&&game.target()==0){c.drawOval(x-7,y+1,x+7,y+17,p);}else if(game.mode==0&&game.target()==2){p.setColor(Color.WHITE);c.drawRoundRect(x-14,y+2,x+14,y+15,4,4,p);p.setColor(0xFF173E42);p.setStrokeWidth(1);for(int i=-8;i<=8;i+=8)c.drawLine(x+i,y+2,x+i,y+15,p);c.drawLine(x-13,y+8,x+13,y+8,p);}else{p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.5f);c.drawArc(x-10,y+1,x+10,y+14,0,180,false,p);p.setStyle(Paint.Style.FILL);}}
 @Override protected void onSizeChanged(int w,int h,int oldw,int oldh){super.onSizeChanged(w,h,oldw,oldh);last=0;}
 @Override protected void onDraw(Canvas c){
  super.onDraw(c);if(getWidth()==0)return;
  long now=SystemClock.elapsedRealtime();if(last!=0)game.step((now-last)/1000f);last=now;saveCoins();
  if(persistenceEnabled&&game.finished&&!game.stopped&&!saved){saved=true;String key="touch_best_"+game.mode;int best=getContext().getSharedPreferences("games",0).getInt(key,0);if(game.score>best)getContext().getSharedPreferences("games",0).edit().putInt(key,game.score).apply();if(game.won){android.content.SharedPreferences progress=getContext().getSharedPreferences("games",0);progress.edit().putInt("stage_unlocked_"+game.mode,Math.max(progress.getInt("stage_unlocked_"+game.mode,1),Math.min(MiniGame.STAGE_COUNT,game.stage+1))).putInt("stage_completed_"+game.mode,Math.max(progress.getInt("stage_completed_"+game.mode,0),game.stage)).apply();}}
  float scale=getWidth()/390f;c.save();c.scale(scale,scale);float h=getHeight()/scale,y=top/scale+14;int accent=ACCENTS[game.mode];
  if(game.mode>=0&&game.mode<GameCatalog.ALL.length){
   if(emojiRenderer==null)emojiRenderer=new EmojiGatesRenderer(getContext());
   OverlayLayout layout=new OverlayLayout(h,bottomReserved/scale,game.mode);
   emojiRenderer.draw(c,game,h,-1000,bottomReserved/scale);
   emojiRenderer.status(c,game,top/scale);if(!faceCheckOpen)instructions.draw(c,game,layout,detectedFaceBottom<0?-1:detectedFaceBottom*h);
   c.restore();if(isShown()&&!game.finished&&!game.paused)postInvalidateOnAnimation();return;
  }
  card(c,14,y,362,87,0xD4172C32);text(c,GameCatalog.ALL[game.mode].name,28,y+29,23,Color.WHITE);text(c,"Score "+game.score+"   |   "+game.hearts+" hearts",28,y+53,14,accent);text(c,"Touch preview",28,y+73,12,0xFFCBD9D3);
  card(c,205,y+55,153,5,0xFF405C60);card(c,205,y+55,153*Math.min(1,game.score/(float)game.goal()),5,accent);
  float ground=h-Math.max(180,bottomReserved/scale+12),z=ground-210;
  card(c,14,z,362,210,0x66172C32);
  for(int i=0;i<13;i++)star(c,26+(i*43)%336,z+14+(i*29)%176,2.5f,0x77FFFFFF);
  switch(game.mode){case 0:gates(c,z,accent);break;case 1:escape(c,z,accent);break;case 2:garden(c,z,accent);break;case 3:clouds(c,z,accent);break;case 4:music(c,z,accent);break;default:duet(c,z,accent);}
  String line=game.finished?game.feedback:game.paused?"Paused":game.countdown>0?"Ready in "+(int)Math.ceil(game.countdown):game.prompt();
  card(c,18,z-88,354,78,0xD4172C32);text(c,line,30,z-55,18,game.flash>0?accent:Color.WHITE);text(c,game.nextHint(),30,z-28,12,0xFFD3E6DF);
  if(!game.finished&&game.countdown<=0&&!game.paused)text(c,game.feedback,28,ground-8,13,Color.WHITE);
  if(game.finished){card(c,38,z+25,314,142,0xF0172C32);for(int i=0;i<(game.won?3:1);i++)star(c,150+i*43,z+65,17,accent);text(c,game.resultTitle(),68,z+110,23,Color.WHITE);text(c,game.resultAction()+" - tap the circle",64,z+140,14,accent);}
  c.restore();if(isShown()&&!game.finished&&!game.paused)postInvalidateOnAnimation();
 }
 private void gates(Canvas c,float z,int a){for(int i=0;i<3;i++){float x=48+i*105;card(c,x,z+35,84,119,0x9957867C);p.setColor(a);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);c.drawRoundRect(x,z+35,x+84,z+154,32,32,p);p.setStyle(Paint.Style.FILL);text(c,new String[]{"O",":)","TEETH"}[i],x+12,z+65,i==2?13:21,Color.WHITE);if(i==game.target())star(c,x+42,z+18,10,a);}hero(c,90+game.target()*105,z+126,a);}
 private void escape(Canvas c,float z,int a){card(c,25,z+155,340,5,0xFFC7E8DB);float x=330-game.phase/3.8f*250;hero(c,78,z+122-(game.resolved&&game.flash>0?35:0),a);if(game.target()==0){Path t=new Path();t.moveTo(x,z+155);t.lineTo(x+22,z+106);t.lineTo(x+44,z+155);t.close();p.setColor(a);c.drawPath(t,p);}else if(game.target()==1){for(int i=0;i<3;i++)star(c,x+i*18,z+85+i*15,9,a);}else{card(c,x,z+110,36,45,a);text(c,"B",x+10,z+140,20,0xFF173E42);}text(c,game.phase>=1&&game.phase<=3?"ACTION WINDOW":"Watch the obstacle",135,z+185,14,a);}
 private void garden(Canvas c,float z,int a){for(int i=0;i<6;i++){float x=45+i*60;p.setColor(0xFF8DCCAA);p.setStrokeWidth(3);c.drawLine(x,z+154,x,z+100,p);card(c,x-18,z+153,36,22,0xFFCFA47D);if(i<game.score/2){for(int j=0;j<6;j++){double angle=j*Math.PI/3;p.setColor(new int[]{0xFFFFB9CD,0xFFF7CF55,0xFFD5B2FF}[i%3]);c.drawCircle(x+(float)Math.cos(angle)*12,z+88+(float)Math.sin(angle)*12,10,p);}p.setColor(0xFFFFF5B7);c.drawCircle(x,z+88,8,p);}else{p.setColor(a);c.drawOval(x-10,z+125,x+10,z+139,p);}}text(c,"Smile to plant. Blink to blossom.",57,z+32,16,Color.WHITE);}
 private void clouds(Canvas c,float z,int a){float x=342-game.phase/4*230,cy=z+25+game.corridor()*145;card(c,x-16,cy-38,52,76,0x6681CDEB);star(c,x+10,cy,14,a);for(int i=0;i<4;i++){p.setColor(0x55FFFFFF);c.drawOval(35+i*85,z+160,85+i*85,z+176,p);}hero(c,95,z+25+game.height*145,a);text(c,"Float through the cloud stars",51,z+26,16,Color.WHITE);}
 private void music(Canvas c,float z,int a){p.setColor(0x55FFFFFF);p.setStrokeWidth(1);for(int i=0;i<5;i++)c.drawLine(28,z+60+i*18,360,z+60+i*18,p);float x=340-game.phase/3.8f*245;p.setColor(a);c.drawOval(x-10,z+99,x+10,z+113,p);p.setStrokeWidth(4);c.drawLine(x+8,z+104,x+8,z+66,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(4);p.setColor(game.phase>=1.2f&&game.phase<=2.8f?a:0x66FFFFFF);c.drawCircle(208,z+106,30,p);p.setStyle(Paint.Style.FILL);text(c,"Play when the note enters the ring",39,z+181,16,Color.WHITE);}
 private void duet(Canvas c,float z,int a){hero(c,100,z+91,0xFFF7CF55);star(c,285,z+91,30,a);p.setColor(0x77FFFFFF);p.setStrokeWidth(3);c.drawLine(143,z+91,239,z+91,p);p.setColor(a);c.drawCircle(game.target()==0?100:285,z+142,5,p);text(c,"FACE",80,z+177,14,Color.WHITE);text(c,"SOUND",259,z+177,14,Color.WHITE);}
}
