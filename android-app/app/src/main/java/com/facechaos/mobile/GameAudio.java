package com.facechaos.mobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.SoundPool;
import java.util.*;

/** Local original sound assets. Live microphone rounds suppress speaker feedback. */
final class GameAudio {
    private final SoundPool pool;
    private final Map<String,Integer> sounds=new HashMap<>();
    private final Set<Integer> ready=new HashSet<>();
    private final ArrayList<Integer> streams=new ArrayList<>();
    private final SoundEvents events=new SoundEvents();
    private final SharedPreferences prefs;
    private int musicStream;
    private boolean released;
    GameAudio(Context context){
        prefs=context.getSharedPreferences("audio",0);
        pool=new SoundPool.Builder().setMaxStreams(4).setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()).build();
        pool.setOnLoadCompleteListener((owner,id,status)->{if(!released&&status==0)ready.add(id);});
        String[] names={"start","countdown","go","correct","coin","miss","stage","complete","fail","pause","click","music"};
        int[] resources=resources();for(int i=0;i<names.length;i++)sounds.put(names[i],pool.load(context,resources[i],1));
    }
    static int[] resources(){return new int[]{R.raw.fx_start,R.raw.fx_countdown,R.raw.fx_go,R.raw.fx_correct,R.raw.fx_coin,R.raw.fx_miss,R.raw.fx_stage,R.raw.fx_complete,R.raw.fx_fail,R.raw.fx_pause,R.raw.fx_click,R.raw.music_garden};}
    boolean effects(){return prefs.getBoolean("effects",true);}
    boolean music(){return prefs.getBoolean("music",true);}
    void settings(boolean effects,boolean music){prefs.edit().putBoolean("effects",effects).putBoolean("music",music).apply();if(!music)stopMusic();if(!effects){for(int stream:streams)pool.stop(stream);streams.clear();}}
    void update(MiniGame game,boolean playing){
        if(released)return;String cue=events.observe(game,playing);
        if(cue!=null&&effects()&&SoundEvents.effectsAllowed(game))play(cue,.5f);
        if(music()&&SoundEvents.musicAllowed(game,playing)){
            Integer id=sounds.get("music");if(musicStream==0&&id!=null&&ready.contains(id))musicStream=pool.play(id,.2f,.2f,0,-1,1);
        }else stopMusic();
    }
    void preview(){if(effects())play("stage",.5f);}
    private void play(String name,float volume){Integer id=sounds.get(name);if(id!=null&&ready.contains(id)){int stream=pool.play(id,volume,volume,1,0,1);if(stream!=0){streams.add(stream);if(streams.size()>12)streams.remove(0);}}}
    private void stopMusic(){if(musicStream!=0){pool.stop(musicStream);musicStream=0;}}
    void suspend(){if(released)return;stopMusic();for(int stream:streams)pool.stop(stream);streams.clear();}
    void release(){if(!released){suspend();released=true;pool.release();}}
}
