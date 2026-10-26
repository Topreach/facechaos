package com.facechaos.mobile;

import android.app.Instrumentation;
import android.content.*;
import android.os.Build;
import android.speech.*;
import java.util.concurrent.*;

/** Queries supported models only. Never starts recognition or opens a microphone. */
final class SpeechSupportCheck {
 static String run(Instrumentation test)throws Exception {
  if(Build.VERSION.SDK_INT<33)return "Support queries require Android 13";
  StringBuilder result=new StringBuilder();
  for(boolean local:new boolean[]{true,false}){
   if(local&&!SpeechRecognizer.isOnDeviceRecognitionAvailable(test.getTargetContext())){result.append("On-device service absent\n");continue;}
   SpeechRecognizer[] recognizer=new SpeechRecognizer[1];CountDownLatch done=new CountDownLatch(1);String[] line={"query timed out"};
   test.runOnMainSync(()->{
    try{
     recognizer[0]=local?SpeechRecognizer.createOnDeviceSpeechRecognizer(test.getTargetContext()):SpeechRecognizer.createSpeechRecognizer(test.getTargetContext());
     Intent intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM).putExtra(RecognizerIntent.EXTRA_LANGUAGE,"en-US").putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE,local);
     recognizer[0].checkRecognitionSupport(intent,test.getTargetContext().getMainExecutor(),new RecognitionSupportCallback(){
      public void onSupportResult(RecognitionSupport support){line[0]="installed="+support.getInstalledOnDeviceLanguages()+"; pending="+support.getPendingOnDeviceLanguages()+"; downloadable="+support.getSupportedOnDeviceLanguages()+"; online="+support.getOnlineLanguages();done.countDown();}
      public void onError(int error){line[0]="support error="+error;done.countDown();}
     });
    }catch(RuntimeException error){line[0]=error.getClass().getSimpleName();done.countDown();}
   });
   done.await(8,TimeUnit.SECONDS);test.runOnMainSync(()->{if(recognizer[0]!=null)recognizer[0].destroy();});
   result.append(local?"ON-DEVICE: ":"DEFAULT: ").append(line[0]).append('\n');
  }return result.toString();
 }
}
