package com.facechaos.mobile;
import android.content.Context;
import android.graphics.*;
import android.view.View;

/** Original camera-game illustration; no camera feed or personal image. */
final class WelcomeArt extends View {
 private final Paint p=new Paint(3);
 WelcomeArt(Context context){super(context);}
 @Override protected void onDraw(Canvas c){super.onDraw(c);c.save();float radius=22*getResources().getDisplayMetrics().density;Path clip=new Path();clip.addRoundRect(0,0,getWidth(),getHeight(),radius,radius,Path.Direction.CW);c.clipPath(clip);c.drawColor(0xFF173E42);float scale=Math.min(getWidth()/400f,getHeight()/154f);c.translate((getWidth()-400*scale)/2,(getHeight()-154*scale)/2);c.scale(scale,scale);
  p.setColor(0xFF173E42);c.drawRoundRect(0,0,400,154,26,26,p);
  p.setColor(0xFF285953);c.drawCircle(55,142,92,p);p.setColor(0xFF326D61);c.drawCircle(365,12,92,p);
  p.setColor(0xFFBDE5D9);c.drawRoundRect(35,29,116,123,16,16,p);p.setColor(0xFF173E42);c.drawRoundRect(45,40,106,109,10,10,p);
  p.setColor(0xFFFFE58A);c.drawCircle(75,72,18,p);p.setColor(0xFF173E42);c.drawCircle(69,69,2,p);c.drawCircle(81,69,2,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);c.drawArc(67,72,83,83,0,180,false,p);p.setStyle(Paint.Style.FILL);
  p.setColor(0xFFF4D452);c.drawCircle(207,78,60,p);p.setColor(0xFFFFE58A);c.drawCircle(200,69,47,p);
  p.setColor(0xFF173E42);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(5);p.setStrokeCap(Paint.Cap.ROUND);c.drawArc(175,52,193,68,190,160,false,p);c.drawArc(218,52,236,68,190,160,false,p);p.setStyle(Paint.Style.FILL);c.drawOval(182,76,232,114,p);
  p.setColor(0xFFFFFAE8);c.drawRoundRect(188,79,226,88,4,4,p);p.setColor(0xFFF28E91);c.drawOval(195,100,221,115,p);
  p.setColor(0xFFD8D0FA);c.drawRoundRect(290,50,364,117,20,20,p);p.setColor(0xFF173E42);c.drawRoundRect(319,64,335,87,8,8,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);c.drawArc(312,73,342,95,0,180,false,p);c.drawLine(327,95,327,103,p);c.drawLine(320,103,334,103,p);p.setStyle(Paint.Style.FILL);
  star(c,137,31,8,0xFFFFFFFF);star(c,279,28,6,0xFFF4D452);star(c,278,132,7,0xFFBDE5D9);star(c,32,129,5,0xFFF4D452);c.restore();
 }
 private void star(Canvas c,float x,float y,float radius,int color){Path path=new Path();for(int i=0;i<8;i++){double a=-Math.PI/2+i*Math.PI/4;float r=i%2==0?radius:radius*.35f;float px=x+(float)Math.cos(a)*r,py=y+(float)Math.sin(a)*r;if(i==0)path.moveTo(px,py);else path.lineTo(px,py);}path.close();p.setColor(color);c.drawPath(path,p);}
}
