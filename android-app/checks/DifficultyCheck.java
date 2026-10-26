package com.facechaos.mobile;
public final class DifficultyCheck {
 static void require(boolean b,String m){if(!b)throw new AssertionError(m);}
 public static void main(String[] args){for(int stage=2;stage<=5;stage++){
  MiniGame prev=new MiniGame(0,0,stage-1),next=new MiniGame(0,0,stage);require(next.gateApproach()<prev.gateApproach()&&next.goal()>prev.goal(),"Gates faster and longer");
  for(int mode:new int[]{1,4}){prev=new MiniGame(mode,0,stage-1);next=new MiniGame(mode,0,stage);prev.microphoneMode=true;next.microphoneMode=true;require(next.windowCloses()-next.windowOpens()<prev.windowCloses()-prev.windowOpens(),"Jump timing narrows "+mode);require(mode==4?next.jumpRequired()>prev.jumpRequired():next.jumpRequired()==prev.jumpRequired(),"Melody power grows gradually; other expressions stay comfortable");}
  prev=new MiniGame(3,0,stage-1);next=new MiniGame(3,0,stage);require(next.voiceApproach()<prev.voiceApproach()&&next.voiceTolerance()<prev.voiceTolerance(),"Flight timing and precision advance");
  prev=new MiniGame(6,0,stage-1);next=new MiniGame(6,0,stage);require(next.snackWindowCloses()-next.snackWindowOpens()<prev.snackWindowCloses()-prev.snackWindowOpens(),"Treat timing narrows");
  for(int mode:new int[]{2,5,9})require(new MiniGame(mode,0,stage).cueTimeout()<new MiniGame(mode,0,stage-1).cueTimeout(),"Cue deadlines shorten "+mode);
  prev=new MiniGame(7,0,stage-1);next=new MiniGame(7,0,stage);require(next.stackTolerance()<prev.stackTolerance()&&next.stackSpeed()>prev.stackSpeed(),"Stack precision advances");
  prev=new MiniGame(8,0,stage-1);next=new MiniGame(8,0,stage);require(next.powerTolerance()<prev.powerTolerance(),"Rocket precision advances");
  prev=new MiniGame(11,0,stage-1);next=new MiniGame(11,0,stage);require(next.glowRequired()>prev.glowRequired()&&next.glowHigh()-next.glowLow()<prev.glowHigh()-prev.glowLow(),"Glow steadiness advances");
 }require(new MiniGame(10,0,5).memoryLength()>new MiniGame(10,0,1).memoryLength(),"Memory sequences grow");System.out.println("PASS: staged timing, speed, precision, sequence length and steady duration without increasing expression thresholds");}
}
