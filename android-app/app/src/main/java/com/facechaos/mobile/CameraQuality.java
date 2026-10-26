package com.facechaos.mobile;

import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.Size;

/** Uses real advertised camera outputs, never an upscaled resolution label. */
final class CameraQuality {
 static Size preview(CameraCharacteristics info){
  StreamConfigurationMap map=info.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
  if(map==null)throw new IllegalStateException("Camera has no output configurations");
  Size[] sizes=map.getOutputSizes(SurfaceTexture.class);
  if(sizes==null||sizes.length==0)throw new IllegalStateException("Camera has no preview output");
  Size best=null;
  // Prefer a video-shaped stream, which loses less detail to the portrait crop.
  for(int pass=0;pass<2&&best==null;pass++)for(Size s:sizes){
   int longEdge=Math.max(s.getWidth(),s.getHeight()),shortEdge=Math.min(s.getWidth(),s.getHeight());
   long duration=map.getOutputMinFrameDuration(SurfaceTexture.class,s);
   if(longEdge>7680||shortEdge>4320||duration>41_666_667L)continue;
   if(pass==0&&Math.abs(longEdge/(float)shortEdge-16f/9)>.04f)continue;
   if(best==null||(long)s.getWidth()*s.getHeight()>(long)best.getWidth()*best.getHeight())best=s;
  }
  if(best==null){best=sizes[0];for(Size s:sizes)if((long)s.getWidth()*s.getHeight()<(long)best.getWidth()*best.getHeight())best=s;}
  return best;
 }
 static boolean includes(int[] modes,int value){if(modes!=null)for(int mode:modes)if(mode==value)return true;return false;}
 static void apply(CaptureRequest.Builder request,CameraCharacteristics info){
  request.set(CaptureRequest.CONTROL_MODE,CameraMetadata.CONTROL_MODE_AUTO);
  if(includes(info.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES),CameraMetadata.CONTROL_AE_MODE_ON))request.set(CaptureRequest.CONTROL_AE_MODE,CameraMetadata.CONTROL_AE_MODE_ON);
  if(includes(info.get(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES),CameraMetadata.CONTROL_AWB_MODE_AUTO))request.set(CaptureRequest.CONTROL_AWB_MODE,CameraMetadata.CONTROL_AWB_MODE_AUTO);
  if(includes(info.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES),CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_VIDEO))request.set(CaptureRequest.CONTROL_AF_MODE,CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_VIDEO);
  if(includes(info.get(CameraCharacteristics.EDGE_AVAILABLE_EDGE_MODES),CameraMetadata.EDGE_MODE_FAST))request.set(CaptureRequest.EDGE_MODE,CameraMetadata.EDGE_MODE_FAST);
  if(includes(info.get(CameraCharacteristics.NOISE_REDUCTION_AVAILABLE_NOISE_REDUCTION_MODES),CameraMetadata.NOISE_REDUCTION_MODE_FAST))request.set(CaptureRequest.NOISE_REDUCTION_MODE,CameraMetadata.NOISE_REDUCTION_MODE_FAST);
 }
}
