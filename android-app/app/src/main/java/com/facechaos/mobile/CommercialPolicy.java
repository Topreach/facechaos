package com.facechaos.mobile;

/** Commercial rules only. Not purchase verification and not yet activated in the app. */
final class CommercialPolicy {
 static final String PRO_PRODUCT_ID="facechaos_pro_lifetime";
 private static final int[] FREE_GAMES={0,4,7,12,13};
 enum Ownership { UNKNOWN, PENDING, VERIFIED, CANCELLED, REVOKED }
 static boolean pro(Ownership ownership){return ownership==Ownership.VERIFIED;}
 static boolean freeGame(int game){for(int id:FREE_GAMES)if(id==game)return true;return false;}
 static boolean canPlay(int game,Ownership ownership){return game>=0&&game<GameCatalog.ALL.length&&(freeGame(game)||pro(ownership));}
 static boolean watermark(Ownership ownership){return !pro(ownership);}
 static boolean adsAllowed(Ownership ownership,boolean consentReady,boolean familySafeConfigured,boolean betweenRounds,boolean recordingBusy,boolean microphoneActive,boolean modalOpen){
  return !pro(ownership)&&consentReady&&familySafeConfigured&&betweenRounds&&!recordingBusy&&!microphoneActive&&!modalOpen;
 }
 static final class AdBreaks {
  private final long sessionStart;
  private long lastShown=-1;
  private int completedRounds;
  AdBreaks(long now){sessionStart=now;}
  void roundFinished(boolean completedNormally){if(completedNormally)completedRounds++;}
  boolean eligible(long now){long reference=lastShown<0?sessionStart:lastShown;return now>=reference&&now-reference>=120_000&&completedRounds>=3;}
  void shown(long now){lastShown=now;completedRounds=0;}
 }
}
