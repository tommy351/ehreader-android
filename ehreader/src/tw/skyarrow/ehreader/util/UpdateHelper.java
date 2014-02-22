package tw.skyarrow.ehreader.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

import com.github.zafarkhaja.semver.Version;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.receiver.UpdateAlarmReceiver;

/**
 * Created by SkyArrow on 2014/2/17.
 */
public class UpdateHelper {
    private static final String API_BASE_URL = "https://api.github.com/";
    private static final String GET_RELEASES_URL = API_BASE_URL + "repos/tommy351/ehreader-android/releases";
    private static final String DOWNLOAD_URL = "https://github.com/tommy351/ehreader-android/releases/download/%s/ehreader-release.apk";

    private Context context;
    private String versionCode;
    private Version version;

    public UpdateHelper(Context context) {
        this.context = context;

        try {
            PackageManager pm = context.getPackageManager();
            versionCode = pm.getPackageInfo(context.getPackageName(), 0).versionName;
            version = Version.valueOf(versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String getVersionCode() {
        return versionCode;
    }

    public Version getVersion() {
        return version;
    }

    public Version getLatestVersion() throws IOException, JSONException {
        String str = HttpRequestHelper.getString(GET_RELEASES_URL);
        JSONArray arr = new JSONArray(str);
        JSONObject latest = arr.getJSONObject(0);
        String tagName = latest.getString("tag_name");

        return Version.valueOf(tagName);
    }

    public static String getDownloadUrl(Version version) {
        return getDownloadUrl(version.toString());
    }

    public static String getDownloadUrl(String version) {
        return String.format(DOWNLOAD_URL, version);
    }

    public boolean compare(Version latestVer) {
        return version.lessThan(latestVer);
    }

    public boolean compare(String latestVer) {
        return compare(Version.valueOf(latestVer));
    }

    public void setupAlarm() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        long lastChecked = preferences.getLong(context.getString(R.string.pref_update_checked_at), System.currentTimeMillis());
        boolean isInterrupted = preferences.getBoolean(context.getString(R.string.pref_update_is_interrupted), false);
        int autoCheck = Integer.parseInt(preferences.getString(context.getString(R.string.pref_auto_check_update),
                context.getString(R.string.pref_auto_check_update_default)));

        if (isInterrupted || autoCheck == 0) return;

        PendingIntent pendingIntent = getPendingIntent();
        AlarmManager am = getAlarmService();
        long triggerAt = lastChecked + autoCheck * 60 * 60 * 1000;

        am.set(AlarmManager.RTC, triggerAt, pendingIntent);
    }

    public void cancelAlarm() {
        PendingIntent pendingIntent = getPendingIntent();
        AlarmManager am = getAlarmService();

        am.cancel(pendingIntent);
    }

    private AlarmManager getAlarmService() {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(context, UpdateAlarmReceiver.class);

        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
    }
}
