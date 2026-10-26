package com.facechaos.mobile;
import android.content.Context;
import android.graphics.*;
/** Matching transparent sprites for the wide-eyes and face-squeeze gates. */
final class GateExtraArt {
 private static Bitmap atlas;
 private static final Rect[] ACTORS={new Rect(72,59,554,538),new Rect(700,60,1182,538)};
 private static final Rect[] GATES={new Rect(23,648,604,1215),new Rect(649,648,1231,1214)};
 GateExtraArt(Context context){if(atlas==null){BitmapFactory.Options options=new BitmapFactory.Options();options.inScaled=false;atlas=BitmapFactory.decodeResource(context.getResources(),R.drawable.emoji_extra_atlas,options);if(atlas==null)throw new IllegalStateException("Extended gate artwork missing");}}
 void actor(Canvas canvas,int gate,RectF bounds,Paint paint){canvas.drawBitmap(atlas,ACTORS[gate-3],bounds,paint);}
 void gate(Canvas canvas,int gate,RectF bounds,Paint paint){canvas.drawBitmap(atlas,GATES[gate-3],bounds,paint);}
}
