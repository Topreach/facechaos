package com.facechaos.mobile;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

/** Camera-side navigation sheet with native touch targets and original vector icons. */
final class AppOptionsPanel extends LinearLayout {
 interface Actions {void select(int index);void close();}
 private final int ink=0xFF173E42;
 AppOptionsPanel(Context context,Actions actions){
  super(context);setLayoutDirection(context.getResources().getConfiguration().getLayoutDirection());setOrientation(VERTICAL);setPadding(dp(20),dp(10),dp(20),dp(20));setBackground(shape(0xFFFFFBF0,28));
  View handle=new View(context);handle.setBackground(shape(0xFFCFDCD4,3));LayoutParams grip=new LayoutParams(dp(36),dp(4));grip.gravity=Gravity.CENTER_HORIZONTAL;grip.bottomMargin=dp(19);addView(handle,grip);
  LinearLayout header=new LinearLayout(context);header.setGravity(Gravity.CENTER_VERTICAL);
  LinearLayout brand=new LinearLayout(context);brand.setOrientation(VERTICAL);brand.addView(text("FACE CHAOS",12,0xFF648078,true));TextView heading=text("Your play space",26,ink,true);heading.setMaxLines(1);heading.setAutoSizeTextTypeUniformWithConfiguration(18,26,1,android.util.TypedValue.COMPLEX_UNIT_SP);brand.addView(heading);header.addView(brand,new LayoutParams(0,-2,1));
  Button close=new Button(context);close.setText("\u00D7");close.setTextSize(26);close.setTextColor(ink);close.setPadding(0,0,0,0);close.setMinWidth(0);close.setMinimumWidth(0);close.setBackground(shape(0xFFE9F0E9,24));close.setBackgroundTintList(null);close.setContentDescription(AppText.t(context,"Close options"));close.setOnClickListener(v->actions.close());header.addView(close,new LayoutParams(dp(48),dp(48)));addView(header);
  TextView intro=text("Everything for your next great take.",14,0xFF648078,false);intro.setPadding(0,dp(6),0,dp(22));addView(intro);
  row("Game options","Controls, sounds and your next challenge",0,0xFFFFE18A,actions);
  row("My recordings","Replay, save and share your best moments",1,0xFFBDE9DC,actions);
  row("Rate Face Chaos","Tell us how the game feels",2,0xFFE0D4F6,actions);
  row("Video storage","Find your clips and Gallery copies",3,0xFFCBE5F5,actions);
  row("Language","Choose the language you play in",4,0xFFFFD9BE,actions);
  row("Privacy policy","How your camera, voice and clips are used",5,0xFFCADFD9,actions);
  Button back=new Button(context);back.setText(AppText.t(context,"Back to camera"));back.setAllCaps(false);back.setTextSize(16);back.setTypeface(null,Typeface.BOLD);back.setTextColor(ink);back.setBackground(shape(0xFFF4CD59,18));back.setBackgroundTintList(null);back.setOnClickListener(v->actions.close());LayoutParams done=new LayoutParams(-1,dp(54));done.topMargin=dp(14);addView(back,done);
 }
 private void row(String title,String subtitle,int index,int color,Actions actions){
  LinearLayout row=new LinearLayout(getContext());row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(12),dp(14),dp(12),dp(14));row.setMinimumHeight(dp(84));
  StateListDrawable states=new StateListDrawable();states.addState(new int[]{android.R.attr.state_pressed},shape(0xFFDCEAE2,18));states.addState(new int[]{},shape(0xFFF0F3EB,18));row.setBackground(new RippleDrawable(android.content.res.ColorStateList.valueOf(0x22446861),states,shape(Color.WHITE,18)));
  Icon icon=new Icon(getContext(),index,color);row.addView(icon,new LayoutParams(dp(46),dp(46)));
  LinearLayout words=new LinearLayout(getContext());words.setOrientation(VERTICAL);words.setPaddingRelative(dp(12),0,dp(5),0);words.addView(text(title,16,ink,true));TextView detail=text(subtitle,12,0xFF627B75,false);detail.setPadding(0,dp(3),0,0);words.addView(detail);row.addView(words,new LayoutParams(0,-2,1));
  TextView arrow=text(getLayoutDirection()==View.LAYOUT_DIRECTION_RTL?"\u2039":"\u203A",26,0xFF718A82,false);arrow.setTextDirection(View.TEXT_DIRECTION_LTR);arrow.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);arrow.setGravity(Gravity.CENTER);row.addView(arrow,new LayoutParams(dp(16),-2));
  row.setFocusable(true);row.setContentDescription(AppText.t(getContext(),title)+". "+AppText.t(getContext(),subtitle));row.setOnClickListener(v->actions.select(index));LayoutParams spacing=new LayoutParams(-1,-2);spacing.bottomMargin=dp(9);addView(row,spacing);
 }
 private TextView text(String value,int size,int color,boolean bold){TextView text=new TextView(getContext());text.setText(AppText.t(getContext(),value));text.setTextSize(size);text.setTextColor(color);if(bold)text.setTypeface(null,Typeface.BOLD);return text;}
 private GradientDrawable shape(int color,int radius){GradientDrawable bg=new GradientDrawable();bg.setColor(color);bg.setCornerRadius(dp(radius));return bg;}
 private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
 private static final class Icon extends View {
  final int type,color;final Paint p=new Paint(3);
  Icon(Context context,int type,int color){super(context);this.type=type;this.color=color;setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
  @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);canvas.save();canvas.scale(getWidth()/46f,getHeight()/46f);p.setStyle(Paint.Style.FILL);p.setColor(color);canvas.drawRoundRect(0,0,46,46,14,14,p);p.setColor(0xFF244C4C);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.8f);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);
   if(type==0){canvas.drawRoundRect(10,16,36,32,6,6,p);canvas.drawLine(15,23,23,23,p);canvas.drawLine(19,19,19,27,p);canvas.drawCircle(29,21,1,p);canvas.drawCircle(32,26,1,p);}
   else if(type==1){canvas.drawRoundRect(10,13,36,33,4,4,p);Path play=new Path();play.moveTo(20,18);play.lineTo(28,23);play.lineTo(20,28);play.close();canvas.drawPath(play,p);}
   else if(type==2){Path star=new Path();for(int i=0;i<10;i++){double a=-Math.PI/2+i*Math.PI/5;float radius=i%2==0?13:6;float x=23+(float)Math.cos(a)*radius,y=23+(float)Math.sin(a)*radius;if(i==0)star.moveTo(x,y);else star.lineTo(x,y);}star.close();canvas.drawPath(star,p);}
   else if(type==5){Path shield=new Path();shield.moveTo(23,9);shield.lineTo(35,14);shield.lineTo(34,25);shield.quadTo(32,33,23,37);shield.quadTo(14,33,12,25);shield.lineTo(11,14);shield.close();canvas.drawPath(shield,p);canvas.drawLine(18,23,22,27,p);canvas.drawLine(22,27,29,19,p);}
   else if(type==4){canvas.drawCircle(23,23,13,p);canvas.drawOval(17,10,29,36,p);canvas.drawLine(10,23,36,23,p);canvas.drawLine(12,17,34,17,p);canvas.drawLine(12,29,34,29,p);}
   else{Path folder=new Path();folder.moveTo(10,17);folder.lineTo(10,13);folder.lineTo(20,13);folder.lineTo(24,17);folder.lineTo(36,17);folder.lineTo(36,33);folder.lineTo(10,33);folder.close();canvas.drawPath(folder,p);canvas.drawLine(23,20,23,29,p);canvas.drawLine(19,25,23,29,p);canvas.drawLine(27,25,23,29,p);}
   p.setStyle(Paint.Style.FILL);canvas.restore();
  }
 }
}
