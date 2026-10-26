package com.facechaos.mobile;

/** Routes independently detected expressions to each game's actual action IDs. */
final class FaceGameInput {
 static int expected(MiniGame game){
  switch(game.mode){
   case 0:return game.target()==3?FaceSignals.WIDE_EYES:game.target()==4?FaceSignals.FACE_SQUEEZE:game.target();
   case 10:return game.target();
   case 1:return game.target()==1?FaceSignals.SMILE:game.target()==2?FaceSignals.TEETH:FaceSignals.NONE;
   case 2:return game.target()==0?FaceSignals.SMILE:FaceSignals.BLINK;
   case 5:return game.target()==0?FaceSignals.SMILE:FaceSignals.NONE;
   case 6:return FaceSignals.MOUTH;
   case 7:return FaceSignals.SMILE;
   default:return FaceSignals.NONE;
  }
 }
 static void send(MiniGame game,int expression){
  if(!game.faceMode||!game.faceReady||game.paused||game.finished||game.countdown>0)return;
  switch(game.mode){
   case 0:if(expression>=0&&expression<=2)game.input(expression);else if(expression==FaceSignals.WIDE_EYES)game.input(3);else if(expression==FaceSignals.FACE_SQUEEZE)game.input(4);break;
   case 10:if(expression>=0&&expression<=2)game.input(expression);break;
   case 1:if(game.target()==1&&expression==1)game.input(1);else if(game.target()==2&&expression==2)game.input(2);break;
   case 2:if(expression==1)game.input(0);else if(expression==3)game.input(1);break;
   case 5:if(expression==1&&game.target()==0)game.input(0);break;
   case 6:if(expression==0)game.input(0);break;
   case 7:if(expression==1)game.input(0);break;
   default:break;
  }
 }
}
