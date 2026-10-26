package com.facechaos.mobile;
/** Actual sound gate, live-input adapter and visible flight path, with synthetic PCM only. */
public final class FireflySoundCheck {
 static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
 static short[] tone(int amplitude){short[] pcm=new short[800];for(int i=0;i<pcm.length;i++)pcm[i]=(short)(Math.sin(i*.2)*amplitude);return pcm;}
 static void feed(MiniGame g,SoundGate gate,short[] pcm){boolean active=gate.feed(pcm,pcm.length);g.acceptSoundLevel(gate.level,gate.strength,active,gate.ready());for(int i=0;i<5;i++)g.step(.01f);}
 public static void main(String[] args){
  short[] quiet=tone(70),hum=tone(1400),loud=tone(18000);
  for(int stage=1;stage<=5;stage++){
   MiniGame g=new MiniGame(11,0,stage);g.countdown=0;require(g.microphoneMode,"Firefly always starts as sound control");
   g.touchDown();g.touchUp();g.input(0);require(!g.voiceActive&&!g.touchHolding&&g.glowProgress==0,"Touch cannot move the firefly");
   for(int i=0;i<100;i++)g.step(.01f);require(g.phase==0&&g.hearts==(stage==5?1:3),"Calibration freezes hazards and lives");
   SoundGate gate=new SoundGate();for(int i=0;i<20;i++)feed(g,gate,quiet);
   float startX=g.glowFlightX(),startY=g.glowFlightY(600);
   for(int i=0;i<8;i++)feed(g,gate,hum);require(g.glowProgress>0&&g.glowFlightX()<startX&&g.glowFlightY(600)<startY,"Gentle PCM moves the firefly visibly toward the first lantern");
   require(g.micLevel<g.glowHigh(),"A normal hum is not incorrectly saturated above the gold range");
   float progress=g.glowProgress;for(int i=0;i<8;i++)feed(g,gate,quiet);require(g.glowProgress<progress&&g.coins==0,"Quiet drifts back without awarding a lantern");
   for(int i=0;i<8;i++)feed(g,gate,loud);require(g.coins==0,"Out-of-range loud sound cannot complete a lantern");
   MiniGame winner=new MiniGame(11,0,stage);winner.countdown=0;SoundGate winGate=new SoundGate();for(int i=0;i<20;i++)feed(winner,winGate,quiet);
   for(int i=0;i<2000&&!winner.finished;i++)feed(winner,winGate,hum);
   require(winner.won&&winner.coins==6,"All six lanterns won through real PCM analysis at stage "+stage);
   for(boolean lit:winner.glowLit)require(lit,"Every reached lantern is lit");
   int coins=winner.coins;winner.touchDown();winner.touchUp();winner.step(.05f);require(winner.coins==coins,"Completed run cannot duplicate rewards");
  }
  require(!GameCatalog.ALL[11].controlBrief().contains("Touch practice"),"Instructions must not ask for touch");
  System.out.println("PASS: five PCM-driven six-lantern wins; visible sound-controlled flight, quiet drift, loud rejection, calibration freeze, no touch substitution and no duplicate coins");
 }
}
