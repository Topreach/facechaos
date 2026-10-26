package com.facechaos.mobile;

/** Consequences and timing are different in each of the three new games. */
public final class CollectionCheck {
    private static void require(boolean b,String m){if(!b)throw new AssertionError(m);}
    private static void land(MiniGame game){for(int i=0;i<200&&!game.cometLanded&&!game.finished;i++)game.step(.02f);}
    public static void main(String[] args){
        MiniGame snack=new MiniGame(6);snack.countdown=0;snack.phase=1.5f;snack.input(0);require(snack.score==1&&snack.coins==1,"Treat earns a coin");snack.input(0);require(snack.coins==1,"One coin per treat");
        MiniGame pepper=new MiniGame(6);pepper.countdown=0;pepper.encounter=2;pepper.phase=1.5f;pepper.input(0);require(pepper.hearts==2&&pepper.coins==0,"Pepper costs a heart, never a coin");
        MiniGame avoid=new MiniGame(6);avoid.countdown=0;avoid.encounter=2;avoid.phase=2.54f;avoid.step(.02f);require(avoid.hearts==3&&avoid.coins==0&&avoid.resolved,"Avoiding pepper is safe");
        MiniGame stack=new MiniGame(7);stack.countdown=0;stack.phase=.7f;stack.input(0);for(int i=0;i<20;i++)stack.step(.02f);require(stack.hearts==2&&stack.coins==0,"Off-centre block is a miss");
        MiniGame aligned=new MiniGame(7);aligned.countdown=0;aligned.phase=(float)Math.PI/1.8f;aligned.input(0);for(int i=0;i<20;i++)aligned.step(.02f);require(aligned.coins==1&&aligned.score==1,"Centred block earns a coin");
        MiniGame rocket=new MiniGame(8);rocket.countdown=0;rocket.micReady=true;rocket.step(.02f);require(!rocket.resolved,"Quiet alone cannot launch");rocket.voiceActive=true;while(rocket.charge<rocket.powerTarget())rocket.step(.02f);rocket.voiceActive=false;for(int i=0;i<6;i++)rocket.step(.02f);require(rocket.resolved&&rocket.coins==0,"Launch starts flight before awarding a coin");land(rocket);require(rocket.coins==1,"Gold launch earns a coin on landing");for(int i=0;i<20;i++)rocket.step(.02f);require(rocket.coins==1,"One coin per launch");
        MiniGame over=new MiniGame(8);over.countdown=0;over.micReady=true;over.charge=.95f;over.wasCharging=true;for(int i=0;i<6;i++)over.step(.02f);land(over);require(over.hearts==2&&over.coins==0,"Overcharged launch misses");
        System.out.println("PASS: treat/pepper decisions, stack alignment, charge/release targeting and non-duplicated rewards");
    }
}
