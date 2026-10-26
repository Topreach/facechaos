package com.facechaos.mobile;
/** Playable camera-overlay games, with input capabilities stated explicitly. */
final class GameCatalog {
 static final class Entry {
  final String name,input,description,instructions;
  final int id,color;
  final boolean preview=true;
  Entry(int id,String name,String input,String description,String instructions,int color){
   this.id=id;this.name=name;this.input=input;this.description=description;this.instructions=instructions;this.color=color;
  }
  String controlBrief() {
   switch(id) {
    case 14:return "See the flag, then say its country in English.\nA correct country earns one coin and reveals the next flag.\nTap the card to retry speech or type an answer.\nLater stages have more flags and less time.";
    case 12:return "Sound volume sets your car speed.\nSoft sound: slower; stronger comfortable sound: faster.\nQuiet lets the car coast. Beat the CPU to the finish.\nWithout a microphone: hold to accelerate, release to coast.";
    case 13:return "Look centred to calibrate head steering.\nMove your head left or right toward a clear lane.\nReturn to centre for the middle lane.\nWith head mode off: tap the left, middle or right of the screen.";
    case 0:return "Jaw drop: open your mouth.\nClosed-lip smile: smile with lips together.\nShow teeth: make your front teeth visible comfortably.\nEyes wide open: widen both eyes with a relaxed mouth.\nFace squeeze: gently scrunch both eyes shut with your cheeks or nose for a brief moment. Relax between expressions.";
    case 1:return "Soft sound: low jump. Stronger sound: higher jump.\nSmile with lips together to shield against stars.\nBare teeth to chomp biscuits.";
    case 2:return "Smile to plant a seed.\nBlink once to make it blossom.\nRepeat for six flowers.";
    case 3:return "A gentle mmm lifts the aviator.\nQuiet lets it drift down.\nMatch the safe gap as each obstacle arrives.";
    case 6:return "Open mouth for strawberries.\nKeep your mouth closed for peppers.\nFace mode: open for treats; relax for peppers. Touch mode: tap for treats.";
    case 7:return "Smile with lips together when the block overlaps the gold centre mark.\nFace mode: smile to place. Touch mode: tap at the right moment.";
    case 9:return "Twelve animals: dog, cat, bird, cow, sheep, duck, owl, frog, rooster, lion, monkey and elephant.\nTap Hear pattern to listen with the round and microphone paused.\nCopy the rhythm and watch each beat marker light up. This checks timing, not animal identity.";
    case 10:return "Watch, then repeat each expression and return to neutral.\nWith face mode off, tap the illustrated answer cards.\nTeeth detection is experimental.";
    case 11:return "Hum gently to fly toward each lantern.\nKeep the sound meter in gold to move forward and light it.\nQuiet lets the firefly drift back. Microphone required; no touch controls.";
    case 8:return "Hum gently to fill the power meter.\nGo quiet inside the gold zone to launch. Watch the rocket fly and land.\nToo little power falls short; too much flies past. Each successful landing earns a coin.\nSound only: the phone microphone controls this game. Nothing to hold on screen.";
    case 4:return "Make a short sound in the highlighted window.\nSoft sound: low hop. Stronger volume: higher hop.\nGo quiet between jumps; no shouting.";
    default:return "Smile with lips closed to create a spark.\nThen hum gently to light a star.\nComplete three smile/sound pairs, one cue at a time.";
   }
  }
 }
 static final Entry[] ALL={
  new Entry(0,"Emoji Gates","Face","The expression parade","Match the face on each approaching gate before it reaches your hero. Relax after matching, then watch your hero pass through. Pass the stage target number of gates; losing all lives ends the round.",0xFFF7CF55),
  new Entry(1,"Expression Escape","Face beta + sound","A tiny hero, a delicious adventure","Jump over each world's hurdles, shield against projectiles and chomp biscuits. Act during the highlighted window. Clear the stage target from eight obstacles.",0xFFFFB28C),
  new Entry(2,"Bubble Garden","Face","Make a little world blossom","Alternate a smile to plant and a blink to bloom. Grow six flowers. No rapid gestures are required.",0xFFA9E7BB),
  new Entry(3,"Voice Runner","Sound","Five worlds, one voice-powered adventure","Hum gently to rise and stay quiet to drift down. Travel through Cloud Meadow, Jungle Ruins, Clockwork Canyon, Frost Caverns and Ember Keep. Match the safe gap as each obstacle arrives. Collect the stage target from eight stars; losing all lives ends the round. Each star earns one coin. Never shout.",0xFF91DDF8),
  new Entry(4,"Melody Hop","Sound","A moving course of fire, towers and drifting ledges","Sound volume controls hop height. Make a short soft sound for low notes, a stronger sound for high notes, then go quiet. Follow a scrolling course: ember gaps, raised steps, twin fire pits, tall towers and drifting ledges. Sound power increases gradually from Simple through Difficult, Hard, Extreme and Finale. Leap between islands above fire. Weak, missing or mistimed jumps fall into the flames and cost a life. Clear the stage target from eight hops.",0xFFD5B2FF),
  new Entry(5,"Star Duet","Face + sound","A conversation made of starlight","Create a smile spark, then hum gently to light a star. Complete three pairs (six cues) to light the constellation. Each pair earns one coin. Face and sound are sequential.",0xFFFFBDDE),
  new Entry(6,"Snack Parade","Face beta","Treats yes, peppers no!","Catch the stage target from 6 strawberries and avoid 3 peppers. Open your mouth only while a treat is in the catch window. Keep your mouth closed for peppers. Losing all lives ends the round. Each treat earns a coin.",0xFFFFBDAD),
  new Entry(7,"Smile Stack","Face beta","Build a tower with perfect timing","Smile when the moving block lines up with the gold centre mark. Relax your smile between blocks. Watch each block land and place the stage target from 8 blocks. Losing all lives ends the round. Each successful landing earns a coin.",0xFFAEDBD8),
  new Entry(8,"Comet Launch","Sound","A gentle hum, a little space adventure","Hum gently to charge. Go quiet when the power meter reaches the gold zone. Land the stage target from 6 rockets at this section's destination. Losing all lives ends the round. Each landing earns a coin.",0xFFD6C0FF),
  new Entry(9,"Animal Choir","Sound patterns","Twelve animals, one playful choir","Meet twelve animals: dog, cat, bird, cow, sheep, duck, owl, frog, rooster, lion, monkey and elephant. Copy each short/long call pattern. New animals and mixed rhythms appear in later stages. Each match earns a coin. Live mode checks sound rhythm and duration, not animal identity.",0xFFFFD6B1),
  new Entry(10,"Face Memory","Face memory beta","Remember a parade of faces","Watch a sequence, then repeat it with your face or the touch answer cards. Complete three sequences that grow during the stage. Losing all lives ends the round. Each sequence earns one coin. Face mode includes neutral calibration; teeth detection is experimental.",0xFFC4D8FF),
  new Entry(11,"Firefly Glow","Sound","Hum to guide a glowing firefly","Use your microphone to guide the firefly to six lanterns. Hum in the gold sound range to move forward; quiet makes it drift back. Each reached lantern earns a coin. Microphone required; touching cannot move the firefly.",0xFFDFD1F5),
  new Entry(12,"Sound Grand Prix","Sound race","Your voice is the accelerator","Race a computer-controlled car to the finish. A stronger comfortable sound increases your speed; quiet lets you coast. Touch control is available. Later stages are faster and the CPU is stronger.",0xFF8DDFF0),
  new Entry(13,"Head Dodge","Head steering","Move your head, weave through traffic","Calibrate your centred head, then move left or right to dodge incoming cars. Return to centre for the middle lane. Touch steering is available. Later stages bring faster traffic and more blocked lanes.",0xFFAABCF8),
  new Entry(14,"Flag World Tour","Spoken names","See a flag. Say its country.","Name the country shown on each flag. A correct answer earns one coin and brings the next flag. Wrong answers and timeouts cost a life. Five stages introduce more countries and faster rounds. Typed answers are available.",0xFF8CE0BA)
 };
}
