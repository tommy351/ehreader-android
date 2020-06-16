package tw.skyarrow.ehreader.util;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;

public class ImageLoaderHelper {
    private static ImageLoaderHelper mInstance;
    private ImageLoader mImageLoader;
    private BitmapMixedCache mImageCache;

    private ImageLoaderHelper(Context context){
        RequestQueue queue = RequestHelper.getRequestQueue(context);
        mImageCache = new BitmapMixedCache(context);
        mImageLoader = new ImageLoader(queue, mImageCache);
    }

    public static synchronized ImageLoaderHelper getInstance(Context context){
        if (mInstance == null){
            mInstance = new ImageLoaderHelper(context.getApplicationContext());
        }

        return mInstance;
    }

    public static ImageLoader getImageLoader(Context context){
        return getInstance(context).getImageLoader();
    }

    public ImageLoader getImageLoader(){
        return mImageLoader;
    }

    public BitmapMixedCache getImageCache(){
        return mImageCache;
    }
}
