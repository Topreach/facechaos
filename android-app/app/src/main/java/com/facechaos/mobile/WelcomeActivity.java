package com.facechaos.mobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Button;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;

/** Public entry point. Camera/microphone acquisition starts only on the next screen. */
public final class WelcomeActivity extends Activity {
 private boolean opening;
 @Override protected void onResume(){super.onResume();opening=false;}
 @Override protected void attachBaseContext(android.content.Context base){super.attachBaseContext(AppLanguage.wrap(base));}
 @Override public void onCreate(Bundle state){
  super.onCreate(state);
  WelcomePanel panel=new WelcomePanel(this,()->{if(!opening){opening=true;startActivity(new Intent(this,MainActivity.class));}},this::chooseLanguage,()->{
   try{startActivity(new Intent(Intent.ACTION_VIEW,android.net.Uri.parse("https://topreach.github.io/facechaos/privacy.html")));}catch(android.content.ActivityNotFoundException error){new AlertDialog.Builder(this).setMessage("https://topreach.github.io/facechaos/privacy.html").setPositiveButton(android.R.string.ok,null).show();}
  });
  setContentView(panel);
  if(android.os.Build.VERSION.SDK_INT>=30){android.view.WindowInsetsController bars=getWindow().getInsetsController();if(bars!=null)bars.setSystemBarsAppearance(android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS|android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS|android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS);}
  else getWindow().getDecorView().setSystemUiVisibility(android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
  getWindow().setNavigationBarColor(0xFFEAF4EE);if(android.os.Build.VERSION.SDK_INT>=29)getWindow().setNavigationBarContrastEnforced(false);
 }
 private void chooseLanguage(){
  String[] names=AppLanguage.NAMES.clone();names[0]=getString(R.string.fc_system);int selected=0;String code=AppLanguage.current(this);
  for(int i=0;i<AppLanguage.CODES.length;i++)if(AppLanguage.CODES[i].equals(code))selected=i;
  new AlertDialog.Builder(this).setTitle(R.string.fc_language).setSingleChoiceItems(names,selected,(dialog,index)->{dialog.dismiss();AppLanguage.set(this,AppLanguage.CODES[index]);}).setNegativeButton(android.R.string.cancel,null).show();
 }
}

/** Native, scrollable and width-bounded so large text and landscape stay usable. */
final class WelcomePanel extends FrameLayout {
 WelcomePanel(android.content.Context context,Runnable play,Runnable language,Runnable privacy){
  super(context);setLayoutDirection(getResources().getConfiguration().getLayoutDirection());setBackgroundColor(0xFFEAF4EE);
  ScrollView scroll=new ScrollView(context);scroll.setFillViewport(true);scroll.setClipToPadding(false);addView(scroll,new LayoutParams(-1,-1));
  FrameLayout container=new FrameLayout(context);scroll.addView(container,new ScrollView.LayoutParams(-1,-2));
  LinearLayout body=new LinearLayout(context);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(24),dp(14),dp(24),dp(20));
  LayoutParams bodyParams=new LayoutParams(-1,-2,Gravity.TOP|Gravity.CENTER_HORIZONTAL);container.addView(body,bodyParams);
  container.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->{int width=Math.min(r-l,dp(560));LayoutParams params=(LayoutParams)body.getLayoutParams();if(params.width!=width){params.width=width;body.setLayoutParams(params);}});
  LinearLayout header=new LinearLayout(context);header.setGravity(Gravity.CENTER_VERTICAL);
  TextView brand=text("FACE CHAOS",17,0xFF173E42,true);brand.setLetterSpacing(.10f);header.addView(brand,new LinearLayout.LayoutParams(0,-2,1));
  Button languages=button(getResources().getString(R.string.fc_language),0xFFD8EBE4);languages.setTextSize(13);languages.setOnClickListener(v->language.run());header.addView(languages,new LinearLayout.LayoutParams(-2,dp(44)));body.addView(header);
  WelcomeArt art=new WelcomeArt(context);art.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);LinearLayout.LayoutParams artwork=new LinearLayout.LayoutParams(-1,dp(136));artwork.topMargin=dp(12);body.addView(art,artwork);
  TextView title=text(getResources().getString(R.string.fc_home_title),30,0xFF173E42,true);title.setLineSpacing(dp(1),1);LinearLayout.LayoutParams titleParams=new LinearLayout.LayoutParams(-1,-2);titleParams.topMargin=dp(14);body.addView(title,titleParams);
  TextView intro=text(getResources().getString(R.string.fc_home_intro),15,0xFF49695F,false);intro.setLineSpacing(dp(3),1);LinearLayout.LayoutParams introParams=new LinearLayout.LayoutParams(-1,-2);introParams.topMargin=dp(8);introParams.bottomMargin=dp(16);body.addView(intro,introParams);
  LinearLayout steps=new LinearLayout(context);steps.setOrientation(LinearLayout.VERTICAL);steps.setPadding(dp(14),dp(4),dp(14),dp(4));steps.setBackground(shape(0xFFFFFEF7,22));
  step(steps,"1",R.string.fc_home_choose,R.string.fc_home_choose_note,0xFFFFE28A);
  step(steps,"2",R.string.fc_home_follow,R.string.fc_home_follow_note,0xFFD8D0FA);
  step(steps,"3",R.string.fc_home_record,R.string.fc_home_record_note,0xFFBDE5D9);body.addView(steps);
  Button start=button(getResources().getString(R.string.fc_home_start),0xFFF4D452);start.setTextSize(18);start.setOnClickListener(v->play.run());LinearLayout.LayoutParams startParams=new LinearLayout.LayoutParams(-1,-2);startParams.topMargin=dp(18);start.setMinHeight(dp(58));body.addView(start,startParams);
  TextView note=text(getResources().getString(R.string.fc_home_privacy_note),12,0xFF49695F,false);note.setGravity(Gravity.CENTER);note.setPadding(dp(8),dp(10),dp(8),dp(2));body.addView(note);
  Button policy=button(getResources().getString(R.string.fc_privacy),0x00000000);policy.setTextSize(12);policy.setOnClickListener(v->privacy.run());body.addView(policy,new LinearLayout.LayoutParams(-1,dp(44)));
  setOnApplyWindowInsetsListener((v,insets)->{if(android.os.Build.VERSION.SDK_INT>=30){android.graphics.Insets safe=insets.getInsets(android.view.WindowInsets.Type.systemBars()|android.view.WindowInsets.Type.displayCutout());scroll.setPadding(safe.left,safe.top,safe.right,safe.bottom);}else scroll.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());return insets;});
 }
 private void step(LinearLayout parent,String number,int title,int note,int color){
  LinearLayout row=new LinearLayout(getContext());row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(0,dp(10),0,dp(10));
  TextView badge=text(number,16,0xFF173E42,true);badge.setGravity(Gravity.CENTER);badge.setBackground(shape(color,12));row.addView(badge,new LinearLayout.LayoutParams(dp(34),dp(34)));
  LinearLayout copy=new LinearLayout(getContext());copy.setOrientation(LinearLayout.VERTICAL);copy.setPaddingRelative(dp(12),0,0,0);copy.addView(text(getResources().getString(title),14,0xFF173E42,true));copy.addView(text(getResources().getString(note),12,0xFF49695F,false));row.addView(copy,new LinearLayout.LayoutParams(0,-2,1));parent.addView(row);
 }
 private TextView text(String value,int size,int color,boolean bold){TextView label=new TextView(getContext());label.setText(value);label.setTextSize(size);label.setTextColor(color);if(bold)label.setTypeface(null,Typeface.BOLD);return label;}
 private Button button(String label,int color){Button button=new Button(getContext());button.setText(label);button.setAllCaps(false);button.setTextColor(0xFF173E42);button.setTypeface(null,Typeface.BOLD);button.setBackground(shape(color,20));button.setBackgroundTintList(null);button.setPadding(dp(16),dp(8),dp(16),dp(8));return button;}
 private GradientDrawable shape(int color,int radius){GradientDrawable shape=new GradientDrawable();shape.setColor(color);shape.setCornerRadius(dp(radius));return shape;}
 private int dp(int value){return Math.round(value*getResources().getDisplayMetrics().density);}
}
