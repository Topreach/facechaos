package com.facechaos.mobile;

/** Shared failure policy used by the rules, instructions and checks. */
final class GamePolicy {
 static boolean singleLife(int mode){return mode==0||mode==1||mode==3||mode==4||mode==7||mode==8||mode==13;}
 static int lives(int mode,int stage){return singleLife(mode)||stage==5?1:3;}
 static String describe(MiniGame game){
  if(game.mode==12)return "First across the finish wins. Losing the race ends the round.";
  if(singleLife(game.mode))return "One life. The first collision or failed obstacle ends the round.";
  if(game.mode==6)return "Eating a pepper ends the round immediately. Missed treats cost a life. The round ends if the target becomes unreachable.";
  if(game.mode==9||game.mode==11)return "Missed cues cost a life. The round also ends if the target becomes unreachable.";
  return game.stage==5?"Final challenge: one life. A miss ends the round.":"Misses cost a life. Losing all three lives ends the round.";
 }
 static boolean attainable(MiniGame game){
  int first=game.encounter+(game.resolved?1:0),remaining;
  if(game.mode==6){remaining=0;for(int i=first;i<9;i++)if(GameSections.treat(game.stage,i))remaining++;}
  else if(game.mode==9)remaining=Math.max(0,AnimalPatterns.cues(game.stage)-first);
  else if(game.mode==11)remaining=Math.max(0,6-first);
  else return true;
  return game.score+remaining>=game.goal();
 }
}
