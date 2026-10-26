package com.facechaos.mobile;

/** Synthetic sound levels only: never opens a microphone. */
public final class SoundGateCheck {
    private static void require(boolean condition,String message){if(!condition)throw new AssertionError(message);}
    public static void main(String[] args){
        SoundGate gate=new SoundGate();short[] buffer=new short[800];
        for(int i=0;i<buffer.length;i++)buffer[i]=(short)(i%2==0?70:-70);
        for(int i=0;i<20;i++)require(!gate.feed(buffer,buffer.length),"Quiet baseline does not lift");
        require(gate.ready(),"Baseline completes after one second");
        for(int i=0;i<buffer.length;i++)buffer[i]=(short)(Math.sin(i*.2)*2200);
        boolean active=false;for(int i=0;i<6;i++)active=gate.feed(buffer,buffer.length);
        require(active&&gate.level>.5f,"Gentle synthetic sound activates lift and meter");
        java.util.Arrays.fill(buffer,(short)0);for(int i=0;i<12;i++)active=gate.feed(buffer,buffer.length);
        require(!active&&gate.level<.1f,"Quiet releases lift");
        MiniGame voice=new MiniGame(3);voice.countdown=0;voice.height=.7f;voice.voiceActive=true;
        for(int i=0;i<30;i++)voice.step(.02f);require(voice.height<.65f,"Holding sound rises");
        voice.voiceActive=false;voice.velocity=0;float before=voice.height;for(int i=0;i<30;i++)voice.step(.02f);require(voice.height>before,"Quiet drifts down");
        voice.phase=3.99f;voice.height=voice.corridor();voice.velocity=0;voice.step(.02f);
        require(voice.resolved&&voice.coins==1&&voice.score==1&&voice.earnedCoin,"One star awards one coin at crossing");
        for(int i=0;i<25;i++)voice.step(.02f);require(voice.coins==1,"Celebration cannot duplicate coins");
        MiniGame held=new MiniGame(3);held.countdown=0;
        for(int i=0;i<4000&&!held.finished;i++){held.voiceActive=held.height+held.velocity*.3f>held.corridor();held.step(.02f);}
        require(held.finished&&held.won&&held.coins>=5&&held.coinBalance==held.coins,"Held sound control can complete a full round with consistent coin rewards");
        System.out.println("PASS: synthetic quiet/sound gate, meter, release, held lift, quiet descent and one coin per cloud");
    }
}
