package com.facechaos.mobile;
public final class StageSpeedCheck {
 static void require(boolean b,String m){if(!b)throw new AssertionError(m);}
 public static void main(String[] args){
  for(int mode=0;mode<GameCatalog.ALL.length;mode++){
   float previous=0;
   for(int stage=1;stage<=5;stage++){
    MiniGame g=new MiniGame(mode,0,stage);g.countdown=0;if(MiniGame.requiresMicrophone(mode))g.acceptSoundLevel(0,0,false,true);for(int i=0;i<40;i++)g.step(.01f);
    require(g.phase>previous,"Real movement/cue clock increases at every stage, mode "+mode);previous=g.phase;
    require(Math.abs(g.phase-.4f*g.pace())<.001f,"World follows published speed multiplier");
    require(StageRules.describe(g).contains(g.paceLabel()),"Instructions show actual speed");
   }
  }
  // Measure real encounter deadlines, not just the shared simulation clock.
  for(int mode:new int[]{0,3,4,13,14}){
   float previous=Float.MAX_VALUE;
   for(int stage=1;stage<=5;stage++){
    MiniGame game=new MiniGame(mode,0,stage);game.countdown=0;float seconds=0;
    for(int tick=0;tick<10000;tick++){
     if(mode==13)game.headLane=game.racing.safeLane();
     game.step(.005f);seconds+=.005f;
     if(mode==0&&game.gateState!=0||mode==3&&game.resolved||mode==4&&game.resolved||mode==13&&game.racing.rows>0||mode==14&&game.hearts<(stage==5?1:3))break;
    }
    require(seconds<previous,"Actual gate/obstacle/traffic/flag deadline must arrive earlier at every stage, mode "+mode+" stage "+stage);previous=seconds;
   }
  }
  for(int mode=0;mode<GameCatalog.ALL.length;mode++){require(new MiniGame(mode,0,1).hearts==3,"Learning stage has three lives");require(new MiniGame(mode,0,5).hearts==1,"Final stage has one life in every game");}
  MiniGame pepper=new MiniGame(6,0,5);pepper.countdown=0;pepper.encounter=2;pepper.phase=1.5f;pepper.input(0);require(pepper.finished&&!pepper.won,"Pepper mistake ends the one-life final challenge");
  for(int stage:new int[]{1,5}){
   MiniGame countdown=new MiniGame(0,0,stage);for(int i=0;i<100;i++)countdown.step(.01f);require(Math.abs(countdown.countdown-1)<.002f,"Ready countdown stays real time");
   MiniGame press=new MiniGame(4,0,stage);press.countdown=0;press.touchDown();for(int i=0;i<25;i++)press.step(.01f);require(Math.abs(press.touchHoldTime-.25f)<.001f,"Press strength stays tied to real duration");
   MiniGame glow=new MiniGame(11,0,stage);glow.countdown=0;glow.acceptSoundLevel(.9f,.5f,true,true);for(int i=0;i<50;i++)glow.step(.01f);require(Math.abs(glow.glowProgress-.5f)<.001f,"Faster stages do not make steady holds artificially shorter");
  }
  for(int mode:new int[]{1,3,4,6,7,8,9,11}){
   MiniGame g=new MiniGame(mode,0,5);require((mode==1||mode==4?g.timedTarget():mode==3?g.voiceTarget():g.goal())==(mode==9?AnimalPatterns.cues(5):mode==1||mode==3||mode==4||mode==7?8:6),"Final challenge needs perfect accuracy mode "+mode);
  }
  for(int mode:new int[]{1,3,4,6,7,8,9,11}){
   MiniGame g=new MiniGame(mode,0,5);g.countdown=0;g.resolved=true;g.hearts=2;if(MiniGame.requiresMicrophone(mode))g.acceptSoundLevel(0,0,false,true);
   if(mode==1||mode==4){g.encounter=7;g.score=7;g.phase=3.8f;if(mode==4)g.melodyOutcomeTime=1.05f;}
   else if(mode==3){g.encounter=7;g.score=7;g.phase=g.voiceApproach()+.9f;}
   else if(mode==6){g.encounter=8;g.score=5;g.phase=3.2f;}
   else if(mode==7){g.encounter=7;g.score=7;g.phase=3.7f;g.flightTime=1.05f;}
   else{g.encounter=mode==9?AnimalPatterns.cues(5)-1:5;g.score=g.goal()-1;g.flightTime=1.1f;}
   if(mode==8){g.cometLanded=true;g.cometLaunched=true;g.flightTime=1.8f;}
   g.step(.01f);require(g.finished&&!g.won,"A missed encounter actually fails the final challenge, mode "+mode);
  }
  for(int stage=1;stage<=5;stage++)for(int offset=0;offset<5;offset++){
   MiniGame g=new MiniGame(4,0,stage);g.countdown=0;g.microphoneMode=true;g.micReady=true;
   for(int i=0;i<5000&&!g.finished;i++){
    if(i%5==offset){float launch=g.windowOpens()+.01f+.15f*g.pace(),t=2.65f-launch,h=MelodyCourse.height(stage,g.encounter)+5;g.soundStrength=Math.max(0,Math.min(1,((h+500*t*t)*(h+500*t*t)/(2000*t*t)-18)/82));g.voiceSignal(g.phase>=g.windowOpens()+.01f&&g.phase<g.windowCloses(),true);}g.step(.01f);
   }require(g.won&&g.score==8,"Actual 50 ms microphone update cadence remains playable at stage "+stage+" offset "+offset);
  }
  System.out.println("PASS: all 15 game clocks accelerate across five stages; unchanged real countdown, touch power and sound holds; perfect final targets; 25 microphone-cadence jump paths");
 }
}
