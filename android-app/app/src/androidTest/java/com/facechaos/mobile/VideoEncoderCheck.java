package com.facechaos.mobile;

import android.content.Context;
import android.graphics.*;
import android.media.MediaMetadataRetriever;
import java.io.File;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/** Real device encode/decode test. Only synthetic coloured frames, never a camera. */
final class VideoEncoderCheck {
    static String run(Context context)throws Exception {
        File file=new File(context.getCacheDir(),"encoder-check.mp4");
        RecordingQuality quality=RecordingQuality.choose(1080,2340);
        CountDownLatch ready=new CountDownLatch(1),done=new CountDownLatch(1);
        AtomicReference<String> error=new AtomicReference<>();
        VideoEncoder encoder=new VideoEncoder(file,quality.width,quality.height,quality.bitrate,new VideoEncoder.Listener(){
            public void ready(){ready.countDown();}
            public void complete(File output,int frames){if(frames<30)error.set("Too few encoded frames: "+frames);done.countDown();}
            public void failed(String message){error.set(message);ready.countDown();done.countDown();}
        });
        Bitmap bitmap=Bitmap.createBitmap(quality.width,quality.height,Bitmap.Config.ARGB_8888);
        try {
            encoder.start();require(ready.await(12,TimeUnit.SECONDS),"Encoder start timeout");require(error.get()==null,"Encoder start: "+error.get());
            sendFrames(encoder,bitmap,18,0);
            encoder.setPaused(true);Thread.sleep(850);
            require(!encoder.submit(bitmap,System.nanoTime()),"Frames must be rejected while paused");
            encoder.setPaused(false);Thread.sleep(80);
            sendFrames(encoder,bitmap,18,18);
            encoder.stop();require(done.await(12,TimeUnit.SECONDS),"Encoder stop timeout");require(error.get()==null,error.get());
            require(file.length()>10000,"Encoded MP4 must contain video data");
            try(MediaMetadataRetriever metadata=new MediaMetadataRetriever()){
                metadata.setDataSource(file.getAbsolutePath());
                int width=Integer.parseInt(metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                int height=Integer.parseInt(metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
                long duration=Long.parseLong(metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                require(width==quality.width&&height==quality.height,"Encoded portrait dimensions: "+width+"x"+height);
                require(duration>900&&duration<2400,"Paused interval must not become recorded time: "+duration);
                Bitmap frame=metadata.getFrameAtTime(100000,MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                require(frame!=null,"Video frame must decode");
                int top=frame.getPixel(width/12,height/26),bottom=frame.getPixel(width*11/12,height*25/26);
                require(Color.red(top)>Color.blue(top)+80,"Top red marker orientation");
                require(Color.blue(bottom)>Color.red(bottom)+80,"Bottom blue marker orientation");
                frame.recycle();return "PASS: native MP4 encode/decode "+width+"x"+height+", pause/resume, portrait marker orientation, no camera/mic ("+duration+" ms)";
            }
        }finally{encoder.stop();bitmap.recycle();if(file.exists())file.delete();}
    }
    private static void sendFrames(VideoEncoder encoder,Bitmap bitmap,int count,int offset)throws Exception {
        for(int i=0;i<count;i++){
            long limit=System.currentTimeMillis()+5000;
            while(!encoder.available()&&System.currentTimeMillis()<limit)Thread.sleep(4);
            require(encoder.available(),"Frame submission timeout");
            Canvas canvas=new Canvas(bitmap);canvas.scale(bitmap.getWidth()/720f,bitmap.getHeight()/1560f);canvas.drawColor(0xFF426557);Paint paint=new Paint(3);
            paint.setColor(Color.RED);canvas.drawRect(0,0,180,180,paint);paint.setColor(Color.BLUE);canvas.drawRect(540,1380,720,1560,paint);
            paint.setColor(0xFFFFD66D);canvas.drawCircle(100+(offset+i)*12,900,60,paint);
            require(encoder.submit(bitmap,System.nanoTime()),"Submit synthetic frame");Thread.sleep(42);
        }
        long limit=System.currentTimeMillis()+5000;while(!encoder.available()&&System.currentTimeMillis()<limit)Thread.sleep(4);
    }
    private static void require(boolean condition,String message){if(!condition)throw new AssertionError(message);}
}
