package com.facechaos.mobile;

import android.media.MediaCodecInfo;
import android.media.MediaCodecList;

/** Higher-detail composite video with encoder-checked dimensions and unchanged framing. */
final class RecordingQuality {
 static final int FPS=24;
 final int width,height,bitrate;
 private RecordingQuality(int width,int height,int bitrate){this.width=width;this.height=height;this.bitrate=bitrate;}
 static RecordingQuality choose(int viewWidth,int viewHeight){
  if(viewWidth<=0||viewHeight<=0)throw new IllegalArgumentException("Preview has no dimensions");
  for(int cap:new int[]{1080,720,480}){
   float factor=Math.min(1f,Math.min(cap/(float)Math.min(viewWidth,viewHeight),(cap*16f/9)/Math.max(viewWidth,viewHeight)));
   for(MediaCodecInfo codec:new MediaCodecList(MediaCodecList.REGULAR_CODECS).getCodecInfos()){
    if(!codec.isEncoder())continue;
    try{
     MediaCodecInfo.CodecCapabilities caps=codec.getCapabilitiesForType("video/avc");
     if(!CameraQuality.includes(caps.colorFormats,MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface))continue;
     MediaCodecInfo.VideoCapabilities video=caps.getVideoCapabilities();
     int aw=Math.max(2,video.getWidthAlignment()),ah=Math.max(2,video.getHeightAlignment());
     int w=Math.max(aw,(int)(viewWidth*factor)/aw*aw),h=Math.max(ah,(int)(viewHeight*factor)/ah*ah);
     if(!video.areSizeAndRateSupported(w,h,FPS))continue;
     int bitrate=video.getBitrateRange().clamp(bitrate(w,h));
     return new RecordingQuality(w,h,bitrate);
    }catch(IllegalArgumentException ignored){}
   }
  }
  throw new IllegalStateException("No compatible video encoder");
 }
 static int bitrate(int w,int h){return Math.max(4_000_000,Math.min(14_000_000,(int)((long)w*h*7)));}
}
