package com.facechaos.mobile;
import android.app.Instrumentation;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
/** Verifies the head-only production adapter using the public portrait; no camera/mic. */
final class HeadModelCheck {
 static String run(Instrumentation test)throws Exception{
  CountDownLatch[] callback={new CountDownLatch(1)};boolean[] ready={false},failed={false};int[] lane={99},expressions={0};
  FaceRecognition engine=new FaceRecognition(test.getTargetContext(),new FaceRecognition.Listener(){public void result(boolean r,String s,int action,String values){expressions[0]++;}public void failed(){failed[0]=true;callback[0].countDown();}public void head(boolean r,int direction,float offset,String state){ready[0]=r;lane[0]=direction;callback[0].countDown();}},true);
  try{
   for(int frame=0;frame<12;frame++){
    long end=android.os.SystemClock.uptimeMillis()+15000;while(!engine.canAccept()&&!failed[0]&&android.os.SystemClock.uptimeMillis()<end)Thread.sleep(10);
    if(failed[0])throw new AssertionError("Head-only model failed to load");callback[0]=new CountDownLatch(1);
    Bitmap image;try(java.io.InputStream input=test.getContext().getAssets().open("face_fixture.jpg")){image=BitmapFactory.decodeStream(input);}
    if(!engine.offer(image,android.os.SystemClock.uptimeMillis())){image.recycle();throw new AssertionError("Head-only adapter rejected a frame");}
    if(!callback[0].await(15,TimeUnit.SECONDS)||failed[0])throw new AssertionError("Head-only callback missing");
   }
   if(!ready[0]||lane[0]!=0||expressions[0]!=0)throw new AssertionError("Head calibration should be independent of mouth/smile/teeth decisions");
  }finally{engine.close();}
  return "PASS: actual head-only landmark adapter calibrates centred steering from twelve public portrait frames without mouth/smile calibration or camera access";
 }
}
