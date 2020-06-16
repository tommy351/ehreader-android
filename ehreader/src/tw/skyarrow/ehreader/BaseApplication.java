package tw.skyarrow.ehreader;

import android.app.Application;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.google.analytics.tracking.android.ExceptionReporter;
import com.google.analytics.tracking.android.GAServiceManager;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import tw.skyarrow.ehreader.util.DownloadHelper;
import tw.skyarrow.ehreader.util.UpdateHelper;

import static tw.skyarrow.ehreader.Constant.*;

/**
 * Created by SkyArrow on 2014/1/25.
 */
public class BaseApplication extends Application {
    private static Tracker tracker;
    private SharedPreferences preferences;

    @Override
    public void onCreate() {
        super.onCreate();

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        initTracker();
        initImageLoader();
        initAutoUpdate();
        setFolderVisibility();
    }

    @Override
    public void onLowMemory() {
        ImageLoader.getInstance().clearMemoryCache();
    }

    private void initTracker() {
        GoogleAnalytics ga = GoogleAnalytics.getInstance(this);
        tracker = ga.getTracker(getString(R.string.ga_trackingId));
        boolean reportUncaughtExceptions = getResources().getBoolean(R.bool.ga_reportUncaughtExceptions);

        if (reportUncaughtExceptions) {
            Thread.setDefaultUncaughtExceptionHandler(new ExceptionReporter(
                    tracker, GAServiceManager.getInstance(), Thread.getDefaultUncaughtExceptionHandler(), this));
        }

        ga.setDefaultTracker(tracker);
        ga.setDryRun(BuildConfig.DEBUG);
    }

    public static Tracker getTracker() {
        return tracker;
    }

    private void initImageLoader() {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .imageDownloader(new BaseImageDownloader(getApplicationContext()) {
                    protected static final String ALLOWED_URI_CHARS = ";@#&=*+-_.,:!?()/~'%";

                    @Override
                    protected HttpURLConnection createConnection(String url, Object extra) throws IOException {
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                        String memberId = preferences.getString(context.getString(R.string.pref_login_memberid), "");
                        String passhash = preferences.getString(context.getString(R.string.pref_login_passhash), "");
                        String igneous = preferences.getString(context.getString(R.string.pref_login_igneous), "");
                        String encodedUrl = Uri.encode(url, ALLOWED_URI_CHARS);
                        HttpURLConnection conn = (HttpURLConnection) new URL(encodedUrl).openConnection();
                        conn.setConnectTimeout(connectTimeout);
                        conn.setReadTimeout(readTimeout);
                        conn.setRequestProperty("Cookie", String.format("%s=%s; %s=%s; %s=%s;",
                                IPB_MEMBER_ID, memberId,
                                IPB_PASS_HASH, passhash,
                                IGNEOUS, igneous));
                        return conn;
                    }
                })
                .build();

        ImageLoader.getInstance().init(config);
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

    private void setFolderVisibility() {
        try {
            boolean isVisible = preferences.getBoolean(getString(R.string.pref_hide_files),
                    getResources().getBoolean(R.bool.pref_hide_files_default));

            DownloadHelper.setFolderVisibility(!isVisible);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
