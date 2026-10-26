package com.facechaos.mobile;

/** Patterns and examples are tested without listening to or playing any real audio. */
public final class ExtrasCheck {
    private static void require(boolean ok,String message){if(!ok)throw new AssertionError(message);}
    private static void advance(MiniGame game,float seconds,boolean sound){game.voiceActive=sound;for(int i=0;i<Math.ceil(seconds/.02f);i++)game.step(.02f);}
    public static void main(String[] args){
        MiniGame choir=new MiniGame(9);choir.countdown=0;choir.microphoneMode=true;choir.micReady=true;
        for(int cue=0;cue<6;cue++){
            advance(choir,.3f,false);int count=choir.animalPulsesRequired();
            for(int pulse=0;pulse<count;pulse++){advance(choir,AnimalPatterns.get(choir.target()).duration(pulse),true);advance(choir,pulse==count-1?.4f:.2f,false);}
            require(choir.resolved&&choir.earnedCoin&&choir.coins==cue+1,"Valid pattern earns one coin, cue "+cue);
            while(choir.resolved&&!choir.finished)choir.step(.02f);
        }
        require(choir.won,"Six animal patterns win");
        MiniGame invalid=new MiniGame(9);invalid.countdown=0;invalid.microphoneMode=true;invalid.micReady=true;advance(invalid,.3f,false);advance(invalid,1,true);advance(invalid,.2f,false);require(invalid.hearts==2&&invalid.coins==0,"Wrong duration earns nothing");
        for(int animal=0;animal<3;animal++){
            SoundGate gate=new SoundGate(true);MiniGame model=new MiniGame(9);model.countdown=0;model.encounter=animal;model.microphoneMode=true;
            short[] quiet=new short[800];for(int i=0;i<28;i++){boolean active=gate.feed(quiet,quiet.length);model.micReady=gate.ready();model.voiceSignal(active,gate.ready());model.step(.05f);}
            short[] guide=AnimalGuide.samples(animal);
            for(int offset=0;offset<guide.length;offset+=800){short[] frame=new short[800];int count=Math.min(800,guide.length-offset);System.arraycopy(guide,offset,frame,0,count);boolean active=gate.feed(frame,frame.length);model.voiceSignal(active,true);model.step(.05f);}
            for(int i=0;i<12;i++){model.voiceSignal(gate.feed(quiet,quiet.length),true);model.step(.05f);}
            require(model.score==1,"Synthetic guide fits its duration pattern "+animal);
        }
        MiniGame memory=new MiniGame(10);memory.countdown=0;memory.input(memory.target());require(memory.memoryIndex==0,"Memory input blocked while showing");while(memory.memoryShowing())memory.step(.02f);memory.input((memory.target()+1)%3);require(memory.hearts==2&&memory.phase==0,"Wrong face replays sequence");
        MiniGame glow=new MiniGame(11);glow.countdown=0;glow.microphoneMode=true;glow.micReady=true;glow.micLevel=1;advance(glow,2,true);require(glow.coins==0,"Over-range sound does not earn lantern");glow.micLevel=.55f;advance(glow,1.6f,true);require(glow.coins==1,"Steady gentle sound earns lantern");
        System.out.println("PASS: animal timing patterns, synthetic guides, wrong-pattern rejection, memory replay and steady-sound lanterns");
    }
}
