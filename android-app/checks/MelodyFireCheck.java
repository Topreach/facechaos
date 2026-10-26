package com.facechaos.mobile;
public final class MelodyFireCheck {
 static void require(boolean b,String m){if(!b)throw new AssertionError(m);}
 static MiniGame falling(int stage,boolean weak){MiniGame g=new MiniGame(4,0,stage);g.countdown=0;g.encounter=1;if(weak){g.phase=2.2f;g.jump(.05f);}while(!g.resolved)g.step(.01f);return g;}
 public static void main(String[] args){
  for(int stage=1;stage<=5;stage++)for(boolean weak:new boolean[]{false,true}){
   MiniGame g=falling(stage,weak);require(g.melodyFalling&&!g.finished&&g.coins==0,"Failed jump begins visible fall before penalty");int hearts=g.hearts;float base=500,start=g.melodyFeet(base);g.paused=true;g.step(.05f);require(g.melodyOutcomeTime==.01f&&g.melodyFeet(base)==start,"Pause freezes fall");g.paused=false;
   for(int i=0;i<25;i++)g.step(.01f);require(g.melodyFeet(base)>start&&g.hearts==hearts,"Character falls before impact");
   while(!g.melodyImpact)g.step(.01f);require(g.hearts==hearts-1&&g.coins==0,"One fire impact costs exactly one life, no coin");
   if(stage==5)require(g.finished&&!g.won&&g.nextRunStage()==1,"Final-stage fire ends run and retries at Stage 1");
   else{for(int i=0;i<60;i++)g.step(.01f);require(g.hearts==hearts-1&&!g.melodyFalling&&g.encounter==2,"Recover once, no duplicate penalty");}
  }
  MiniGame safe=new MiniGame(4);safe.countdown=0;safe.encounter=1;safe.phase=2.2f;safe.jump(.85f);while(!safe.resolved)safe.step(.01f);require(safe.earnedCoin&&!safe.melodyFalling&&safe.jumpHeight==safe.jumpRequired(),"Adequate jump lands on platform");float feet=safe.melodyFeet(500);for(int i=0;i<30;i++)safe.step(.01f);require(safe.melodyFeet(500)==feet&&safe.hearts==3&&safe.coins==1,"Successful landing stays on platform and rewards once");
  System.out.println("PASS: ten missing/weak jump fire falls, visible descent, pause, one impact penalty, recovery and final-stage retry; successful landing stays safe");
 }
}
