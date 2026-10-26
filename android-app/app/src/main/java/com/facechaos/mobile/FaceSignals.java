package com.facechaos.mobile;

/** Personalized neutral baseline, timed calibration and deliberate one-shot reactions. */
final class FaceSignals {
 static final int NONE=-1,MOUTH=0,SMILE=1,TEETH=2,BLINK=3,WIDE_EYES=4,FACE_SQUEEZE=5;
 int neutralFrames,releaseFrames,candidate=NONE;
 long candidateSince,lastEvent=-1000,neutralSince=-1,releaseSince=-1,lastSample=-1;
 float jawBase,smileBase,wideBase;
 boolean ready,armed,recovering;
 String status="Relax your face to calibrate";
 static float teethThreshold(float sensitivity){float scale=Float.isFinite(sensitivity)?Math.max(.7f,Math.min(1.3f,sensitivity)):1;return Math.max(.45f,Math.min(.75f,.60f*scale));}
 void reset(){neutralFrames=releaseFrames=0;candidate=NONE;candidateSince=0;lastEvent=-1000;neutralSince=releaseSince=lastSample=-1;jawBase=smileBase=wideBase=0;ready=armed=recovering=false;status="Relax your face to calibrate";}
 void lost(){recovering=ready;armed=false;releaseFrames=0;releaseSince=-1;candidate=NONE;if(!ready){neutralFrames=0;neutralSince=-1;jawBase=smileBase=wideBase=0;}status="No face - move into view";}
 static float squeezeScore(float blink,float squint,float cheek,float brow,float nose){return Math.min(blink,Math.max(Math.max(squint,cheek),Math.max(brow,nose)));}
 int accept(float jaw,float smile,float blink,float teeth,long now,float sensitivity){
  return accept(jaw,smile,blink,teeth,now,sensitivity,NONE);
 }
 int accept(float jaw,float smile,float blink,float teeth,long now,float sensitivity,int expected){
  return accept(jaw,smile,blink,teeth,0,0,now,sensitivity,expected);
 }
 int accept(float jaw,float smile,float blink,float teeth,float wide,float squeeze,long now,float sensitivity,int expected){
  if(!Float.isFinite(jaw)||!Float.isFinite(smile)||!Float.isFinite(blink)||!Float.isFinite(teeth)||!Float.isFinite(wide)||!Float.isFinite(squeeze)){lost();return NONE;}
  lastSample=now;
  if(!ready){
   // A natural blink pauses sampling rather than throwing away a good baseline.
   if(blink>=.35f&&jaw<.24f&&smile<.45f&&teeth<.35f){status="Open your eyes, keep relaxed";return NONE;}
   if(jaw>=.24f||smile>=.45f||teeth>=.35f||wide>=.70f||squeeze>=.35f){neutralFrames=0;neutralSince=-1;jawBase=smileBase=wideBase=0;status="Relax lips and eyes to calibrate";return NONE;}
   if(neutralFrames>0&&(Math.abs(jaw-jawBase/neutralFrames)>.07f||Math.abs(smile-smileBase/neutralFrames)>.10f)){neutralFrames=0;neutralSince=-1;jawBase=smileBase=wideBase=0;}
   if(neutralSince<0)neutralSince=now;
   jawBase+=jaw;smileBase+=smile;wideBase+=wide;neutralFrames++;
   status="Calibrating "+Math.min(99,(int)((now-neutralSince)*100/650))+"% - stay relaxed";
   if(neutralFrames>=6&&now-neutralSince>=650){jawBase/=neutralFrames;smileBase/=neutralFrames;wideBase/=neutralFrames;ready=true;armed=true;status="Face ready - follow the cue";}return NONE;
  }
  boolean extended=expected==NONE||expected==WIDE_EYES||expected==FACE_SQUEEZE;
  boolean neutral=jaw<Math.max(.18f,jawBase+.08f)&&smile<Math.max(.28f,smileBase+.10f)&&blink<.28f&&teeth<.35f&&(!extended||wide<Math.max(.28f,wideBase+.10f)&&squeeze<.25f);
  if(neutral){
   if(releaseSince<0)releaseSince=now;
   if(++releaseFrames>=2&&now-releaseSince>=70){armed=true;recovering=false;}
   // Slow adaptation only during confirmed relaxation, never during an expression.
   if(releaseFrames>=3){jawBase=jawBase*.99f+jaw*.01f;smileBase=smileBase*.99f+smile*.01f;wideBase=wideBase*.99f+wide*.01f;}
   candidate=NONE;status=armed?"Neutral - ready":"Relax briefly";return NONE;
  }
  releaseFrames=0;releaseSince=-1;
  float scale=Float.isFinite(sensitivity)?Math.max(.7f,Math.min(1.3f,sensitivity)):1;
  float jawThreshold=Math.max(.28f,jawBase+.18f)*scale,smileThreshold=Math.max(.38f,smileBase+.20f)*scale;
  float teethThreshold=teethThreshold(sensitivity);
  int action=teeth>=teethThreshold?TEETH:jaw>=jawThreshold&&teeth<.45f?MOUTH:smile>=smileThreshold&&jaw<Math.max(.20f,jawBase+.07f)&&teeth<.30f?SMILE:blink>=.55f&&jaw<Math.max(.20f,jawBase+.07f)&&smile<smileThreshold?BLINK:NONE;
  boolean relaxedMouth=jaw<Math.max(.22f,jawBase+.10f)&&teeth<.30f;
  if(extended&&relaxedMouth){
   if(blink>=.60f&&squeeze>=.45f*scale)action=FACE_SQUEEZE;
   else if(wide>=Math.max(.45f,wideBase+.22f)*scale&&blink<.20f&&squeeze<.25f&&smile<smileThreshold)action=WIDE_EYES;
  }
  // Forming a teeth display often starts with jaw opening. An unrelated reaction
  // must not consume the neutral rearm while the player is forming the requested cue.
  if(expected!=NONE&&action!=expected)action=NONE;
  status=action==NONE?"Relax, then try one expression":name(action)+(armed?" detected":" - return to neutral");
  if(action!=candidate){candidate=action;candidateSince=now;}
  long dwell=action==BLINK?0:action==FACE_SQUEEZE?350:action==WIDE_EYES?160:action==TEETH?130:90;
  if(armed&&action!=NONE&&now-candidateSince>=dwell&&now-lastEvent>=260){armed=false;lastEvent=now;return action;}
  return NONE;
 }
 static String name(int action){return action==0?"Open mouth":action==1?"Closed-lip smile":action==2?"Show teeth":action==3?"Blink":action==4?"Eyes wide open":action==5?"Face squeeze":"None";}
}
