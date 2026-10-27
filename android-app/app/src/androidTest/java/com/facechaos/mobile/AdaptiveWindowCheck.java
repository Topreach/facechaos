package com.facechaos.mobile;

import android.app.Instrumentation;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;

final class AdaptiveWindowCheck {
 static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
 static String run(Instrumentation check)throws Exception{
  check.runOnMainSync(()->{
   int[][] windows={{1080,2340},{2340,1080},{1600,2560},{2560,1600},{800,600},{600,800}};
   for(int[] window:windows)for(int mode=0;mode<15;mode++){
    int w=AdaptiveViewport.overlayWidth(window[0],window[1],2);
    GameOverlay view=new GameOverlay(check.getTargetContext());view.setPersistenceEnabled(false);view.select(mode);
    view.setPaused(true);view.setSafeTop(32);view.setBottomReserved(260);
    view.measure(View.MeasureSpec.makeMeasureSpec(w,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(window[1],View.MeasureSpec.EXACTLY));view.layout(0,0,w,window[1]);
    Bitmap frame=Bitmap.createBitmap(w,window[1],Bitmap.Config.ARGB_8888);view.draw(new Canvas(frame));frame.recycle();
    OverlayLayout geometry=view.gameLayout();require(geometry.instructionTop>=0&&geometry.gameTop<geometry.gameBottom,"Game clipped: "+mode+" at "+window[0]+"x"+window[1]);
   }
  });
  MainActivity activity=(MainActivity)check.startActivitySync(new Intent(check.getTargetContext(),MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
  check.waitForIdleSync();Thread.sleep(1200);
  GameOverlay overlay=(GameOverlay)RecordingPlaybackCheck.field(activity,"overlay");MiniGame owner=overlay.game;
  check.runOnMainSync(()->{overlay.setPersistenceEnabled(false);owner.score=3;owner.coins=2;owner.hearts=2;activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);});
  Thread.sleep(1800);check.waitForIdleSync();
  check.runOnMainSync(()->{
   require(overlay.game==owner&&owner.score==3&&owner.coins==2&&owner.hearts==2,"Rotation lost game progress");
   require(owner.paused,"Rotation must pause the game");
   View root=(View)overlay.getParent();require(root.getWidth()>root.getHeight(),"Device did not rotate to landscape");
   require(overlay.getWidth()==AdaptiveViewport.overlayWidth(root.getWidth(),root.getHeight(),activity.getResources().getDisplayMetrics().density),"Landscape viewport not applied");
   activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
  });
  Thread.sleep(1600);check.waitForIdleSync();
  check.runOnMainSync(()->{require(overlay.game==owner&&owner.score==3&&owner.paused,"Return rotation lost session");owner.score=0;owner.coins=0;owner.hearts=3;overlay.setPersistenceEnabled(true);activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);});
  boolean originalFace=(Boolean)RecordingPlaybackCheck.field(activity,"faceChoice");
  android.widget.Switch recording=(android.widget.Switch)RecordingPlaybackCheck.field(activity,"recordSwitch");boolean originalRecording=recording.isChecked();
  try{
   check.runOnMainSync(()->{overlay.setPersistenceEnabled(false);GameIsolationCheck.set(activity,"faceChoice",false);recording.setChecked(true);GameIsolationCheck.select(activity,7);});
   Thread.sleep(2200);check.waitForIdleSync();
   MiniGame recordingOwner=overlay.game;
   check.runOnMainSync(()->{require(RecordingPlaybackCheck.field(activity,"encoder")!=null,"Recording did not start");require(((SelfieCamera)RecordingPlaybackCheck.field(activity,"camera")).hasFreshFrame(),"No fresh camera before rotation");activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);});
   long deadline=android.os.SystemClock.uptimeMillis()+12000;
   while(RecordingPlaybackCheck.field(activity,"encoder")!=null&&android.os.SystemClock.uptimeMillis()<deadline)Thread.sleep(100);
   Thread.sleep(1400);check.waitForIdleSync();
   check.runOnMainSync(()->{require(overlay.game==recordingOwner&&recordingOwner.paused,"Recording rotation lost game or resumed unexpectedly");require(RecordingPlaybackCheck.field(activity,"encoder")==null,"Recording not finalized on resize");require(((SelfieCamera)RecordingPlaybackCheck.field(activity,"camera")).hasFreshFrame(),"Camera did not recover after rotation");});
   java.io.File clip=(java.io.File)RecordingPlaybackCheck.field(activity,"lastClip");require(clip!=null&&clip.length()>0,"Missing finalized recording");
   android.media.MediaMetadataRetriever metadata=new android.media.MediaMetadataRetriever();try{metadata.setDataSource(clip.getAbsolutePath());require(Long.parseLong(metadata.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION))>0,"Invalid recording duration");Bitmap decoded=metadata.getFrameAtTime();require(decoded!=null,"Recording cannot be decoded");decoded.recycle();}finally{metadata.release();}
  }finally{
   check.runOnMainSync(()->{recording.setChecked(originalRecording);GameIsolationCheck.set(activity,"faceChoice",originalFace);activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);});Thread.sleep(1200);
   check.runOnMainSync(()->{overlay.setPersistenceEnabled(true);activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);});
  }
  return "PASS: 90 game renders; real rotation preserves game and progress; recording finalizes to a decodable clip and camera recovers after rotation";
 }
}
