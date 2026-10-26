package com.facechaos.mobile;

import android.content.*;
import android.database.*;
import android.net.Uri;
import android.os.*;
import android.provider.OpenableColumns;
import java.io.*;

/** Read-only grants for explicitly shared Face Chaos clips, never arbitrary paths. */
public final class ClipProvider extends ContentProvider {
    @Override public boolean onCreate(){return true;}
    private File clip(Uri uri)throws FileNotFoundException{
        String name=uri.getLastPathSegment();
        if(!uri.getPathSegments().isEmpty()&&uri.getPathSegments().size()==1&&name!=null&&name.matches("facechaos_[0-9]+\\.mp4")){
            File folder=getContext().getExternalFilesDir(Environment.DIRECTORY_MOVIES);
            if(folder!=null){File file=new File(folder,name);if(file.isFile())return file;}
        }
        throw new FileNotFoundException("Clip not found");
    }
    @Override public ParcelFileDescriptor openFile(Uri uri,String mode)throws FileNotFoundException{
        if(!"r".equals(mode))throw new FileNotFoundException("Read only");return ParcelFileDescriptor.open(clip(uri),ParcelFileDescriptor.MODE_READ_ONLY);
    }
    @Override public String getType(Uri uri){return "video/mp4";}
    @Override public Cursor query(Uri uri,String[] projection,String selection,String[] args,String order){
        try{File file=clip(uri);String[] columns=projection==null?new String[]{OpenableColumns.DISPLAY_NAME,OpenableColumns.SIZE}:projection;
            MatrixCursor cursor=new MatrixCursor(columns);Object[] row=new Object[columns.length];
            for(int i=0;i<columns.length;i++)row[i]=OpenableColumns.DISPLAY_NAME.equals(columns[i])?file.getName():OpenableColumns.SIZE.equals(columns[i])?file.length():null;
            cursor.addRow(row);return cursor;
        }catch(FileNotFoundException error){return null;}
    }
    @Override public Uri insert(Uri uri,ContentValues values){throw new UnsupportedOperationException("Read only");}
    @Override public int delete(Uri uri,String selection,String[] args){throw new UnsupportedOperationException("Read only");}
    @Override public int update(Uri uri,ContentValues values,String selection,String[] args){throw new UnsupportedOperationException("Read only");}
}
