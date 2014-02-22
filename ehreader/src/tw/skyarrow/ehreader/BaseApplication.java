package tw.skyarrow.ehreader;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import tw.skyarrow.ehreader.api.DataLoader;
import tw.skyarrow.ehreader.util.DownloadHelper;
import tw.skyarrow.ehreader.util.UpdateHelper;

/**
 * Created by SkyArrow on 2014/1/25.
 */
public class BaseApplication extends Application {
    private static boolean loggedIn;
    private static Tracker tracker;

    private SharedPreferences preferences;

    @Override
    public void onCreate() {
        super.onCreate();

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        loggedIn = preferences.getBoolean(getString(R.string.pref_logged_in), false);
        initTracker();
        initImageLoader();
        initDataLoader();
        initDownloadHelper();
        initAutoUpdate();
    }

    @Override
    public void onLowMemory() {
        ImageLoader.getInstance().clearMemoryCache();
    }

    public static boolean isLoggedIn() {
        return loggedIn;
    }

    public static void setLoggedIn(boolean isLoggedIn) {
        loggedIn = isLoggedIn;
        DataLoader.getInstance().setLoggedIn(isLoggedIn);
    }

    private void initTracker() {
        GoogleAnalytics ga = GoogleAnalytics.getInstance(this);

        ga.setDryRun(BuildConfig.DEBUG);
        tracker = ga.getTracker(getString(R.string.ga_trackingId));
    }

    public static Tracker getTracker() {
        return tracker;
    }

    private void initImageLoader() {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .build();

        ImageLoader.getInstance().init(config);
    }

    private void initDataLoader() {
        DataLoader.getInstance().init(this);
    }

    private void initDownloadHelper() {
        DownloadHelper.getInstance().init(this);
    }

    private void initAutoUpdate() {
        boolean firstInstalled = preferences.getBoolean(getString(R.string.pref_first_installed), true);

        if (firstInstalled) {
            SharedPreferences.Editor editor = preferences.edit();
            UpdateHelper updateHelper = new UpdateHelper(this);

            updateHelper.setupAlarm();
            editor.putBoolean(getString(R.string.pref_first_installed), false);
            editor.commit();
        }
    }
}
