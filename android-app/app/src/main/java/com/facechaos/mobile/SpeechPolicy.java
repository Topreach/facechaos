package com.facechaos.mobile;
import java.util.List;
import java.util.Locale;

/** Service availability does not imply a particular language model is installed. */
final class SpeechPolicy {
 static String installedEnglish(List<String> installed){
  if(installed==null)return null;
  for(String language:installed)if(language!=null&&language.replace('_','-').equalsIgnoreCase("en-US"))return "en-US";
  for(String language:installed)if(language!=null){String tag=language.replace('_','-');if(tag.toLowerCase(Locale.ROOT).startsWith("en-"))return tag;}
  return null;
 }
 static boolean fallback(int error){return error==1||error==2||error==4||error==5||error==11||error==12||error==13||error==14||error==15;}
 static String errorMessage(int error){
  switch(error){
   case 1:case 2:return "Speech needs a connection. Connect and tap to retry.";
   case 3:return "Microphone could not open. Close other recording apps.";
   case 6:case 7:return "Didn't catch that. Tap to retry or type.";
   case 8:return "Speech is busy. Wait a moment, then tap to retry.";
   case 9:return "Microphone access is off. Enable it or tap to type.";
   case 10:return "Too many speech requests. Wait, then tap to retry.";
   case 12:case 13:return "English speech model missing. Set up English or type.";
   default:return "Speech provider error "+error+". Tap to retry or type.";
  }
 }
}
