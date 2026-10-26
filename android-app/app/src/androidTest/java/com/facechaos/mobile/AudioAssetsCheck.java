package com.facechaos.mobile;

import android.app.Instrumentation;
import android.media.SoundPool;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/** Decode packaged assets without playing any audio or opening a microphone. */
final class AudioAssetsCheck {
 static String run(Instrumentation test)throws Exception{
  int[] resources=GameAudio.resources();CountDownLatch loaded=new CountDownLatch(resources.length);AtomicReference<String> failure=new AtomicReference<>();SoundPool[] pool=new SoundPool[1];
  test.runOnMainSync(()->{pool[0]=new SoundPool.Builder().setMaxStreams(1).build();pool[0].setOnLoadCompleteListener((owner,id,status)->{if(status!=0)failure.set("Audio decode status "+status);loaded.countDown();});for(int resource:resources)pool[0].load(test.getTargetContext(),resource,1);});
  try{if(!loaded.await(15,TimeUnit.SECONDS))throw new AssertionError("Audio asset decode timed out");if(failure.get()!=null)throw new AssertionError(failure.get());}
  finally{test.runOnMainSync(()->pool[0].release());}
  return "PASS: all 12 original music/effect assets decoded, without audio playback";
 }
}
