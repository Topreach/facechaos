package com.facechaos.mobile;

/** Original five-world flight course. Heights are screen-normalized: zero is up. */
final class VoiceCourse {
 static final float BODY_RADIUS=24;
 static final String[] WORLDS={"Cloud Meadow","Jungle Ruins","Clockwork Canyon","Frost Caverns","Ember Keep"};
 static String world(int stage){return WORLDS[Math.max(0,Math.min(4,stage-1))];}
 static int obstacle(int stage,int encounter){return (stage-1)*2+(stage==1?0:encounter%2);}
 static String obstacleName(int stage,int encounter){return new String[]{"Cloud hoop","Cloud hoop","Hanging vines","Thorn ridge","Moving pistons","Wind tunnel","Ice ceiling","Drifting ice gate","Lava jet","Moving firebars"}[obstacle(stage,encounter)];}
 static float target(int stage,int encounter,float phase){
  switch(obstacle(stage,encounter)){
   case 2:return .68f;case 3:return .28f;
   case 4:return .5f+.14f*(float)Math.sin(phase*.9f+encounter);
   case 5:return .5f+.12f*(float)Math.sin(encounter*1.7f);
   case 6:return .7f;
   case 7:return .5f+.16f*(float)Math.sin(phase*.75f+encounter);
   case 8:return .28f;
   case 9:return .5f+.18f*(float)Math.sin(phase*.8f+encounter);
   default:return .5f+.15f*(float)Math.sin(encounter*1.7f);
  }
 }
 static float wind(int stage,int encounter,float phase){return obstacle(stage,encounter)==5?.12f*(float)Math.sin(phase*1.1f):0;}
 static String hint(int stage,int encounter){switch(obstacle(stage,encounter)){
  case 2:return "Stay low beneath the vines";case 3:return "Rise above the thorns";
  case 4:return "Follow the moving piston gap";case 5:return "Counter the canyon wind";
  case 6:return "Stay low beneath the ice";case 7:return "Follow the drifting ice gap";
  case 8:return "Rise above the lava jet";case 9:return "Follow the moving firebar gap";
  default:return "Fly through the star hoop";
 }}
}
