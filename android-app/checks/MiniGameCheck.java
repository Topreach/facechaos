package com.facechaos.mobile;

/** Rule-level playthroughs, not evidence of face/sound recognition or visual polish. */
public final class MiniGameCheck {
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    private static void ready(MiniGame game) { for (int i=0;i<110;i++) game.step(.02f); }
    public static void main(String[] args) {
        for (int mode=0;mode<GameCatalog.ALL.length;mode++) {
            MiniGame game=new MiniGame(mode);
            game.input(0);require(game.score==0,"Countdown input blocked");ready(game);
            for (int i=0;i<6000&&!game.finished;i++) {
                if(mode==14){game.flagAnswer(game.flags.country(game.encounter).name);}
                else if(mode==9){game.touchSoundPatterns=true;boolean held=AnimalPatterns.demoActive(game.target(),game.phase/game.pace()-.3f);if(held&&!game.touchHolding)game.touchDown();else if(!held&&game.touchHolding)game.touchUp();}
                else if(mode==11){game.acceptSoundLevel(.9f,.5f,true,true);}
                else if(mode==12){if(!game.touchHolding)game.touchDown();}
                else if(mode==13)game.input(game.racing.safeLane()+1);
                else if(mode==6){if(game.snackIsTreat()&&game.phase>=1.5f&&!game.resolved)game.input(0);}
                else if(mode==7){if(game.phase>=.5f&&Math.abs(game.stackOffset())<.15f&&!game.resolved)game.input(0);}
                else if(mode==8){game.acceptSoundLevel(.5f,.5f,!game.resolved&&game.charge<game.powerTarget(),true);}
                else if(mode==3) {
                    if(game.height>game.corridor()+.015f&&game.velocity>-.02f) game.input(0);
                } else if(mode==1||mode==4) {
                    if(game.phase>=(game.soundJump()?2.2f:1.6f)&&!game.resolved) game.input(game.target());
                } else game.input(game.target());
                game.step(.02f);
            }
            require(game.finished&&game.won,"Winning playthrough mode "+mode+": score "+game.score+", hearts "+game.hearts);
            int score=game.score;game.input(0);game.step(3);require(game.score==score,"Finished score locked");
            MiniGame idle=new MiniGame(mode);if(MiniGame.requiresMicrophone(mode))idle.acceptSoundLevel(0,0,false,true);ready(idle);
            for(int i=0;i<6000&&!idle.finished;i++)idle.step(.02f);
            require(idle.finished&&!idle.won,"Idle losing playthrough "+mode);
            require(idle.resultTitle().equals("Mission failed")&&idle.resultAction().equals("Try again"),"Failure and retry wording "+mode);
        }
        MiniGame emoji=new MiniGame(0);ready(emoji);emoji.input(1);require(emoji.score==0,"Wrong action rejected");
        emoji.input(-1);emoji.input(99);require(emoji.score==0,"Invalid actions rejected");
        emoji.paused=true;float time=emoji.elapsed;emoji.input(0);emoji.step(10);
        require(emoji.elapsed==time&&emoji.score==0,"Pause freezes gameplay and input");
        emoji.paused=false;emoji.input(0);emoji.input(1);
        require(emoji.gateMatched&&emoji.score==0,"Matched expression waits for visible gate passage");
        while(emoji.gateState==0)emoji.step(.02f);
        require(emoji.gateState==1&&emoji.score==0,"Gate reaches pass animation before reward");
        emoji.paused=true;float animation=emoji.gateAnimation;emoji.step(1);
        require(emoji.gateAnimation==animation,"Pause freezes gate animation");
        emoji.paused=false;
        while(emoji.score==0)emoji.step(.02f);
        require(emoji.score==1&&!emoji.gateMatched&&emoji.gateState==0,"One reward after passage, then next gate");
        float phase=emoji.phase;emoji.step(Float.NaN);require(emoji.phase==phase,"Invalid delta ignored");
        MiniGame rhythm=new MiniGame(4);ready(rhythm);rhythm.input(0);require(rhythm.score==0,"Early note rejected");
        while(rhythm.phase<2.2f)rhythm.step(.02f);rhythm.input(0);
        for(int i=0;i<40;i++){rhythm.step(.02f);rhythm.input(0);}require(rhythm.score==1,"One reward per note");
        MiniGame garden=new MiniGame(2);ready(garden);
        require(garden.prompt().contains("Smile"),"Garden starts with planting instruction");
        garden.input(0);require(garden.prompt().contains("Blink"),"Garden instruction advances to bloom");
        for(int i=0;i<30;i++)garden.step(.02f);garden.input(1);
        require(garden.nextHint().contains("Flower 2"),"Progress advances to next flower");
        MiniGame duet=new MiniGame(5);ready(duet);duet.input(0);
        require(duet.prompt().contains("Hum")&&duet.nextHint().contains("Sound cue"),"Duet switches face to sound instruction");
        require(rhythm.prompt().contains("Relax"),"Timed action does not request a repeat after success");
        for(GameCatalog.Entry entry:GameCatalog.ALL)require(!entry.controlBrief().isEmpty()&&!entry.instructions.isEmpty(),"Pre-start instructions for every game");
        MiniGame escape=new MiniGame(1);ready(escape);escape.coinBalance=20;
        escape.input(0);require(escape.coins==0&&escape.coinBalance==20,"Early response earns no coin");
        while(escape.phase<2.2f)escape.step(.02f);
        escape.input(1);require(escape.coins==0,"Wrong face earns no coin");
        escape.input(0);while(escape.phase<2.7f)escape.step(.02f);require(escape.coins==1&&escape.coinBalance==21&&escape.earnedCoin&&escape.feedback.equals("Good job! +1 coin"),"Correct face earns one coin and success message");
        for(int i=0;i<40;i++){escape.step(.02f);escape.input(0);}require(escape.coins==1,"Repeated input cannot duplicate reward");
        while(escape.encounter==0)escape.step(.02f);
        require(!escape.earnedCoin&&escape.prompt().equals("Smile with lips closed"),"Next encounter advances popup and clears reward");
        while(escape.phase<3.3f)escape.step(.02f);require(!escape.earnedCoin&&escape.coins==1&&escape.coinBalance==21,"Miss does not award coins or show success");
        MiniGame stopped=new MiniGame(0);ready(stopped);stopped.stopRound();
        require(stopped.stopped&&stopped.resultTitle().equals("Round stopped"),"Manual stop is distinct from failure");
        stopped.input(0);require(!stopped.gateMatched,"Stopped round rejects input");
        System.out.println("PASS: fifteen winning/losing playthroughs, input rules, pause, timing, progressive prompts and fifteen pre-start briefs");
    }
}
