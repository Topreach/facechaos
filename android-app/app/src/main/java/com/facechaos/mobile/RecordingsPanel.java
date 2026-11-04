package com.facechaos.mobile;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.*;
import android.widget.*;
import java.io.File;
import java.util.function.Consumer;

/** Recycled native rows; metadata is read on one cancellable worker, never on the UI thread. */
final class RecordingsPanel extends ListView {
 private final File[] clips;private final String[] details;private final BaseAdapter rows;private volatile boolean closed;private Thread worker;
 RecordingsPanel(Context context,File[] files,Consumer<File> selected){super(context);clips=files.clone();details=new String[clips.length];setDivider(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));setDividerHeight(CanvasSheet.dp(context,8));setCacheColorHint(Color.TRANSPARENT);setBackgroundColor(Color.TRANSPARENT);setClipToPadding(false);setPadding(0,0,0,CanvasSheet.dp(context,6));setLayoutDirection(getResources().getConfiguration().getLayoutDirection());
  java.text.DateFormat date=java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM,java.text.DateFormat.SHORT,getResources().getConfiguration().getLocales().get(0));
  for(int i=0;i<clips.length;i++)details[i]=date.format(new java.util.Date(clips[i].lastModified()))+"  \u00b7  "+android.text.format.Formatter.formatShortFileSize(context,clips[i].length());
  rows=new BaseAdapter(){public int getCount(){return clips.length;}public Object getItem(int position){return clips[position];}public long getItemId(int position){return position;}public View getView(int position,View recycled,ViewGroup parent){Row row=recycled instanceof Row?(Row)recycled:new Row(context);row.title.setText(context.getString(R.string.fc_library_recording,clips.length-position));row.detail.setText(details[position]);row.setContentDescription(context.getString(R.string.fc_library_open)+". "+row.title.getText()+". "+details[position]);return row;}};setAdapter(rows);setOnItemClickListener((parent,view,position,id)->selected.accept(clips[position]));
 }
 void loadDetails(){if(worker!=null)return;Handler ui=new Handler(Looper.getMainLooper());worker=new Thread(()->{for(int i=0;i<clips.length&&!closed;i++){long duration=ClipDetails.duration(clips[i]);if(closed)return;if(duration>=0){int index=i;ui.post(()->{if(!closed){details[index]=details[index]+"  \u00b7  "+ClipDetails.time(duration);rows.notifyDataSetChanged();}});}}},"FaceChaosLibraryDetails");worker.start();}
 void close(){closed=true;if(worker!=null)worker.interrupt();}
 static View empty(Context context){LinearLayout box=new LinearLayout(context);box.setOrientation(LinearLayout.VERTICAL);box.setGravity(Gravity.CENTER);box.setPadding(CanvasSheet.dp(context,12),CanvasSheet.dp(context,20),CanvasSheet.dp(context,12),CanvasSheet.dp(context,24));box.addView(new PlayArt(context),new LinearLayout.LayoutParams(CanvasSheet.dp(context,88),CanvasSheet.dp(context,88)));TextView title=new TextView(context);title.setText(R.string.fc_library_empty_title);title.setTextSize(20);title.setTypeface(null,Typeface.BOLD);title.setTextColor(0xFF173E42);title.setGravity(Gravity.CENTER);title.setPadding(0,CanvasSheet.dp(context,14),0,CanvasSheet.dp(context,10));box.addView(title);TextView help=new TextView(context);help.setText(R.string.fc_no_clips);help.setTextSize(14);help.setTextColor(0xFF49695F);help.setGravity(Gravity.CENTER);box.addView(help);return box;}
 private static final class Row extends LinearLayout {
  final TextView title,detail;
  Row(Context context){super(context);setOrientation(HORIZONTAL);setGravity(Gravity.CENTER_VERTICAL);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);setPadding(CanvasSheet.dp(context,12),CanvasSheet.dp(context,12),CanvasSheet.dp(context,12),CanvasSheet.dp(context,12));setMinimumHeight(CanvasSheet.dp(context,96));GradientDrawable background=new GradientDrawable();background.setColor(0xFFE4EFE7);background.setCornerRadius(CanvasSheet.dp(context,18));setBackground(background);addView(new PlayArt(context),new LayoutParams(CanvasSheet.dp(context,56),CanvasSheet.dp(context,64)));LinearLayout copy=new LinearLayout(context);copy.setOrientation(VERTICAL);copy.setPaddingRelative(CanvasSheet.dp(context,12),0,0,0);title=new TextView(context);title.setTextSize(16);title.setTextColor(0xFF173E42);title.setTypeface(null,Typeface.BOLD);detail=new TextView(context);detail.setTextSize(12);detail.setTextColor(0xFF49695F);detail.setPadding(0,CanvasSheet.dp(context,5),0,0);copy.addView(title);copy.addView(detail);copy.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);addView(copy,new LayoutParams(0,-2,1));}
 }
 private static final class PlayArt extends View {
  private final Paint p=new Paint(3);PlayArt(Context context){super(context);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
  @Override protected void onDraw(Canvas c){float w=getWidth(),h=getHeight(),s=Math.min(w,h),x=w/2,y=h/2;p.setColor(0xFFB8DBCB);c.drawRoundRect(x-s*.48f,y-s*.43f,x+s*.48f,y+s*.43f,s*.18f,s*.18f,p);p.setColor(0xFF29675E);c.drawCircle(x,y,s*.30f,p);p.setColor(0xFFFFFBF0);Path path=new Path();path.moveTo(x-s*.07f,y-s*.13f);path.lineTo(x+s*.14f,y);path.lineTo(x-s*.07f,y+s*.13f);path.close();c.drawPath(path,p);}
 }
}
