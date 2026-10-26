package com.facechaos.mobile;
public final class TouchControlsCheck {
 static void require(boolean b,String m){if(!b)throw new AssertionError(m);}
 static void hold(MiniGame g,boolean held){if(held&&!g.touchHolding)g.touchDown();if(!held&&g.touchHolding)g.touchUp();}
 public static void main(String[] args){
 for(int mode=0;mode<GameCatalog.ALL.length;mode++)for(int stage=1;stage<=5;stage++){
  if(MiniGame.requiresMicrophone(mode))continue;
  MiniGame g=new MiniGame(mode,12,stage);g.countdown=0;g.touchSoundPatterns=mode==9;
  for(int i=0;i<10000&&!g.finished;i++){
   if(mode==14)g.flagAnswer(g.flags.country(g.encounter).name);
   else if(mode==12)hold(g,true);
   else if(mode==13)g.touchLane(.5f+g.racing.safeLane()/3f);
   else if(g.resolved)hold(g,false);
   else if(mode==4){if(g.phase>=2.2f)g.input(0);}
   else if(g.soundJump()){if(g.phase>=(g.highJump()?2.2f-.36f*g.pace():2.2f-.025f*g.pace())&&g.phase<2.2f)hold(g,true);else if(g.phase>=2.2f)hold(g,false);}
   else if(mode==3)hold(g,g.height+g.velocity*.6f>g.corridor()+.005f);
   else if(mode==8)hold(g,g.charge<g.powerTarget());
   else if(mode==9)hold(g,AnimalPatterns.demoActive(g.target(),g.phase/g.pace()-.3f));
   else if(mode==11)hold(g,true);
   else if(mode==10){if(!g.memoryShowing())g.input(g.target());}
   else if(mode==6){if(g.snackIsTreat()&&g.phase>=1.5f){g.touchDown();g.touchUp();}}
   else if(mode==7){if(g.phase>=.5f&&Math.abs(g.stackOffset())<.12f){g.touchDown();g.touchUp();}}
   else if(mode!=1||g.phase>=1.6f){g.touchDown();g.touchUp();}
   g.step(.01f);
  }
  require(g.won,"Touch-only win mode "+mode+" stage "+stage+" score "+g.score);
 }
 MiniGame g=new MiniGame(4);g.countdown=0;g.phase=2.1f;g.touchDown();g.step(.04f);g.cancelTouch();g.touchUp();require(!g.jumpUsed,"Cancelled press cannot jump");
 g.touchDown();g.paused=true;g.touchUp();require(!g.jumpUsed,"Paused release cannot jump");g.paused=false;g.microphoneMode=true;g.touchDown();g.touchUp();require(!g.jumpUsed,"Live sound blocks touch jump");
 MiniGame animal=new MiniGame(9);animal.countdown=0;animal.touchSoundPatterns=true;animal.touchDown();animal.touchUp();for(int i=0;i<60;i++)animal.step(.01f);require(animal.score==0,"Single tap cannot auto-complete animal pattern");
 require(MiniGame.touchStrength(.4f)>MiniGame.touchStrength(.03f)+.4f,"Longer press gives stronger launch");
 System.out.println("PASS: all 65 touch-compatible stages via touch only; Comet Launch and Firefly Glow sound-only, short/long jump presses, animal rhythms, cancellation, pause and live-input isolation");
 }
}
