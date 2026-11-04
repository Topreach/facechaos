package com.facechaos.mobile;

import android.app.Instrumentation;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import java.io.File;
import java.io.FileOutputStream;

/** Real native failure scenes, pixel stability and result audio selection, no camera/microphone. */
final class GameFailureDeviceCheck {
 static void require(boolean ok,String message){if(!ok)throw new AssertionError(message);}
 static String run(Instrumentation test){
  test.runOnMainSync(()->{
   for(int mode=0;mode<15;mode++)for(int stage=1;stage<=5;stage++){
    GameOverlay overlay=new GameOverlay(test.getTargetContext());overlay.setPersistenceEnabled(false);overlay.select(mode,stage);overlay.setSafeTop(30);overlay.setBottomReserved(210);MiniGame game=overlay.game;game.countdown=0;if(MiniGame.requiresMicrophone(mode))game.acceptSoundLevel(0,0,false,true);
    SoundEvents sounds=new SoundEvents();sounds.observe(game,true);
    for(int tick=0;tick<10000&&!game.finished;tick++)game.step(.05f);
    require(game.finished&&!game.won&&game.resultAction().equals("Try again"),"Idle round must reach mission failure: "+mode+"/"+stage);
    require("fail".equals(sounds.observe(game,true))&&sounds.observe(game,true)==null,"Failure jingle must be selected exactly once");
    require(GameLanguage.action(test.getTargetContext(),game).equals(test.getTargetContext().getString(R.string.fc_failed)),"Wrong localized result title");
    overlay.measure(View.MeasureSpec.makeMeasureSpec(720,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(1560,View.MeasureSpec.EXACTLY));overlay.layout(0,0,720,1560);
    Bitmap first=Bitmap.createBitmap(720,1560,Bitmap.Config.ARGB_8888),second=Bitmap.createBitmap(720,1560,Bitmap.Config.ARGB_8888);
    try{
     overlay.draw(new Canvas(first));for(int tick=0;tick<100;tick++){overlay.input(0);game.step(.05f);game.voiceSignal(true,true);}overlay.draw(new Canvas(second));require(first.sameAs(second),"Failed scene continues moving: "+mode+"/"+stage);
     if(stage==1){File folder=test.getTargetContext().getExternalFilesDir(null);if(folder!=null)try(FileOutputStream out=new FileOutputStream(new File(folder,"mission-failed-"+mode+".png"))){first.compress(Bitmap.CompressFormat.PNG,100,out);}}
    }catch(java.io.IOException error){throw new RuntimeException(error);}finally{first.recycle();second.recycle();}
   }
  });
  return "PASS: 75 native mission-failed scenes, pixel-identical frozen results after steps/inputs/sound, localized failure titles and one failure sound event per round; no camera/mic/player data";
 }
}
