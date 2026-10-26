package com.facechaos.mobile;
import android.app.Instrumentation;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker;
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
/** Public test portrait and blank image only. Never opens camera or microphone. */
final class FaceModelCheck {
 static String run(Instrumentation test)throws Exception{
  Bitmap portrait;try(java.io.InputStream input=test.getContext().getAssets().open("face_fixture.jpg")){portrait=BitmapFactory.decodeStream(input);}
  Bitmap blank=Bitmap.createBitmap(384,512,Bitmap.Config.ARGB_8888);blank.eraseColor(0xFF808080);
  try(FaceLandmarker detector=FaceLandmarker.createFromOptions(test.getTargetContext(),FaceLandmarker.FaceLandmarkerOptions.builder().setBaseOptions(BaseOptions.builder().setModelAssetPath("face_landmarker.task").build()).setRunningMode(RunningMode.IMAGE).setOutputFaceBlendshapes(true).build());MPImage image=new BitmapImageBuilder(portrait).build();MPImage empty=new BitmapImageBuilder(blank).build()){
   FaceLandmarkerResult found=detector.detect(image);
   if(found.faceLandmarks().size()!=1||found.faceLandmarks().get(0).size()<468||!found.faceBlendshapes().isPresent()||found.faceBlendshapes().get().get(0).size()<50)throw new AssertionError("Public portrait did not produce landmarks and blendshapes");
   java.util.Set<String> names=new java.util.HashSet<>();for(com.google.mediapipe.tasks.components.containers.Category category:found.faceBlendshapes().get().get(0))names.add(category.categoryName());
   for(String required:new String[]{"eyeWideLeft","eyeWideRight","eyeSquintLeft","eyeSquintRight","cheekSquintLeft","cheekSquintRight","browDownLeft","browDownRight","noseSneerLeft","noseSneerRight"})if(!names.contains(required))throw new AssertionError("Bundled model missing "+required);
   if(!detector.detect(empty).faceLandmarks().isEmpty())throw new AssertionError("Blank image falsely detected a face");
  }
  portrait.recycle();blank.recycle();
  CountDownLatch callback=new CountDownLatch(1);String[] state={null};boolean[] failed={false};
  FaceRecognition engine=new FaceRecognition(test.getTargetContext(),new FaceRecognition.Listener(){public void result(boolean ready,String status,int action,String values){state[0]=values;callback.countDown();}public void failed(){failed[0]=true;callback.countDown();}});
  try{
   long end=android.os.SystemClock.uptimeMillis()+15000;while(!engine.canAccept()&&android.os.SystemClock.uptimeMillis()<end&&!failed[0])Thread.sleep(20);
   Bitmap frame;try(java.io.InputStream input=test.getContext().getAssets().open("face_fixture.jpg")){frame=BitmapFactory.decodeStream(input);}
   if(!engine.offer(frame,android.os.SystemClock.uptimeMillis())){frame.recycle();throw new AssertionError("Live adapter failed to initialize");}
   if(!callback.await(15,TimeUnit.SECONDS)||failed[0]||state[0]==null||!state[0].contains("Mouth"))throw new AssertionError("Frame adapter did not return actual expression measurements: "+state[0]);
  }finally{engine.close();}
  return "PASS: bundled face model detects public portrait landmarks/blendshapes, rejects blank image, and threaded frame adapter returns expression measurements; no camera/microphone access";
 }
}
