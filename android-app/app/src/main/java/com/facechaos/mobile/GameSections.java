package com.facechaos.mobile;

/** Per-game journeys. Quizzes, memory and animal rhythm rounds do not need a landscape course. */
final class GameSections {
 static final int MEADOW=0,JUNGLE=1,DESERT=2,FROST=3,CLOCKWORK=4,EMBER=5,SPACE=6,NEON=7,COAST=8,CITY=9,CANDY=10;
 static final class Section {
  final String name,brief;final int biome;
  Section(String name,int biome,String brief){this.name=name;this.biome=biome;this.brief=brief;}
 }
 private static Section s(String name,int biome,String brief){return new Section(name,biome,brief);}
 private static final Section[][] ALL=new Section[15][];
 static {
  ALL[0]=new Section[]{s("Petal Parade",MEADOW,"Learn five expressions: mouth, smile, teeth, wide eyes and face squeeze."),s("Dune Doorways",DESERT,"Read a different face order as the gates arrive faster."),s("Crystal Crossing",FROST,"Watch the gate carefully; the order changes again."),s("Neon Portals",NEON,"Faster portals leave less time to match and relax."),s("Ember Arches",EMBER,"One life: match every gate in the final parade.")};
  ALL[1]=new Section[]{s("Cookie Trail",CANDY,"Learn low and high jumps, smile shields and cookie chomps."),s("Bramble Bakery",JUNGLE,"Jump the thorn bundles, shield the seeds and chomp cookies."),s("Frosted Citadel",FROST,"Ice hurdles and snow crystals arrive in tighter action windows."),s("Clockwork Kitchen",CLOCKWORK,"Jump gear hurdles and block cog projectiles at a faster pace."),s("Dragon Oven",EMBER,"Clear ember hurdles and fire sparks with one life.")};
  ALL[2]=new Section[]{s("Daisy Courtyard",MEADOW,"Smile to plant, blink to bloom: grow six flowers."),s("Rainforest Nursery",JUNGLE,"Plant a lush garden with a shorter response window."),s("Oasis Conservatory",DESERT,"Grow an oasis using the same comfortable smile and blink."),s("Snowdrop Glasshouse",FROST,"Bring colour to the winter garden before each cue expires."),s("Moonflower Terrace",SPACE,"Complete the final garden with one life.")};
  ALL[4]=new Section[]{s("Simple - Ember Trail",MEADOW,"Gentle short sounds cross the first scrolling gaps and small steps."),s("Difficult - Fire River",COAST,"Higher ledges and twin fire pits require more sound power."),s("Hard - Furnace Canyon",DESERT,"Tall towers mix with changing heights and faster approaching gaps."),s("Extreme - Inferno Waltz",EMBER,"Drifting ledges, narrow towers and fast fire pits demand precise short sounds."),s("Finale - Neon Volcano",NEON,"The fastest mixed course: clear all eight obstacles with one life.")};
  ALL[5]=new Section[]{s("Moonlight Duet",SPACE,"Smile, then hum to light three star pairs."),s("Aurora Dance",FROST,"Light a different constellation before each cue expires."),s("Sunset Serenade",COAST,"Smile and sound remain separate turns at a faster pace."),s("Nebula Waltz",NEON,"Follow the new constellation through shorter cue windows."),s("Starlight Crown",SPACE,"Light the final constellation with one life.")};
  ALL[6]=new Section[]{s("Berry Picnic",MEADOW,"Catch berries and stay closed for peppers."),s("Forest Feast",JUNGLE,"The pepper order changes: watch each incoming item."),s("Sunset Boardwalk",COAST,"A new treat order and shorter catch window test your timing."),s("Frost Fair",FROST,"Catch berries in the faster winter parade."),s("Midnight Market",NEON,"Catch all six berries and avoid every pepper with one life.")};
  ALL[7]=new Section[]{s("Garden Workshop",MEADOW,"Smile when the block reaches the centre guide."),s("Harbour Heights",COAST,"Build higher as the blocks move faster."),s("Canyon Columns",DESERT,"The block motion gains a second sway; watch the guide."),s("Crystal Spire",FROST,"Place blocks accurately with a narrower overlap window."),s("Skyline Summit",CITY,"Place all eight blocks with one life.")};
  ALL[8]=new Section[]{s("Moon Dock",SPACE,"Charge gently, then go quiet inside the gold launch zone."),s("Coral Orbit",COAST,"A different destination changes the required power."),s("Amber Belt",DESERT,"The landing power window gets narrower."),s("Ice Satellite",FROST,"Launch to the frozen satellite at a faster pace."),s("Solar Beacon",EMBER,"Land all six launches with one life.")};
  ALL[11]=new Section[]{s("Twilight Pond",COAST,"Hold a gentle sound in gold to light the lanterns."),s("Fern Hollow",JUNGLE,"Keep the sound steady for slightly longer."),s("Desert Lanterns",DESERT,"Light the oasis without raising your voice."),s("Winter Lights",FROST,"Stay inside the tighter sound range in the winter garden."),s("Aurora Sanctuary",SPACE,"Light all six lanterns with one life.")};
  ALL[12]=new Section[]{s("Palm Coast Circuit",COAST,"Learn to control speed and beat the CPU over 450 metres."),s("Dune Sprint",DESERT,"The desert opponent sets a stronger pace."),s("Alpine Circuit",FROST,"Keep your throttle steady against a faster CPU."),s("Neon City Cup",NEON,"Race the city lights against the stronger rival."),s("Volcanic Grand Prix",EMBER,"Beat the fastest CPU to win the final cup.")};
  ALL[13]=new Section[]{s("Palm Boulevard",COAST,"Calibrate, then move toward each clear lane."),s("Desert Freightway",DESERT,"Traffic comes faster through the dunes."),s("Alpine Pass",FROST,"Two lanes can be blocked; read the remaining opening."),s("Neon Metro",CITY,"Dodge the faster city traffic with precise head steering."),s("Ember Expressway",EMBER,"Clear every traffic wave with one life.")};
 }
 static boolean applies(int mode){return mode>=0&&mode<ALL.length&&ALL[mode]!=null;}
 static Section at(int mode,int stage){if(!applies(mode))throw new IllegalArgumentException("No journey for game "+mode);return ALL[mode][Math.max(0,Math.min(4,stage-1))];}
 private static final int[][] FACE_ORDER={{0,1,2,3,4},{1,3,0,4,2},{2,4,1,0,3},{3,0,4,2,1},{4,2,3,1,0}};
 private static final int[] HOP_PATTERN={0b10101010,0b11001100,0b10110100,0b11010010,0b10101100};
 private static final int[] PEPPERS={0b100100100,0b010010100,0b100010100,0b010100100,0b100010100};
 static int faceAt(int stage,int score){return FACE_ORDER[Math.max(0,Math.min(4,stage-1))][Math.floorMod(score,5)];}
 static boolean highNote(int stage,int encounter){return (HOP_PATTERN[Math.max(0,Math.min(4,stage-1))]&(1<<Math.floorMod(encounter,8)))!=0;}
 static boolean treat(int stage,int encounter){return (PEPPERS[Math.max(0,Math.min(4,stage-1))]&(1<<Math.floorMod(encounter,9)))==0;}
}
