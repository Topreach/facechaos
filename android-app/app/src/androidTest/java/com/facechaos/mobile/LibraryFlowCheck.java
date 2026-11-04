package com.facechaos.mobile;

import android.app.Instrumentation;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;
import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/** Real clip review and cancel flows; no personal clips are modified or shared. */
final class LibraryFlowCheck {
 static void require(boolean ok,String message){if(!ok)throw new AssertionError(message);}
 static View find(View view,Class<?> type){if(type.isInstance(view))return view;if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++){View found=find(((ViewGroup)view).getChildAt(i),type);if(found!=null)return found;}return null;}
 static View findWindow(Class<?> type){for(View root:android.view.inspector.WindowInspector.getGlobalWindowViews()){View found=find(root,type);if(found!=null)return found;}return null;}
 static void call(MainActivity activity,String name,File file){try{java.lang.reflect.Method method=MainActivity.class.getDeclaredMethod(name,File.class);method.setAccessible(true);method.invoke(activity,file);}catch(Exception error){throw new RuntimeException(error);}}
 static String run(Instrumentation test)throws Exception{
  MainActivity activity=(MainActivity)test.startActivitySync(new android.content.Intent(test.getTargetContext(),MainActivity.class).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK));test.waitForIdleSync();
  String language=AppLanguage.current(activity);
  test.runOnMainSync(()->RecordingPlaybackCheck.call(activity,"showLanguage"));
  RecordingPlaybackCheck.click(test,activity.getString(R.string.fc_cancel));require(language.equals(AppLanguage.current(activity)),"Cancelling changed the language");
  test.runOnMainSync(()->RecordingPlaybackCheck.call(activity,"showRecordings"));RecordingPlaybackCheck.click(test,activity.getString(R.string.fc_done));
  File clip=File.createTempFile("library-flow-",".mp4",activity.getCacheDir());Bitmap frame=Bitmap.createBitmap(480,640,Bitmap.Config.ARGB_8888);CountDownLatch ready=new CountDownLatch(1),done=new CountDownLatch(1);String[] failure={null};
  VideoEncoder encoder=new VideoEncoder(clip,480,640,new VideoEncoder.Listener(){public void ready(){ready.countDown();}public void complete(File file,int frames){done.countDown();}public void failed(String error){failure[0]=error;ready.countDown();done.countDown();}});
  try{
   test.runOnMainSync(encoder::start);require(ready.await(15,TimeUnit.SECONDS)&&failure[0]==null,"Synthetic recording startup");
   for(int i=0;i<36;i++){long end=android.os.SystemClock.uptimeMillis()+3000;while(!encoder.available()&&android.os.SystemClock.uptimeMillis()<end)Thread.sleep(5);frame.eraseColor(i%2==0?0xFF75B6A3:0xFFF4D452);require(encoder.submit(frame,System.nanoTime()),"Synthetic frame rejected");Thread.sleep(45);}
   test.runOnMainSync(encoder::stop);require(done.await(15,TimeUnit.SECONDS)&&failure[0]==null,"Synthetic video finalize");
   test.runOnMainSync(()->call(activity,"showClip",clip));VideoView[] player={null};long end=android.os.SystemClock.uptimeMillis()+10000;
   while(android.os.SystemClock.uptimeMillis()<end){test.runOnMainSync(()->player[0]=(VideoView)findWindow(VideoView.class));if(player[0]!=null&&player[0].getCurrentPosition()>100)break;Thread.sleep(100);}
   test.runOnMainSync(()->require(player[0]!=null&&player[0].getDuration()>1000&&player[0].getCurrentPosition()>100,"Review video must load and play"));
   RecordingPlaybackCheck.click(test,activity.getString(R.string.fc_share));RecordingPlaybackCheck.click(test,activity.getString(R.string.fc_cancel));
   require(clip.isFile(),"Opening/cancelling Share must preserve clip");require(language.equals(AppLanguage.current(activity)),"Library flow changed language");
   return "PASS: native language cancel preserves locale; recordings opens/closes; synthetic MP4 plays in Canvas review; Share opens/cancels without posting or deleting clips";
  }finally{encoder.stop();if(done.getCount()>0)done.await(5,TimeUnit.SECONDS);frame.recycle();clip.delete();}
 }
}
