package com.facechaos.mobile;

/** Sequential sound gating and rewards, without opening a camera or microphone. */
public final class StarDuetCheck {
    private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
    private static void settle(MiniGame game){for(int i=0;i<40;i++)game.step(.02f);}
    public static void main(String[] args){
        MiniGame game=new MiniGame(5);game.countdown=0;game.coinBalance=10;
        game.input(1);require(game.score==0&&game.coins==0,"Hum cannot replace smile");
        for(int pair=0;pair<3;pair++){
            game.voiceSignal(true,true);require(game.score==pair*2,"Sound ignored during smile cue");
            game.input(0);require(game.score==pair*2+1&&game.coins==pair,"Smile makes spark without coin");
            settle(game);game.voiceSignal(true,true);require(game.score==pair*2+1,"Held sound cannot carry into hum cue");
            game.voiceSignal(false,true);require(game.duetSoundArmed,"Quiet arms the hum cue");
            game.voiceSignal(true,true);require(game.score==pair*2+2&&game.coins==pair+1,"Fresh hum completes pair and awards one coin");
            game.voiceSignal(true,true);require(game.coins==pair+1,"Held hum cannot duplicate coins");settle(game);
        }
        require(game.finished&&game.won&&game.coinBalance==13,"Three duets win with exactly three saved coins");
        MiniGame paused=new MiniGame(5);paused.countdown=0;paused.input(0);settle(paused);paused.paused=true;paused.voiceSignal(false,true);paused.voiceSignal(true,true);require(paused.score==1&&paused.coins==0,"Paused sound earns nothing");
        MiniGame idle=new MiniGame(5);idle.countdown=0;for(int i=0;i<1200&&!idle.finished;i++)idle.step(.02f);require(idle.finished&&!idle.won&&idle.coins==0,"Idle cue misses lose the round without coins");
        System.out.println("PASS: Star Duet smile/sound order, quiet-before-hum, held sound rejection, one coin per pair, pause and win/loss");
    }
}
