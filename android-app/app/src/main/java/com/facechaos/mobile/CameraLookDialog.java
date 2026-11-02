package com.facechaos.mobile;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.View;
import android.widget.*;
import java.util.function.Consumer;

final class CameraLookDialog {
 static void show(Activity activity,CameraLook initial,boolean smoothing,Consumer<CameraLook> change,Consumer<Boolean> compare,Runnable dismissed){
  android.app.Dialog[] dialog={null};Consumer<Boolean> fullPreview=original->{compare.accept(original);if(dialog[0]!=null&&dialog[0].getWindow()!=null&&dialog[0].getWindow().getDecorView().isAttachedToWindow()){android.view.Window window=dialog[0].getWindow();window.getDecorView().setAlpha(original?.07f:1);android.view.WindowManager.LayoutParams attributes=window.getAttributes();attributes.dimAmount=original?0:.28f;window.setAttributes(attributes);}};
  dialog[0]=CanvasSheet.show(activity,true,create(activity,initial,smoothing,change,fullPreview),dismissed);
 }
 static View create(android.content.Context activity,CameraLook initial,boolean smoothing,Consumer<CameraLook> change,Consumer<Boolean> compare){
  LinearLayout body=new LinearLayout(activity);body.setOrientation(LinearLayout.VERTICAL);int pad=CanvasSheet.dp(activity,4);body.setPadding(pad,pad,pad,pad);body.setLayoutDirection(activity.getResources().getConfiguration().getLayoutDirection());
  TextView note=new TextView(activity);note.setText(R.string.fc_look_note);note.setTextColor(0xFF49695F);body.addView(note);
  RadioGroup modes=new RadioGroup(activity);modes.setOrientation(LinearLayout.HORIZONTAL);int[] labels={R.string.fc_look_off,R.string.fc_look_natural,R.string.fc_look_smooth};
  for(int i=0;i<labels.length;i++){RadioButton button=new LookModeCard(activity,i,labels[i]);button.setId(View.generateViewId());button.setTag(i);button.setEnabled(i!=CameraLook.SMOOTH||smoothing);modes.addView(button,new RadioGroup.LayoutParams(0,Math.round(CanvasSheet.dp(activity,72)*Math.max(1,activity.getResources().getConfiguration().fontScale)),1));if(i==initial.mode)button.setChecked(true);}LinearLayout.LayoutParams modeSpacing=new LinearLayout.LayoutParams(-1,-2);modeSpacing.topMargin=CanvasSheet.dp(activity,6);modeSpacing.bottomMargin=CanvasSheet.dp(activity,8);body.addView(modes,modeSpacing);
  if(!smoothing){TextView unavailable=new TextView(activity);unavailable.setText(R.string.fc_look_smoothing_unavailable);unavailable.setTextSize(12);unavailable.setTextColor(0xFF49695F);body.addView(unavailable);}
  LinearLayout adjustments=new LinearLayout(activity);LinearLayout intensityBox=new LinearLayout(activity),warmthBox=new LinearLayout(activity);intensityBox.setOrientation(LinearLayout.VERTICAL);warmthBox.setOrientation(LinearLayout.VERTICAL);adjustments.addView(intensityBox,new LinearLayout.LayoutParams(0,-2,1));adjustments.addView(warmthBox,new LinearLayout.LayoutParams(0,-2,1));body.addView(adjustments);
  TextView intensityLabel=new TextView(activity);intensityLabel.setTypeface(null,Typeface.BOLD);intensityLabel.setTextColor(0xFF173E42);intensityBox.addView(intensityLabel);SeekBar intensity=new SeekBar(activity);intensity.setContentDescription(activity.getString(R.string.fc_look_intensity));intensity.setMax(100);intensity.setProgress(Math.round(initial.intensity*100));intensity.setMinimumHeight(CanvasSheet.dp(activity,48));intensityBox.addView(intensity);
  TextView warmthLabel=new TextView(activity);warmthLabel.setTypeface(null,Typeface.BOLD);warmthLabel.setTextColor(0xFF173E42);warmthBox.addView(warmthLabel);SeekBar warmth=new SeekBar(activity);warmth.setContentDescription(activity.getString(R.string.fc_look_warmth));warmth.setMax(100);warmth.setProgress(Math.round(initial.warmth*50+50));warmth.setMinimumHeight(CanvasSheet.dp(activity,48));warmthBox.addView(warmth);
  for(SeekBar slider:new SeekBar[]{intensity,warmth}){slider.setProgressTintList(android.content.res.ColorStateList.valueOf(0xFF29665D));slider.setThumbTintList(android.content.res.ColorStateList.valueOf(0xFF29665D));slider.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFD2E1DA));}
  CompareButton original=new CompareButton(activity,compare);body.addView(original);
  int[] mode={initial.mode};Runnable update=()->{original.release();intensityLabel.setText(activity.getString(R.string.fc_look_intensity)+"  "+intensity.getProgress()+"%");warmthLabel.setText(activity.getString(R.string.fc_look_warmth)+"  "+(warmth.getProgress()-50));intensity.setEnabled(mode[0]!=CameraLook.OFF);warmth.setEnabled(mode[0]!=CameraLook.OFF);original.setEnabled(mode[0]!=CameraLook.OFF&&intensity.getProgress()>0);change.accept(new CameraLook(mode[0],intensity.getProgress()/100f,(warmth.getProgress()-50)/50f));};
  modes.setOnCheckedChangeListener((group,id)->{RadioButton selected=group.findViewById(id);if(selected!=null){mode[0]=(Integer)selected.getTag();update.run();}});
  SeekBar.OnSeekBarChangeListener slider=new SeekBar.OnSeekBarChangeListener(){public void onStartTrackingTouch(SeekBar view){}public void onStopTrackingTouch(SeekBar view){}public void onProgressChanged(SeekBar view,int progress,boolean user){update.run();}};intensity.setOnSeekBarChangeListener(slider);warmth.setOnSeekBarChangeListener(slider);update.run();
  return body;
 }
 private static final class CompareButton extends Button {
  private final Consumer<Boolean> compare;
  private final Runnable reset=this::release;
  CompareButton(android.content.Context activity,Consumer<Boolean> compare){super(activity);this.compare=compare;setText(R.string.fc_look_compare);setTextColor(0xFF173E42);setAllCaps(false);android.graphics.drawable.GradientDrawable background=new android.graphics.drawable.GradientDrawable();background.setColor(0xFFD8EBE4);background.setCornerRadius(12*getResources().getDisplayMetrics().density);setBackground(background);setBackgroundTintList(null);setMinimumHeight(Math.round(48*getResources().getDisplayMetrics().density));}
  private void original(){removeCallbacks(reset);setPressed(true);setText(R.string.fc_look_original);compare.accept(true);}
  void release(){removeCallbacks(reset);setPressed(false);setText(R.string.fc_look_compare);compare.accept(false);}
  @Override public boolean onTouchEvent(android.view.MotionEvent event){if(!isEnabled())return false;switch(event.getActionMasked()){case android.view.MotionEvent.ACTION_DOWN:getParent().requestDisallowInterceptTouchEvent(true);original();return true;case android.view.MotionEvent.ACTION_UP:release();getParent().requestDisallowInterceptTouchEvent(false);super.performClick();return true;case android.view.MotionEvent.ACTION_CANCEL:release();return true;case android.view.MotionEvent.ACTION_MOVE:return true;default:return super.onTouchEvent(event);}}
  @Override public boolean performClick(){if(!isEnabled())return false;super.performClick();original();postDelayed(reset,1000);return true;}
  @Override protected void onDetachedFromWindow(){release();super.onDetachedFromWindow();}
 }
}
