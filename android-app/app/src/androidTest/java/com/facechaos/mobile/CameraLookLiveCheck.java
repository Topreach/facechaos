package com.facechaos.mobile;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.camera2.CameraCaptureSession;
import android.os.SystemClock;
import android.view.TextureView;
import android.view.accessibility.AccessibilityNodeInfo;

/** Live camera pixels stay in RAM; only aggregate deltas are reported. */
final class CameraLookLiveCheck {
 static void require(boolean ok,String message){if(!ok)throw new AssertionError(message);}
 static Object field(Object owner,String name){return RecordingPlaybackCheck.field(owner,name);}
 static void call(MainActivity activity,String name){RecordingPlaybackCheck.call(activity,name);}
 static String run(Instrumentation check)throws Exception{
  MainActivity activity=(MainActivity)check.startActivitySync(new Intent(check.getTargetContext(),MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));check.waitForIdleSync();
  SelfieCamera camera=(SelfieCamera)field(activity,"camera");TextureView texture=(TextureView)field(activity,"preview");CameraLook original=(CameraLook)field(activity,"cameraLook");Bitmap before=null,after=null;Bitmap[] raw={null},rawAfter={null};
  try{
   long end=SystemClock.uptimeMillis()+12000;while(!camera.hasFreshFrame()&&SystemClock.uptimeMillis()<end)Thread.sleep(50);require(camera.hasFreshFrame(),"Live camera unavailable");
   check.runOnMainSync(()->{try{((CameraCaptureSession)field(camera,"session")).stopRepeating();}catch(Exception error){throw new RuntimeException(error);}GameIsolationCheck.set(activity,"cameraLook",CameraLook.ORIGINAL);call(activity,"applyCameraLook");});Thread.sleep(450);
   check.runOnMainSync(()->raw[0]=texture.getBitmap());before=CameraLookDeviceCheck.screen(check,activity);
   check.runOnMainSync(()->{GameIsolationCheck.set(activity,"cameraLook",new CameraLook(CameraLook.NATURAL,.65f,0));call(activity,"applyCameraLook");});Thread.sleep(250);after=CameraLookDeviceCheck.screen(check,activity);check.runOnMainSync(()->rawAfter[0]=texture.getBitmap());require(raw[0]!=null&&rawAfter[0]!=null&&raw[0].sameAs(rawAfter[0]),"Live raw recognition frame changed");
   int samples=0,changed=0;double delta=0;for(int y=(int)(before.getHeight()*.23);y<before.getHeight()*.43;y+=12)for(int x=(int)(before.getWidth()*.32);x<before.getWidth()*.68;x+=12){int a=before.getPixel(x,y),b=after.getPixel(x,y);double old=(Color.red(a)+Color.green(a)+Color.blue(a))/3.0,newValue=(Color.red(b)+Color.green(b)+Color.blue(b))/3.0;if(old>=30&&old<=190){samples++;delta+=newValue-old;if(newValue-old>4)changed++;}}
   require(samples>30&&changed>samples*.9&&delta/samples>8,"Live preview effect too weak: samples="+samples+" changed="+changed+" mean delta="+(delta/Math.max(1,samples)));
   check.runOnMainSync(()->{camera.stop();camera.start();call(activity,"showCameraLook");});
   RecordingPlaybackCheck.click(check,activity.getString(R.string.fc_look_smooth));setSlider(check,activity.getString(R.string.fc_look_intensity),75);setSlider(check,activity.getString(R.string.fc_look_warmth),70);
   check.runOnMainSync(()->{CameraLook selected=(CameraLook)field(activity,"cameraLook");require(selected.mode==CameraLook.SMOOTH&&Math.abs(selected.intensity-.75f)<.01&&Math.abs(selected.warmth-.4f)<.01,"Actual slider controls did not update settings");});
   RecordingPlaybackCheck.click(check,activity.getString(R.string.fc_look_compare));Thread.sleep(1400);check.runOnMainSync(()->require(!(Boolean)field(activity,"compareOriginal")&&((CameraLook)field(activity,"recordLook")).strength()>.7f,"Original comparison remained stuck on"));
   RecordingPlaybackCheck.click(check,activity.getString(R.string.fc_done));check.runOnMainSync(()->require(((CameraLook)field(activity,"recordLook")).mode==CameraLook.SMOOTH,"Selected effect lost on closing controls"));
   return "PASS: live camera "+changed+"/"+samples+" midtone samples visibly changed; average brightness +"+Math.round(delta/samples)+" levels at 65%; raw frame byte-identical; actual mode/intensity/warmth controls work; original comparison resets automatically";
  }finally{
   if(before!=null)before.recycle();if(after!=null)after.recycle();if(raw[0]!=null)raw[0].recycle();if(rawAfter[0]!=null)rawAfter[0].recycle();
   check.runOnMainSync(()->{GameIsolationCheck.set(activity,"cameraLook",original);GameIsolationCheck.set(activity,"compareOriginal",false);activity.getSharedPreferences("appearance",0).edit().putInt("mode",original.mode).putFloat("intensity",original.intensity).putFloat("warmth",original.warmth).apply();call(activity,"applyCameraLook");camera.stop();});
  }
 }
 private static void setSlider(Instrumentation check,String name,float value){AccessibilityNodeInfo root=check.getUiAutomation().getRootInActiveWindow();require(root!=null&&change(root,name,value),"Missing adjustable control: "+name);check.waitForIdleSync();}
 private static boolean change(AccessibilityNodeInfo node,String name,float value){if(name.contentEquals(node.getContentDescription()==null?"":node.getContentDescription())&&"android.widget.SeekBar".contentEquals(node.getClassName())){android.os.Bundle args=new android.os.Bundle();args.putFloat(AccessibilityNodeInfo.ACTION_ARGUMENT_PROGRESS_VALUE,value);return node.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_PROGRESS.getId(),args);}for(int i=0;i<node.getChildCount();i++){AccessibilityNodeInfo child=node.getChild(i);if(child!=null&&change(child,name,value))return true;}return false;}
}
