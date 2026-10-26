package com.facechaos.mobile;

/** Campaign and audio-event checks without actual audio playback. */
public final class StagesSoundCheck {
 private static void require(boolean b,String m){if(!b)throw new AssertionError(m);}
 public static void main(String[] args){
  for(int mode=0;mode<GameCatalog.ALL.length;mode++)for(int stage=1;stage<=MiniGame.STAGE_COUNT;stage++){
   MiniGame game=new MiniGame(mode,17,stage);game.countdown=0;
   for(int i=0;i<7000&&!game.finished;i++){
    if(mode==14){game.flagAnswer(game.flags.country(game.encounter).name);}
                else if(mode==9){game.touchSoundPatterns=true;boolean held=AnimalPatterns.demoActive(game.target(),game.phase/game.pace()-.3f);if(held&&!game.touchHolding)game.touchDown();else if(!held&&game.touchHolding)game.touchUp();}
                else if(mode==11){game.acceptSoundLevel(.9f,.5f,true,true);}
                else if(mode==12){if(!game.touchHolding)game.touchDown();}
                else if(mode==13)game.input(game.racing.safeLane()+1);
                else if(mode==6){if(game.snackIsTreat()&&game.phase>=1.5f&&!game.resolved)game.input(0);}
    else if(mode==7){if(game.phase>=.5f&&Math.abs(game.stackOffset())<.12f&&!game.resolved)game.input(0);}
    else if(mode==8){game.acceptSoundLevel(.5f,.5f,!game.resolved&&game.charge<game.powerTarget(),true);}
    else if(mode==3){game.voiceActive=game.height+game.velocity*.6f>game.corridor()+.005f;}
    else if(mode==1||mode==4){if(game.phase>=(game.soundJump()?2.2f:1.6f)&&!game.resolved)game.input(game.target());}
    else game.input(game.target());
    game.step(.02f);
   }
   require(game.coins>0&&game.coinBalance==game.coins,"Every game awards real coins: "+mode+" stage "+stage);
   require(game.finished&&game.won,"Win mode "+mode+" stage "+stage+" score "+game.score);
   require(game.resultAction().equals(stage<5?"Next stage":"Play again"),"Stage next/replay action");
   require(game.resultTitle().equals(stage<5?"Stage "+stage+" complete!":"All stages complete!"),"Stage completion title");
  }
  require(new MiniGame(10,0,5).memoryLength()==4,"Later memory stage starts with four faces");
  SoundEvents sounds=new SoundEvents();MiniGame game=new MiniGame(1);
  require("start".equals(sounds.observe(game,true)),"Start cue");require(sounds.observe(game,true)==null,"No duplicate start cue");
  game.countdown=1;require("countdown".equals(sounds.observe(game,true)),"Countdown cue");game.countdown=0;require("go".equals(sounds.observe(game,true)),"Go cue");
  game.score=1;game.coins=1;require("coin".equals(sounds.observe(game,true)),"Coin priority");require(sounds.observe(game,true)==null,"No duplicate coin cue");
  game.hearts--;require("miss".equals(sounds.observe(game,true)),"Miss cue");game.finished=true;game.won=true;require("stage".equals(sounds.observe(game,true)),"Stage fanfare");
  game.microphoneMode=true;game.finished=false;require(!SoundEvents.effectsAllowed(game)&&!SoundEvents.musicAllowed(game,true),"Speaker audio suppressed during microphone round");game.finished=true;require(SoundEvents.effectsAllowed(game),"Result jingle allowed after listening ends");
  System.out.println("PASS: all 75 game/stage winning paths, stage labels, longer memory, unique sound cues and microphone feedback policy");
 }
}
