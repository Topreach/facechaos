package com.facechaos.mobile;
import android.media.MediaMetadataRetriever;
import java.io.File;
import java.util.Locale;

final class ClipDetails {
 /** Read off the UI thread; damaged/removed clips do not break the library. */
 static long duration(File file){
  MediaMetadataRetriever reader=new MediaMetadataRetriever();
  try{reader.setDataSource(file.getAbsolutePath());String value=reader.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);return value==null?-1:Long.parseLong(value);}
  catch(RuntimeException error){return -1;}
  finally{try{reader.release();}catch(Exception ignored){}}
 }
 static String time(long millis){long seconds=Math.max(0,millis)/1000;return String.format(Locale.US,"%d:%02d",seconds/60,seconds%60);}
}
