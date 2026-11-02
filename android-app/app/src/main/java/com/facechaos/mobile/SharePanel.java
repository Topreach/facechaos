package com.facechaos.mobile;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import java.util.function.IntConsumer;

final class SharePanel extends LinearLayout {
 static void show(Activity activity,IntConsumer selected,Runnable dismissed){
  Dialog[] dialog={null};SharePanel panel=new SharePanel(activity,index->{dialog[0].dismiss();selected.accept(index);});dialog[0]=CanvasSheet.show(activity,false,panel,dismissed);
 }
 SharePanel(Context context,IntConsumer selected){super(context);setOrientation(VERTICAL);setLayoutDirection(getResources().getConfiguration().getLayoutDirection());
  TextView note=new TextView(context);note.setText(R.string.fc_share_note);note.setTextSize(14);note.setTextColor(0xFF49695F);note.setPadding(CanvasSheet.dp(context,4),0,CanvasSheet.dp(context,4),CanvasSheet.dp(context,16));addView(note);
  String[] names={"TikTok","Instagram","YouTube","WhatsApp",AppText.t(context,"Other apps")};
  float font=Math.max(1,getResources().getConfiguration().fontScale);
  for(int row=0;row<2;row++){LinearLayout pair=new LinearLayout(context);for(int column=0;column<2;column++){int index=row*2+column;Tile tile=new Tile(context,names[index],index,()->selected.accept(index));LayoutParams params=new LayoutParams(0,Math.round(CanvasSheet.dp(context,106)*font),1);params.setMarginStart(CanvasSheet.dp(context,4));params.setMarginEnd(CanvasSheet.dp(context,4));pair.addView(tile,params);}LayoutParams spacing=new LayoutParams(-1,-2);spacing.bottomMargin=CanvasSheet.dp(context,8);addView(pair,spacing);}
  Tile more=new Tile(context,names[4],4,()->selected.accept(4));LayoutParams last=new LayoutParams(-1,Math.round(CanvasSheet.dp(context,84)*font));last.setMarginStart(CanvasSheet.dp(context,4));last.setMarginEnd(CanvasSheet.dp(context,4));addView(more,last);
 }
 private static final class Tile extends FrameLayout {
  private final Paint p=new Paint(3);private final int type;
  Tile(Context context,String name,int type,Runnable selected){super(context);this.type=type;setWillNotDraw(false);setFocusable(true);setClickable(true);setContentDescription(name);setOnClickListener(v->selected.run());TextView label=new TextView(context);label.setText(name);label.setTextColor(type==0?Color.WHITE:0xFF173E42);label.setTextSize(14);label.setTypeface(null,Typeface.BOLD);label.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);label.setPadding(CanvasSheet.dp(context,4),0,CanvasSheet.dp(context,4),CanvasSheet.dp(context,10));label.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);addView(label,new LayoutParams(-1,-1));}
  @Override protected void drawableStateChanged(){super.drawableStateChanged();invalidate();}
  @Override protected void onDraw(Canvas c){super.onDraw(c);float density=getResources().getDisplayMetrics().density;c.save();c.scale(density,density);float w=getWidth()/density,h=getHeight()/density,x=w/2,y=Math.min(36,(h-25)/2);int[] backgrounds={0xFF233B42,0xFFF3DFE9,0xFFFFE4DA,0xFFDCEFE1,0xFFE3EAF4};p.setColor(backgrounds[type]);p.setStyle(Paint.Style.FILL);c.drawRoundRect(0,0,w,h,18,18,p);if(isPressed()||isFocused()){p.setColor(0x204A7769);c.drawRoundRect(0,0,w,h,18,18,p);}p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);p.setStrokeWidth(3);
   if(type==0){p.setStyle(Paint.Style.STROKE);p.setColor(0xFF8DE1DC);note(c,x-2,y-1);p.setColor(0xFFF494A4);note(c,x+2,y+1);p.setColor(Color.WHITE);note(c,x,y);}
   else if(type==1){p.setStyle(Paint.Style.STROKE);p.setColor(0xFF985780);c.drawRoundRect(x-17,y-17,x+17,y+17,10,10,p);c.drawCircle(x,y,8,p);p.setStyle(Paint.Style.FILL);c.drawCircle(x+10,y-10,2,p);}
   else if(type==2){p.setColor(0xFFE15D50);c.drawRoundRect(x-23,y-15,x+23,y+15,9,9,p);p.setColor(Color.WHITE);Path path=new Path();path.moveTo(x-5,y-8);path.lineTo(x+9,y);path.lineTo(x-5,y+8);path.close();c.drawPath(path,p);}
   else if(type==3){p.setColor(0xFF4C9770);c.drawCircle(x,y,20,p);Path bubble=new Path();bubble.moveTo(x-12,y+10);bubble.lineTo(x-20,y+22);bubble.lineTo(x-3,y+16);bubble.close();c.drawPath(bubble,p);p.setColor(Color.WHITE);p.setStyle(Paint.Style.STROKE);c.drawArc(x-10,y-11,x+12,y+10,80,125,false,p);c.drawLine(x-9,y-6,x-6,y-10,p);c.drawLine(x+2,y+9,x+8,y+5,p);}
   else{p.setColor(0xFF5876A0);p.setStyle(Paint.Style.STROKE);c.drawLine(x-12,y,x+12,y-11,p);c.drawLine(x-12,y,x+12,y+11,p);p.setStyle(Paint.Style.FILL);c.drawCircle(x-12,y,5,p);c.drawCircle(x+12,y-11,5,p);c.drawCircle(x+12,y+11,5,p);}p.setStyle(Paint.Style.FILL);c.restore();
  }
  private void note(Canvas c,float x,float y){c.drawLine(x+5,y-18,x+5,y+8,p);c.drawArc(x-9,y+2,x+5,y+14,0,360,false,p);c.drawArc(x+5,y-23,x+23,y-7,90,90,false,p);}
 }
}
