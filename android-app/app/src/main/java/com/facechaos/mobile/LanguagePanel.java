package com.facechaos.mobile;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.widget.*;
import java.util.function.Consumer;

/** A staged language choice: opening or cancelling never changes the locale. */
final class LanguagePanel extends LinearLayout {
 final RadioGroup choices; final Button apply;
 LanguagePanel(Context context,String current,Consumer<String> selected){this(context,current,true,selected);}
 LanguagePanel(Context context,String current,boolean inGame,Consumer<String> selected){super(context);setOrientation(VERTICAL);setLayoutDirection(getResources().getConfiguration().getLayoutDirection());
  TextView note=new TextView(context);note.setText(inGame?R.string.fc_language_notice:R.string.fc_language_home_note);note.setTextColor(0xFF49695F);note.setTextSize(13);note.setPadding(8,0,8,CanvasSheet.dp(context,12));addView(note);
  choices=new RadioGroup(context);choices.setOrientation(VERTICAL);addView(choices,new LayoutParams(-1,-2));
  String[] names=AppLanguage.NAMES.clone();names[0]=context.getString(R.string.fc_system);
  for(int i=0;i<names.length;i++){RadioButton option=new RadioButton(context);option.setId(android.view.View.generateViewId());option.setTag(AppLanguage.CODES[i]);option.setText(names[i]);option.setTextSize(16);option.setTextColor(0xFF173E42);option.setTypeface(null,Typeface.BOLD);option.setButtonTintList(ColorStateList.valueOf(0xFF29675E));option.setPaddingRelative(CanvasSheet.dp(context,14),CanvasSheet.dp(context,10),CanvasSheet.dp(context,14),CanvasSheet.dp(context,10));option.setMinHeight(CanvasSheet.dp(context,60));StateListDrawable states=new StateListDrawable();states.addState(new int[]{android.R.attr.state_checked},shape(context,0xFFD9EBE2));states.addState(new int[]{},shape(context,0xFFF0F2E9));option.setBackground(states);RadioGroup.LayoutParams row=new RadioGroup.LayoutParams(-1,-2);row.bottomMargin=CanvasSheet.dp(context,7);choices.addView(option,row);option.setChecked(AppLanguage.CODES[i].equals(current));option.jumpDrawablesToCurrentState();}
  apply=new Button(context);apply.setText(R.string.fc_language_apply);apply.setTextSize(16);apply.setAllCaps(false);apply.setTextColor(0xFF173E42);apply.setTypeface(null,Typeface.BOLD);apply.setBackgroundTintList(new ColorStateList(new int[][]{new int[]{android.R.attr.state_enabled},new int[]{}},new int[]{0xFFF4D452,0xFFE5E7DE}));apply.setMinHeight(CanvasSheet.dp(context,52));apply.setEnabled(false);addView(apply,new LayoutParams(-1,-2));
  choices.setOnCheckedChangeListener((group,id)->{RadioButton option=group.findViewById(id);apply.setEnabled(option!=null&&!current.equals(option.getTag()));});
  apply.setOnClickListener(v->{RadioButton option=choices.findViewById(choices.getCheckedRadioButtonId());if(option!=null&&!current.equals(option.getTag()))selected.accept((String)option.getTag());});
 }
 private static GradientDrawable shape(Context context,int color){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(CanvasSheet.dp(context,16));return d;}
}
