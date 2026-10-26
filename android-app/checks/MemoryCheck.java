package com.facechaos.mobile;

/** Memory stages and replay consistency, without camera or microphone access. */
public final class MemoryCheck {
 private static void require(boolean b,String m){if(!b)throw new AssertionError(m);}
 public static void main(String[] args){
  MiniGame game=new MiniGame(10,17);game.countdown=0;
  require(game.memoryShowDuration()>3,"Two-face sequence is shown slowly");
  game.phase=.6f;require(game.memoryWatchIndex()==0&&game.memoryFaceVisible(),"First face appears in its slot");
  game.phase=1.45f;require(!game.memoryFaceVisible(),"Neutral gap separates face prompts");
  for(int round=0;round<3;round++){
   require(game.memoryLength()==round+2,"Sequences grow from two to four");
   int[] before=new int[4];for(int i=0;i<4;i++)before[i]=game.memoryAt(i);
   game.phase=game.memoryShowDuration()+.1f;game.input((game.target()+1)%3);require(game.phase==0&&game.memoryIndex==0,"Wrong choice restarts watching");
   for(int i=0;i<4;i++)require(game.memoryAt(i)==before[i],"Retry keeps the same sequence");
   game.hearts=3;game.phase=game.memoryShowDuration()+.1f;
   for(int i=0;i<game.memoryLength();i++){game.input(game.target());if(!game.resolved)for(int frame=0;frame<30;frame++)game.step(.02f);}
   require(game.resolved&&game.coins==round+1,"One coin per completed sequence");
   while(game.resolved&&!game.finished)game.step(.02f);
  }
  require(game.won&&game.score==3,"Three sequences complete the game");
  System.out.println("PASS: paced memory stages, neutral gaps, same-sequence retries, progression and one coin per sequence");
 }
}
