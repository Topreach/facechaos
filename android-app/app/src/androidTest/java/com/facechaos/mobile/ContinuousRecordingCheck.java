package com.facechaos.mobile;

import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.MediaStore;
import java.io.*;
import java.util.Arrays;

/** Actual camera/game session and Gallery export, isolated to the verification app. */
final class ContinuousRecordingCheck {
 static void require(boolean ok,String message){if(!ok)throw new AssertionError(message);}
 static Object field(Object owner,String name){return RecordingPlaybackCheck.field(owner,name);}
 static void call(MainActivity activity,String name){RecordingPlaybackCheck.call(activity,name);}
 static byte[] hash(InputStream input)throws Exception{try(InputStream in=input){java.security.MessageDigest digest=java.security.MessageDigest.getInstance("SHA-256");byte[] buffer=new byte[65536];int n;while((n=in.read(buffer))!=-1)digest.update(buffer,0,n);return digest.digest();}}
 static String run(Instrumentation check)throws Exception{
  MainActivity activity=(MainActivity)check.startActivitySync(new Intent(check.getTargetContext(),MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));check.waitForIdleSync();Thread.sleep(1600);
  GameOverlay overlay=(GameOverlay)field(activity,"overlay");android.widget.Switch recording=(android.widget.Switch)field(activity,"recordSwitch");boolean originalRecording=recording.isChecked(),originalFace=(Boolean)field(activity,"faceChoice");File original=(File)field(activity,"lastClip");File[] recorded={null};Uri exported=null;
  try{
   check.runOnMainSync(()->{overlay.setPersistenceEnabled(false);GameIsolationCheck.set(activity,"faceChoice",false);recording.setChecked(true);GameIsolationCheck.select(activity,7);});Thread.sleep(2200);check.waitForIdleSync();
   Object owner=field(activity,"encoder");require(owner!=null&&!(Boolean)field(activity,"preparingVideo"),"Recording must start");
   check.runOnMainSync(()->{overlay.game.hearts=99;overlay.game.countdown=0;});Thread.sleep(3500);
   check.runOnMainSync(()->{require(field(activity,"encoder")==owner,"Recording stopped early");overlay.game.finished=true;overlay.game.won=false;overlay.game.stopped=false;overlay.invalidate();});
   Thread.sleep(1800);
   check.runOnMainSync(()->{require(field(activity,"encoder")==owner,"Game over must not cut the recording after one second");MiniGame old=overlay.game;call(activity,"primaryCaptureAction");require(overlay.game!=old&&overlay.game.stage==1&&field(activity,"encoder")==owner&&!overlay.game.paused,"Retry must reuse the same recording");overlay.game.hearts=99;});
   Thread.sleep(3200);
   check.runOnMainSync(()->{overlay.game.finished=true;overlay.game.won=true;overlay.game.stopped=false;overlay.invalidate();});Thread.sleep(300);
   check.runOnMainSync(()->{call(activity,"primaryCaptureAction");require(overlay.game.stage==2&&field(activity,"encoder")==owner&&!overlay.game.paused,"Next stage must reuse the same recording");overlay.game.hearts=99;});Thread.sleep(3200);
   check.runOnMainSync(()->call(activity,"primaryCaptureAction"));Thread.sleep(1300);check.runOnMainSync(()->call(activity,"primaryCaptureAction"));Thread.sleep(2300);
   long[] expected={0};check.runOnMainSync(()->{require(field(activity,"encoder")==owner,"Pause/resume must keep the recording");expected[0]=SystemClock.elapsedRealtime()-(Long)field(activity,"recordingStart")-(Long)field(activity,"pausedTime");call(activity,"stopOrReview");});
   long deadline=SystemClock.uptimeMillis()+15000;while(field(activity,"encoder")!=null&&SystemClock.uptimeMillis()<deadline)Thread.sleep(100);check.waitForIdleSync();require(field(activity,"encoder")==null,"Stop must finalize the recording");recorded[0]=(File)field(activity,"lastClip");require(recorded[0]!=null&&recorded[0]!=original,"A new complete clip must be selected");
   long duration=ClipDetails.duration(recorded[0]);require(duration>12000&&Math.abs(duration-expected[0])<1800,"Saved duration must match the full session: actual="+duration+" expected="+expected[0]);
   MediaMetadataRetriever reader=new MediaMetadataRetriever();try{reader.setDataSource(recorded[0].getAbsolutePath());Bitmap tail=reader.getFrameAtTime((duration-300)*1000,MediaMetadataRetriever.OPTION_CLOSEST);require(tail!=null,"End of saved session must decode");tail.recycle();}finally{reader.release();}
   check.runOnMainSync(()->{call(activity,"primaryCaptureAction");require(field(activity,"encoder")==null&&field(activity,"lastClip")==recorded[0],"Retry after Stop must not replace the full clip with a new short take");call(activity,"stopOrReview");try{java.lang.reflect.Method save=MainActivity.class.getDeclaredMethod("saveClip",File.class);save.setAccessible(true);save.invoke(activity,recorded[0]);}catch(Exception error){throw new RuntimeException(error);}});
   deadline=SystemClock.uptimeMillis()+15000;
   while(exported==null&&SystemClock.uptimeMillis()<deadline){
    try(android.database.Cursor cursor=activity.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,new String[]{MediaStore.Video.Media._ID,MediaStore.Video.Media.IS_PENDING},MediaStore.Video.Media.DISPLAY_NAME+"=?",new String[]{recorded[0].getName()},null)){
     if(cursor!=null&&cursor.moveToFirst()&&cursor.getInt(1)==0)exported=android.content.ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,cursor.getLong(0));
    }if(exported==null)Thread.sleep(100);
   }
   require(exported!=null,"Save to Gallery must finish");require(Arrays.equals(hash(new FileInputStream(recorded[0])),hash(activity.getContentResolver().openInputStream(exported))),"Gallery export must preserve every byte of the full recording");
   MediaMetadataRetriever gallery=new MediaMetadataRetriever();long galleryDuration;try{gallery.setDataSource(activity,exported);galleryDuration=Long.parseLong(gallery.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));}finally{gallery.release();}require(galleryDuration==duration,"Gallery duration must match private clip");
   return "PASS: game over, retry, next stage and pause/resume kept one encoder; full clip "+duration+" ms vs "+expected[0]+" ms active session; final frames decode; Gallery export byte-identical and duration "+galleryDuration+" ms; Retry after Stop does not replace the clip";
  }finally{
   if(exported!=null)activity.getContentResolver().delete(exported,null,null);
   check.runOnMainSync(()->{call(activity,"stopOrReview");GameIsolationCheck.set(activity,"faceChoice",originalFace);recording.setChecked(originalRecording);GameIsolationCheck.set(activity,"lastClip",original);android.content.SharedPreferences.Editor prefs=activity.getPreferences(0).edit();if(original==null)prefs.remove("last_clip");else prefs.putString("last_clip",original.getName());prefs.apply();GameIsolationCheck.set(activity,"awaitingStart",true);overlay.setPaused(true);overlay.setPersistenceEnabled(true);});
   if(recorded[0]!=null&&recorded[0].getParentFile().equals(activity.getExternalFilesDir(android.os.Environment.DIRECTORY_MOVIES)))recorded[0].delete();
  }
 }
}
