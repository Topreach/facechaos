package com.facechaos.mobile;
import android.content.Context;
final class GameLanguage {
 static String action(Context c,MiniGame g){
  int id;
  if(g.finished)return g.stopped?c.getString(R.string.fc_stopped):g.won?(g.stage<5?c.getString(R.string.fc_stage_complete,g.stage):c.getString(R.string.fc_all_complete)):c.getString(R.string.fc_failed);
  if(g.paused)return c.getString(R.string.fc_paused);
  if(g.faceBlocked())return c.getString(g.mode==13?R.string.fc_centre_head:R.string.fc_find_face);
  if(g.microphoneMode&&!g.micReady)return c.getString(R.string.fc_calibrating);
  if(g.countdown>0)return c.getString(R.string.fc_ready,(int)Math.ceil(g.countdown));
  if(g.earnedCoin&&g.flash>0)return c.getString(R.string.fc_good);
  if(g.resolved&&(g.mode==8&&!g.cometLanded||g.mode==7&&g.stackDropping&&!g.stackLanded))return c.getString(R.string.fc_relax_next);
  if(g.resolved)return c.getString(g.melodyFalling||!g.earnedCoin?R.string.fc_follow_cue:R.string.fc_relax_next);
  switch(g.mode){
   case 0:id=g.target()==0?R.string.fc_mouth:g.target()==1?R.string.fc_smile:g.target()==2?R.string.fc_teeth:g.target()==3?R.string.fc_wide_eyes:R.string.fc_face_squeeze;break;
   case 1:id=g.target()==1?R.string.fc_smile:g.target()==2?R.string.fc_teeth:g.phase<g.windowOpens()?R.string.fc_wait_gold:g.highJump()?R.string.fc_high_hop:R.string.fc_low_hop;break;
   case 2:id=g.target()==0?R.string.fc_smile:R.string.fc_blink;break;
   case 3:id=g.height<g.corridor()?R.string.fc_quiet_down:R.string.fc_hum;break;
   case 4:id=g.phase<g.windowOpens()?R.string.fc_wait_gold:g.phase>g.windowCloses()?R.string.fc_relax_next:Float.isFinite(MelodyCourse.ceilingMargin(g.stage,g.encounter))?R.string.fc_ceiling:g.highJump()?R.string.fc_high_hop:R.string.fc_low_hop;break;
   case 5:id=g.target()==0?R.string.fc_smile:R.string.fc_hum;break;
   case 6:id=!g.snackIsTreat()?R.string.fc_closed:g.phase<g.snackWindowOpens()?R.string.fc_wait_gold:R.string.fc_mouth;break;
   case 7:id=g.stackAligned()?R.string.fc_smile_now:R.string.fc_wait_gold;break;
   case 8:id=g.cometInGold()?R.string.fc_quiet_launch:R.string.fc_hum_charge;break;
   case 9:id=R.string.fc_animal;break;
   case 10:id=g.memoryShowing()?R.string.fc_watch:R.string.fc_repeat;break;
   case 11:id=R.string.fc_gentle_glow;break;
   case 12:id=R.string.fc_race;break;
   case 13:int lane=g.racing.safeLane();id=lane<0?R.string.fc_left:lane>0?R.string.fc_right:R.string.fc_middle;break;
   default:id=R.string.fc_country;
  }
  return c.getString(id);
 }
 static String hint(Context c,MiniGame g){if(g.finished)return c.getString(R.string.fc_run_best,g.runCoins(),g.bestRunCoins);if(g.countdown>0)return AppText.summary(c,g);if(g.mode==14)return c.getString(R.string.fc_country);return c.getString(R.string.fc_progress,g.score,g.mode==1||g.mode==4?g.timedTarget():g.mode==3?g.voiceTarget():g.goal());}
}
