package com.facechaos.mobile;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/** One primary action, with an explicit icon for its current state. */
final class CaptureButton extends View {
    enum Mode { PLAY, PAUSE, RECORD, RETRY, BUSY }
    private final Paint p=new Paint(3);
    private final Path path=new Path();
    private Mode mode=Mode.RECORD;
    private boolean recording;
    CaptureButton(Context context){super(context);setClickable(true);setFocusable(true);}
    void state(Mode value,boolean rec){
        if(mode!=value||recording!=rec){mode=value;recording=rec;invalidate();}
        setContentDescription(value==Mode.RETRY?"Try again":value==Mode.PLAY?"Play or resume":value==Mode.PAUSE?"Pause game and recording":value==Mode.BUSY?"Preparing video":"Record video");
    }
    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);float size=Math.min(getWidth(),getHeight()),r=size/2-4,cx=getWidth()/2f,cy=getHeight()/2f;
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3*getResources().getDisplayMetrics().density);p.setColor(Color.WHITE);canvas.drawCircle(cx,cy,r,p);
        p.setStyle(Paint.Style.FILL);p.setColor(recording||mode==Mode.RECORD?0xFFFF355E:0xFF268D80);canvas.drawCircle(cx,cy,r-9*getResources().getDisplayMetrics().density,p);
        p.setColor(Color.WHITE);float u=r*.38f;
        if(mode==Mode.PAUSE){canvas.drawRoundRect(cx-u,cy-u,cx-u*.28f,cy+u,3,3,p);canvas.drawRoundRect(cx+u*.28f,cy-u,cx+u,cy+u,3,3,p);}
        else if(mode==Mode.PLAY||mode==Mode.RETRY){path.reset();path.moveTo(cx-u*.6f,cy-u);path.lineTo(cx+u,cy);path.lineTo(cx-u*.6f,cy+u);path.close();canvas.drawPath(path,p);}
        else if(mode==Mode.BUSY){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(4);canvas.drawArc(cx-u,cy-u,cx+u,cy+u,-90,250,false,p);}
        if(isPressed()||isFocused()){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);p.setColor(0xFFFFD66D);canvas.drawCircle(cx,cy,r-2,p);}
    }
    @Override protected void drawableStateChanged(){super.drawableStateChanged();invalidate();}
}
