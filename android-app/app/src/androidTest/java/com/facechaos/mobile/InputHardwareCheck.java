package com.facechaos.mobile;
import android.app.Instrumentation;
import android.Manifest;
import android.content.pm.PackageManager;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
/** Real microphone acquisition only. No PCM is retained; this is not a voice-accuracy test. */
final class InputHardwareCheck {
 static String run(Instrumentation check)throws Exception{
  String assets=AudioAssetsCheck.run(check);
  if(check.getTargetContext().checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)return assets+"\nNOT TESTED: microphone permission missing";
  CountDownLatch calibrated=new CountDownLatch(1);AtomicBoolean failed=new AtomicBoolean();AtomicInteger frames=new AtomicInteger();
  MicrophoneInput mic=new MicrophoneInput(new MicrophoneInput.Listener(){public void level(float level,float strength,boolean active,boolean ready){frames.incrementAndGet();if(ready)calibrated.countDown();}public void failed(){failed.set(true);calibrated.countDown();}});
  try{mic.start();if(!calibrated.await(8,TimeUnit.SECONDS)||failed.get()||frames.get()<20)throw new AssertionError("Real microphone failed to acquire/calibrate: frames="+frames.get()+" failure="+failed.get());}
  finally{mic.stop();}
  return assets+"\nPASS: real phone microphone acquired and delivered at least 20 PCM-analysis callbacks; no samples saved. Comfortable voice levels, ambient-noise tolerance and rhythm accuracy still require a player test.";
 }
}
