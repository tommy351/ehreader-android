package tw.skyarrow.ehreader.util;

import android.content.Context;
import android.os.Environment;
import android.support.v4.content.ContextCompat;

import java.io.File;

public class StorageHelper {
    public static boolean isExternalStorageReadable(){
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
               Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    public static boolean isExternalStorageWritable(){
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public static File getExternalDownloadDir(Context context){
        File[] dirs = ContextCompat.getExternalFilesDirs(context, Environment.DIRECTORY_DOWNLOADS);
        return dirs[0];
    }

    public static File getInternalDownloadDir(Context context){
        File dir = context.getFilesDir();
        if (dir == null) return null;

        return new File(dir, Environment.DIRECTORY_DOWNLOADS);
    }

    public static File getDownloadDir(Context context){
        if (isExternalStorageWritable()){
            return getExternalDownloadDir(context);
        } else {
            return getInternalDownloadDir(context);
        }
    }

    public static File getExternalCacheDir(Context context, String type){
        File dir = context.getExternalCacheDir();
        if (dir == null) return null;

        return new File(dir, type);
    }

    public static File getInternalCacheDir(Context context, String type){
        File dir = context.getCacheDir();
        if (dir == null) return null;

        return new File(dir, type);
    }

    public static File getCacheDir(Context context, String type){
        if (isExternalStorageWritable()){
            return getExternalCacheDir(context, type);
        } else {
            return getInternalCacheDir(context, type);
        }
    }
}
