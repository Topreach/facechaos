package com.facechaos.mobile;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.Size;
/** Reads capabilities only; never opens a camera or captures a face. */
final class CameraQualityCheck {
 static String run(Context context)throws Exception{
  CameraManager manager=context.getSystemService(CameraManager.class);
  StringBuilder report=new StringBuilder();boolean found=false;
  for(String id:manager.getCameraIdList()){
   CameraCharacteristics info=manager.getCameraCharacteristics(id);
   Integer facing=info.get(CameraCharacteristics.LENS_FACING);
   if(facing==null||facing!=CameraCharacteristics.LENS_FACING_FRONT)continue;
   found=true;StreamConfigurationMap map=info.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
   Size chosen=CameraQuality.preview(info);boolean supported=false;
   report.append("Front camera ").append(id).append(" outputs: ");
   for(Size s:map.getOutputSizes(SurfaceTexture.class)){report.append(s).append(" ");if(s.equals(chosen))supported=true;}
   if(!supported)throw new AssertionError("Selected preview not natively supported");
   report.append("; selected ").append(chosen).append("; minimum frame duration ").append(map.getOutputMinFrameDuration(SurfaceTexture.class,chosen)).append(" ns\n");
   report.append("FAST detail enhancement: ").append(CameraQuality.includes(info.get(CameraCharacteristics.EDGE_AVAILABLE_EDGE_MODES),CameraMetadata.EDGE_MODE_FAST)).append("; FAST noise reduction: ").append(CameraQuality.includes(info.get(CameraCharacteristics.NOISE_REDUCTION_AVAILABLE_NOISE_REDUCTION_MODES),CameraMetadata.NOISE_REDUCTION_MODE_FAST)).append("\n");
   break;
  }
  if(!found)throw new AssertionError("No front camera");
  for(int[] dimensions:new int[][]{{1080,2340},{1080,1920},{720,1560},{1920,1080}}){
   RecordingQuality q=RecordingQuality.choose(dimensions[0],dimensions[1]);
   float expected=dimensions[0]/(float)dimensions[1],actual=q.width/(float)q.height;
   if(Math.abs(actual/expected-1)>.025f)throw new AssertionError("Framing distorted");
   if(q.width>dimensions[0]||q.height>dimensions[1])throw new AssertionError("Recording upscaled");
   report.append("Composite ").append(dimensions[0]).append("x").append(dimensions[1]).append(" -> ").append(q.width).append("x").append(q.height).append(" @ ").append(q.bitrate).append(" bps\n");
  }
  return report.toString();
 }
}
