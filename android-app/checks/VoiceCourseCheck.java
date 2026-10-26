package com.facechaos.mobile;

/** PCM → sound gate → production flight rules. No actual microphone is opened. */
public final class VoiceCourseCheck {
 static void require(boolean value,String text){if(!value)throw new AssertionError(text);}
 public static void main(String[] args){
  java.util.Set<String> worlds=new java.util.HashSet<>();
  for(int stage=1;stage<=5;stage++){
   require(worlds.add(VoiceCourse.world(stage)),"Distinct named world");
   MiniGame blocked=new MiniGame(3,0,stage);blocked.countdown=0;blocked.microphoneMode=true;
   for(int i=0;i<100;i++)blocked.step(.02f);require(blocked.phase==0&&blocked.height==.5f,"Calibration freezes flight");
   blocked.micReady=true;blocked.voiceSignal(true,true);blocked.touchDown();blocked.touchUp();require(!blocked.touchHolding,"Live voice cannot be replaced by touch gestures");
   for(int offset=0;offset<5;offset++){
    MiniGame g=new MiniGame(3,0,stage);g.countdown=0;g.microphoneMode=true;SoundGate gate=new SoundGate();short[] quiet=new short[800],hum=new short[800];
    for(int i=0;i<800;i++){quiet[i]=(short)(i%2==0?60:-60);hum[i]=(short)(Math.sin(i*.2)*2400);}
    for(int i=0;i<18000&&!g.finished;i++){
     if(i%5==offset){boolean lift=g.height+g.velocity*.6f>g.corridor()+.005f;boolean active=gate.feed(gate.ready()&&lift?hum:quiet,800);g.micLevel=gate.level;g.soundStrength=gate.strength;g.micReady=gate.ready();g.voiceSignal(active,gate.ready());}
     g.step(.01f);
    }
    require(g.won&&g.coins>=g.voiceTarget(),"PCM-driven course playable at stage "+stage+" sample offset "+offset+" score "+g.score);
   }
   MiniGame collision=new MiniGame(3,0,stage);collision.countdown=0;collision.phase=collision.voiceApproach()-.01f;collision.height=collision.corridor()>.5f?.08f:.92f;collision.step(.02f);require(collision.resolved&&!collision.earnedCoin&&collision.coins==0&&collision.hearts==(stage==5?0:2),"Wrong side collides with actual obstacle");
  }
  require(VoiceCourse.target(2,0,1)>.6f&&VoiceCourse.target(2,1,1)<.4f,"Vines demand low flight; thorns demand high flight");
  require(VoiceCourse.target(3,0,1)!=VoiceCourse.target(3,0,2),"Pistons move the safe gap");
  require(VoiceCourse.wind(3,1,1)!=0&&VoiceCourse.wind(3,0,1)==0,"Wind tunnel changes actual flight physics");
  require(VoiceCourse.target(4,1,1)!=VoiceCourse.target(4,1,2),"Ice gate drifts");
  require(VoiceCourse.target(5,0,1)<.4f&&VoiceCourse.target(5,1,1)!=VoiceCourse.target(5,1,2),"Lava requires high flight and firebars move");
  System.out.println("PASS: five distinct worlds, real gap/wind hazards, collision lives, mic-calibration freeze, touch isolation and 25 PCM-driven campaign paths");
 }
}
