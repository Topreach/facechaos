package com.facechaos.mobile;
import java.util.*;
public final class GameSectionsCheck {
 static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
 public static void main(String[] args){
  int count=0;
  for(int mode=0;mode<GameCatalog.ALL.length;mode++){
   if(!GameSections.applies(mode))continue;count++;Set<String> names=new HashSet<>();
   for(int stage=1;stage<=5;stage++){
    GameSections.Section section=GameSections.at(mode,stage);MiniGame game=new MiniGame(mode,11,stage);
    require(names.add(section.name),"Five distinct section names for "+mode);require(section.biome>=0&&section.biome<=10&&!section.brief.isEmpty(),"Valid section scenery and instructions");
    require(game.stageName().equals(section.name)&&StageRules.describe(game).contains(section.name),"Section name appears in actual stage rules");
    require(game.stage==stage&&game.countdown==2&&game.coins==0,"Section selection preserves new-round rules");
   }
  }
  require(count==11,"Eleven appropriate games get journeys");
  for(int mode:new int[]{3,9,10,14})require(!GameSections.applies(mode),"Existing Voice Runner and round/quiz games are not overwritten");
  Set<String> faceOrders=new HashSet<>(),hopOrders=new HashSet<>(),pepperOrders=new HashSet<>();
  for(int stage=1;stage<=5;stage++){
   Set<Integer> faces=new HashSet<>();StringBuilder f=new StringBuilder(),h=new StringBuilder(),p=new StringBuilder();int treats=0,high=0;
   for(int n=0;n<5;n++){MiniGame gate=new MiniGame(0,0,stage);gate.score=n;faces.add(gate.target());f.append(gate.target());}require(faces.size()==5,"All five Emoji Gates controls remain present");faceOrders.add(f.toString());
   for(int n=0;n<8;n++){MiniGame hop=new MiniGame(4,0,stage);hop.encounter=n;if(hop.highJump())high++;h.append(MelodyCourse.height(stage,n)).append(",");}require(stage!=1||high==0,"Simple course starts with gentle jumps");hopOrders.add(h.toString());
   for(int n=0;n<9;n++){MiniGame snack=new MiniGame(6,0,stage);snack.encounter=n;if(snack.snackIsTreat())treats++;p.append(snack.snackIsTreat()?'B':'P');}require(treats==6,"Every snack world keeps six berries and three peppers");pepperOrders.add(p.toString());
  }
  require(faceOrders.size()==5&&hopOrders.size()==5&&pepperOrders.size()>=4,"Gameplay patterns genuinely change between sections");
  MiniGame plain=new MiniGame(7,0,1),sway=new MiniGame(7,0,3);plain.phase=sway.phase=.8f;require(Math.abs(plain.stackOffset()-sway.stackOffset())>.01,"Later stack sections add a second sway");
  System.out.println("PASS: 55 named sections, appropriate exclusions, matching stage instructions, unique face/hop patterns, balanced snack schedules and real stack sway");
 }
}
