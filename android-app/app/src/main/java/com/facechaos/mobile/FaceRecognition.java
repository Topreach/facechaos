package com.facechaos.mobile;

import android.content.Context;
import android.graphics.Bitmap;
import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker;
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult;
import com.google.mediapipe.tasks.components.containers.Category;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Local, bounded, single-worker inference; frames are transient and never saved/uploaded. */
final class FaceRecognition {
 interface Listener {void result(boolean ready,String state,int action,String measurements);void failed();default void head(boolean ready,int lane,float offset,String status){} default void bounds(float left,float top,float right,float bottom){}}
 private final ExecutorService worker=Executors.newSingleThreadExecutor();
 private final FaceSignals signals=new FaceSignals();
 private final HeadSignals headSignals=new HeadSignals();
 private final boolean headOnly;
 private final Listener listener;
 private volatile boolean closed,busy=true,loaded;
 volatile float sensitivity=1;
 volatile int expectedAction=FaceSignals.NONE;
 private FaceLandmarker landmarker;
 private static final int[] INNER={78,191,80,81,82,13,312,311,310,415,308,324,318,402,317,14,87,178,88,95};
 FaceRecognition(Context context,Listener listener){this(context,listener,false);}
 FaceRecognition(Context context,Listener listener,boolean headOnly){this.listener=listener;this.headOnly=headOnly;worker.execute(()->{
  try{landmarker=FaceLandmarker.createFromOptions(context.getApplicationContext(),FaceLandmarker.FaceLandmarkerOptions.builder().setBaseOptions(BaseOptions.builder().setModelAssetPath("face_landmarker.task").build()).setRunningMode(RunningMode.VIDEO).setNumFaces(1).setMinFaceDetectionConfidence(.6f).setMinFacePresenceConfidence(.6f).setMinTrackingConfidence(.6f).setOutputFaceBlendshapes(true).build());loaded=true;}
  catch(RuntimeException|UnsatisfiedLinkError error){if(!closed)listener.failed();}
  finally{busy=false;}
 });}
 void recalibrate(){if(closed)return;worker.execute(()->{signals.reset();headSignals.reset();if(!closed){listener.result(false,signals.status,-1,"");listener.head(false,0,0,headSignals.status);}});}
 boolean canAccept(){return loaded&&!busy&&!closed;}
 boolean offer(Bitmap bitmap,long time){
  if(!canAccept())return false;busy=true;
  worker.execute(()->{try(MPImage image=new BitmapImageBuilder(bitmap).build()){
   FaceLandmarkerResult result=landmarker.detectForVideo(image,time);
   if(closed)return;
   if(result.faceLandmarks().isEmpty()||!result.faceBlendshapes().isPresent()||result.faceBlendshapes().get().isEmpty()){signals.lost();headSignals.lost();listener.bounds(-1,-1,-1,-1);listener.head(false,0,0,headSignals.status);listener.result(false,signals.status,-1,"No face found");return;}
   List<NormalizedLandmark> landmarks=result.faceLandmarks().get(0);
   float left=1,top=1,right=0,bottom=0;for(NormalizedLandmark point:landmarks){left=Math.min(left,point.x());top=Math.min(top,point.y());right=Math.max(right,point.x());bottom=Math.max(bottom,point.y());}listener.bounds(left,top,right,bottom);
   headSignals.accept(landmarks.get(1).x(),Math.abs(landmarks.get(454).x()-landmarks.get(234).x()),sensitivity);
   listener.head(headSignals.ready,headSignals.lane,headSignals.offset,headSignals.status);
   if(headOnly)return;
   float[] xs=new float[INNER.length],ys=new float[INNER.length];for(int i=0;i<INNER.length;i++){xs[i]=landmarks.get(INNER[i]).x()*bitmap.getWidth();ys[i]=landmarks.get(INNER[i]).y()*bitmap.getHeight();}
   float teeth=mouthEvidence(bitmap,xs,ys);
   boolean teethVisible=Float.isFinite(teeth);if(!teethVisible)teeth=0;
   float jaw=0,leftSmile=0,rightSmile=0,leftBlink=0,rightBlink=0,wideLeft=0,wideRight=0,squintLeft=0,squintRight=0,cheekLeft=0,cheekRight=0,browLeft=0,browRight=0,noseLeft=0,noseRight=0;
   for(Category category:result.faceBlendshapes().get().get(0))switch(category.categoryName()){
    case "jawOpen":jaw=category.score();break;case "mouthSmileLeft":leftSmile=category.score();break;case "mouthSmileRight":rightSmile=category.score();break;case "eyeBlinkLeft":leftBlink=category.score();break;case "eyeBlinkRight":rightBlink=category.score();break;
    case "eyeWideLeft":wideLeft=category.score();break;case "eyeWideRight":wideRight=category.score();break;
    case "eyeSquintLeft":squintLeft=category.score();break;case "eyeSquintRight":squintRight=category.score();break;
    case "cheekSquintLeft":cheekLeft=category.score();break;case "cheekSquintRight":cheekRight=category.score();break;
    case "browDownLeft":browLeft=category.score();break;case "browDownRight":browRight=category.score();break;
    case "noseSneerLeft":noseLeft=category.score();break;case "noseSneerRight":noseRight=category.score();break;default:break;
   }
   float smile=(leftSmile+rightSmile)/2,blink=Math.min(leftBlink,rightBlink);
   float wide=Math.min(wideLeft,wideRight),squeeze=FaceSignals.squeezeScore(blink,Math.min(squintLeft,squintRight),Math.min(cheekLeft,cheekRight),(browLeft+browRight)/2,(noseLeft+noseRight)/2);
   int action=signals.accept(jaw,smile,blink,teeth,wide,squeeze,time,sensitivity,expectedAction);
   String values=(teethVisible?"":"Teeth image too small - move a little closer.\n")+String.format(java.util.Locale.US,"Mouth %.2f   Smile %.2f\nBlink %.2f\nTeeth evidence %.2f / threshold %.2f\nTeeth detection is experimental; test it against smiles and open mouth.",jaw,smile,blink,teeth,FaceSignals.teethThreshold(sensitivity))+String.format(java.util.Locale.US,"\nHead offset %.2f - %s",headSignals.offset,headSignals.status);
   values+=String.format(java.util.Locale.US,"\nEyes wide %.2f   Face squeeze %.2f",wide,squeeze);
   boolean detailReady=expectedAction!=FaceSignals.TEETH||teethVisible;
   if(!closed)listener.result(signals.ready&&!signals.recovering&&detailReady,detailReady?signals.status:"Move a little closer for teeth detection",detailReady?action:FaceSignals.NONE,values);
  }catch(RuntimeException error){if(!closed)listener.failed();}finally{bitmap.recycle();busy=false;}});return true;
 }
 private static float mouthEvidence(Bitmap bitmap,float[] xs,float[] ys){
  float minX=bitmap.getWidth(),minY=bitmap.getHeight(),maxX=0,maxY=0;
  for(int i=0;i<xs.length;i++){if(!Float.isFinite(xs[i])||!Float.isFinite(ys[i])||xs[i]<0||ys[i]<0||xs[i]>=bitmap.getWidth()||ys[i]>=bitmap.getHeight())return Float.NaN;minX=Math.min(minX,xs[i]);minY=Math.min(minY,ys[i]);maxX=Math.max(maxX,xs[i]);maxY=Math.max(maxY,ys[i]);}
  int left=(int)minX,top=(int)minY,right=Math.min(bitmap.getWidth(),(int)Math.ceil(maxX)+1),bottom=Math.min(bitmap.getHeight(),(int)Math.ceil(maxY)+1),width=right-left,height=bottom-top;
  if(width<1||height<1)return Float.NaN;
  int[] pixels=new int[width*height];bitmap.getPixels(pixels,0,width,left,top,width,height);
  for(int i=0;i<xs.length;i++){xs[i]-=left;ys[i]-=top;}
  return TeethEvidence.measure(pixels,width,height,xs,ys);
 }
 void close(){closed=true;worker.execute(()->{if(landmarker!=null)landmarker.close();});worker.shutdown();}
}
