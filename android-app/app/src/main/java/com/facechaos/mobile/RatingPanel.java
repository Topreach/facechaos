package com.facechaos.mobile;
import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.RatingBar;
final class RatingPanel extends LinearLayout {
 final RatingBar stars;
 RatingPanel(Context context){super(context);setOrientation(VERTICAL);setBackgroundColor(0xFFFFFBF0);float d=getResources().getDisplayMetrics().density;setPadding((int)(20*d),(int)(12*d),(int)(20*d),(int)(8*d));
  text("Choose 1 to 5 stars. Your feedback is optional and never affects coins or progress.",15);
  stars=new RatingBar(context);stars.setNumStars(5);stars.setStepSize(1);stars.setRating(0);stars.setProgressTintList(android.content.res.ColorStateList.valueOf(0xFFF1BC40));stars.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFB4C8C2));addView(stars,new LayoutParams((int)(240*d),(int)(64*d)));
  text("Prototype feedback: saved on this phone only. This is not a public Play Store review. Public ratings will be available after the app is released on Google Play.",13);
 }
 @Override protected void onSizeChanged(int w,int h,int oldw,int oldh){super.onSizeChanged(w,h,oldw,oldh);float available=Math.max(1,w-getPaddingLeft()-getPaddingRight()),scale=Math.min(1,available/Math.max(1,stars.getMeasuredWidth()));stars.setPivotX(0);stars.setPivotY(0);stars.setScaleX(scale);stars.setScaleY(scale);}
 private void text(String value,int size){TextView text=new TextView(getContext());text.setText(AppText.t(getContext(),value));text.setTextSize(size);text.setTextColor(0xFF314B50);addView(text,new LayoutParams(-1,-2));}
}
