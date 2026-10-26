package com.facechaos.mobile;
import android.content.*;
import android.net.Uri;
import java.io.*;
final class SharingRatingCheck {
 static void require(boolean b,String m){if(!b)throw new AssertionError(m);}
 static String run(Context context){
  File folder=context.getExternalFilesDir(android.os.Environment.DIRECTORY_MOVIES);require(folder!=null,"Movie folder available");File test=new File(folder,"facechaos_99000123456789.mp4");require(!test.exists(),"Never overwrite an existing recording");Uri uri=Uri.parse("content://com.facechaos.mobile.clips/"+test.getName());
  try{
   try(FileOutputStream out=new FileOutputStream(test)){out.write(new byte[]{1,2,3,4});}
   for(String target:new String[]{null,"com.zhiliaoapp.musically","com.ss.android.ugc.trill","com.instagram.android","com.google.android.youtube","com.whatsapp"}){
    Intent share=ClipSharing.video(test,target);require(Intent.ACTION_SEND.equals(share.getAction())&&"video/mp4".equals(share.getType()),"Video intent type");require(uri.equals(share.getParcelableExtra(Intent.EXTRA_STREAM)),"Only selected clip is attached");require((share.getFlags()&Intent.FLAG_GRANT_READ_URI_PERMISSION)!=0&&share.getClipData().getItemAt(0).getUri().equals(uri),"Read grant and ClipData match");require(target==null?share.getPackage()==null:target.equals(share.getPackage()),"Correct social target");
   }
   try(InputStream in=context.getContentResolver().openInputStream(uri)){require(in!=null&&in.read()==1,"Shared provider opens selected synthetic clip");}
   boolean denied=false;try{context.getContentResolver().openFileDescriptor(uri,"w");}catch(FileNotFoundException expected){denied=true;}require(denied,"Provider is read-only");
   for(String name:new String[]{"../README.md","other.mp4","facechaos_bad.mp4"})require(!ClipSharing.validName(name),"Unrelated files excluded");
   android.content.SharedPreferences prefs=context.getSharedPreferences("rating_test_only",0);prefs.edit().clear().putInt("stars",1).commit();require(prefs.getInt("stars",0)==1,"One-star feedback accepted");prefs.edit().putInt("stars",5).commit();require(prefs.getInt("stars",0)==5,"Five-star feedback accepted equally");context.deleteSharedPreferences("rating_test_only");
   return "PASS: six share destinations, selected-clip URI grants, read-only provider, invalid filenames and isolated local rating persistence; no social app opened and no user clip accessed";
  }catch(Exception error){throw new RuntimeException(error);}finally{if(test.exists()&&!test.delete())throw new AssertionError("Synthetic clip cleanup failed");}
 }
}
