package com.facechaos.mobile;
import android.content.Context;
import android.content.SharedPreferences;
final class SoundInputSettingsCheck {
 static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
 static String run(Context context){
  String file="sound_input_test_only";SharedPreferences prefs=context.getSharedPreferences(file,0);
  try{
   require(prefs.edit().clear().putBoolean("microphone",false).commit(),"Initialize test prefs");
   require(SoundInputSettings.enabled(prefs,11),"Firefly is sound-only regardless of shared setting");SoundInputSettings.remember(prefs,11,false);require(SoundInputSettings.enabled(prefs,11),"Firefly cannot switch to touch");
   require(SoundInputSettings.enabled(prefs,12),"Sound Grand Prix defaults to voice despite shared microphone being off");
   require(SoundInputSettings.enabled(prefs,3),"Voice Runner retains voice default");
   require(SoundInputSettings.enabled(prefs,8),"Comet defaults to microphone");SoundInputSettings.remember(prefs,8,false);SoundInputSettings.remember(prefs,4,true);require(SoundInputSettings.enabled(prefs,8),"Comet remains sound-only when a legacy touch preference exists");SoundInputSettings.remember(prefs,8,true);SoundInputSettings.remember(prefs,4,false);require(SoundInputSettings.enabled(prefs,8),"Other games do not disable Comet sound");
   require(SoundInputSettings.enabled(prefs,9),"Animal Choir defaults to microphone");SoundInputSettings.remember(prefs,9,false);SoundInputSettings.remember(prefs,4,true);require(!SoundInputSettings.enabled(prefs,9),"Animal touch practice choice is independent");SoundInputSettings.remember(prefs,9,true);SoundInputSettings.remember(prefs,4,false);require(SoundInputSettings.enabled(prefs,9),"Other games do not turn Animal Choir sound off");
   require(!SoundInputSettings.enabled(prefs,4),"Melody explicit practice choice persists");
   prefs.edit().remove("melody_hop_microphone").commit();require(SoundInputSettings.enabled(prefs,4),"Melody defaults to sound despite legacy shared setting");
   SoundInputSettings.remember(prefs,5,false);require(SoundInputSettings.enabled(prefs,4),"Other games cannot disable Melody sound");
   SoundInputSettings.remember(prefs,12,false);require(!SoundInputSettings.enabled(prefs,12)&&SoundInputSettings.enabled(prefs,3),"Race touch selection is independent");
   SoundInputSettings.remember(prefs,4,true);require(!SoundInputSettings.enabled(prefs,12),"Another game cannot overwrite race touch choice");
   SoundInputSettings.remember(prefs,12,true);SoundInputSettings.remember(prefs,4,false);
   require(SoundInputSettings.enabled(prefs,12),"Race stays voice-enabled when optional sound is switched off elsewhere");
   SoundInputSettings.remember(prefs,3,false);require(SoundInputSettings.enabled(prefs,12),"Voice Runner does not change race preference");
   require(prefs.edit().commit()&&SoundInputSettings.enabled(context.getSharedPreferences(file,0),12),"Race choice persists");
   return "PASS: race microphone on by default, explicit touch choice remembered, independent settings across games; player preferences untouched";
  }finally{context.deleteSharedPreferences(file);}
 }
}
