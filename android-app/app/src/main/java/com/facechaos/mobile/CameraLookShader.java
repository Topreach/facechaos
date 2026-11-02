package com.facechaos.mobile;

/** Shared edge-aware filter math for preview (AGSL) and camera-only video (GLSL). */
final class CameraLookShader {
 private static final String FILTER="""
 vec4 applyLook(vec2 p){
  vec4 c=sampleAt(p);
  if(strength<=0.0)return c;
  vec3 color=c.rgb;
  if(softness>0.0){
   vec3 a=sampleAt(p+vec2(offset.x,0.0)).rgb;
   vec3 b=sampleAt(p-vec2(offset.x,0.0)).rgb;
   vec3 d=sampleAt(p+vec2(0.0,offset.y)).rgb;
   vec3 e=sampleAt(p-vec2(0.0,offset.y)).rgb;
   float wa=exp(-dot(a-color,a-color)*80.0);
   float wb=exp(-dot(b-color,b-color)*80.0);
   float wd=exp(-dot(d-color,d-color)*80.0);
   float we=exp(-dot(e-color,e-color)*80.0);
   vec3 soft=(color*2.0+a*wa+b*wb+d*wd+e*we)/(2.0+wa+wb+wd+we);
   color=mix(color,soft,softness);
  }
  float lift=0.10*strength;
  color=color*(1.0-lift)+vec3(lift)+vec3(0.025,0.0,-0.025)*warmth*strength;
  return vec4(clamp(color,0.0,1.0),c.a);
 }
 """;
 static String glsl(){return "precision mediump float;varying vec2 vTex;uniform sampler2D uTexture;uniform vec2 offset;uniform float strength;uniform float softness;uniform float warmth;vec4 sampleAt(vec2 p){return texture2D(uTexture,p);}"+FILTER+"void main(){gl_FragColor=applyLook(vTex);}";}
 static String agsl(){String filter=FILTER.replace("vec2","float2").replace("vec3","float3").replace("vec4","float4");return "uniform shader image;uniform float2 offset;uniform float strength;uniform float softness;uniform float warmth;float4 sampleAt(float2 p){return image.eval(p);}"+filter+"half4 main(float2 p){return half4(applyLook(p));}";}
}
