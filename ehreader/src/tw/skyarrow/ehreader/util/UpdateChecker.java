package tw.skyarrow.ehreader.util;

import android.content.Context;
import android.content.pm.PackageManager;

import com.github.zafarkhaja.semver.Version;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by SkyArrow on 2014/2/17.
 */
public class UpdateChecker {
    private static final String API_BASE_URL = "https://api.github.com/";
    private static final String GET_RELEASES_URL = API_BASE_URL + "repos/tommy351/ehreader-android/releases";
    private static final String DOWNLOAD_URL = "https://github.com/tommy351/ehreader-android/releases/download/%s/ehreader-release.apk";

    private Context context;
    private String versionCode;
    private Version version;

    public UpdateChecker(Context context) {
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
}
