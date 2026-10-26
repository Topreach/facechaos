package com.facechaos.mobile;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

/** Foreground-only PCM analysis. Samples are discarded, never saved or transmitted. */
final class MicrophoneInput {
    interface Listener{void level(float level,float strength,boolean active,boolean ready);void failed();}
    private volatile boolean running;
    private volatile AudioRecord recorder;
    private final Listener listener;
    private final boolean rhythm;
    MicrophoneInput(Listener listener){this(listener,false);}
    MicrophoneInput(Listener listener,boolean rhythm){this.listener=listener;this.rhythm=rhythm;}
    void start(){running=true;new Thread(this::read,"FaceChaosSoundLevel").start();}
    void stop(){running=false;AudioRecord current=recorder;if(current!=null)try{current.stop();}catch(IllegalStateException ignored){}}
    @android.annotation.SuppressLint("MissingPermission")
    private void read(){
        AudioRecord current=null;
        try{
            int minimum=AudioRecord.getMinBufferSize(16000,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
            if(minimum<=0)throw new IllegalStateException("Audio format unavailable");
            current=new AudioRecord(MediaRecorder.AudioSource.MIC,16000,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT,Math.max(minimum,3200));
            recorder=current;if(!running)return;
            if(current.getState()!=AudioRecord.STATE_INITIALIZED)throw new IllegalStateException("Microphone unavailable");
            current.startRecording();if(current.getRecordingState()!=AudioRecord.RECORDSTATE_RECORDING)throw new IllegalStateException("Microphone did not start");short[] samples=new short[800];SoundGate gate=new SoundGate(rhythm);
            while(running){int count=current.read(samples,0,samples.length);if(count<0){if(running)throw new IllegalStateException("Microphone read failed");break;}if(count==0)continue;
                boolean active=gate.feed(samples,count);if(running)listener.level(gate.level,gate.strength,active,gate.ready());}
        }catch(Exception error){if(running)listener.failed();}
        finally{running=false;recorder=null;if(current!=null){try{current.stop();}catch(IllegalStateException ignored){}current.release();}}
    }
}
