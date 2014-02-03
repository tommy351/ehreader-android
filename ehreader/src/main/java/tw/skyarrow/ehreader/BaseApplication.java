package tw.skyarrow.ehreader;

import android.app.Application;
import android.content.SharedPreferences;

import com.androidquery.callback.BitmapAjaxCallback;

/**
 * Created by SkyArrow on 2014/1/25.
 */
public class BaseApplication extends Application {
    @Override
    public void onLowMemory() {
        BitmapAjaxCallback.clearCache();
    }
}
