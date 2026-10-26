package com.facechaos.mobile;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import java.io.File;
final class ClipSharing {
 static boolean validName(String name){return name!=null&&name.matches("facechaos_[0-9]+\\.mp4");}
 static Intent video(File file,String target){
  if(!validName(file.getName()))throw new IllegalArgumentException("Invalid clip name");
  Uri uri=Uri.parse("content://com.facechaos.mobile.clips/"+file.getName());
  Intent intent=new Intent(Intent.ACTION_SEND).setType("video/mp4").putExtra(Intent.EXTRA_STREAM,uri).putExtra(Intent.EXTRA_TITLE,"My Face Chaos game");
  intent.setClipData(ClipData.newRawUri("Face Chaos video",uri));intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);if(target!=null)intent.setPackage(target);return intent;
 }
}
