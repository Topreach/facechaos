package com.facechaos.mobile;

import android.app.Instrumentation;
import android.content.Context;
import android.graphics.*;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import java.io.File;
import java.io.FileOutputStream;

final class CanvasPanelsCheck {
 static void require(boolean ok,String message){if(!ok)throw new AssertionError(message);}
 static String run(Instrumentation test){
  test.runOnMainSync(()->{
   String[] languages={"en","ar","fr","es","pt"};int[][] sizes={{1080,1380},{900,1000},{1600,1500},{900,1280}};
   for(String language:languages)for(int size=0;size<sizes.length;size++){
    android.content.res.Configuration config=new android.content.res.Configuration(test.getTargetContext().getResources().getConfiguration());config.setLocale(java.util.Locale.forLanguageTag(language));if(size==3)config.fontScale=1.5f;Context context=test.getTargetContext().createConfigurationContext(config);
    CameraLook[] selected={null};View look=CameraLookDialog.create(context,new CameraLook(CameraLook.NATURAL,.65f,0),true,value->selected[0]=value,value->{});CanvasSheet.Panel lookPanel=new CanvasSheet.Panel(context,true,look,()->{});render(test,lookPanel,language,"look",size,sizes[size]);
    require(clickText(look,context.getString(R.string.fc_look_smooth))&&selected[0].mode==CameraLook.SMOOTH,"Canvas mode card not connected");checkRadioLabels(look);
    int[] chosen={-1},count={0};SharePanel share=new SharePanel(context,index->{chosen[0]=index;count[0]++;});CanvasSheet.Panel sharePanel=new CanvasSheet.Panel(context,false,share,()->{});render(test,sharePanel,language,"share",size,sizes[size]);String[] names={"TikTok","Instagram","YouTube","WhatsApp",context.getString(R.string.fc_other_apps)};
    for(int i=0;i<names.length;i++){require(clickDescription(share,names[i])&&chosen[0]==i,"Wrong sharing callback");}require(count[0]==5,"Share destination callback count");
   }
  });
  return "PASS: 40 Canvas Look/Share layouts across five languages, compact/tablet/large-text sizes; mode cards and all five sharing callbacks connected; sticky footers fit; no social app opened";
 }
 private static void render(Instrumentation test,CanvasSheet.Panel panel,String language,String type,int size,int[] dimensions){
  int width=dimensions[0],height=dimensions[1];for(int pass=0;pass<2;pass++){panel.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(height,View.MeasureSpec.EXACTLY));panel.layout(0,0,width,height);}
  require(panel.done.getHeight()>=CanvasSheet.dp(panel.getContext(),48)&&panel.done.getBottom()<=height,"Footer clipped");require(panel.scroll.getHeight()>CanvasSheet.dp(panel.getContext(),48),"No usable scrolling viewport");require(panel.getLayoutDirection()==panel.getResources().getConfiguration().getLayoutDirection(),"Incorrect panel direction");
  Bitmap frame=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);panel.draw(new Canvas(frame));try{File folder=test.getTargetContext().getExternalFilesDir(null);if(folder!=null&&size==0)try(FileOutputStream output=new FileOutputStream(new File(folder,"panel-"+type+"-"+language+".png"))){frame.compress(Bitmap.CompressFormat.PNG,100,output);}}catch(Exception error){throw new RuntimeException(error);}finally{frame.recycle();}
 }
 private static void checkRadioLabels(View view){if(view instanceof RadioButton){RadioButton button=(RadioButton)view;require(button.getLayout()!=null&&button.getLayout().getHeight()<=button.getHeight()-button.getTotalPaddingTop()-button.getTotalPaddingBottom()+2,"Mode label clipped");}if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++)checkRadioLabels(((ViewGroup)view).getChildAt(i));}
 private static boolean clickText(View view,String text){if(view instanceof RadioButton&&((RadioButton)view).getText().toString().equals(text)){view.performClick();return true;}if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++)if(clickText(((ViewGroup)view).getChildAt(i),text))return true;return false;}
 private static boolean clickDescription(View view,String text){if(text.contentEquals(view.getContentDescription()==null?"":view.getContentDescription())&&view.isClickable()){view.performClick();return true;}if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++)if(clickDescription(((ViewGroup)view).getChildAt(i),text))return true;return false;}
}
