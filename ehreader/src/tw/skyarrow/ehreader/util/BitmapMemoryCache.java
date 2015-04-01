package tw.skyarrow.ehreader.util;

import android.graphics.Bitmap;

import com.android.volley.toolbox.ImageLoader;

public class BitmapMemoryCache implements ImageLoader.ImageCache {
    private BitmapLruCache mCache;

    public BitmapMemoryCache(int size){
        mCache = new BitmapLruCache(size);
    }

    @Override
    public Bitmap getBitmap(String key) {
        return mCache.get(key);
    }

    @Override
    public void putBitmap(String key, Bitmap bitmap) {
        mCache.put(key, bitmap);
    }
}
