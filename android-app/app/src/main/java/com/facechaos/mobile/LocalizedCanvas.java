package com.facechaos.mobile;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.*;
final class LocalizedCanvas {
 static void draw(Context context,Canvas canvas,Paint paint,String text,float x,float baseline,boolean centre,int width){
  if(!context.getResources().getConfiguration().getLocales().get(0).getLanguage().equals("ar")){canvas.drawText(text,centre?x-paint.measureText(text)/2:x,baseline,paint);return;}
  TextPaint tp=new TextPaint(paint);tp.setTextAlign(Paint.Align.LEFT);StaticLayout layout=StaticLayout.Builder.obtain(text,0,text.length(),tp,width).setAlignment(centre?Layout.Alignment.ALIGN_CENTER:TextDirectionHeuristics.FIRSTSTRONG_LTR.isRtl(text,0,text.length())?Layout.Alignment.ALIGN_OPPOSITE:Layout.Alignment.ALIGN_NORMAL).setTextDirection(TextDirectionHeuristics.FIRSTSTRONG_LTR).setIncludePad(false).setMaxLines(1).setEllipsize(TextUtils.TruncateAt.END).build();canvas.save();canvas.translate(centre?x-width/2f:x,baseline-layout.getLineBaseline(0));layout.draw(canvas);canvas.restore();
 }
}
