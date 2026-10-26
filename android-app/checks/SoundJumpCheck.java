package com.facechaos.mobile;
/** Synthetic volume-to-motion integration; does not access a microphone. */
public final class SoundJumpCheck {
 static void require(boolean b,String m){if(!b)throw new AssertionError(m);}
 static float strength(int amplitude){SoundGate gate=new SoundGate();short[] pcm=new short[800];for(int i=0;i<20;i++)gate.feed(pcm,pcm.length);java.util.Arrays.fill(pcm,(short)amplitude);for(int i=0;i<5;i++)gate.feed(pcm,pcm.length);return gate.strength;}
 static MiniGame attempt(int encounter,float strength){MiniGame g=new MiniGame(4);g.countdown=0;g.encounter=encounter;g.phase=2.2f;g.jump(strength);while(g.phase<2.7f)g.step(.01f);return g;}
 public static void main(String[] args){
  MiniGame calibrating=new MiniGame(4);calibrating.microphoneMode=true;for(int i=0;i<500;i++)calibrating.step(.02f);require(calibrating.countdown==2&&calibrating.phase==0&&calibrating.hearts==3,"Melody calibration freezes countdown and lives");calibrating.acceptSoundLevel(0,0,false,true);calibrating.step(.02f);require(calibrating.countdown<2,"Melody resumes after microphone calibration");
  float soft=strength(500),strong=strength(3000);require(soft>0&&strong>soft+.4f,"PCM volume preserves soft/strong separation");
  MiniGame low=new MiniGame(4),high=new MiniGame(4);low.countdown=high.countdown=0;low.phase=high.phase=2.2f;low.jump(soft);high.jump(strong);float lp=0,hp=0;for(int i=0;i<100;i++){low.step(.01f);high.step(.01f);lp=Math.max(lp,low.jumpHeight);hp=Math.max(hp,high.jumpHeight);}require(hp>lp+30,"Stronger sound produces visibly higher actual trajectory");
  require(attempt(1,soft).score==0,"Soft sound cannot clear tall obstacle");require(attempt(1,strong).score==1,"Strong sound clears tall obstacle");require(attempt(0,.35f).score==1,"Gentle jump clears low obstacle");
  for(int mode:new int[]{1,4})for(int stage=1;stage<=5;stage++){
   MiniGame g=new MiniGame(mode,0,stage);g.countdown=0;g.microphoneMode=true;g.micReady=true;
   for(int i=0;i<6000&&!g.finished;i++){
    if(g.soundJump()){boolean active=g.phase>=g.windowOpens()+.02f&&g.phase<g.windowCloses()+.01f;float launch=g.windowOpens()+.02f+.15f*g.pace(),t=2.65f-launch,h=MelodyCourse.height(stage,g.encounter)+5;
    g.soundStrength=mode==4?Math.max(0,Math.min(1,((h+500*t*t)*(h+500*t*t)/(2000*t*t)-18)/82)):g.highJump()?.85f:.35f;g.voiceSignal(active,true);}
    else if(g.phase>=1.6f&&!g.resolved)g.input(g.target());g.step(.01f);
   }require(g.won&&g.coins==8,"Live-signal playthrough mode "+mode+" stage "+stage);
  }
  MiniGame held=new MiniGame(4);held.countdown=0;held.microphoneMode=true;held.micReady=true;held.soundStrength=1;for(int i=0;i<320;i++){held.voiceSignal(true,true);held.step(.01f);}require(!held.jumpUsed&&held.score==0,"Held background sound cannot trigger without quiet rearm");
  MiniGame touch=new MiniGame(4);touch.countdown=0;touch.phase=2.2f;touch.microphoneMode=true;touch.micReady=true;touch.input(0);require(!touch.jumpUsed,"Live jump cannot be bypassed by tapping");
  touch.voiceSignal(false,true);touch.voiceSignal(true,true);touch.paused=true;for(int i=0;i<30;i++)touch.step(.01f);require(!touch.jumpUsed,"Paused sampling cannot launch");
  System.out.println("PASS: PCM volume separation, trajectory heights, real obstacle clearance, ten microphone-stage playthroughs, quiet rearm, pause and live touch rejection");
 }
}
