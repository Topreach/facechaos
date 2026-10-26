package com.facechaos.mobile;
/** Real sound-level algorithm fed synthetic PCM, through MiniGame to the race engine. */
public final class RaceSoundCheck {
 static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
 static short[] tone(int amplitude){short[] samples=new short[800];for(int i=0;i<samples.length;i++)samples[i]=(short)(Math.sin(i*.2)*amplitude);return samples;}
 static void feed(MiniGame game,SoundGate gate,short[] pcm){boolean active=gate.feed(pcm,pcm.length);game.acceptSoundLevel(gate.level,gate.strength,active,gate.ready());for(int i=0;i<5;i++)game.step(.01f);}
 public static void main(String[] args){
  short[] quiet=tone(70),soft=tone(900),strong=tone(4500);
  for(int stage=1;stage<=5;stage++){
   MiniGame game=new MiniGame(12,0,stage);game.countdown=0;game.microphoneMode=true;SoundGate gate=new SoundGate();
   for(int i=0;i<19;i++)feed(game,gate,quiet);require(game.racing.time==0&&!game.micReady,"Race waits for quiet calibration");feed(game,gate,quiet);
   for(int i=0;i<15;i++)feed(game,gate,soft);float softSpeed=game.racing.playerSpeed,softPower=game.raceThrottle();require(softPower>0&&softSpeed>20,"Audible soft PCM accelerates car");
   game.touchDown();require(!game.touchHolding,"Touch must not impersonate microphone input");
   for(int i=0;i<15;i++)feed(game,gate,strong);float strongSpeed=game.racing.playerSpeed;require(game.raceThrottle()>softPower&&strongSpeed>softSpeed+50,"Stronger PCM produces more throttle and speed");
   for(int i=0;i<15;i++)feed(game,gate,quiet);require(game.raceThrottle()==0&&game.racing.playerSpeed<strongSpeed,"Quiet removes throttle and coasts");
   MiniGame winner=new MiniGame(12,0,stage);winner.countdown=0;winner.microphoneMode=true;SoundGate winGate=new SoundGate();for(int i=0;i<20;i++)feed(winner,winGate,quiet);
   for(int i=0;i<1000&&!winner.finished;i++)feed(winner,winGate,strong);
   require(winner.won&&winner.coins==4,"PCM-driven sound race completes stage "+stage);
  }
  System.out.println("PASS: five full PCM-driven sound race wins; quiet calibration, soft acceleration, stronger acceleration, quiet coasting and no touch substitution");
 }
}
