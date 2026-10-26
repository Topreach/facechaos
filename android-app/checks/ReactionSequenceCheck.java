package com.facechaos.mobile;
/** Transitional inputs through detection and real game rules, rather than ideal isolated poses. */
public final class ReactionSequenceCheck {
 static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
 static FaceSignals calibrated(){FaceSignals f=new FaceSignals();for(int i=0;i<12;i++)f.accept(.03f,.05f,0,0,i*80,1);require(f.ready,"Calibration");return f;}
 public static void main(String[] args){
  for(int cadence:new int[]{40,60,100,150})for(float sensitivity:new float[]{.7f,1,1.3f}){
   FaceSignals f=calibrated();long time=2000;int events=0;
   for(int i=0;i<8;i++){require(f.accept(.75f,.1f,0,0,time,sensitivity,FaceSignals.TEETH)==-1,"Jaw opening must not consume a teeth cue");time+=cadence;}
   require(f.armed,"Teeth cue remains armed during mouth preparation");
   for(int i=0;i<8;i++){int event=f.accept(.75f,.8f,0,.9f,time,sensitivity,FaceSignals.TEETH);if(event>=0){require(event==FaceSignals.TEETH,"Teeth evidence must beat jaw/smile");events++;}time+=cadence;}
   require(events==1,"Teeth reacts once at cadence "+cadence+" sensitivity "+sensitivity);
   for(int i=0;i<5;i++){require(f.accept(.75f,.8f,0,.9f,time,sensitivity,FaceSignals.TEETH)==-1,"Held teeth cannot repeat");time+=cadence;}
  }
  FaceSignals sensitive=calibrated();int event=-1;
  for(long t=2000;t<2400;t+=60){int value=sensitive.accept(.5f,.5f,0,.5f,t,.7f,FaceSignals.TEETH);if(value>=0)event=value;}
  require(event==FaceSignals.TEETH,"Higher sensitivity must apply to teeth evidence too");
  FaceSignals ordinary=calibrated();for(long t=2000;t<2400;t+=60)require(ordinary.accept(.5f,.5f,0,.5f,t,1,FaceSignals.TEETH)==-1,"Default sensitivity keeps ambiguous evidence unconfirmed");
  FaceSignals negatives=calibrated();for(long t=2000;t<5000;t+=60){require(negatives.accept(.85f,.9f,0,0,t,1,FaceSignals.TEETH)==-1,"Jaw/smile without tooth pixels must never clear teeth");}
  // A real six-gate Stage 1: raw scores -> timed detector -> game adapter -> gate and coin rules.
  MiniGame gates=new MiniGame(0);gates.countdown=0;gates.faceMode=true;gates.faceReady=true;
  FaceSignals f=calibrated();long time=2000;int[] seen=new int[5];
  for(int gate=0;gate<gates.goal();gate++){
   for(int i=0;i<4;i++){f.accept(.03f,.05f,0,0,time,1);time+=100;}
   int expected=FaceGameInput.expected(gates);seen[gates.target()]++;
   if(expected==FaceSignals.TEETH)for(int i=0;i<6;i++){int action=f.accept(.8f,.1f,0,0,time,1,expected);require(action==-1,"Mouth preparation must wait for teeth");time+=60;}
   for(int i=0;i<5;i++){
    int action=f.accept(expected==0?.8f:expected==2?.5f:.03f,expected==1?.85f:expected==2?.6f:.05f,expected==FaceSignals.FACE_SQUEEZE?.95f:0,expected==2?.9f:0,expected==FaceSignals.WIDE_EYES?.85f:0,expected==FaceSignals.FACE_SQUEEZE?.85f:0,time,1,expected);time+=100;
    if(action>=0)FaceGameInput.send(gates,action);
   }
   require(gates.gateMatched,"Requested expression must clear gate "+gate+" action "+expected);
   int before=gates.score;for(int i=0;i<600&&gates.score==before&&!gates.finished;i++)gates.step(.02f);
   require(gates.score==before+1,"Gate awards exactly one success");
  }
  require(gates.won&&gates.coins==6&&seen[0]>0&&seen[1]>0&&seen[2]>0&&seen[3]>0&&seen[4]>0,"All five expressions complete Emoji Gates with six coins");
  System.out.println("PASS: mouth-to-teeth transitions at four cadences and three sensitivities, held-input rejection, negative tooth evidence, sensitivity and full Emoji Gates detector-to-coins playthrough");
 }
}
