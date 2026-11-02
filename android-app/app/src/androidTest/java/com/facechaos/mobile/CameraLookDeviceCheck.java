package com.facechaos.mobile;

import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.*;
import android.media.MediaMetadataRetriever;
import android.os.*;
import android.view.*;
import android.widget.FrameLayout;
import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/** Synthetic texture only: compares GPU preview, raw recognition input and MP4 pixels. */
final class CameraLookDeviceCheck {
 static void require(boolean ok,String message){if(!ok)throw new AssertionError(message);}
 static int distance(int a,int b){return Math.max(Math.abs(Color.red(a)-Color.red(b)),Math.max(Math.abs(Color.green(a)-Color.green(b)),Math.abs(Color.blue(a)-Color.blue(b))));}
 static String run(Instrumentation test)throws Exception{
  MainActivity activity=(MainActivity)test.startActivitySync(new Intent(test.getTargetContext(),MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));test.waitForIdleSync();
  FrameLayout[] container={null};TextureView[] texture={null};CameraLookPreview[] preview={null};CountDownLatch ready=new CountDownLatch(1),updated=new CountDownLatch(1);
  test.runOnMainSync(()->{
   ((SelfieCamera)RecordingPlaybackCheck.field(activity,"camera")).stop();RecordingPlaybackCheck.call(activity,"stopFaceRecognition");RecordingPlaybackCheck.call(activity,"stopMicrophone");GameIsolationCheck.set(activity,"paused",true);((GameOverlay)RecordingPlaybackCheck.field(activity,"overlay")).setPaused(true);
   FrameLayout root=new FrameLayout(activity);root.setBackgroundColor(Color.BLACK);container[0]=new FrameLayout(activity);texture[0]=new TextureView(activity);container[0].addView(texture[0],new FrameLayout.LayoutParams(-1,-1));root.addView(container[0],new FrameLayout.LayoutParams(480,640));activity.setContentView(root);preview[0]=new CameraLookPreview(container[0]);
   texture[0].setSurfaceTextureListener(new TextureView.SurfaceTextureListener(){public void onSurfaceTextureAvailable(SurfaceTexture surface,int w,int h){ready.countDown();}public void onSurfaceTextureSizeChanged(SurfaceTexture surface,int w,int h){}public boolean onSurfaceTextureDestroyed(SurfaceTexture surface){return true;}public void onSurfaceTextureUpdated(SurfaceTexture surface){updated.countDown();}});
  });
  require(ready.await(10,TimeUnit.SECONDS),"Synthetic texture not ready");int original=Color.rgb(80,100,120);Surface surface=new Surface(texture[0].getSurfaceTexture());try{Canvas canvas=surface.lockCanvas(null);canvas.drawColor(original);surface.unlockCanvasAndPost(canvas);}finally{surface.release();}require(updated.await(10,TimeUnit.SECONDS),"Synthetic frame not displayed");test.waitForIdleSync();
  Bitmap[] raw={null};test.runOnMainSync(()->raw[0]=texture[0].getBitmap());require(raw[0]!=null&&distance(raw[0].getPixel(240,400),original)<=1,"Raw texture color");
  CameraLook look=new CameraLook(CameraLook.SMOOTH,1,1);test.runOnMainSync(()->preview[0].apply(look));Thread.sleep(250);require(preview[0].supportsSmoothing(),"AGSL filter did not compile on supported phone");
  Bitmap screenshot=screen(test,activity);int[] location=new int[2];test.runOnMainSync(()->container[0].getLocationInWindow(location));int enhanced=screenshot.getPixel(location[0]+240,location[1]+400);screenshot.recycle();require(Color.red(enhanced)>Color.red(original)+12,"Preview did not brighten");
  Bitmap[] unchanged={null};test.runOnMainSync(()->unchanged[0]=texture[0].getBitmap());require(unchanged[0]!=null&&raw[0].sameAs(unchanged[0]),"Preview enhancement changed the recognition source");unchanged[0].recycle();
  test.runOnMainSync(()->preview[0].apply(CameraLook.ORIGINAL));Thread.sleep(180);screenshot=screen(test,activity);require(distance(screenshot.getPixel(location[0]+240,location[1]+400),original)<=2,"Original comparison must restore preview");screenshot.recycle();
  test.runOnMainSync(()->{CameraLookPreview basic=new CameraLookPreview(container[0],false);CameraLook result=basic.apply(look);require(result.softness()==0&&result.mode==CameraLook.NATURAL,"Basic fallback");});Thread.sleep(180);screenshot=screen(test,activity);require(distance(screenshot.getPixel(location[0]+240,location[1]+400),enhanced)<=3,"Older-device brightness/warmth differs");screenshot.recycle();
  File clip=File.createTempFile("look-check-",".mp4",activity.getCacheDir());CountDownLatch encoderReady=new CountDownLatch(1),done=new CountDownLatch(1);AtomicReference<String> failure=new AtomicReference<>();
  VideoEncoder encoder=new VideoEncoder(clip,480,640,new VideoEncoder.Listener(){public void ready(){encoderReady.countDown();}public void complete(File file,int frames){done.countDown();}public void failed(String message){failure.set(message);encoderReady.countDown();done.countDown();}});
  Bitmap overlay=Bitmap.createBitmap(480,640,Bitmap.Config.ARGB_8888);Canvas graphics=new Canvas(overlay);Paint paint=new Paint();paint.setColor(Color.MAGENTA);graphics.drawRect(0,0,480,160,paint);paint.setColor(0x8000FF00);graphics.drawRect(0,160,160,320,paint);
  int encoded;
  try{
   encoder.start();require(encoderReady.await(15,TimeUnit.SECONDS)&&failure.get()==null,"Video startup: "+failure.get());
   for(int i=0;i<36;i++){long end=SystemClock.uptimeMillis()+3000;while(!encoder.available()&&failure.get()==null&&SystemClock.uptimeMillis()<end)Thread.sleep(5);require(encoder.available()&&encoder.submit(raw[0],overlay,look,System.nanoTime()),"Enhanced frame rejected");Thread.sleep(45);}
   encoder.stop();require(done.await(15,TimeUnit.SECONDS)&&failure.get()==null,"Video finish: "+failure.get());
   MediaMetadataRetriever reader=new MediaMetadataRetriever();Bitmap decoded=null;try{reader.setDataSource(clip.getAbsolutePath());decoded=reader.getFrameAtTime(700000,MediaMetadataRetriever.OPTION_CLOSEST);require(decoded!=null,"Enhanced clip cannot decode");encoded=decoded.getPixel(240,400);require(distance(encoded,enhanced)<=9,"Recorded look differs from preview: "+Integer.toHexString(encoded)+" / "+Integer.toHexString(enhanced));require(distance(decoded.getPixel(240,80),Color.MAGENTA)<=9,"Game graphics were filtered");int blended=Color.rgb(Color.red(enhanced)/2,(Color.green(enhanced)+255)/2,Color.blue(enhanced)/2);require(distance(decoded.getPixel(80,240),blended)<=10,"Transparent game graphics did not compose correctly");}finally{if(decoded!=null)decoded.recycle();reader.release();}
  }finally{encoder.stop();if(done.getCount()>0)done.await(5,TimeUnit.SECONDS);overlay.recycle();raw[0].recycle();clip.delete();}
  Bitmap pattern=Bitmap.createBitmap(480,640,Bitmap.Config.ARGB_8888);for(int y=0;y<640;y++)for(int x=0;x<480;x++){int level=x>=360?20:((x/2+y/2)%2==0?96:108);pattern.setPixel(x,y,Color.rgb(level,level,level));}
  Surface patterned=new Surface(texture[0].getSurfaceTexture());try{Canvas canvas=patterned.lockCanvas(null);canvas.drawBitmap(pattern,0,0,null);patterned.unlockCanvasAndPost(canvas);}finally{patterned.release();pattern.recycle();}
  test.runOnMainSync(()->preview[0].apply(CameraLook.ORIGINAL));Thread.sleep(220);Bitmap before=screen(test,activity);double originalVariance=variance(before,location[0]+180,location[1]+240);before.recycle();
  test.runOnMainSync(()->preview[0].apply(new CameraLook(CameraLook.SMOOTH,1,0)));Thread.sleep(220);Bitmap after=screen(test,activity);double smoothVariance=variance(after,location[0]+180,location[1]+240);require(smoothVariance<originalVariance*.70,"Smoothing did not reduce fine texture: "+smoothVariance+" / "+originalVariance);require(Color.red(after.getPixel(location[0]+340,location[1]+400))-Color.red(after.getPixel(location[0]+400,location[1]+400))>40,"Smoothing erased the strong edge");after.recycle();
  return "PASS: GPU preview brightens; original comparison restores it; raw recognition source stays byte-identical; smoothing reduces fine texture and preserves strong edges; basic fallback matches; MP4 look matches preview within codec tolerance; opaque and translucent game graphics stay unchanged";
 }
 private static double variance(Bitmap image,int x,int y){double sum=0,squared=0;int n=0;for(int row=y;row<y+80;row++)for(int column=x;column<x+80;column++){int value=Color.red(image.getPixel(column,row));sum+=value;squared+=value*value;n++;}return squared/n-(sum/n)*(sum/n);}
 private static Bitmap screen(Instrumentation test,MainActivity activity)throws Exception{
  Bitmap output=Bitmap.createBitmap(activity.getWindow().getDecorView().getWidth(),activity.getWindow().getDecorView().getHeight(),Bitmap.Config.ARGB_8888);CountDownLatch done=new CountDownLatch(1);int[] status={-1};test.runOnMainSync(()->PixelCopy.request(activity.getWindow(),output,result->{status[0]=result;done.countDown();},new Handler(Looper.getMainLooper())));require(done.await(10,TimeUnit.SECONDS)&&status[0]==PixelCopy.SUCCESS,"GPU screenshot failed");return output;
 }
}
