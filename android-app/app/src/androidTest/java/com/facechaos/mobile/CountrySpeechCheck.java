package com.facechaos.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.speech.*;
import java.util.*;

/** Production adapter state machine driven by fake engines: no microphone or audio playback. */
final class CountrySpeechCheck {
 static void require(boolean value,String text){if(!value)throw new AssertionError(text);}
 static final class Engine implements CountrySpeech.Engine {
  final boolean local;final RecognitionListener events;CountrySpeech.Support support;Intent request;int starts,closes;
  Engine(boolean local,RecognitionListener events){this.local=local;this.events=events;}
  public void support(Intent intent,CountrySpeech.Support callback){support=callback;}
  public void start(Intent intent){request=intent;starts++;}
  public void close(){closes++;}
 }
 static final class Factory implements CountrySpeech.Factory {
  final List<Engine> engines=new ArrayList<>();
  public Engine create(boolean local,RecognitionListener events){Engine engine=new Engine(local,events);engines.add(engine);return engine;}
  Engine last(){return engines.get(engines.size()-1);}
 }
 static final class Listener implements CountrySpeech.Listener {
  int ready,answers,failures;String text;
  public void ready(){ready++;} public void processing(){} public void answer(String text){answers++;this.text=text;} public void failed(String text){failures++;this.text=text;}
 }
 static Bundle result(String word){Bundle bundle=new Bundle();bundle.putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION,new ArrayList<>(Arrays.asList(word)));return bundle;}
 static String run(){
  Factory factory=new Factory();Listener listener=new Listener();CountrySpeech speech=new CountrySpeech(listener,factory,true,true);
  speech.start();Engine uk=factory.last();uk.support.installed(Arrays.asList("en-GB"));
  require(uk.starts==1&&"en-GB".equals(uk.request.getStringExtra(RecognizerIntent.EXTRA_LANGUAGE)),"Use Samsung's installed British English instead of missing US English");
  require(uk.request.getBooleanExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE,false),"Local engine stays offline");
  uk.support.installed(Arrays.asList("en-US"));require(uk.starts==1,"Duplicate support callback cannot restart capture");
  uk.events.onReadyForSpeech(new Bundle());uk.events.onPartialResults(result("Japan"));require(listener.ready==1&&listener.answers==0,"Partial transcript cannot answer");
  uk.events.onResults(result("Japan"));uk.events.onResults(result("France"));require(listener.answers==1&&"Japan".equals(listener.text)&&uk.closes==1,"One final result and engine teardown");
  factory=new Factory();listener=new Listener();speech=new CountrySpeech(listener,factory,true,true);speech.start();Engine missing=factory.last();missing.support.installed(Collections.emptyList());Engine normal=factory.last();
  require(!normal.local&&missing.closes==1,"Missing local model switches to default service");
  normal.support.error(14);require(normal.starts==1&&!normal.request.getBooleanExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE,true),"Unsupported support-query still allows default online-capable recognition");
  missing.events.onResults(result("Japan"));require(listener.answers==0,"Old provider callback rejected");normal.events.onError(2);require(listener.failures==1&&listener.text.contains("connection")&&factory.engines.size()==2,"Specific network failure with no fallback loop");
  factory=new Factory();listener=new Listener();speech=new CountrySpeech(listener,factory,true,true);speech.start();Engine broken=factory.last();broken.support.installed(Arrays.asList("en-US"));broken.events.onError(13);require(factory.engines.size()==2&&!factory.last().local,"Runtime local language failure falls back once");speech.close();factory.last().support.installed(Arrays.asList("en-GB"));require(factory.last().starts==0,"Pause/cancel blocks pending language query");
  for(int error:new int[]{3,6,7,8,9,10}){
   factory=new Factory();listener=new Listener();speech=new CountrySpeech(listener,factory,true,true);speech.start();Engine engine=factory.last();engine.support.installed(Arrays.asList("en-GB"));engine.events.onError(error);require(factory.engines.size()==1&&listener.failures==1,"Permission/audio/no-match errors do not switch microphones: "+error);
  }
  require("en-US".equals(SpeechPolicy.installedEnglish(Arrays.asList("en-GB","en-US"))),"Prefer installed US English when available");
  require(SpeechPolicy.installedEnglish(Arrays.asList("fr-FR"))==null,"Do not mistake non-English model for English");
  return "PASS: installed UK/US English selection, default fallback, service errors, stale/duplicate/partial callbacks and cancellation; fake engines only, no microphone opened";
 }
}
