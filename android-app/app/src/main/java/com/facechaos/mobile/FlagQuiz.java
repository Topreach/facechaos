package com.facechaos.mobile;

import java.text.Normalizer;
import java.util.*;

/** Country identity and answer rules, independent of microphone and rendering. */
final class FlagQuiz {
 static final class Country {
  final String name,code; final String[] aliases;
  Country(String code,String name,String... aliases){this.code=code;this.name=name;this.aliases=aliases;}
  boolean matches(String answer){String text=normalize(answer);if(text.equals(normalize(name)))return true;for(String alias:aliases)if(text.equals(normalize(alias)))return true;return false;}
 }
 static final Country[] COUNTRIES={
  new Country("JP","Japan"),new Country("FR","France"),new Country("DE","Germany"),new Country("IT","Italy"),
  new Country("NG","Nigeria"),new Country("UA","Ukraine"),new Country("PL","Poland"),new Country("IE","Ireland","Republic of Ireland"),
  new Country("BE","Belgium"),new Country("NL","Netherlands","The Netherlands","Holland"),new Country("AT","Austria"),new Country("SE","Sweden"),
  new Country("FI","Finland"),new Country("DK","Denmark"),new Country("CH","Switzerland"),new Country("BD","Bangladesh"),
  new Country("RO","Romania"),new Country("HU","Hungary"),new Country("BG","Bulgaria"),new Country("EE","Estonia"),
  new Country("ID","Indonesia"),new Country("NO","Norway"),new Country("IS","Iceland"),new Country("PW","Palau")
 };
 final int[] deck;
 FlagQuiz(int seed,int stage){int size=Math.min(COUNTRIES.length,8+(stage-1)*4);List<Integer> ids=new ArrayList<>();for(int i=0;i<size;i++)ids.add(i);Collections.shuffle(ids,new Random(seed));deck=new int[size];for(int i=0;i<size;i++)deck[i]=ids.get(i);}
 Country country(int card){return COUNTRIES[deck[Math.floorMod(card,deck.length)]];}
 static String normalize(String input){if(input==null)return "";String text=Normalizer.normalize(input,Normalizer.Form.NFD).replaceAll("\\p{M}","").toLowerCase(Locale.ROOT).replaceAll("[^a-z ]"," ").trim().replaceAll(" +"," ");for(String prefix:new String[]{"the country is ","this is ","it is ","it s "})if(text.startsWith(prefix)){text=text.substring(prefix.length());break;}return text;}
}
