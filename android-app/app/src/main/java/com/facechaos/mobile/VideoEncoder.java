package com.facechaos.mobile;

import android.graphics.Bitmap;
import android.media.MediaRecorder;
import android.opengl.*;
import android.os.*;
import android.view.Surface;
import java.io.File;
import java.nio.*;
import java.util.concurrent.atomic.AtomicBoolean;

/** Encodes only frames supplied by our app. No screen projection, microphone or network. */
final class VideoEncoder {
    interface Listener { void ready(); void complete(File file, int frames); void failed(String message); }
    private final Handler main=new Handler(Looper.getMainLooper());
    private final HandlerThread thread=new HandlerThread("FaceChaosVideo");
    private Handler worker;
    private final Listener listener;
    private final File file;
    private final int width,height,bitrate;
    private final AtomicBoolean busy=new AtomicBoolean();
    private volatile boolean stopping,started,paused;
    private MediaRecorder recorder;
    private Surface input;
    private EGLDisplay display=EGL14.EGL_NO_DISPLAY;
    private EGLContext context=EGL14.EGL_NO_CONTEXT;
    private EGLSurface surface=EGL14.EGL_NO_SURFACE;
    private int program,texture,frames;
    private FloatBuffer vertices,coords;
    private boolean allocated,terminal;

    VideoEncoder(File file,int width,int height,Listener listener){this(file,width,height,RecordingQuality.bitrate(width,height),listener);}
    VideoEncoder(File file,int width,int height,int bitrate,Listener listener){this.file=file;this.width=width;this.height=height;this.bitrate=bitrate;this.listener=listener;}
    void start(){thread.start();worker=new Handler(thread.getLooper());worker.post(()->{
        try{prepare();if(!stopping){started=true;main.post(listener::ready);}}catch(Exception error){fail("Video could not start on this device. Play-only mode remains available.");}
    });}
    boolean available(){return started&&!stopping&&!paused&&!busy.get();}
    boolean submit(Bitmap bitmap,long time){
        if(!started||stopping||paused||!busy.compareAndSet(false,true))return false;
        worker.post(()->{try{if(!stopping&&!paused){draw(bitmap,time);frames++;}}catch(Exception error){fail("Video recording was interrupted. Try a shorter clip.");}finally{busy.set(false);}});return true;
    }
    void setPaused(boolean value){
        // MediaRecorder starts in the recording state. Repeated resume/pause calls
        // are invalid on some devices, including the first game-start callback.
        if(paused==value)return;
        paused=value;
        if(worker!=null)worker.post(()->{if(started&&!stopping)try{if(value)recorder.pause();else recorder.resume();}catch(Exception error){fail("Video pause/resume failed. Your round can continue without recording.");}});
    }
    void stop(){
        if(stopping)return;stopping=true;
        if(worker!=null)worker.post(()->{
            if(terminal)return;
            try{
                if(!started||frames<3)throw new IllegalStateException("Clip too short");
                recorder.stop();started=false;
                if(file.length()==0)throw new IllegalStateException("Empty clip");
                terminal=true;release();
                main.post(()->listener.complete(file,frames));
            }catch(Exception error){fail("Clip was too short or could not be finalized. Please try recording for a few seconds.");}
        });
    }
    private void prepare() throws Exception {
        recorder=new MediaRecorder();
        recorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        recorder.setVideoSize(width,height);recorder.setVideoFrameRate(RecordingQuality.FPS);
        recorder.setVideoEncodingBitRate(bitrate);recorder.setOutputFile(file.getAbsolutePath());
        recorder.setMaxDuration(120_000);
        recorder.setOnInfoListener((source,what,extra)->{if(what==MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED)stop();});
        recorder.prepare();input=recorder.getSurface();initGl();recorder.start();
    }
    private void initGl(){
        display=EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);int[] version=new int[2];
        if(!EGL14.eglInitialize(display,version,0,version,1))throw new IllegalStateException("EGL init");
        int[] configSpec={EGL14.EGL_RED_SIZE,8,EGL14.EGL_GREEN_SIZE,8,EGL14.EGL_BLUE_SIZE,8,EGL14.EGL_ALPHA_SIZE,8,EGL14.EGL_RENDERABLE_TYPE,EGL14.EGL_OPENGL_ES2_BIT,EGL14.EGL_SURFACE_TYPE,EGL14.EGL_WINDOW_BIT,0x3142,1,EGL14.EGL_NONE};
        EGLConfig[] configs=new EGLConfig[1];int[] count=new int[1];
        if(!EGL14.eglChooseConfig(display,configSpec,0,configs,0,1,count,0)||count[0]==0)throw new IllegalStateException("EGL config");
        context=EGL14.eglCreateContext(display,configs[0],EGL14.EGL_NO_CONTEXT,new int[]{EGL14.EGL_CONTEXT_CLIENT_VERSION,2,EGL14.EGL_NONE},0);
        surface=EGL14.eglCreateWindowSurface(display,configs[0],input,new int[]{EGL14.EGL_NONE},0);
        if(!EGL14.eglMakeCurrent(display,surface,surface,context))throw new IllegalStateException("EGL surface");
        String vertex="attribute vec4 aPosition;attribute vec2 aTex;varying vec2 vTex;void main(){gl_Position=aPosition;vTex=aTex;}";
        String fragment="precision mediump float;varying vec2 vTex;uniform sampler2D uTexture;void main(){gl_FragColor=texture2D(uTexture,vTex);}";
        int vs=shader(GLES20.GL_VERTEX_SHADER,vertex),fs=shader(GLES20.GL_FRAGMENT_SHADER,fragment);
        program=GLES20.glCreateProgram();GLES20.glAttachShader(program,vs);GLES20.glAttachShader(program,fs);GLES20.glLinkProgram(program);
        int[] status=new int[1];GLES20.glGetProgramiv(program,GLES20.GL_LINK_STATUS,status,0);if(status[0]==0)throw new IllegalStateException("Shader link");
        GLES20.glDeleteShader(vs);GLES20.glDeleteShader(fs);
        int[] ids=new int[1];GLES20.glGenTextures(1,ids,0);texture=ids[0];GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,texture);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR);GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE);GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE);
        vertices=buffer(new float[]{-1,-1,1,-1,-1,1,1,1});coords=buffer(new float[]{0,1,1,1,0,0,1,0});
    }
    private int shader(int type,String source){int shader=GLES20.glCreateShader(type);GLES20.glShaderSource(shader,source);GLES20.glCompileShader(shader);int[] ok=new int[1];GLES20.glGetShaderiv(shader,GLES20.GL_COMPILE_STATUS,ok,0);if(ok[0]==0)throw new IllegalStateException("Shader compile");return shader;}
    private FloatBuffer buffer(float[] data){FloatBuffer b=ByteBuffer.allocateDirect(data.length*4).order(ByteOrder.nativeOrder()).asFloatBuffer();b.put(data).position(0);return b;}
    private void draw(Bitmap bitmap,long timestamp){
        GLES20.glViewport(0,0,width,height);GLES20.glUseProgram(program);GLES20.glActiveTexture(GLES20.GL_TEXTURE0);GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,texture);
        if(!allocated){GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,bitmap,0);allocated=true;}else GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D,0,0,0,bitmap);
        int pos=GLES20.glGetAttribLocation(program,"aPosition"),uv=GLES20.glGetAttribLocation(program,"aTex");
        GLES20.glEnableVertexAttribArray(pos);GLES20.glEnableVertexAttribArray(uv);
        GLES20.glVertexAttribPointer(pos,2,GLES20.GL_FLOAT,false,0,vertices);GLES20.glVertexAttribPointer(uv,2,GLES20.GL_FLOAT,false,0,coords);GLES20.glUniform1i(GLES20.glGetUniformLocation(program,"uTexture"),0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4);
        EGLExt.eglPresentationTimeANDROID(display,surface,timestamp);
        if(!EGL14.eglSwapBuffers(display,surface))throw new IllegalStateException("Video frame swap");
    }
    private void fail(String message){
        if(terminal)return;terminal=true;stopping=true;started=false;release();
        // Only this newly-created, invalid app clip is removed; existing clips are untouched.
        if(file.exists())file.delete();main.post(()->listener.failed(message));
    }
    private void release(){
        if(display!=EGL14.EGL_NO_DISPLAY){
            if(program!=0)GLES20.glDeleteProgram(program);
            if(texture!=0)GLES20.glDeleteTextures(1,new int[]{texture},0);
            EGL14.eglMakeCurrent(display,EGL14.EGL_NO_SURFACE,EGL14.EGL_NO_SURFACE,EGL14.EGL_NO_CONTEXT);
            if(surface!=EGL14.EGL_NO_SURFACE)EGL14.eglDestroySurface(display,surface);
            if(context!=EGL14.EGL_NO_CONTEXT)EGL14.eglDestroyContext(display,context);
            EGL14.eglTerminate(display);display=EGL14.EGL_NO_DISPLAY;
        }
        if(input!=null){input.release();input=null;}
        if(recorder!=null){recorder.release();recorder=null;}
        thread.quitSafely();
    }
}
