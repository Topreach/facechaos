package com.facechaos.mobile;

/** Authored contact/miss events and terminal immutability across all 75 game/stage pairs. */
public final class FailurePolicyCheck {
 static void require(boolean ok,String message){if(!ok)throw new AssertionError(message);}
 static MiniGame ready(int mode,int stage){MiniGame g=new MiniGame(mode,17,stage);g.countdown=0;if(MiniGame.requiresMicrophone(mode))g.acceptSoundLevel(0,0,false,true);return g;}
 static String state(MiniGame g){StringBuilder s=new StringBuilder();try{for(java.lang.reflect.Field f:MiniGame.class.getDeclaredFields())if(!java.lang.reflect.Modifier.isStatic(f.getModifiers())&&(f.getType().isPrimitive()||f.getType()==String.class))s.append(f.getName()).append('=').append(f.get(g)).append(';');if(g.racing!=null)for(java.lang.reflect.Field f:RacingGame.class.getDeclaredFields())if(!java.lang.reflect.Modifier.isStatic(f.getModifiers())&&f.getType().isPrimitive())s.append(f.getName()).append('=').append(f.get(g.racing)).append(';');}catch(Exception e){throw new RuntimeException(e);}return s.toString();}
 static void locked(MiniGame g){
  require(g.finished&&!g.won&&!g.stopped,"Mission must fail: mode "+g.mode+" stage "+g.stage);
  require(g.resultTitle().equals("Mission failed")&&g.resultAction().equals("Try again")&&g.nextRunStage()==1&&g.coinsForNextStage()==0,"Failure must offer a fresh Stage 1 retry");
  String before=state(g);for(int i=0;i<100;i++){g.step(.05f);g.input(i%g.controls().length);g.touchDown();g.touchLane(.9f);g.touchUp();g.jump(1);g.voiceSignal(true,true);g.acceptSoundLevel(1,1,true,true);if(g.flags!=null)g.flagAnswer(g.flags.country(g.encounter).name);}
  g.stopRound();try{java.lang.reflect.Method finish=MiniGame.class.getDeclaredMethod("finish",boolean.class);finish.setAccessible(true);finish.invoke(g,true);}catch(Exception error){throw new RuntimeException(error);}
  require(before.equals(state(g)),"Late step/input/sound callbacks changed failed game "+g.mode+" stage "+g.stage);
 }
 static void fault(MiniGame g){
  switch(g.mode){
   case 0:g.phase=g.gateApproach()-.01f;g.step(.02f);break;
   case 1:g.phase=g.escapeContact()-.01f;g.step(.02f);break;
   case 2:case 5:g.phase=g.cueTimeout()-.01f;g.step(.02f);break;
   case 3:g.phase=g.voiceApproach()-.01f;g.height=g.corridor()>.5f?.08f:.92f;g.step(.02f);break;
   case 4:g.phase=2.64f;for(int i=0;i<100&&!g.finished;i++)g.step(.02f);break;
   case 6:for(int i=0;i<9;i++)if(!GameSections.treat(g.stage,i)){g.encounter=i;break;}g.phase=1.5f;g.input(0);break;
   case 7:g.phase=.5f;while(g.stackAligned()&&g.phase<2.5f)g.phase+=.01f;g.input(0);for(int i=0;i<30&&!g.finished;i++)g.step(.02f);break;
   case 8:g.charge=.98f;g.wasCharging=true;for(int i=0;i<150&&!g.finished;i++)g.step(.02f);break;
   case 9:g.phase=g.cueTimeout()-.01f;g.step(.02f);break;
   case 10:g.resolved=false;g.phase=g.memoryShowDuration()+.1f;g.flash=0;g.input((g.target()+1)%3);break;
   case 11:g.phase=7;g.step(.02f);break;
   case 12:g.racing.rivalDistance=RacingGame.FINISH-.1f;g.racing.rivalSpeed=200;g.step(.02f);break;
   case 13:int blocked=0;for(int lane=-1;lane<=1;lane++)if(g.racing.blocked(lane)){blocked=lane;break;}g.headLane=blocked;g.racing.steerPosition=blocked;g.racing.trafficTime=g.racing.approachTime()-.01f;int mask=g.racing.trafficMask;g.step(.02f);require(g.racing.trafficMask==mask&&g.racing.trafficProgress()==1&&g.racing.result==-1,"Crash must keep the impact wave rather than spawn another car");break;
   case 14:g.flagAnswer("not a country");break;
  }
 }
 public static void main(String[] args){
  for(int mode=0;mode<15;mode++)for(int stage=1;stage<=5;stage++){
   MiniGame g=ready(mode,stage);int attempts=0;do{fault(g);require(++attempts<=3,"Failure must be reachable for mode "+mode+" stage "+stage);if(!g.finished&&g.resolved){g.encounter++;g.resolved=false;g.flash=0;}}while(!g.finished);locked(g);
   if(GamePolicy.singleLife(mode)||mode==6||mode==12)require(attempts==1,"First physical collision must end the round "+mode+" stage "+stage);
  }
  for(int stage=1;stage<=5;stage++)for(int action=1;action<=2;action++){MiniGame escape=ready(1,stage);escape.encounter=action;require(escape.windowCloses()<=escape.escapeContact(),"Input window extends past collision");escape.phase=escape.escapeContact()-.01f;escape.step(.02f);locked(escape);}
  for(int mode:new int[]{2,5,9,10,11,14}){MiniGame g=ready(mode,1);fault(g);require(!g.finished&&g.hearts==2&&g.missNotice>0,"A recoverable cue miss costs a visible life, mode "+mode);}
  MiniGame fruit=ready(6,1);for(int i=0;i<9&&!fruit.finished;i++)if(GameSections.treat(1,i)){fruit.encounter=i;fruit.resolved=false;fruit.phase=2.55f;fruit.step(.02f);}require(fruit.finished&&fruit.hearts>0,"Unreachable collection target ends the round before useless extra encounters");
  System.out.println("PASS: 75 game/stage losses; first physical contact is fatal; ten Escape shield/attack contacts; frozen results and late inputs; visible recoverable lives; unattainable target; Stage 1 retries");
 }
}
