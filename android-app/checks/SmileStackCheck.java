package com.facechaos.mobile;
public final class SmileStackCheck {
 static void require(boolean b,String m){if(!b)throw new AssertionError(m);}
 public static void main(String[] args){
  for(int stage=1;stage<=5;stage++){
   MiniGame g=new MiniGame(7,0,stage);g.countdown=0;g.faceMode=true;g.faceReady=true;
   FaceSignals signals=new FaceSignals();long time=0;
   for(int i=0;i<15;i++){signals.accept(.03f,.05f,.02f,0,time,1);time+=60;}
   for(int tick=0;tick<30000&&!g.finished;tick++){
    // Actual 60 ms detector cadence, stable smile dwell and neutral rearm.
    if(tick%6==0){float smile=!g.resolved&&g.phase>.5f&&Math.abs(g.stackOffset())<.17f?.8f:.05f;int event=signals.accept(.03f,smile,.02f,0,time,1);time+=60;if(event>=0)FaceGameInput.send(g,event);}
    int oldCoins=g.coins;g.step(.01f);require(g.coins-oldCoins<=1,"No duplicate landing reward");
   }
   require(g.won&&g.score==8&&g.coins==8,"Detector cadence can complete stage "+stage+" score "+g.score);
  }
  MiniGame g=new MiniGame(7);g.countdown=0;g.phase=(float)Math.PI/g.stackSpeed();g.input(0);float offset=g.stackDropOffset;
  require(g.resolved&&g.coins==0,"Smile starts drop, no early coin");g.input(0);g.paused=true;g.step(.05f);require(g.flightTime==0,"Pause freezes drop");g.paused=false;
  for(int i=0;i<20;i++)g.step(.02f);require(g.coins==1&&g.score==1&&g.stackOffsets[0]==offset,"Landing preserves exact horizontal position");
  for(int i=0;i<20;i++){g.input(0);g.step(.02f);}require(g.coins==1,"Held input cannot duplicate reward");
  MiniGame miss=new MiniGame(7);miss.countdown=0;miss.phase=.7f;miss.input(0);for(int i=0;i<20;i++)miss.step(.02f);require(miss.hearts==2&&miss.coins==0,"Off-centre landing costs one life");
  System.out.println("PASS: five full smile-detector cadence stages, exact landing positions, delayed coins, pause and misses");
 }
}
