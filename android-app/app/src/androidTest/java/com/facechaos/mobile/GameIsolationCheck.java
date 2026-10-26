package com.facechaos.mobile;

import android.app.Instrumentation;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.Switch;

/** Session lifecycle regression; synthetic progress is never written to player records. */
final class GameIsolationCheck {
 static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
 static Object field(Object owner,String name){return RecordingPlaybackCheck.field(owner,name);}
 static void set(Object owner,String name,Object value){try{java.lang.reflect.Field f=owner.getClass().getDeclaredField(name);f.setAccessible(true);f.set(owner,value);}catch(Exception e){throw new RuntimeException(e);}}
 static void select(MainActivity activity,int mode){try{java.lang.reflect.Method m=MainActivity.class.getDeclaredMethod("previewGame",int.class);m.setAccessible(true);m.invoke(activity,mode);}catch(Exception e){throw new RuntimeException(e);}}
 static void answer(Instrumentation check,MainActivity activity,int button){
  check.runOnMainSync(()->{android.app.AlertDialog dialog=(android.app.AlertDialog)field(activity,"switchConfirmation");require(dialog!=null&&dialog.isShowing(),"Switching must show the close-game question");dialog.getButton(button).performClick();});check.waitForIdleSync();
 }
 static void fresh(MiniGame game,int mode){
  require(game.mode==mode&&game.stage==1,"Selected game must start at Stage 1");
  require(game.score==0&&game.coins==0&&game.runCoins()==0&&game.hearts==3,"Run score, coins and hearts must reset for game "+mode);
  require(game.phase==0&&game.elapsed==0&&game.countdown==2,"Progress and countdown must reset for game "+mode);
  require(!game.touchHolding&&!game.voiceActive&&!game.jumpUsed&&!game.jumpSampling&&game.soundPulses==0,"Input state must reset for game "+mode);
  require(!game.finished&&!game.resolved&&!game.paused&&!game.faceReady&&!game.micReady,"New game lifecycle/calibration must reset for game "+mode);
 }
 static String run(Instrumentation check) throws Exception {
  MainActivity activity=(MainActivity)check.startActivitySync(new Intent(check.getTargetContext(),MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
  check.waitForIdleSync();Thread.sleep(1800);
  GameOverlay overlay=(GameOverlay)field(activity,"overlay");Switch recording=(Switch)field(activity,"recordSwitch");
  boolean originalRecording=recording.isChecked(),originalFace=(Boolean)field(activity,"faceChoice");
  try{
   check.runOnMainSync(()->{
    overlay.setPersistenceEnabled(false);recording.setChecked(false);set(activity,"faceChoice",false);
    // Do not open 225 hardware input sessions. Exercise the real activity transition
    // with acquisition suspended; recording is verified separately below.
    set(activity,"resumed",false);
    for(int from=0;from<15;from++)for(int to=0;to<15;to++){
     select(activity,from);MiniGame old=overlay.game;
     old.score=9;old.coins=4;old.coinsBeforeStage=7;old.hearts=1;old.countdown=0;old.phase=1.7f;old.elapsed=30;
     old.touchHolding=true;old.voiceActive=true;old.jumpSampling=true;old.jumpUsed=true;old.soundPulses=3;
     int previous=(Integer)field(activity,"gameGeneration");
     set(activity,"permissionGeneration",previous);
     select(activity,to);require(overlay.game!=old,"Switch must replace the previous session");fresh(overlay.game,to);
     require(old.paused&&old.finished,"Previous game must be stopped, not hidden and running");
     float oldPhase=old.phase;old.step(.05f);require(old.phase==oldPhase,"Old game cannot keep running");
     boolean quiz=(Boolean)field(activity,"quizVoice");
     activity.onRequestPermissionsResult(13,new String[]{android.Manifest.permission.RECORD_AUDIO},new int[]{quiz?PackageManager.PERMISSION_DENIED:PackageManager.PERMISSION_GRANTED});
     require((Boolean)field(activity,"quizVoice")==quiz,"Old permission callback must not alter the new game");
    }
    select(activity,7);set(activity,"resumed",true);
   });
   // The matrix intentionally occupies the UI thread. Let camera frame callbacks
   // run again before the separate live-recording scenario checks freshness.
   Thread.sleep(1200);
   MiniGame[] continuing={overlay.game};
   check.runOnMainSync(()->{RecordingPlaybackCheck.call(activity,"showGames");require(overlay.game==continuing[0]&&overlay.game.paused&&!overlay.game.finished,"Prompt must pause and preserve the current game");});
   answer(check,activity,android.app.AlertDialog.BUTTON_NEGATIVE);
   check.runOnMainSync(()->{require(overlay.game==continuing[0]&&!overlay.game.paused&&!overlay.game.finished,"No must resume the same game");RecordingPlaybackCheck.call(activity,"primaryCaptureAction");RecordingPlaybackCheck.call(activity,"showGames");});
   answer(check,activity,android.app.AlertDialog.BUTTON_NEGATIVE);
   check.runOnMainSync(()->{require(overlay.game==continuing[0]&&overlay.game.paused,"No must preserve an already-paused game");RecordingPlaybackCheck.call(activity,"primaryCaptureAction");RecordingPlaybackCheck.call(activity,"showGames");((android.app.AlertDialog)field(activity,"switchConfirmation")).cancel();});
   check.waitForIdleSync();
   check.runOnMainSync(()->require(overlay.game==continuing[0]&&!overlay.game.paused,"Back/dismiss must act as No"));
   check.runOnMainSync(()->{recording.setChecked(true);select(activity,7);});
   Thread.sleep(1800);
   Object[] oldEncoder={null};MiniGame[] oldGame={null};
   check.runOnMainSync(()->{
    oldEncoder[0]=field(activity,"encoder");oldGame[0]=overlay.game;
    require(oldEncoder[0]!=null&&!(Boolean)field(activity,"preparingVideo"),"Initial recording must start");
    select(activity,2);select(activity,6);select(activity,7);
    require(oldGame[0].paused&&oldGame[0].finished,"Switching must stop the recording game's session immediately");
   });
   Thread.sleep(2300);
   check.runOnMainSync(()->{
    require(overlay.game.mode==7&&overlay.game!=oldGame[0],"Latest selection must win after recording finalizes");
    require(overlay.game.stage==1&&overlay.game.runCoins()==0&&!overlay.game.paused&&!overlay.game.finished,"Replacement game must start fresh and move");
    require(field(activity,"encoder")!=null&&field(activity,"encoder")!=oldEncoder[0]&&!(Boolean)field(activity,"preparingVideo"),"New game must own a new recorder");
    require(overlay.game.countdown<2,"Recording switch must not leave the new game frozen");
    oldGame[0]=overlay.game;((android.widget.Button)field(activity,"gamesButton")).performClick();
    require(overlay.game==oldGame[0]&&!overlay.game.finished,"Game must not close until Yes");
   });
   answer(check,activity,android.app.AlertDialog.BUTTON_POSITIVE);
   long deadline=android.os.SystemClock.uptimeMillis()+15000;
   while(field(activity,"encoder")!=null&&android.os.SystemClock.uptimeMillis()<deadline)Thread.sleep(100);
   check.waitForIdleSync();
   check.runOnMainSync(()->{require(field(activity,"encoder")==null,"Yes must finalize the old recording");require(oldGame[0].finished&&oldGame[0].stopped&&(Boolean)field(activity,"awaitingStart"),"Yes must close the old session before opening the picker");});
   RecordingPlaybackCheck.click(check,GameCatalog.ALL[6].name);RecordingPlaybackCheck.click(check,"Let's play!");Thread.sleep(1800);
   check.runOnMainSync(()->{require(overlay.game.mode==6&&overlay.game.stage==1&&overlay.game.runCoins()==0&&overlay.game!=oldGame[0]&&!overlay.game.paused,"New selection after Yes must start a fresh game");RecordingPlaybackCheck.call(activity,"stopOrReview");});
   deadline=android.os.SystemClock.uptimeMillis()+15000;
   while(field(activity,"encoder")!=null&&android.os.SystemClock.uptimeMillis()<deadline)Thread.sleep(100);
   require(field(activity,"encoder")==null,"Final test clip must finish");
   return "PASS: all 225 transitions isolated; No resumes same game, paused No stays paused, dismiss acts as No, Yes closes old game and recording before selecting a fresh game; rapid recording switches and stale permission results protected; player records preserved";
  }finally{check.runOnMainSync(()->{set(activity,"resumed",true);set(activity,"faceChoice",originalFace);recording.setChecked(originalRecording);overlay.setPersistenceEnabled(true);});}
 }
}
