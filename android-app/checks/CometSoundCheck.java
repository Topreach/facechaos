package com.facechaos.mobile;
/** Synthetic PCM through the production gate and rocket lifecycle, never a real microphone. */
public final class CometSoundCheck {
 static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
 static short[] tone(int amplitude){short[] pcm=new short[800];for(int i=0;i<pcm.length;i++)pcm[i]=(short)(Math.sin(i*.2)*amplitude);return pcm;}
 static void feed(MiniGame g,SoundGate gate,short[] pcm){boolean active=gate.feed(pcm,pcm.length);g.acceptSoundLevel(gate.level,gate.strength,active,gate.ready());for(int i=0;i<5;i++)g.step(.01f);}
 static void land(MiniGame g){for(int i=0;i<200&&!g.cometLanded&&!g.finished;i++)g.step(.01f);}
 public static void main(String[] args){
  short[] quiet=tone(70),hum=tone(1400);
  for(int stage=1;stage<=5;stage++){
   MiniGame g=new MiniGame(8,0,stage);g.countdown=0;g.microphoneMode=true;SoundGate gate=new SoundGate(true);
   for(int i=0;i<19;i++)feed(g,gate,quiet);require(g.phase==0&&g.racing==null,"Calibration freezes launch deadline");feed(g,gate,quiet);
   for(int shot=0;shot<6&&!g.finished;shot++){
    int before=g.coins;for(int i=0;i<100&&g.charge<g.powerTarget()-.025f;i++)feed(g,gate,hum);
    require(g.charge>0&&!g.resolved,"Comfortable hum charges the rocket");
    for(int i=0;i<20&&!g.resolved;i++)feed(g,gate,quiet);
    require(g.resolved&&g.cometLaunched&&g.coins==before,"Release starts an unscored flight");
    float launchPower=g.launchPower;g.paused=true;g.cancelTouch();g.step(.05f);require(g.launchPower==launchPower&&g.charge>0,"Pause preserves a launched trajectory");g.paused=false;
    while(!g.cometLanded&&!g.finished)feed(g,gate,quiet);
    require(g.coins==before+1&&g.earnedCoin,"Reward arrives only after successful landing");
    while(g.resolved&&!g.finished)feed(g,gate,quiet);
   }
   require(g.won&&g.coins==6,"Six PCM-controlled landings win stage "+stage);
   for(float power:new float[]{.05f,.98f}){
    MiniGame miss=new MiniGame(8,0,stage);miss.countdown=0;miss.micReady=true;miss.charge=power;miss.wasCharging=true;for(int i=0;i<12;i++)miss.step(.01f);require(miss.resolved&&miss.coins==0&&miss.hearts==(stage==5?1:3),"Miss is not judged at release");land(miss);require(miss.coins==0&&miss.hearts==(stage==5?0:2),"Bad power loses one life at landing");require(miss.cometFlightX()>=70&&miss.cometFlightX()<=354,"Miss trajectory remains visible");
   }
  }
  MiniGame dropout=new MiniGame(8);dropout.countdown=0;dropout.microphoneMode=true;dropout.micReady=true;dropout.charge=.25f;dropout.wasCharging=true;dropout.voiceSignal(false,true);for(int i=0;i<4;i++)dropout.step(.01f);require(!dropout.resolved,"Brief microphone dropout cannot trigger a launch");dropout.voiceSignal(true,true);dropout.step(.01f);require(dropout.launchQuietTime==0,"Resumed hum cancels the short dropout");
  dropout.touchDown();dropout.touchUp();dropout.input(0);require(!dropout.touchHolding&&!dropout.resolved,"Touch cannot force a launch in microphone mode");
  System.out.println("PASS: 30 PCM-controlled landings across five stage wins, calibration freeze, quiet-release debounce, actual flight before reward, pause preservation and visible short/long misses");
 }
}
