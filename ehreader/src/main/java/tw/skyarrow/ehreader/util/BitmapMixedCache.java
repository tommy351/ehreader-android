package tw.skyarrow.ehreader.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.StatFs;

import com.android.volley.toolbox.ImageLoader;
import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BitmapMixedCache implements ImageLoader.ImageCache {
    public static final String CACHE_DIR = "images";
    public static final Bitmap.CompressFormat COMPRESS_FORMAT = Bitmap.CompressFormat.JPEG;
    public static final int COMPRESS_QUALITY = 70;
    public static final int MEMORY_CACHE_RESTRICT = 300;

    private Context mContext;
    private BitmapLruCache mMemoryCache;
    private DiskLruCache mDiskCache;

    public BitmapMixedCache(Context context){
        mContext = context;
        mMemoryCache = new BitmapLruCache(getMemoryCacheSize());
    }

    @Override
    public Bitmap getBitmap(String url) {
        Bitmap bitmap = getBitmapFromMemoryCache(url);

        if (bitmap == null){
            bitmap = getBitmapFromDiskCache(url);
        }

        return bitmap;
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        putBitmapToMemoryCache(url, bitmap);
        putBitmapToDiskCache(url, bitmap);
    }

    private File getCacheDir(){
        File cacheDir = StorageHelper.getExternalCacheDir(mContext, CACHE_DIR);

        if (cacheDir == null || !cacheDir.canWrite()){
            cacheDir = StorageHelper.getInternalCacheDir(mContext, CACHE_DIR);
        }

        return cacheDir;
    }

    private int getAppVersion(){
        try {
            PackageInfo info = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            L.e(e);
        }
        return 1;
    }

    private DiskLruCache getDiskCache(){
        if (mDiskCache != null) return mDiskCache;

        try {
            File cacheDir = getCacheDir();
            int appVersion = getAppVersion();

            if (!cacheDir.exists()){
                cacheDir.mkdirs();
            }

            mDiskCache = DiskLruCache.open(cacheDir, appVersion, 1, getDiskCacheSize());
        } catch (IOException e){
            L.e(e);
        }

        return mDiskCache;
    }

    private int getDiskCacheSize(){
        File cacheDir = getCacheDir();
        StatFs statFs = new StatFs(cacheDir.getAbsolutePath());
        int totalSize = getDiskTotalSize(statFs) / 1024;

        return totalSize / 10;
    }

    @SuppressWarnings("deprecation")
    private int getDiskTotalSize(StatFs statFs){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2){
            return (int) statFs.getTotalBytes();
        } else {
            return statFs.getBlockCount() * statFs.getBlockSize();
        }
    }

    private int getMemoryCacheSize(){
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        return maxMemory / 8;
    }

    private Bitmap getBitmapFromMemoryCache(String url){
        return mMemoryCache.get(url);
    }

    private void putBitmapToMemoryCache(String url, Bitmap bitmap){
        if (shouldPutToMemoryCache(bitmap)){
            mMemoryCache.put(url, bitmap);
        }
    }

    private Bitmap getBitmapFromDiskCache(String url) {
        String key = getHashKey(url);
        Bitmap bitmap = null;

        try {
            DiskLruCache.Snapshot snapshot = getDiskCache().get(key);

            if (snapshot != null){
                FileInputStream input = (FileInputStream) snapshot.getInputStream(0);
                FileDescriptor fd = input.getFD();

                if (fd != null){
                    bitmap = BitmapFactory.decodeFileDescriptor(fd);
                }

                if (bitmap != null){
                    putBitmapToMemoryCache(url, bitmap);
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }

        return bitmap;
    }

    private void putBitmapToDiskCache(String url, Bitmap bitmap){
        String key = getHashKey(url);

        try {
            DiskLruCache.Editor editor = getDiskCache().edit(key);

            if (editor != null){
                OutputStream output = editor.newOutputStream(0);

                if (bitmap.compress(COMPRESS_FORMAT, COMPRESS_QUALITY, output)){
                    editor.commit();
                } else {
                    editor.abort();
                }

                output.close();
            }
        } catch (IOException e){
            L.e(e);
        }
    }

    private String getHashKey(String url){
        return String.valueOf(url.hashCode());
    }

    private boolean shouldPutToMemoryCache(Bitmap bitmap){
        return bitmap.getWidth() < MEMORY_CACHE_RESTRICT;
    }
}
