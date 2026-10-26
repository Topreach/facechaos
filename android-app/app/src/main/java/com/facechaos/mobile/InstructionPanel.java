package com.facechaos.mobile;
import android.graphics.*;

/** All player-facing reaction cues sit immediately above the game, never over the selfie. */
final class InstructionPanel {
 private final android.content.Context context;
 InstructionPanel(android.content.Context context){this.context=context;}
 private final Paint p=new Paint(3);
 void draw(Canvas c,MiniGame g,OverlayLayout layout,float faceBottom){
  boolean calibrating=g.faceMode&&!g.faceReady&&!g.finished&&!g.paused;
  float y=layout.captionTop(faceBottom,calibrating);
  if(calibrating){if(y+28<=layout.captionBottom){p.setColor(0xDC204148);c.drawRoundRect(14,y,376,y+28,10,10,p);text(c,AppText.english(context)?g.mode==13?"Centre your head - calibrating":g.faceStatus:context.getString(g.mode==13?R.string.fc_centre_head:R.string.fc_find_face),195,y+19,12,Color.WHITE);}return;}
  if(y+layout.instructionHeight>layout.captionBottom)return;boolean now=!g.paused&&!g.finished&&g.countdown<=0&&!g.resolved&&(g.soundJump()||g.mode==1)&&g.phase>=g.windowOpens()&&g.phase<=g.windowCloses();p.setColor(now?0xF04D5947:0xF0204148);c.drawRoundRect(14,y,376,y+layout.instructionHeight,12,12,p);
  if(now){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);p.setColor(0xFFFFD36B);c.drawRoundRect(14,y,376,y+layout.instructionHeight,12,12,p);p.setStyle(Paint.Style.FILL);}
  String section=GameSections.applies(g.mode)?GameSections.at(g.mode,g.stage).name:g.mode==3?VoiceCourse.world(g.stage):g.mode==9?"Animal Choir - "+AnimalPatterns.get(g.target()).name:GameCatalog.ALL[g.mode].name;
  text(c,section,195,y+15,12,0xFFFFDE8A);
  text(c,AppText.english(context)?action(g):GameLanguage.action(context,g),195,y+35,16,Color.WHITE);
  String hint=AppText.english(context)?hint(g):GameLanguage.hint(context,g);
  text(c,hint,195,y+51,10,0xFFCCE5DD);
  if(g.microphoneMode&&MiniGame.supportsMicrophone(g.mode)&&!g.finished){p.setColor(0xFF547071);c.drawRoundRect(92,y+56,298,y+59,2,2,p);if(g.mode==11){p.setColor(0xFFE8BE59);c.drawRect(92+206*g.glowLow(),y+56,92+206*g.glowHigh(),y+59,p);}
   p.setColor(0xFF80DFBB);if(g.mode==11){p.setStrokeWidth(2);float x=92+206*Math.min(1,g.micLevel);c.drawLine(x,y+55,x,y+61,p);}else c.drawRoundRect(92,y+56,92+206*Math.min(1,g.mode==12?g.raceThrottle():g.micLevel),y+59,2,2,p);}
 }
 private String hint(MiniGame g){
  if(g.mode==9&&g.animalDemoPlaying)return "Example only - no coins until your turn";
  if(g.finished)return "Run: "+g.runCoins()+" coins | Best: "+g.bestRunCoins+" | "+(g.nextRunStage()==1?"Restart at Stage 1":"Next: "+g.nextPaceLabel());if(g.paused)return "Press the circle to resume";if(g.countdown>0)return "Stage "+g.stage+" - "+g.paceLabel()+" speed";if(g.faceBlocked())return g.faceStatus;
  if(g.mode==4&&!g.resolved)return MelodyCourse.difficulty(g.stage)+" | "+MelodyCourse.obstacle(g.stage,g.encounter)+" | Power target ~"+Math.round(MelodyCourse.targetStrength(g.stage,g.encounter)*100)+"%";
  if(g.soundJump())return g.microphoneMode&&!g.micReady?"Stay quiet briefly - the course is paused":g.resolved?g.feedback:g.phase<g.windowOpens()?"Wait for the gold signal":g.phase<=g.windowCloses()?"Act now, then relax":"Next obstacle coming";
  switch(g.mode){
   case 0:return "Gate "+Math.min(g.goal(),g.score+1)+" / "+g.goal()+" - return to neutral";
   case 1:return g.faceMode?"Match the face, then relax":"Tap during the action window";
   case 2:return "Flower "+Math.min(6,g.score/2+1)+" / 6 - plant, then bloom";
   case 3:return g.microphoneMode?"VOICE: hum up / quiet down":"TOUCH: hold up / release down";
   case 5:return "Duet "+Math.min(3,g.score/2+1)+" / 3 - smile, then sound";
   case 6:return "Item "+Math.min(9,g.encounter+1)+" / 9 - berries yes, peppers no";
   case 7:return "Block "+Math.min(8,g.encounter+1)+" / 8 - "+(g.resolved?"Relax before the next block":"Smile once, then relax");
   case 8:return g.resolved?(g.cometLanded?g.earnedCoin?"Landed! +1 coin":g.feedback:"In flight - landing decides the reward"):"SOUND ONLY | Power "+Math.round(g.charge*100)+"% | Gold "+Math.round((g.powerTarget()-g.powerTolerance())*100)+"-"+Math.round((g.powerTarget()+g.powerTolerance())*100)+"%";
   case 9:return g.animalDemoPlaying?"Listening paused - copy it after the example":(g.microphoneMode?"MIC ON":"TOUCH PRACTICE")+" | "+g.soundPulses+" / "+g.animalPulsesRequired()+" calls | "+(g.soundArmed?"Pause between calls":"Briefly quiet, then copy");
   case 10:return g.memoryShowing()?"Watch the full sequence":g.faceMode?"Make each face, then relax":"Tap the illustrated face cards";
   case 11:return "Flight "+Math.round(g.glowFlightProgress()*100)+"% | Hum in gold | Sound only";
   case 12:return g.microphoneMode?"MIC ON | Power "+Math.round(g.raceThrottle()*100)+"% | "+Math.round(g.racing.playerSpeed)+" km/h":"TOUCH MODE | Hold to accelerate / release to coast";
   case 13:return g.faceMode?"Move your head toward the clear lane":"Tap a lane to steer";
   case 14:return g.quizStatus;
   default:return g.nextHint();
  }
 }
 private String action(MiniGame g){
  if(g.finished)return g.resultTitle();if(g.mode==9&&g.animalDemoPlaying)return "Listen: "+g.animalRhythm();if(g.paused)return "Paused";
  if(g.faceBlocked())return g.mode==13?"Centre your head to calibrate":"Move your face into view";
  if(g.microphoneMode&&!g.micReady&&MiniGame.supportsMicrophone(g.mode))return "Calibrating - stay quiet";
  if(g.countdown>0)return "Ready in "+(int)Math.ceil(g.countdown);
  if(g.earnedCoin&&g.flash>0)return "Good job! +1 coin";
  if(g.resolved&&(g.mode==1||g.mode==3||g.mode==4||g.mode==6||g.mode==7||g.mode==8||g.mode==9||g.mode==11))return g.feedback;
  if(g.mode==14)return g.quizListening?"Listening - say the country":"Name the country / tap to answer";
  if(g.mode==13){int lane=g.racing.safeLane();return "Clear lane: "+(lane<0?"LEFT":lane>0?"RIGHT":"MIDDLE");}
  if(g.mode==12)return g.microphoneMode?"Use your voice to beat the CPU":"Hold to beat the CPU";
  if(g.mode==6)return !g.snackIsTreat()?"Pepper: keep your mouth closed":g.phase<g.snackWindowOpens()?"Wait for the berry":g.faceMode?"Open your mouth now":"Tap for the berry";
  if(g.mode==4)return g.phase<g.windowOpens()?"Wait for the gold signal":g.phase<=g.windowCloses()?(Float.isFinite(MelodyCourse.ceilingMargin(g.stage,g.encounter))?"Controlled hop - low ceiling":g.highJump()?"Sound now - higher hop":"Sound now - low hop"):"Go quiet - next note coming";
  if(g.mode==2&&!g.faceMode)return g.target()==0?"Tap to plant a seed":"Tap to bloom";
  if(g.mode==9)return !g.microphoneMode?"Copy the short / long beat markers":g.animalRhythm();
  if(g.mode==7)return g.stackAligned()?(g.faceMode?"Smile now - block aligned":"Tap now - block aligned"):"Wait for the gold centre line";
  if(g.mode==8)return g.cometInGold()?"Go quiet now to launch":g.charge>g.powerTarget()+g.powerTolerance()?"Overcharged - go quiet to launch":"Hum gently to charge";
  if(g.mode==11)return g.micLevel>g.glowHigh()?"Softer hum to guide the firefly":!g.voiceActive?"Hum gently to fly":"Keep humming toward the lantern";
  if(g.mode==5&&!g.faceMode&&g.target()==0)return "Tap to create a smile spark";
  if(g.mode==5&&!g.microphoneMode&&g.target()==1)return "Tap to light the star";
  return g.prompt();
 }
 private void text(Canvas c,String text,float x,float y,float size,int color){text=AppText.t(context,text);p.setColor(color);p.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));p.setTextAlign(Paint.Align.CENTER);p.setTextSize(size);while(p.measureText(text)>338&&size>9)p.setTextSize(--size);if(p.measureText(text)>338){while(text.length()>1&&p.measureText(text+"...")>338)text=text.substring(0,text.length()-1);text+="...";}p.setTextAlign(Paint.Align.LEFT);LocalizedCanvas.draw(context,c,p,text,x,y,true,338);p.setTextAlign(Paint.Align.LEFT);}
}
