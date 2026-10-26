package com.facechaos.mobile;
public final class HeadSignalsCheck {
 static void require(boolean b,String m){if(!b)throw new AssertionError(m);}
 static HeadSignals calibrated(){HeadSignals h=new HeadSignals();for(int i=0;i<12;i++)h.accept(.5f,.3f);require(h.ready&&h.lane==0,"Independent centred-head calibration");return h;}
 public static void main(String[] args){
  HeadSignals h=calibrated();h.accept(.42f,.3f);require(h.lane==-1,"Head left selects left lane");h.accept(.46f,.3f);require(h.lane==-1,"Hysteresis prevents jitter changing lanes");h.accept(.5f,.3f);require(h.lane==0,"Return to centre selects middle lane");h.accept(.58f,.3f);require(h.lane==1,"Head right selects right lane");
  h.lost();require(!h.ready&&h.lane==0,"Lost head resets readiness");for(int i=0;i<12;i++)h.accept(.51f,.3f);require(h.ready,"Tracking regain recalibrates centred head");
  HeadSignals edge=new HeadSignals();for(int i=0;i<30;i++)edge.accept(.85f,.3f);require(!edge.ready,"Head at selfie edge cannot become neutral centre");edge.accept(Float.NaN,.3f);require(!edge.ready,"Invalid landmark values rejected");
  HeadSignals normal=calibrated(),sensitive=calibrated();normal.accept(.55f,.3f,1);sensitive.accept(.55f,.3f,.7f);require(normal.lane==0&&sensitive.lane==1,"Head sensitivity is adjustable");
  for(int stage=1;stage<=5;stage++){
   MiniGame g=new MiniGame(13,99,stage);g.countdown=0;g.faceMode=true;g.faceReady=true;HeadSignals controller=calibrated();
   for(int i=0;i<15000&&!g.finished;i++){if(i%8==0){int safe=g.racing.safeLane();controller.accept(.5f+safe*.08f,.3f);g.headLane=controller.lane;}g.step(.01f);}require(g.won,"80ms head input cadence can clear stage "+stage);
  }
  System.out.println("PASS: head left/centre/right, jitter hysteresis, calibration/loss/recovery, sensitivity and all five stages at 80ms steering cadence");
 }
}
