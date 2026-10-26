package com.facechaos.mobile;
/** No store, billing, ad network or real purchase is involved. */
public final class CommercialPolicyCheck {
 static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
 public static void main(String[] args){
  for(CommercialPolicy.Ownership state:CommercialPolicy.Ownership.values()){
   int unlocked=0;for(int game=0;game<GameCatalog.ALL.length;game++)if(CommercialPolicy.canPlay(game,state))unlocked++;
   require(unlocked==(state==CommercialPolicy.Ownership.VERIFIED?15:5),"Exactly five free games; only verified Pro unlocks the collection");
   require(CommercialPolicy.watermark(state)==(state!=CommercialPolicy.Ownership.VERIFIED),"Pending/cancelled/refunded purchases cannot remove watermark");
   require(!CommercialPolicy.canPlay(-1,state)&&!CommercialPolicy.canPlay(99,state),"Invalid game IDs never unlock");
   require(CommercialPolicy.adsAllowed(state,true,true,true,false,false,false)==(state!=CommercialPolicy.Ownership.VERIFIED),"Verified Pro removes all ads");
  }
  CommercialPolicy.Ownership free=CommercialPolicy.Ownership.UNKNOWN;
  require(!CommercialPolicy.adsAllowed(free,false,true,true,false,false,false),"Consent unresolved: no ad");
  require(!CommercialPolicy.adsAllowed(free,true,false,true,false,false,false),"No family-safe provider configuration: no ad");
  require(!CommercialPolicy.adsAllowed(free,true,true,false,false,false,false),"Never interrupt gameplay");
  require(!CommercialPolicy.adsAllowed(free,true,true,true,true,false,false),"Never interrupt recording or finalization");
  require(!CommercialPolicy.adsAllowed(free,true,true,true,false,true,false),"Never show an ad over a live microphone session");
  require(!CommercialPolicy.adsAllowed(free,true,true,true,false,false,true),"Never stack an ad over a purchase/parent/confirmation dialog");
  CommercialPolicy.AdBreaks breaks=new CommercialPolicy.AdBreaks(1000);
  for(int i=0;i<10;i++)breaks.roundFinished(false);require(!breaks.eligible(999999),"Stopping a game is not an ad trigger");
  for(int i=0;i<3;i++)breaks.roundFinished(true);require(!breaks.eligible(120999),"No ad before two minutes");require(breaks.eligible(121000),"Three completed rounds and two minutes enable a natural break");
  breaks.shown(121000);require(!breaks.eligible(999999),"An actually shown ad resets the completed-round counter");
  for(int i=0;i<3;i++)breaks.roundFinished(true);require(!breaks.eligible(240999)&&breaks.eligible(241000),"Two-minute repeat cap");
  System.out.println("PASS: five free games, verified-only Pro, watermark rules, child-safe/consent/recording guards and ad frequency; policy foundation only, not live monetization");
 }
}
