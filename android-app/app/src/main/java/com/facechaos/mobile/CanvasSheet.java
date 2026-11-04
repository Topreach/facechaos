package com.facechaos.mobile;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.ColorDrawable;
import android.view.*;
import android.widget.*;

/** Shared illustrated shell; native children retain accessibility and scrolling. */
final class CanvasSheet {
 static Dialog show(Activity activity,boolean look,View content,Runnable close){
  return show(activity,look?0:1,content,close);
 }
 static Dialog show(Activity activity,int kind,View content,Runnable close){
  boolean look=kind==0;
  Dialog dialog=new Dialog(activity,android.R.style.Theme_Material_Light_Dialog_NoActionBar);
  Panel panel=new Panel(activity,kind,content,()->dialog.dismiss());dialog.setContentView(panel);dialog.setOnDismissListener(v->{if(close!=null)close.run();});
  android.view.Window window=dialog.getWindow();if(window!=null){window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);WindowManager.LayoutParams attributes=window.getAttributes();attributes.dimAmount=.28f;window.setAttributes(attributes);}
  dialog.show();
  if(window!=null){View decor=activity.getWindow().getDecorView();int width=decor.getWidth(),height=decor.getHeight(),bottom=0;WindowInsets insets=decor.getRootWindowInsets();if(insets!=null){if(android.os.Build.VERSION.SDK_INT>=30){Insets safe=insets.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());width-=safe.left+safe.right;height-=safe.top+safe.bottom;bottom=safe.bottom;}else{width-=insets.getSystemWindowInsetLeft()+insets.getSystemWindowInsetRight();height-=insets.getSystemWindowInsetTop()+insets.getSystemWindowInsetBottom();bottom=insets.getSystemWindowInsetBottom();}}
   window.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);WindowManager.LayoutParams attributes=window.getAttributes();attributes.y=bottom+dp(activity,8);window.setAttributes(attributes);window.setLayout(Math.max(1,Math.min(dp(activity,560),width-dp(activity,20))),Math.min(dp(activity,680),Math.round(height*(width>height?.90f:look?.58f:.77f))));
  }
  return dialog;
 }
 static int dp(Context context,int value){return Math.round(value*context.getResources().getDisplayMetrics().density);}
 static class Surface extends LinearLayout {
  private final Paint paint=new Paint(3);private final int color;private final float radius;
  Surface(Context context,int color,int radius){super(context);this.color=color;this.radius=dp(context,radius);setWillNotDraw(false);setOrientation(VERTICAL);setLayoutDirection(getResources().getConfiguration().getLayoutDirection());setOutlineProvider(new ViewOutlineProvider(){public void getOutline(View view,Outline outline){outline.setRoundRect(0,0,view.getWidth(),view.getHeight(),Surface.this.radius);}});setClipToOutline(true);}
  @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);paint.setColor(color);canvas.drawRoundRect(0,0,getWidth(),getHeight(),radius,radius,paint);}
 }
 static final class Panel extends Surface {
  final ScrollView scroll;final Button done;
  Panel(Context context,boolean look,View content,Runnable closed){this(context,look?0:1,content,closed);}
  Panel(Context context,int kind,View content,Runnable closed){super(context,0xFFFFFBF0,28);boolean look=kind==0;int space=dp(context,16);setPadding(space,dp(context,10),space,space);
   View grip=new View(context);grip.setBackgroundColor(0xFFBACFC5);LayoutParams handle=new LayoutParams(dp(context,34),dp(context,3));handle.gravity=Gravity.CENTER_HORIZONTAL;handle.bottomMargin=dp(context,9);addView(grip,handle);
   LinearLayout scrolling=new LinearLayout(context);scrolling.setOrientation(VERTICAL);scrolling.addView(new Header(context,kind),new LayoutParams(-1,Math.round(dp(context,look?76:100)*Math.max(1,getResources().getConfiguration().fontScale))));LayoutParams contentSpace=new LayoutParams(-1,-2);contentSpace.topMargin=dp(context,12);scrolling.addView(content,contentSpace);
   scroll=new ScrollView(context);scroll.setFillViewport(false);scroll.setClipToPadding(false);scroll.setPadding(0,0,0,dp(context,10));scroll.addView(scrolling,new ScrollView.LayoutParams(-1,-2));if(content instanceof ListView){scrolling.removeView(content);scrolling.removeViewAt(0);addView(new Header(context,kind),new LayoutParams(-1,Math.round(dp(context,100)*Math.max(1,getResources().getConfiguration().fontScale))));LayoutParams listSpace=new LayoutParams(-1,0,1);listSpace.topMargin=dp(context,12);listSpace.bottomMargin=dp(context,10);addView(content,listSpace);}else addView(scroll,new LayoutParams(-1,0,1));
   done=new Button(context);done.setText(look||kind==2||kind==4?R.string.fc_done:R.string.fc_cancel);done.setAllCaps(false);done.setTextSize(16);done.setTypeface(null,Typeface.BOLD);done.setTextColor(0xFF173E42);done.setBackgroundTintList(null);android.graphics.drawable.GradientDrawable shape=new android.graphics.drawable.GradientDrawable();shape.setColor(look?0xFFF4D452:0xFFDDECE4);shape.setCornerRadius(dp(context,18));done.setBackground(shape);done.setMinHeight(dp(context,52));done.setOnClickListener(v->closed.run());if(content instanceof LanguagePanel){Button apply=((LanguagePanel)content).apply;((ViewGroup)apply.getParent()).removeView(apply);LinearLayout footer=new LinearLayout(context);footer.addView(done,new LayoutParams(0,-2,1));LayoutParams confirmSpace=new LayoutParams(0,-2,1);confirmSpace.setMarginStart(dp(context,8));footer.addView(apply,confirmSpace);addView(footer,new LayoutParams(-1,-2));}else addView(done,new LayoutParams(-1,-2));
  }
 }
 private static final class Header extends FrameLayout {
  private final Paint p=new Paint(3);private final boolean look;private final int kind;
  Header(Context context,int kind){super(context);this.look=kind==0;this.kind=kind;setWillNotDraw(false);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
   LinearLayout copy=new LinearLayout(context);copy.setOrientation(LinearLayout.VERTICAL);copy.setGravity(Gravity.CENTER_VERTICAL);copy.setPaddingRelative(dp(context,16),dp(context,8),dp(context,96),dp(context,8));TextView title=new TextView(context);title.setText(kind==4?R.string.fc_review_clip:kind==2?R.string.fc_recordings:kind==3?R.string.fc_language:look?R.string.fc_look_title:R.string.fc_share_video);title.setTextColor(0xFF173E42);title.setTypeface(null,Typeface.BOLD);title.setMaxLines(2);title.setAutoSizeTextTypeUniformWithConfiguration(17,24,1,android.util.TypedValue.COMPLEX_UNIT_SP);copy.addView(title,new LinearLayout.LayoutParams(-1,Math.round(dp(context,look?32:51)*Math.max(1,getResources().getConfiguration().fontScale))));TextView sub=new TextView(context);sub.setText(kind==4?R.string.fc_recordings_sub:kind==2?R.string.fc_library_subtitle:kind==3?R.string.fc_language_subtitle:look?R.string.fc_look_subtitle:R.string.fc_share_subtitle);sub.setTextColor(0xFF49695F);sub.setTextSize(12);copy.addView(sub);addView(copy,new LayoutParams(-1,-1));
  }
  @Override protected void onDraw(Canvas c){super.onDraw(c);float d=getResources().getDisplayMetrics().density;c.save();c.scale(d,d);float width=getWidth()/d,height=getHeight()/d;p.setColor(look?0xFFDDEFE7:0xFFE5DDF8);c.drawRoundRect(0,0,width,height,22,22,p);if(getLayoutDirection()==LAYOUT_DIRECTION_RTL){c.translate(width,0);c.scale(-1,1);}float x=width-48,y=height/2;p.setColor(0x33FFFFFF);c.drawCircle(x,y,42,p);p.setColor(look?0xFFF4D452:0xFFB7DDD3);c.drawCircle(x,y,29,p);p.setColor(0xFF173E42);p.setStrokeWidth(2.4f);p.setStrokeCap(Paint.Cap.ROUND);
   if(look){c.drawCircle(x-9,y-5,2.4f,p);c.drawCircle(x+9,y-5,2.4f,p);p.setStyle(Paint.Style.STROKE);c.drawArc(x-12,y-1,x+12,y+14,0,180,false,p);p.setStyle(Paint.Style.FILL);star(c,x+23,y-27,8);star(c,x-29,y+20,5);}else if(kind==3){p.setStyle(Paint.Style.STROKE);c.drawCircle(x,y,21,p);c.drawOval(x-10,y-21,x+10,y+21,p);c.drawLine(x-20,y,x+20,y,p);c.drawArc(x-19,y-10,x+19,y+10,0,180,false,p);p.setStyle(Paint.Style.FILL);star(c,x+25,y-24,6);}else{p.setColor(0xFF173E42);c.drawRoundRect(x-21,y-15,x+21,y+15,7,7,p);p.setColor(Color.WHITE);Path play=new Path();play.moveTo(x-5,y-8);play.lineTo(x+8,y);play.lineTo(x-5,y+8);play.close();c.drawPath(play,p);p.setColor(0xFF715E9D);star(c,x+25,y-24,7);star(c,x-28,y+23,5);}c.restore();
  }
  private void star(Canvas c,float x,float y,float radius){Path path=new Path();path.moveTo(x,y-radius);path.lineTo(x+radius*.3f,y-radius*.3f);path.lineTo(x+radius,y);path.lineTo(x+radius*.3f,y+radius*.3f);path.lineTo(x,y+radius);path.lineTo(x-radius*.3f,y+radius*.3f);path.lineTo(x-radius,y);path.lineTo(x-radius*.3f,y-radius*.3f);path.close();c.drawPath(path,p);}
 }
}
