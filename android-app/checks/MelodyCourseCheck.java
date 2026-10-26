package com.facechaos.mobile;
public final class MelodyCourseCheck {
 static void require(boolean b,String m){if(!b)throw new AssertionError(m);}
 static MiniGame attempt(int stage,int obstacle,float power){MiniGame g=new MiniGame(4,0,stage);g.countdown=0;g.encounter=obstacle;g.phase=2.2f;g.jump(power);while(!g.resolved)g.step(.005f);return g;}
 public static void main(String[] args){
  float priorHeight=0,priorSpeed=0;boolean[] kinds=new boolean[5];
  for(int stage=1;stage<=5;stage++){
   float average=0;for(int i=0;i<8;i++){average+=MelodyCourse.height(stage,i);kinds[MelodyCourse.kind(stage,i)]=true;MiniGame g=new MiniGame(4,0,stage);g.countdown=0;g.encounter=i;float target=g.melodyLandingX();g.step(.02f);require(g.melodyLandingX()<target,"Course actually approaches runner");MiniGame clear=attempt(stage,i,g.melodyPracticeStrength());require(clear.earnedCoin&&!clear.melodyFalling,"Controlled power clears authored obstacle "+stage+"/"+i);}
   average/=8;float speed=MelodyCourse.speed(stage)*MiniGame.paceAtStage(stage);require(average>priorHeight&&speed>priorSpeed,"Actual height and speed increase at every difficulty");priorHeight=average;priorSpeed=speed;
  }
  for(boolean kind:kinds)require(kind,"All five obstacle families present");
  require(attempt(1,0,.35f).earnedCoin,"Gentle sound clears first challenge");require(attempt(5,0,.35f).melodyFalling,"Same gentle sound fails final height");
  MiniGame over=attempt(4,3,1);require(over.melodyFalling&&over.feedback.contains("overhang"),"Maximum power hits actual low ceiling");
  require(Math.abs(MelodyCourse.required(4,1,1.9f)-MelodyCourse.required(4,1,2.65f))>1,"Drifting ledge changes actual landing height");
  MiniGame pause=new MiniGame(4);pause.countdown=0;pause.paused=true;float x=pause.melodyLandingX();pause.step(.05f);require(pause.melodyLandingX()==x,"Pause freezes approaching course");
  System.out.println("PASS: forty controlled-power obstacle landings; five increasing height/speed tiers; approaching geometry; weak final jump, real overhang failure, drifting landing height and pause");
 }
}
