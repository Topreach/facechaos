package com.facechaos.mobile;

/** Local RMS sound gate with a quiet baseline, hysteresis and no speech recognition. */
final class SoundGate {
    private final boolean rhythm;
    SoundGate(){this(false);}
    SoundGate(boolean rhythm){this.rhythm=rhythm;}
    private int baselineFrames;
    private float floor, smoothed;
    private boolean active;
    float level, strength;
    boolean ready(){return baselineFrames>=20;}
    boolean feed(short[] samples,int count){
        if(count<=0)return active;
        double sum=0;for(int i=0;i<count;i++){double value=samples[i]/32768.0;sum+=value*value;}
        float rms=(float)Math.sqrt(sum/count);
        if(!ready()){floor+=rms;baselineFrames++;if(ready())floor/=20;level=0;strength=0;return false;}
        smoothed=smoothed*(rhythm?.2f:.4f)+rms*(rhythm?.8f:.6f);
        float threshold=Math.max(.008f,floor*2.5f+.004f);
        active=smoothed>(active?threshold*(rhythm?.85f:.65f):threshold);
        if(!active)floor=floor*.995f+rms*.005f;
        level=Math.min(1,smoothed/(threshold*2));
        // Relative decibels preserve soft/strong differences beyond the binary gate.
        strength=Math.max(0,Math.min(1,(float)(20*Math.log10(Math.max(1,smoothed/threshold))/24)));return active;
    }
}
