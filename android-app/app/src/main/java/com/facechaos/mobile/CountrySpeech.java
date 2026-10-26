package com.facechaos.mobile;

import android.content.*;
import android.os.*;
import android.speech.*;
import android.util.Log;
import java.util.*;

/** Bounded utterance with installed-English selection and one default-provider fallback. */
final class CountrySpeech {
 interface Listener {void ready();void processing();void answer(String text);void failed(String message);}
 interface Support {void installed(List<String> languages);void error(int code);}
 interface Engine {void support(Intent intent,Support callback);void start(Intent intent);void close();}
 interface Factory {Engine create(boolean local,RecognitionListener events);}
 private final Listener listener;private final Factory factory;
 private final boolean localAvailable,canQuery;
 private final Handler handler=new Handler(Looper.getMainLooper());
 private Engine engine;private boolean closed,started,local;
 private int generation;
 private Runnable supportTimeout;
 private final Runnable timeout=()->fail("Speech took too long. Tap to retry or type your answer.");
 static boolean available(Context context){return SpeechRecognizer.isRecognitionAvailable(context)||Build.VERSION.SDK_INT>=31&&SpeechRecognizer.isOnDeviceRecognitionAvailable(context);}
 CountrySpeech(Context context,Listener listener){this(listener,new AndroidFactory(context),Build.VERSION.SDK_INT>=31&&SpeechRecognizer.isOnDeviceRecognitionAvailable(context),Build.VERSION.SDK_INT>=33);}
 CountrySpeech(Listener listener,Factory factory,boolean localAvailable,boolean canQuery){this.listener=listener;this.factory=factory;this.localAvailable=localAvailable;this.canQuery=canQuery;}
 static Intent request(String language,boolean local){return new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM).putExtra(RecognizerIntent.EXTRA_LANGUAGE,language).putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,false).putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1).putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE,local);}
 void start(){if(closed||started)return;started=true;handler.postDelayed(timeout,13000);open(localAvailable);}
 private boolean active(int token){return !closed&&generation==token;}
 private void open(boolean onDevice){
  clearEngine();local=onDevice;final int token=++generation;
  try{
   engine=factory.create(onDevice,new Events(token));
   if(!canQuery){listen(token,"en-US");return;}
   final boolean[] pending={true};
   supportTimeout=()->{if(active(token)&&pending[0]){pending[0]=false;if(onDevice)open(false);else listen(token,"en-US");}};
   handler.postDelayed(supportTimeout,2000);
   engine.support(request("en-US",onDevice),new Support(){
    public void installed(List<String> languages){if(!active(token)||!pending[0])return;pending[0]=false;String english=SpeechPolicy.installedEnglish(languages);Log.i("FaceChaosSpeech",(onDevice?"local":"default")+" installed English="+english);if(onDevice&&english==null)open(false);else listen(token,english==null?"en-US":english);}
    public void error(int code){if(!active(token)||!pending[0])return;pending[0]=false;Log.i("FaceChaosSpeech","Support query error="+code+" local="+onDevice);if(onDevice)open(false);else listen(token,"en-US");}
   });
  }catch(RuntimeException error){Log.w("FaceChaosSpeech","Provider initialization failed: "+error.getClass().getSimpleName());if(onDevice)open(false);else fail("Speech provider could not start. Tap to retry or type.");}
 }
 private void listen(int token,String language){if(!active(token))return;if(supportTimeout!=null){handler.removeCallbacks(supportTimeout);supportTimeout=null;}Log.i("FaceChaosSpeech","Starting "+(local?"on-device":"default")+" language="+language);try{engine.start(request(language,local));}catch(RuntimeException error){if(local)open(false);else fail("Speech provider could not start. Tap to retry or type.");}}
 private void clearEngine(){if(supportTimeout!=null){handler.removeCallbacks(supportTimeout);supportTimeout=null;}if(engine!=null){Engine previous=engine;engine=null;generation++;previous.close();}}
 void close(){if(closed)return;closed=true;handler.removeCallbacks(timeout);clearEngine();}
 private void fail(String message){if(closed)return;close();listener.failed(message);}
 private final class Events implements RecognitionListener {
  final int token;Events(int token){this.token=token;}
  public void onReadyForSpeech(Bundle params){if(active(token))listener.ready();}
  public void onResults(Bundle results){if(!active(token))return;ArrayList<String> words=results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);String text=words==null||words.isEmpty()?"":words.get(0);close();if(text.isEmpty())listener.failed("No words heard. Tap to retry or type.");else listener.answer(text);}
  public void onError(int error){if(!active(token))return;Log.w("FaceChaosSpeech","Recognition error="+error+" local="+local);if(local&&SpeechPolicy.fallback(error)){listener.processing();open(false);}else fail(SpeechPolicy.errorMessage(error));}
  public void onEndOfSpeech(){if(active(token))listener.processing();}
  public void onBeginningOfSpeech(){} public void onRmsChanged(float rms){} public void onBufferReceived(byte[] buffer){} public void onPartialResults(Bundle results){} public void onEvent(int type,Bundle params){}
 }
 private static final class AndroidFactory implements Factory {
  final Context context;AndroidFactory(Context context){this.context=context;}
  public Engine create(boolean local,RecognitionListener events){
   SpeechRecognizer recognizer=local&&Build.VERSION.SDK_INT>=31?SpeechRecognizer.createOnDeviceSpeechRecognizer(context):SpeechRecognizer.createSpeechRecognizer(context);recognizer.setRecognitionListener(events);
   return new Engine(){
    public void support(Intent intent,Support callback){if(Build.VERSION.SDK_INT<33){callback.error(14);return;}recognizer.checkRecognitionSupport(intent,context.getMainExecutor(),new RecognitionSupportCallback(){public void onSupportResult(RecognitionSupport support){callback.installed(support.getInstalledOnDeviceLanguages());}public void onError(int code){callback.error(code);}});}
    public void start(Intent intent){recognizer.startListening(intent);}
    public void close(){recognizer.cancel();recognizer.destroy();}
   };
  }
 }
}
