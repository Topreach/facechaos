package com.facechaos.mobile;
import java.util.*;
public final class FlagQuizCheck {
 static void require(boolean ok,String message){if(!ok)throw new AssertionError(message);}
 public static void main(String[] args){
  for(FlagQuiz.Country country:FlagQuiz.COUNTRIES){
   require(country.matches(country.name.toUpperCase(Locale.ROOT)+"!"),"Case and punctuation: "+country.name);
   require(country.matches("This is "+country.name),"Spoken sentence: "+country.name);
   require(!country.matches("not "+country.name)&&!country.matches(country.name+" Germany"),"Reject misleading or multiple names");
  }
  require(FlagQuiz.COUNTRIES[9].matches("The Netherlands"),"Accepted country alias");
  require(!FlagQuiz.COUNTRIES[0].matches("Japanese"),"Do not use substring matching");
  float previous=100;
  for(int stage=1;stage<=5;stage++)for(int seed=0;seed<20;seed++){
   MiniGame g=new MiniGame(14,seed,stage);require(g.racing==null,"Quiz cannot enter racing rules");
   g.flagAnswer(g.flags.country(0).name);require(g.score==0,"Countdown blocks answer");g.countdown=0;
   Set<String> seen=new HashSet<>();for(int card=0;card<g.flags.deck.length;card++)require(seen.add(g.flags.country(card).code),"No repeats in shuffled deck");
   g.paused=true;g.flagAnswer(g.flags.country(0).name);require(g.score==0,"Paused answer rejected");g.paused=false;
   g.flagAnswer("");require(g.hearts==(stage==5?1:3),"Empty input is not a wrong country");
   g.quizListening=true;g.quizProcessing=true;float phase=g.phase;for(int i=0;i<100;i++)g.step(.02f);require(Math.abs(g.phase-phase)<.001,"Processing does not consume answer time");g.quizListening=false;g.quizProcessing=false;
   while(!g.finished){String expected=g.flags.country(g.encounter).name;int coins=g.coins;g.flagAnswer(expected);require(g.coins==coins+1,"Exactly one coin per correct flag");g.flagAnswer(expected);require(g.coins==coins+1,"Duplicate callback cannot earn again");for(int i=0;i<70;i++)g.step(.02f);}
   require(g.won&&g.coins==g.goal()&&g.coinBalance==g.coins,"Complete stage with saved coin balance");
   MiniGame wrong=new MiniGame(14,seed,stage);wrong.countdown=0;wrong.flagAnswer("Mars");require(wrong.score==0&&wrong.coins==0&&wrong.encounter==0,"Wrong answer keeps flag and earns nothing");require(wrong.hearts==(stage==5?0:2),"Wrong answer loses a life");
   MiniGame idle=new MiniGame(14,seed,stage);idle.countdown=0;while(!idle.finished)idle.step(.05f);require(!idle.won&&idle.coins==0,"Timed idle failure");
   if(seed==0){float limit=g.flagLimit()/g.pace();require(limit<previous,"Answer windows shorten every stage");previous=limit;}
  }
  MiniGame speaking=new MiniGame(14,0,5);speaking.countdown=0;speaking.quizListening=true;
  for(int i=0;i<10;i++)speaking.step(.02f);require(speaking.phase>0,"Timer runs while speaking");
  speaking.phase=speaking.flagLimit()-.01f;int attempt=speaking.flagAttempt;speaking.step(.02f);
  require(speaking.flagAttempt>attempt&&!speaking.quizListening&&!speaking.won,"Expired speech attempt invalidates callbacks");
  System.out.println("PASS: 24 flag identities; exact spoken/typed matching; aliases; 100 shuffled campaign paths; pause, timer, wrong-answer, duplicate and coin rules");
 }
}
