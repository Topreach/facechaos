package com.facechaos.mobile;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.TextView;
import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/** Only compiled by -PoptimizedCheck. No camera, microphone or personal clips. */
public final class OptimizedCheckActivity extends Activity {
 private TextView status;
 private static void require(boolean ok,String message){if(!ok)throw new AssertionError(message);}
 @Override public void onCreate(Bundle state){super.onCreate(state);status=new TextView(this);status.setText("Checking optimized build...");setContentView(status);new Thread(()->{
  try{checkFace();checkVideo();runOnUiThread(()->{try{checkLayouts();report("PASS: optimized face model and expression adapter; video start/pause/resume/decode; 75 game/language renders; 20 welcome layouts and navigation callbacks");}catch(Throwable error){report("FAIL: "+android.util.Log.getStackTraceString(error));}});}
  catch(Throwable error){runOnUiThread(()->report("FAIL: "+android.util.Log.getStackTraceString(error)));}
 }).start();}
 private void report(String message){status.setText(message);android.util.Log.i("FaceChaosOptimized",message);}
 private void checkFace()throws Exception{
  CountDownLatch result=new CountDownLatch(1);AtomicReference<String> values=new AtomicReference<>();
  FaceRecognition engine=new FaceRecognition(this,new FaceRecognition.Listener(){public void result(boolean ready,String state,int action,String measurements){values.set(measurements);result.countDown();}public void failed(){values.set("FAILED");result.countDown();}});
  try{long end=android.os.SystemClock.uptimeMillis()+20000;while(!engine.canAccept()&&result.getCount()>0&&android.os.SystemClock.uptimeMillis()<end)Thread.sleep(20);
   require(engine.canAccept(),"Native face model did not load");Bitmap frame;try(java.io.InputStream input=getAssets().open("face_fixture.jpg")){frame=BitmapFactory.decodeStream(input);}
   require(engine.offer(frame,android.os.SystemClock.uptimeMillis()),"Frame not accepted");require(result.await(20,TimeUnit.SECONDS)&&values.get()!=null&&values.get().contains("Mouth"),"No expression result: "+values.get());
  }finally{engine.close();}
 }
 private void checkVideo()throws Exception{
  File clip=new File(getCacheDir(),"optimized-check.mp4");CountDownLatch ready=new CountDownLatch(1),done=new CountDownLatch(1);AtomicReference<String> failure=new AtomicReference<>();
  VideoEncoder encoder=new VideoEncoder(clip,480,640,new VideoEncoder.Listener(){public void ready(){ready.countDown();}public void complete(File file,int frames){done.countDown();}public void failed(String message){failure.set(message);ready.countDown();done.countDown();}});
  Bitmap frame=Bitmap.createBitmap(480,640,Bitmap.Config.ARGB_8888);
  try{encoder.start();require(ready.await(15,TimeUnit.SECONDS)&&failure.get()==null,"Encoder startup: "+failure.get());encoder.setPaused(false);encoder.setPaused(false);
   for(int i=0;i<48;i++){long end=android.os.SystemClock.uptimeMillis()+3000;while(!encoder.available()&&failure.get()==null&&android.os.SystemClock.uptimeMillis()<end)Thread.sleep(5);require(failure.get()==null&&encoder.available(),"No encoder frame slot");frame.eraseColor(i<24?0xFFFF0000:0xFF0000FF);require(encoder.submit(frame,System.nanoTime()),"Frame rejected");Thread.sleep(45);if(i==23){encoder.setPaused(true);encoder.setPaused(true);Thread.sleep(250);encoder.setPaused(false);encoder.setPaused(false);}}
   encoder.stop();require(done.await(15,TimeUnit.SECONDS)&&failure.get()==null,"Recording finalization: "+failure.get());android.media.MediaMetadataRetriever reader=new android.media.MediaMetadataRetriever();try{reader.setDataSource(clip.getAbsolutePath());Bitmap decoded=reader.getFrameAtTime();require(decoded!=null,"Saved clip cannot decode");decoded.recycle();}finally{reader.release();}
  }finally{encoder.stop();if(done.getCount()>0)done.await(5,TimeUnit.SECONDS);frame.recycle();clip.delete();}
 }
 private void checkLayouts(){
  for(String language:new String[]{"en","ar","fr","es","pt"}){
   android.content.res.Configuration config=new android.content.res.Configuration(getResources().getConfiguration());config.setLocale(java.util.Locale.forLanguageTag(language));android.content.Context context=createConfigurationContext(config);
   require(!context.getString(R.string.fc_games_short).equals("Games")||language.equals("en"),"Missing translation "+language);
   for(int mode=0;mode<15;mode++){GameOverlay overlay=new GameOverlay(context);overlay.setPersistenceEnabled(false);overlay.select(mode);overlay.setPaused(true);overlay.measure(android.view.View.MeasureSpec.makeMeasureSpec(720,1073741824),android.view.View.MeasureSpec.makeMeasureSpec(1560,1073741824));overlay.layout(0,0,720,1560);Bitmap frame=Bitmap.createBitmap(720,1560,Bitmap.Config.ARGB_8888);overlay.draw(new android.graphics.Canvas(frame));frame.recycle();}
   for(int i=0;i<4;i++){
    int width=i==1?1280:i==2?1600:720,height=i==1?720:i==2?2560:i==3?1280:1560;
    android.content.res.Configuration homeConfig=new android.content.res.Configuration(config);if(i==3)homeConfig.fontScale=1.5f;
    android.content.Context homeContext=createConfigurationContext(homeConfig);int[] clicked={0,0,0};
    WelcomePanel home=new WelcomePanel(homeContext,()->clicked[0]++,()->clicked[1]++,()->clicked[2]++);
    for(int pass=0;pass<2;pass++){home.measure(android.view.View.MeasureSpec.makeMeasureSpec(width,1073741824),android.view.View.MeasureSpec.makeMeasureSpec(height,1073741824));home.layout(0,0,width,height);}
    require(home.getLayoutDirection()==homeConfig.getLayoutDirection(),"Welcome layout direction");
    clickLabel(home,homeContext.getString(R.string.fc_home_start));clickLabel(home,homeContext.getString(R.string.fc_language));clickLabel(home,homeContext.getString(R.string.fc_privacy));require(clicked[0]==1&&clicked[1]==1&&clicked[2]==1,"Welcome actions not connected");
    Bitmap frame=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);home.draw(new android.graphics.Canvas(frame));
    File folder=getExternalFilesDir(null);if(folder!=null)try(java.io.FileOutputStream out=new java.io.FileOutputStream(new File(folder,"welcome-"+language+"-"+i+".png"))){frame.compress(Bitmap.CompressFormat.PNG,100,out);}catch(java.io.IOException error){throw new RuntimeException(error);}finally{frame.recycle();}
   }
  }
 }
 private void clickLabel(android.view.View view,String text){if(view instanceof android.widget.Button&&((android.widget.Button)view).getText().toString().equals(text)){view.performClick();return;}if(view instanceof android.view.ViewGroup){android.view.ViewGroup group=(android.view.ViewGroup)view;for(int i=0;i<group.getChildCount();i++)clickLabel(group.getChildAt(i),text);}}
}
