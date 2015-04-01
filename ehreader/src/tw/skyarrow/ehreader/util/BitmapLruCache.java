package tw.skyarrow.ehreader.util;

import android.graphics.Bitmap;
import android.util.LruCache;

public class BitmapLruCache extends LruCache<String, Bitmap> {
    public BitmapLruCache(int maxSize) {
        super(maxSize);
    }

    @Override
    protected int sizeOf(String key, Bitmap bitmap) {
        return bitmap.getRowBytes() * bitmap.getHeight();
    }
}
