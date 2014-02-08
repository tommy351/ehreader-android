package tw.skyarrow.ehreader;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.androidquery.callback.BitmapAjaxCallback;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Logger;
import com.google.analytics.tracking.android.Tracker;

/**
 * Created by SkyArrow on 2014/1/25.
 */
public class BaseApplication extends Application {
    private static boolean loggedIn;
    private static Tracker tracker;

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        loggedIn = preferences.getBoolean(getString(R.string.pref_logged_in), false);
        initTracker();
    }

    @Override
    public void onLowMemory() {
        BitmapAjaxCallback.clearCache();
    }

    public static boolean isLoggedIn() {
        return loggedIn;
    }

    public static void setLoggedIn(boolean isLoggedIn) {
        loggedIn = isLoggedIn;
    }

    private void initTracker() {
        GoogleAnalytics ga = GoogleAnalytics.getInstance(this);

        ga.setDryRun(BuildConfig.DEBUG);
        tracker = ga.getTracker(getString(R.string.ga_trackingId));

        if (BuildConfig.DEBUG) {
            ga.getLogger().setLogLevel(Logger.LogLevel.INFO);
        }
    }

    public static Tracker getTracker() {
        return tracker;
    }
}
