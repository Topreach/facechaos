package com.facechaos.mobile;
/** Searchable animal calls and their playable cartoon rhythms; not species classification. */
final class AnimalPatterns {
 static final class Entry {
  final String name,words;final float frequency;final float[] calls;
  Entry(String name,String words,float frequency,float... calls){this.name=name;this.words=words;this.frequency=frequency;this.calls=calls;}
  float duration(int call){return calls[Math.min(calls.length-1,Math.max(0,call))];}
  float minimum(int call){return duration(call)>=.55f?.48f:duration(call)>=.28f?.16f:.06f;}
  float maximum(int call){return duration(call)>=.55f?1.5f:duration(call)>=.28f?.8f:.6f;}
 }
 static final Entry[] ALL={
  new Entry("Dog","WOOF - WOOF",180,.18f,.18f),
  new Entry("Cat","MEE-OW",460,.8f),
  new Entry("Bird","TWEET - TWEET - TWEET",2400,.12f,.12f,.12f),
  new Entry("Cow","MOOO - MOOO",115,.65f,.65f),
  new Entry("Sheep","BAAA - BA",310,.65f,.18f),
  new Entry("Duck","QUACK - QUACK - QUACK",780,.32f,.32f,.32f),
  new Entry("Owl","HOO - HOO",350,.36f,.36f),
  new Entry("Frog","RIB - RIBBIIIT - RIB",160,.16f,.6f,.16f),
  new Entry("Rooster","COCK - A - DOODLE - DOOO",960,.14f,.14f,.18f,.6f),
  new Entry("Lion","ROAAR - RA - RA",95,.7f,.16f,.16f),
  new Entry("Monkey","OO - OO - OO - OO",620,.16f,.16f,.16f,.16f),
  new Entry("Elephant","PAAA - PA - PAAA",230,.6f,.16f,.6f)
 };
 static Entry get(int animal){return ALL[Math.floorMod(animal,ALL.length)];}
 static int cues(int stage){return Math.min(12,4+stage*2);}
 static int at(int stage,int encounter){int start=stage==1?0:stage==2?4:stage==3?8:3;return Math.floorMod(stage==5?11-encounter:start+encounter,ALL.length);}
 static String names(){StringBuilder result=new StringBuilder();for(Entry entry:ALL){if(result.length()>0)result.append(", ");result.append(entry.name);}return result.toString();}
 static boolean demoActive(int animal,float seconds){float start=.08f;for(float duration:get(animal).calls){if(seconds>=start&&seconds<start+duration)return true;start+=duration+.27f;}return false;}
}
