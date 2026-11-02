package com.facechaos.mobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Typeface;
import android.view.View;
import android.widget.*;
import java.util.function.Consumer;

final class CameraLookDialog {
 static void show(Activity activity,CameraLook initial,boolean smoothing,Consumer<CameraLook> change,Consumer<Boolean> compare,Runnable dismissed){
  LinearLayout body=new LinearLayout(activity);body.setOrientation(LinearLayout.VERTICAL);int pad=Math.round(20*activity.getResources().getDisplayMetrics().density);body.setPadding(pad,pad/2,pad,pad/2);body.setLayoutDirection(activity.getResources().getConfiguration().getLayoutDirection());
  TextView note=new TextView(activity);note.setText(R.string.fc_look_note);note.setTextColor(0xFF49695F);body.addView(note);
  RadioGroup modes=new RadioGroup(activity);modes.setOrientation(LinearLayout.HORIZONTAL);int[] labels={R.string.fc_look_off,R.string.fc_look_natural,R.string.fc_look_smooth};
  for(int i=0;i<labels.length;i++){RadioButton button=new RadioButton(activity);button.setId(View.generateViewId());button.setText(labels[i]);button.setTextSize(13);button.setTextColor(0xFF173E42);button.setButtonTintList(new android.content.res.ColorStateList(new int[][]{{android.R.attr.state_checked},{-android.R.attr.state_enabled},{}},new int[]{0xFF29665D,0xFFB6C5BE,0xFF6D837B}));button.setTag(i);button.setEnabled(i!=CameraLook.SMOOTH||smoothing);modes.addView(button,new RadioGroup.LayoutParams(0,-2,1));if(i==initial.mode)button.setChecked(true);}body.addView(modes);
  if(!smoothing){TextView unavailable=new TextView(activity);unavailable.setText(R.string.fc_look_smoothing_unavailable);unavailable.setTextSize(12);unavailable.setTextColor(0xFF49695F);body.addView(unavailable);}
  TextView intensityLabel=new TextView(activity);intensityLabel.setTypeface(null,Typeface.BOLD);intensityLabel.setTextColor(0xFF173E42);body.addView(intensityLabel);SeekBar intensity=new SeekBar(activity);intensity.setContentDescription(activity.getString(R.string.fc_look_intensity));intensity.setMax(100);intensity.setProgress(Math.round(initial.intensity*100));body.addView(intensity);
  TextView warmthLabel=new TextView(activity);warmthLabel.setTypeface(null,Typeface.BOLD);warmthLabel.setTextColor(0xFF173E42);body.addView(warmthLabel);SeekBar warmth=new SeekBar(activity);warmth.setContentDescription(activity.getString(R.string.fc_look_warmth));warmth.setMax(100);warmth.setProgress(Math.round(initial.warmth*50+50));body.addView(warmth);
  for(SeekBar slider:new SeekBar[]{intensity,warmth}){slider.setProgressTintList(android.content.res.ColorStateList.valueOf(0xFF29665D));slider.setThumbTintList(android.content.res.ColorStateList.valueOf(0xFF29665D));slider.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFD2E1DA));}
  Switch original=new Switch(activity);original.setText(R.string.fc_look_compare);original.setTextColor(0xFF173E42);body.addView(original);original.setOnCheckedChangeListener((button,checked)->compare.accept(checked));
  int[] mode={initial.mode};Runnable update=()->{intensityLabel.setText(activity.getString(R.string.fc_look_intensity)+"  "+intensity.getProgress()+"%");warmthLabel.setText(activity.getString(R.string.fc_look_warmth)+"  "+(warmth.getProgress()-50));intensity.setEnabled(mode[0]!=CameraLook.OFF);warmth.setEnabled(mode[0]!=CameraLook.OFF);change.accept(new CameraLook(mode[0],intensity.getProgress()/100f,(warmth.getProgress()-50)/50f));};
  modes.setOnCheckedChangeListener((group,id)->{RadioButton selected=group.findViewById(id);if(selected!=null){mode[0]=(Integer)selected.getTag();update.run();}});
  SeekBar.OnSeekBarChangeListener slider=new SeekBar.OnSeekBarChangeListener(){public void onStartTrackingTouch(SeekBar view){}public void onStopTrackingTouch(SeekBar view){}public void onProgressChanged(SeekBar view,int progress,boolean user){if(user)update.run();}};intensity.setOnSeekBarChangeListener(slider);warmth.setOnSeekBarChangeListener(slider);update.run();
  ScrollView scroll=new ScrollView(activity);scroll.addView(body);AlertDialog dialog=new AlertDialog.Builder(activity,android.R.style.Theme_Material_Light_Dialog_Alert).setTitle(R.string.fc_look_title).setView(scroll).setPositiveButton(R.string.fc_done,null).create();dialog.setOnDismissListener(v->dismissed.run());dialog.show();
 }
}
