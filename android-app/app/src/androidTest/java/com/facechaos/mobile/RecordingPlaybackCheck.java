package com.facechaos.mobile;

import android.app.Instrumentation;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/** Real encoder regression; synthetic frames only, no camera, microphone or player clips. */
final class RecordingPlaybackCheck {
 static void require(boolean ok,String message){if(!ok)throw new AssertionError(message);}
 static Object field(Object owner,String name){try{java.lang.reflect.Field f=owner.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(owner);}catch(Exception e){throw new RuntimeException(e);}}
 static void call(Object owner,String name){try{java.lang.reflect.Method m=owner.getClass().getDeclaredMethod(name);m.setAccessible(true);m.invoke(owner);}catch(Exception e){throw new RuntimeException(e);}}
 static void click(Instrumentation check,String text) throws Exception {
  long deadline=android.os.SystemClock.uptimeMillis()+6000;
  while(android.os.SystemClock.uptimeMillis()<deadline){
   android.view.accessibility.AccessibilityNodeInfo root=check.getUiAutomation().getRootInActiveWindow();
   if(root!=null)for(android.view.accessibility.AccessibilityNodeInfo node:root.findAccessibilityNodeInfosByText(text)){
    if(node.isClickable()&&node.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK)){check.waitForIdleSync();return;}
   }
   Thread.sleep(100);
  }
  throw new AssertionError("Missing button: "+text);
 }
 static String runGame(Instrumentation check) throws Exception {
  MainActivity activity=(MainActivity)check.startActivitySync(new android.content.Intent(check.getTargetContext(),MainActivity.class).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK));
  check.waitForIdleSync();Thread.sleep(2000);
  boolean originalFace=(Boolean)field(activity,"faceChoice");
  android.widget.Switch recording=(android.widget.Switch)field(activity,"recordSwitch");
  boolean originalRecording=recording.isChecked();
  try{
   check.runOnMainSync(()->{try{java.lang.reflect.Field f=MainActivity.class.getDeclaredField("faceChoice");f.setAccessible(true);f.setBoolean(activity,false);}catch(Exception e){throw new RuntimeException(e);}recording.setChecked(true);call(activity,"showGames");});
   click(check,"Smile Stack");click(check,"Let's play!");Thread.sleep(1800);
   GameOverlay overlay=(GameOverlay)field(activity,"overlay");
   check.runOnMainSync(()->{require(overlay.game.mode==7,"Chosen game must start");require(!(Boolean)field(activity,"paused"),"Picker dismissal must not pause the new recording round");require(field(activity,"encoder")!=null&&!(Boolean)field(activity,"preparingVideo"),"Recording must start with the game");});
   Thread.sleep(2200);
   float[] position=new float[2];
   check.runOnMainSync(()->{require(overlay.game.countdown<=0&&overlay.game.phase>0,"Game must move during recording");call(activity,"primaryCaptureAction");position[0]=overlay.game.phase;require(overlay.game.paused,"Circle pauses gameplay");});
   Thread.sleep(400);
   check.runOnMainSync(()->{require(overlay.game.phase==position[0],"Paused game must not move");call(activity,"primaryCaptureAction");require(!overlay.game.paused,"Circle resumes gameplay");position[1]=overlay.game.phase;});
   Thread.sleep(600);
   check.runOnMainSync(()->{require(overlay.game.phase>position[1],"Game must move again after resume");require(field(activity,"encoder")!=null,"Recording must survive resume");call(activity,"stopOrReview");require(overlay.game.finished&&overlay.game.stopped,"Stop ends the round");});
   long deadline=android.os.SystemClock.uptimeMillis()+15000;
   while(field(activity,"encoder")!=null&&android.os.SystemClock.uptimeMillis()<deadline)Thread.sleep(100);
   check.runOnMainSync(()->{require(field(activity,"encoder")==null,"Stop must finalize recording");File clip=(File)field(activity,"lastClip");require(clip!=null&&clip.length()>0,"Playable clip must be saved");});
   return "PASS: real game picker and instructions, recording startup, moving game, circle pause/resume, stop and saved clip on device (camera captured, touch mode used)";
  }finally{check.runOnMainSync(()->{recording.setChecked(originalRecording);try{java.lang.reflect.Field f=MainActivity.class.getDeclaredField("faceChoice");f.setAccessible(true);f.setBoolean(activity,originalFace);}catch(Exception e){throw new RuntimeException(e);}});}
 }
 static String run(Instrumentation check) throws Exception {
  File file=File.createTempFile("recording-check-",".mp4",check.getTargetContext().getCacheDir());
  CountDownLatch ready=new CountDownLatch(1),done=new CountDownLatch(1);
  AtomicReference<String> failure=new AtomicReference<>();
  VideoEncoder encoder=new VideoEncoder(file,480,640,4_000_000,new VideoEncoder.Listener(){
   public void ready(){ready.countDown();}
   public void complete(File clip,int frames){if(frames<20)failure.set("Too few encoded frames");done.countDown();}
   public void failed(String message){failure.set(message);ready.countDown();done.countDown();}
  });
  Bitmap frame=Bitmap.createBitmap(480,640,Bitmap.Config.ARGB_8888);
  try {
   check.runOnMainSync(encoder::start);
   require(ready.await(15,TimeUnit.SECONDS),"Encoder startup timeout");
   require(failure.get()==null,"Encoder startup: "+failure.get());
   // The round starts by requesting resume, although the recorder is already running.
   check.runOnMainSync(()->{encoder.setPaused(false);encoder.setPaused(false);});
   for(int section=0;section<2;section++){
    for(int i=0;i<24;i++){
     long deadline=android.os.SystemClock.uptimeMillis()+3000;
     while(!encoder.available()&&failure.get()==null&&android.os.SystemClock.uptimeMillis()<deadline)Thread.sleep(5);
     require(failure.get()==null&&encoder.available(),"Recording stopped while game was playing: "+failure.get());
     frame.eraseColor(section==0?Color.RED:Color.BLUE);
     require(encoder.submit(frame,System.nanoTime()),"Frame rejected");Thread.sleep(45);
    }
    if(section==0){
     check.runOnMainSync(()->{encoder.setPaused(true);encoder.setPaused(true);});
     require(!encoder.available(),"Paused recording must reject frames");Thread.sleep(200);
     check.runOnMainSync(()->{encoder.setPaused(false);encoder.setPaused(false);});
    }
   }
   check.runOnMainSync(encoder::stop);
   require(done.await(15,TimeUnit.SECONDS),"Finalizing timeout");
   require(failure.get()==null,"Recording failure: "+failure.get());
   MediaMetadataRetriever reader=new MediaMetadataRetriever();
   try{reader.setDataSource(file.getAbsolutePath());long duration=Long.parseLong(reader.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));require(duration>1200,"Clip must contain moving frames before and after pause");Bitmap decoded=reader.getFrameAtTime();require(decoded!=null,"Saved video must decode");if(decoded!=null)decoded.recycle();}finally{reader.release();}
   return "PASS: real MP4 startup, duplicate resume, duplicate pause, continued frames after resume, stop and saved-video decoding; synthetic frames only";
  } finally {encoder.stop();if(done.getCount()!=0)done.await(5,TimeUnit.SECONDS);frame.recycle();file.delete();}
 }
}
