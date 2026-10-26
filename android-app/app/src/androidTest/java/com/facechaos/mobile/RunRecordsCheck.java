package com.facechaos.mobile;
import android.content.Context;
import android.content.SharedPreferences;
/** Uses a dedicated temporary preference file, preserving the player's records. */
final class RunRecordsCheck {
 static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
 static String run(Context context){
  String file="run_records_test_only";SharedPreferences prefs=context.getSharedPreferences(file,0);
  try{
   require(prefs.edit().clear().commit(),"Initialize test records");
   for(int mode=0;mode<15;mode++){
    require(RunRecords.best(prefs,mode)==0,"No record inherited from another game");
    require(RunRecords.save(prefs,mode,5)==5,"First run saved");
    require(RunRecords.save(prefs,mode,3)==5,"Lower run must not replace best");
    require(RunRecords.save(prefs,mode,5)==5,"Tie keeps best");
    require(RunRecords.save(prefs,mode,9+mode)==9+mode,"Higher run replaces record");
   }
   require(prefs.edit().commit(),"Flush records to disk");
   for(int mode=0;mode<15;mode++)require(RunRecords.best(context.getSharedPreferences(file,0),mode)==9+mode,"Saved per-game record reloads");
   GameOverlay view=new GameOverlay(context);view.setPersistenceEnabled(false);
   for(int mode=0;mode<15;mode++){
    view.select(mode,4,12);view.game.coins=3;view.game.finished=true;view.game.won=false;
    view.restart();require(view.game.stage==1&&view.game.coins==0&&view.game.runCoins()==0&&view.game.hearts==3&&view.game.countdown==2,"Retry is a fresh Stage 1 for game "+mode);
    view.select(mode,2,7);require(view.game.runCoins()==7,"Successful stage transition preserves run coins");
   }
   return "PASS: 15 independent persistent coin records, lower/tied/higher replacement, reload, fresh Stage 1 retries and carried run totals; player records untouched";
  }finally{context.deleteSharedPreferences(file);}
 }
}
