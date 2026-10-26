package com.facechaos.mobile;
import android.content.SharedPreferences;
/** Each game's single-run coin record is independent of its score and lifetime wallet. */
final class RunRecords {
 static int best(SharedPreferences preferences,int mode){return preferences.getInt("best_run_coins_"+mode,0);}
 static int save(SharedPreferences preferences,int mode,int coins){
  int previous=best(preferences,mode);
  if(coins>previous){preferences.edit().putInt("best_run_coins_"+mode,coins).apply();return coins;}
  return previous;
 }
}
