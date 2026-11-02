package com.facechaos.mobile;
import android.content.Context;
import android.graphics.*;
import android.view.Gravity;
import android.widget.RadioButton;

/** Canvas mode tile with native RadioButton checked/accessibility semantics. */
final class LookModeCard extends RadioButton {
 private final Paint p=new Paint(3);private final int mode;
 LookModeCard(Context context,int mode,int label){super(context);this.mode=mode;setText(label);setTextSize(13);setButtonDrawable((android.graphics.drawable.Drawable)null);setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);setPadding(2,CanvasSheet.dp(context,38),2,CanvasSheet.dp(context,10));setBackgroundColor(Color.TRANSPARENT);setTextColor(new android.content.res.ColorStateList(new int[][]{{android.R.attr.state_checked},{}},new int[]{Color.WHITE,0xFF173E42}));}
 @Override protected void drawableStateChanged(){super.drawableStateChanged();invalidate();}
 @Override protected void onDraw(Canvas c){float d=getResources().getDisplayMetrics().density;c.save();c.scale(d,d);float w=getWidth()/d,h=getHeight()/d;p.setStyle(Paint.Style.FILL);p.setColor(!isEnabled()?0xFFE4E9E4:isChecked()?0xFF29665D:isPressed()?0xFFD0E5DA:0xFFEDF4ED);c.drawRoundRect(3,2,w-3,h-2,16,16,p);p.setColor(isChecked()?0xFFFFE18A:0xFF52796B);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);p.setStrokeCap(Paint.Cap.ROUND);float x=w/2,y=23;c.drawCircle(x,y,11,p);
  if(mode==CameraLook.OFF){c.drawLine(x-8,y+8,x+8,y-8,p);}else if(mode==CameraLook.NATURAL){for(int i=0;i<8;i++){double angle=i*Math.PI/4;c.drawLine(x+(float)Math.cos(angle)*15,y+(float)Math.sin(angle)*15,x+(float)Math.cos(angle)*18,y+(float)Math.sin(angle)*18,p);}}else{p.setStyle(Paint.Style.FILL);c.drawCircle(x-4,y-2,1.5f,p);c.drawCircle(x+4,y-2,1.5f,p);p.setStyle(Paint.Style.STROKE);c.drawArc(x-5,y+1,x+5,y+7,0,180,false,p);}p.setStyle(Paint.Style.FILL);c.restore();super.onDraw(c);}
}
