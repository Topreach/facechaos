package com.facechaos.mobile;
/** Real classifier scores and gate actions with temporal negatives for ordinary blinks. */
public final class EyeReactionsCheck {
 static void require(boolean value,String text){if(!value)throw new AssertionError(text);}
 static FaceSignals calibrated(){FaceSignals f=new FaceSignals();for(int i=0;i<12;i++)f.accept(.03f,.05f,.02f,0,.12f,.02f,i*80,1,FaceSignals.NONE);require(f.ready,"Neutral eye baseline");return f;}
 static int sample(FaceSignals f,int cue,long time,float blink,float wide,float scrunch){return f.accept(.03f,.05f,blink,0,wide,scrunch,time,1,cue);}
 public static void main(String[] args){
  for(int cadence:new int[]{40,60,100,150})for(int cue:new int[]{FaceSignals.WIDE_EYES,FaceSignals.FACE_SQUEEZE}){
   FaceSignals f=calibrated();int events=0;long time=2000;
   for(int i=0;i<16;i++){int event=sample(f,cue,time,cue==FaceSignals.FACE_SQUEEZE?.95f:.02f,cue==FaceSignals.WIDE_EYES?.85f:.02f,cue==FaceSignals.FACE_SQUEEZE?.8f:.02f);if(event>=0){require(event==cue,"Distinct wide/squeeze action");events++;}time+=cadence;}
   require(events==1,"Held eye gesture fires once at "+cadence);
   for(int i=0;i<4;i++){sample(f,cue,time,.02f,.12f,.02f);time+=100;}
   events=0;for(int i=0;i<8;i++){int event=sample(f,cue,time,cue==FaceSignals.FACE_SQUEEZE?.95f:.02f,cue==FaceSignals.WIDE_EYES?.85f:.02f,cue==FaceSignals.FACE_SQUEEZE?.8f:.02f);if(event==cue)events++;time+=100;}
   require(events==1,"Neutral rearms the eye gesture");f.lost();require(sample(f,cue,time,.95f,.85f,.8f)==-1,"Tracking loss cannot score without neutral recovery");
  }
  FaceSignals ordinary=calibrated();
  for(long start=2000;start<5000;start+=500){for(int i=0;i<3;i++)require(sample(ordinary,FaceSignals.FACE_SQUEEZE,start+i*50,.95f,.02f,.8f)==-1,"Even a strong short blink cannot clear squeeze");for(int i=0;i<3;i++)sample(ordinary,FaceSignals.FACE_SQUEEZE,start+200+i*100,.02f,.12f,.02f);}
  FaceSignals shut=calibrated();for(long t=2000;t<3000;t+=80)require(sample(shut,FaceSignals.FACE_SQUEEZE,t,.95f,.02f,.05f)==-1,"Eye closure without facial scrunch is not face squeeze");
  require(FaceSignals.squeezeScore(.05f,.9f,.9f,.9f,.9f)==.05f,"Wrinkling without both eyes closing cannot squeeze");
  FaceSignals mouth=calibrated();for(long t=2000;t<3000;t+=80)require(mouth.accept(.8f,.05f,0,0,.9f,0,t,1,FaceSignals.WIDE_EYES)==-1,"Open-mouth surprise cannot replace the relaxed-mouth wide-eyes cue");
  for(int gate:new int[]{3,4}){
   MiniGame g=new MiniGame(0);g.countdown=0;g.score=gate;g.faceMode=g.faceReady=true;
   require(g.target()==gate,"Stage 1 contains both new gates");FaceGameInput.send(g,FaceSignals.BLINK);require(!g.gateMatched,"A blink must never be routed to a new gate");
   FaceGameInput.send(g,gate==3?FaceSignals.FACE_SQUEEZE:FaceSignals.WIDE_EYES);require(!g.gateMatched,"Wrong new expression cannot pass");
   FaceGameInput.send(g,gate==3?FaceSignals.WIDE_EYES:FaceSignals.FACE_SQUEEZE);require(g.gateMatched,"Correct expression clears its gate");
  }
  MiniGame garden=new MiniGame(2);garden.countdown=0;garden.score=1;garden.faceMode=garden.faceReady=true;FaceGameInput.send(garden,FaceSignals.FACE_SQUEEZE);require(garden.score==1,"New squeeze does not substitute for blink in other games");FaceGameInput.send(garden,FaceSignals.BLINK);require(garden.score==2,"Existing garden blink remains mapped");
  System.out.println("PASS: wide/squeeze dwell at four cadences, one-shot/rearm/loss, normal blink and eye-closure negatives, distinct gate routing and existing garden blink isolation");
 }
}
