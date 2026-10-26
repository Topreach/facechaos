package com.facechaos.mobile;

final class StageRules {
    static String describe(MiniGame game){return (GameSections.applies(game.mode)?GameSections.at(game.mode,game.stage).name+". ":"")+"Stage speed: "+game.paceLabel()+". "+details(game).replace(" Three misses ends the stage.","")+(game.mode==12?" First across the finish wins.":game.stage==5?" Final challenge: one life. A miss ends the stage.":" You have three lives.");}
    private static String details(MiniGame game){
        switch(game.mode){
            case 14:return "Name "+game.goal()+" flags to finish. Each correct country earns one coin. You have "+String.format(java.util.Locale.US,"%.1f",game.flagLimit()/game.pace())+" seconds per flag. Speech setup and processing pause the timer; each request is limited to 13 seconds. Wrong countries or timeouts cost a life. English spoken or typed answers.";
            case 12:return "Beat the CPU across "+(int)RacingGame.FINISH+" metres. Sound volume sets speed; quiet coasts. Touch mode: hold to accelerate. The CPU gets stronger each stage.";
            case 13:return "Clear at least "+game.goal()+" of "+game.racing.trafficGoal()+" traffic waves. Move your head left/right; centre for the middle lane. Later stages bring two blocked lanes. Touch mode: tap a screen third.";
            case 0:return "Pass "+game.goal()+" gates. Return to neutral after matching. Gate arrival: "+String.format(java.util.Locale.US,"%.1f",game.gateApproach()/game.pace())+" seconds. Three misses ends the stage.";
            case 1:return "Clear at least "+game.timedTarget()+" of 8 obstacles. Use sound volume for jump height; use smile/teeth face controls or touch. Three misses ends the stage.";
            case 2:return "Grow six flowers: smile to plant, then blink to bloom. Later stages give a little less time per action.";
            case 3:return VoiceCourse.world(game.stage)+": clear "+game.voiceTarget()+" of 8 obstacles. Hum to rise; quiet descends. Watch for "+VoiceCourse.obstacleName(game.stage,0).toLowerCase(java.util.Locale.ROOT)+" and "+VoiceCourse.obstacleName(game.stage,1).toLowerCase(java.util.Locale.ROOT)+". Arrival: "+String.format(java.util.Locale.US,"%.1f",game.voiceApproach()/game.pace())+" seconds; later stages ask for more precise height.";
            case 4:return "Clear at least "+game.timedTarget()+" of 8 hops. Soft sound jumps low; stronger sound jumps higher. Make a short sound inside the highlighted window, then go quiet. Reach the next platform or fall into fire and lose a life.";
            case 5:return "Complete three smile/sound pairs. Each pair earns a coin. Later stages shorten the wait allowed for each cue.";
            case 6:return "Catch "+game.goal()+" of 6 treats and avoid 3 peppers. Later stages narrow the catch window. Every treat earns one coin.";
            case 7:return "Place "+game.goal()+" of 8 blocks. Later stages ask for closer alignment with the gold centre mark.";
            case 8:return "Land "+game.goal()+" of 6 rockets. Hum gently to charge, then go quiet in the gold power zone. Sound only; screen touches do not control the rocket. Later stages narrow the zone without asking for louder sound.";
            case 9:return "Match "+game.goal()+" of "+AnimalPatterns.cues(game.stage)+" animal rhythm patterns from a choir of twelve animals. Later stages allow a little less response time; animal identity is not classified.";
            case 10:int start=game.memoryLength();return "Repeat three sequences of "+start+", "+(start+1)+" and "+(start+2)+" faces. Return to neutral between faces in face mode. A mistake replays the sequence and costs a heart.";
            default:return "Guide your firefly to "+game.goal()+" of 6 lanterns using sound only. Hum in gold for "+String.format(java.util.Locale.US,"%.1f",game.glowRequired())+" seconds per lantern. Quiet makes the firefly drift back; touching cannot move it.";
        }
    }
}
