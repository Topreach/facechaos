package com.facechaos.mobile;

/** Shared pure racing rules; no camera, microphone or renderer dependency. */
final class RacingGame {
 final int mode,stage;
 final java.util.Random random;
 final int[] laneBag={0,1,2};
 static final float FINISH=450;
 float playerSpeed,rivalSpeed,playerDistance,rivalDistance,time,steerPosition,trafficTime,recovery;
 int trafficMask,rows,dodged,crashes,checkpoints,coins,result;
 boolean justCrashed;
 RacingGame(int mode,int stage,int seed){this.mode=mode;this.stage=stage;random=new java.util.Random(seed);nextTraffic();}
 int trafficRequired(){return trafficGoal()-(stage==5?0:2);}
 float rivalBase(){return 88+(stage-1)*17;}
 int trafficGoal(){return 12+(stage-1)*2;}
 float approachTime(){return 3-(stage-1)*.2f;}
 float trafficProgress(){return Math.min(1,trafficTime/approachTime());}
 int safeLane(){int best=0;float distance=Float.MAX_VALUE;for(int lane=-1;lane<=1;lane++)if(!blocked(lane)&&Math.abs(lane-steerPosition)<distance){best=lane;distance=Math.abs(lane-steerPosition);}return best;}
 boolean blocked(int lane){return (trafficMask&(1<<(lane+1)))!=0;}
 private void nextTraffic(){
  if(rows%3==0)for(int i=2;i>0;i--){int j=random.nextInt(i+1),old=laneBag[i];laneBag[i]=laneBag[j];laneBag[j]=old;}
  int lane=laneBag[rows%3];trafficMask=1<<lane;
  if(stage>=3&&rows%3!=1){int other=(lane+1+random.nextInt(2))%3;trafficMask|=1<<other;}
 }
 void steer(float dt,int lane){float target=Math.max(-1,Math.min(1,lane));steerPosition+=Math.max(-dt*5,Math.min(dt*5,target-steerPosition));}
 void step(float dt,float throttle,int lane){
  if(result!=0||dt<=0||!Float.isFinite(dt))return;
  time+=dt;justCrashed=false;
  if(mode==12){
   float target=20+215*(Float.isFinite(throttle)?Math.max(0,Math.min(1,throttle)):0);
   float aiTarget=rivalBase()+12*(float)Math.sin(time*1.3f)+6*(float)Math.cos(time*.43f);
   float oldPlayer=playerDistance,oldRival=rivalDistance;
   playerSpeed+=(target-playerSpeed)*(1-(float)Math.exp(-dt*2.6f));
   rivalSpeed+=(aiTarget-rivalSpeed)*(1-(float)Math.exp(-dt*2.6f));
   playerDistance+=playerSpeed/3.6f*dt;rivalDistance+=rivalSpeed/3.6f*dt;
   if(playerDistance>=FINISH||rivalDistance>=FINISH){
    float playerCross=playerDistance>=FINISH?(FINISH-oldPlayer)/Math.max(.0001f,playerDistance-oldPlayer):Float.POSITIVE_INFINITY;
    float rivalCross=rivalDistance>=FINISH?(FINISH-oldRival)/Math.max(.0001f,rivalDistance-oldRival):Float.POSITIVE_INFINITY;
    result=playerCross<=rivalCross?1:-1;float stop=Math.max(0,Math.min(1,Math.min(playerCross,rivalCross)));playerDistance=oldPlayer+(playerDistance-oldPlayer)*stop;rivalDistance=oldRival+(rivalDistance-oldRival)*stop;
   }
   int passed=Math.min(4,(int)((playerDistance+.0001f)/(FINISH/4)));coins+=Math.max(0,passed-checkpoints);checkpoints=passed;
  }else{
   steer(dt,lane);
   if(recovery>0){recovery=Math.max(0,recovery-dt);return;}
   trafficTime+=dt;
   if(trafficTime>=approachTime()){
    rows++;boolean collision=false;
    // Car width occupies .62 lane widths. Crossing a blocked lane at impact is a real collision.
    for(int i=-1;i<=1;i++)if(blocked(i)&&Math.abs(steerPosition-i)<.62f)collision=true;
    if(collision){crashes++;justCrashed=true;}else{dodged++;coins++;}
    trafficTime=0;recovery=.45f;
    if(rows>=trafficGoal())result=dodged>=trafficRequired()?1:-1;else nextTraffic();
   }
  }
 }
}
