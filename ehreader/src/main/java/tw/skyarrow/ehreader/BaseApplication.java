package tw.skyarrow.ehreader;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.androidquery.callback.BitmapAjaxCallback;

/**
 * Created by SkyArrow on 2014/1/25.
 */
public class BaseApplication extends Application {
    private boolean loggedIn;

    @Override
    public void onCreate() {
        super.onCreate();

        Context context = getBaseContext();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        loggedIn = preferences.getBoolean(getString(R.string.pref_logged_in), false);
    }

    @Override
    public void onLowMemory() {
        BitmapAjaxCallback.clearCache();
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }
}
