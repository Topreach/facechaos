package com.facechaos.mobile;
public final class RunProgressCheck {
 static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
 public static void main(String[] args){
  for(int mode=0;mode<15;mode++)for(int stage=1;stage<=5;stage++){
   MiniGame g=new MiniGame(mode,7,stage);g.coinsBeforeStage=(stage-1)*4;g.coins=3;g.finished=true;
   require(g.nextRunStage()==1&&g.coinsForNextStage()==0,"Failure restarts every game/stage with no carried coins");
   g.won=true;require(g.nextRunStage()==(stage<5?stage+1:1),"Only a win advances");
   require(g.coinsForNextStage()==(stage<5?g.runCoins():0),"Coins continue only within the same run");
   g.won=false;g.stopped=true;require(g.nextRunStage()==1&&g.coinsForNextStage()==0,"Stopped round starts a new run");
  }
  MiniGame gate=new MiniGame(0);gate.countdown=0;gate.input(gate.target());
  for(int i=0;i<500&&gate.score==0;i++)gate.step(.02f);
  require(gate.score==1&&gate.coins==1,"Passed emoji gate earns one coin");
  MiniGame flower=new MiniGame(2);flower.countdown=0;flower.input(0);flower.flash=0;flower.input(1);
  require(flower.coins==2&&flower.coinBalance==2,"Garden actions earn genuine run coins");
  System.out.println("PASS: 75 game/stage restart/advance/carry decisions; stop/finale resets; gate and garden coin rewards");
 }
}
