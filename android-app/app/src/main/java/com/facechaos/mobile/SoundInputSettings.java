package com.facechaos.mobile;
import android.content.SharedPreferences;
/** Dedicated defaults for games whose primary mechanic is live voice control. */
final class SoundInputSettings {
 private static String key(int mode){return mode==4?"melody_hop_microphone":mode==3?"voice_runner_microphone":mode==12?"grand_prix_microphone":mode==9?"animal_choir_microphone":mode==8?"comet_launch_microphone":"microphone";}
 static boolean enabled(SharedPreferences preferences,int mode){return MiniGame.requiresMicrophone(mode)||preferences.getBoolean(key(mode),mode==3||mode==4||mode==8||mode==9||mode==12);}
 static void remember(SharedPreferences preferences,int mode,boolean enabled){if(!MiniGame.requiresMicrophone(mode))preferences.edit().putBoolean(key(mode),enabled).apply();}
}
