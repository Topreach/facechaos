package com.facechaos.mobile;
public final class RacingCheck {
 static void require(boolean b,String m){if(!b)throw new AssertionError(m);}
 public static void main(String[] args){
  RacingGame soft=new RacingGame(12,1,0),strong=new RacingGame(12,1,0);for(int i=0;i<100;i++){soft.step(.01f,.2f,0);strong.step(.01f,.8f,0);}require(strong.playerSpeed>soft.playerSpeed+100,"Sound strength controls actual speed, not a binary boost");float speed=strong.playerSpeed;for(int i=0;i<100;i++)strong.step(.01f,0,0);require(strong.playerSpeed<speed,"Quiet coasts and slows the car");
  for(int stage=1;stage<=5;stage++){
   for(float power:new float[]{.1f,.8f}){RacingGame race=new RacingGame(12,stage,10);for(int i=0;i<5000&&race.result==0;i++)race.step(.02f,power,0);require(race.result==(power>.5f?1:-1),"CPU can win low power and lose high power at stage "+stage);if(race.result==1)require(race.coins==4,"Four unique track checkpoint coins");int coins=race.coins;race.step(1,1,0);require(race.coins==coins,"Finished race cannot duplicate coins");}
   MiniGame liveRace=new MiniGame(12,42,stage);liveRace.countdown=0;liveRace.microphoneMode=true;liveRace.micReady=true;liveRace.soundStrength=.8f;liveRace.voiceSignal(true,true);for(int i=0;i<5000&&!liveRace.finished;i++)liveRace.step(.01f);require(liveRace.won&&liveRace.score==100&&liveRace.coins==4,"Full live-signal race with final progress and checkpoint coins");
   for(int seed=0;seed<20;seed++){
    MiniGame dodge=new MiniGame(13,seed,stage);dodge.countdown=0;
    for(int i=0;i<15000&&!dodge.finished;i++){dodge.touchLane(.5f+dodge.racing.safeLane()/3f);dodge.step(.01f);}require(dodge.won&&dodge.racing.crashes==0&&dodge.coins==dodge.racing.trafficGoal(),"Safe steering wins all waves "+stage+" seed "+seed);
    MiniGame idle=new MiniGame(13,seed,stage);idle.countdown=0;for(int i=0;i<15000&&!idle.finished;i++)idle.step(.01f);require(idle.finished&&!idle.won,"Staying stationary cannot win by lucky traffic seed "+seed);
   }
  }
  MiniGame live=new MiniGame(12);live.countdown=0;live.microphoneMode=true;live.step(.02f);require(live.racing.time==0,"CPU waits for microphone readiness");live.micReady=true;live.soundStrength=.8f;live.voiceSignal(true,true);for(int i=0;i<100;i++)live.step(.01f);require(live.racing.playerSpeed>100,"Actual microphone signal drives race throttle");
  MiniGame head=new MiniGame(13);head.countdown=0;head.faceMode=true;head.faceReady=false;for(int i=0;i<100;i++)head.step(.01f);require(head.racing.time==0&&head.hearts==3,"Head tracking loss freezes traffic without losing lives");head.faceReady=true;head.headGrace=.6f;head.headLane=-1;for(int i=0;i<40;i++)head.step(.01f);require(head.racing.time==0&&head.racing.steerPosition<-.9f,"Recovery grace lets the player steer before traffic restarts");head.touchLane(.9f);require(head.headLane==-1,"Touch cannot masquerade as head steering");
  head.paused=true;float time=head.racing.time;head.step(.02f);require(head.racing.time==time,"Pause freezes racing");
  System.out.println("PASS: analog sound speed, CPU win/loss, unique coins, 100 traffic seeds/stages, guaranteed idle loss, microphone readiness, head loss/recovery and touch isolation");
 }
}
