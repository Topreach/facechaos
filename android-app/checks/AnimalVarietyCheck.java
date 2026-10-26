package com.facechaos.mobile;
import java.util.*;
public final class AnimalVarietyCheck {
 static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
 public static void main(String[] args){
  Set<String> names=new HashSet<>(),rhythms=new HashSet<>();Set<Integer> roster=new HashSet<>();int lastCount=0;
  require(AnimalPatterns.ALL.length==12,"Twelve animals available");
  for(int animal=0;animal<12;animal++){
   AnimalPatterns.Entry entry=AnimalPatterns.get(animal);require(names.add(entry.name),"Distinct animal name");require(rhythms.add(Arrays.toString(entry.calls)),"Distinct reference rhythm");
   short[] guide=AnimalGuide.samples(animal);require(guide.length>8000&&guide.length<64000,"Playable guide length for "+entry.name);long energy=0;for(short sample:guide)energy+=Math.abs((int)sample);require(energy>100000,"Non-silent example for "+entry.name);
  }
  for(int stage=1;stage<=5;stage++){Set<Integer> seen=new HashSet<>();int cues=AnimalPatterns.cues(stage);require(cues>=lastCount,"More calls in later stages");lastCount=cues;for(int n=0;n<cues;n++){int animal=AnimalPatterns.at(stage,n);require(seen.add(animal),"No repeated animal in a stage");roster.add(animal);}}
  require(roster.size()==12,"All twelve animals appear during the campaign");
  require(AnimalPatterns.cues(1)==6&&AnimalPatterns.cues(5)==12,"Learning stage is shorter than finale");
  System.out.println("PASS: twelve distinct named animals and reference rhythms, twelve non-silent guides, varied stage order, no stage duplicates and full campaign roster coverage");
 }
}
