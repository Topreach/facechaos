package com.facechaos.mobile;

import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Build;
import android.view.View;

/** Filters the camera container, never the raw TextureView or the game overlay. */
final class CameraLookPreview {
 private final View layer;
 private boolean smoothing;
 private Api33 shader;
 CameraLookPreview(View layer){this(layer,true);}
 CameraLookPreview(View layer,boolean allowSmoothing){this.layer=layer;smoothing=allowSmoothing&&Build.VERSION.SDK_INT>=33;}
 boolean supportsSmoothing(){return smoothing;}
 CameraLook apply(CameraLook requested){
  if(smoothing&&Build.VERSION.SDK_INT>=33){
   try{if(shader==null)shader=new Api33();layer.setLayerType(View.LAYER_TYPE_NONE,null);shader.apply(layer,requested);return requested.supported(true);}
   catch(RuntimeException error){smoothing=false;layer.setRenderEffect(null);android.util.Log.w("FaceChaosLook","Using basic appearance adjustments",error);}
  }
  CameraLook effective=requested.supported(false);if(effective.strength()==0){layer.setLayerType(View.LAYER_TYPE_NONE,null);return effective;}Paint paint=new Paint();paint.setColorFilter(new ColorMatrixColorFilter(effective.colorMatrix()));layer.setLayerType(View.LAYER_TYPE_HARDWARE,paint);layer.invalidate();return effective;
 }
 @android.annotation.TargetApi(33)
 private static final class Api33 {
  private final android.graphics.RuntimeShader shader=new android.graphics.RuntimeShader(CameraLookShader.agsl());
  void apply(View view,CameraLook look){
   if(look.strength()==0){view.setRenderEffect(null);return;}
   float radius=Math.max(1,Math.min(view.getWidth(),view.getHeight())*.006f);
   shader.setFloatUniform("offset",radius,radius);shader.setFloatUniform("strength",look.strength());shader.setFloatUniform("softness",look.softness());shader.setFloatUniform("warmth",look.warmth);
   view.setRenderEffect(android.graphics.RenderEffect.createRuntimeShaderEffect(shader,"image"));
  }
 }
}
