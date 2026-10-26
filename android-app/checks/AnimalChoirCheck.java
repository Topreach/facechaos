package com.facechaos.mobile;
/** Exercises PCM-to-rhythm-to-coins, not animal identity classification. */
public final class AnimalChoirCheck {
 static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
 static short[] quiet=new short[800];
 static void feed(MiniGame g,SoundGate gate,short[] pcm){boolean active=gate.feed(pcm,pcm.length);g.acceptSoundLevel(gate.level,gate.strength,active,gate.ready());for(int i=0;i<5;i++)g.step(.01f);}
 static void quiet(MiniGame g,SoundGate gate,int frames){for(int i=0;i<frames;i++)feed(g,gate,quiet);}
 static void signal(MiniGame g,float seconds,boolean active){g.voiceSignal(active,true);for(int i=0;i<Math.round(seconds/.01f);i++)g.step(.01f);}
 public static void main(String[] args){
  for(int stage=1;stage<=5;stage++){
   MiniGame g=new MiniGame(9,0,stage);g.microphoneMode=true;g.countdown=0;SoundGate gate=new SoundGate(true);
   quiet(g,gate,19);require(g.phase==0&&g.hearts==(stage==5?1:3),"Calibration never consumes time or lives");quiet(g,gate,1);
   for(int animal=0;animal<AnimalPatterns.cues(stage)&&!g.finished;animal++){
    int expected=g.coins+1;quiet(g,gate,5);short[] demo=AnimalGuide.samples(g.target());
    for(int offset=0;offset<demo.length;offset+=800){short[] frame=new short[800];System.arraycopy(demo,offset,frame,0,Math.min(800,demo.length-offset));feed(g,gate,frame);if(g.coins>=expected)break;}
    for(int tail=0;tail<12&&g.coins<expected;tail++)quiet(g,gate,1);require(g.coins==expected,"A full PCM rhythm earns exactly one coin, stage "+stage+" animal "+animal+" coins="+g.coins+" pulses="+g.soundPulses+" duration="+g.soundDuration+" feedback="+g.feedback+" encounter="+g.encounter+" resolved="+g.resolved);
    while(g.resolved&&!g.finished)quiet(g,gate,1);
   }
   require(g.won&&g.coins==AnimalPatterns.cues(stage),"Expanded PCM animal rhythms win stage "+stage);
  }
  MiniGame dropout=new MiniGame(9);dropout.countdown=0;dropout.microphoneMode=true;dropout.micReady=true;
  signal(dropout,.25f,false);signal(dropout,.10f,true);signal(dropout,.04f,false);signal(dropout,.10f,true);signal(dropout,.20f,false);
  require(dropout.soundPulses==1&&!dropout.resolved,"A 40ms microphone dropout does not split one woof into two");
  signal(dropout,.20f,true);signal(dropout,.4f,false);require(dropout.coins==1,"Two real calls still complete the dog rhythm");
  MiniGame shortcut=new MiniGame(9);shortcut.countdown=0;shortcut.input(shortcut.target());require(shortcut.score==0&&shortcut.coins==0,"Direct button input cannot bypass the rhythm");
  MiniGame demo=new MiniGame(9);demo.countdown=0;demo.animalDemoPlaying=true;for(int i=0;i<100;i++)demo.step(.01f);require(demo.phase==0&&demo.coins==0,"Demo never advances or scores the round");demo.animalDemoPlaying=false;demo.soundPulses=1;demo.phase=2;demo.resetAnimalAttempt();require(demo.soundPulses==0&&demo.phase==0&&demo.hearts==3,"Replaying a guide resets the partial attempt without punishment");
  MiniGame cat=new MiniGame(9);cat.countdown=0;cat.encounter=1;cat.microphoneMode=true;cat.micReady=true;signal(cat,.25f,false);signal(cat,.15f,true);signal(cat,.2f,false);require(cat.coins==0&&cat.hearts==2&&cat.feedback.contains("meow"),"Short meow gets specific corrective feedback");
  System.out.println("PASS: 48 PCM-driven animal cues across five stage wins, calibration freeze, brief-dropout tolerance, correct durations/counts, demo isolation, no scoring shortcut and useful failure feedback");
 }
}
