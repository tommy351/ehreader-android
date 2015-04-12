package tw.skyarrow.ehreader.util;

import android.content.Context;
import android.content.pm.PackageManager;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.github.zafarkhaja.semver.Version;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UpdateHelper {
    public static final String TAG = UpdateHelper.class.toString();

    private static final String API_BASE_URL = "https://api.github.com/";
    private static final String GET_REPO_URL = API_BASE_URL + "repos/tommy351/ehreader-android/";
    private static final String GET_TAGS_URL = GET_REPO_URL + "tags";
    private static final String GET_RELEASES_URL = API_BASE_URL + "releases";
    private static final String DOWNLOAD_URL = "https://github.com/tommy351/ehreader-android/releases/download/%s/ehreader-release.apk";

    private Context mContext;
    private String mVersionCode;
    private Version mVersion;

    public UpdateHelper(Context context){
        mContext = context;

        try {
            PackageManager pm = context.getPackageManager();
            mVersionCode = pm.getPackageInfo(context.getPackageName(), 0).versionName;
            mVersion = Version.valueOf(mVersionCode);
        } catch (PackageManager.NameNotFoundException e){
            L.e(e);
        }
    }

    public String getVersionCode() {
        return mVersionCode;
    }

    public Version getVersion() {
        return mVersion;
    }

    public void checkUpdate(final Listener listener, final ErrorListener errorListener){
        JsonArrayRequest req = new JsonArrayRequest(GET_TAGS_URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {
                //
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {
                errorListener.onError(e);
            }
        });

        req.setShouldCache(false);
        RequestHelper.getInstance(mContext).addToRequestQueue(req, TAG);
    }

    private Version getLatestStableVersion(JSONArray arr){
        try {
            List<Version> versionList = new ArrayList<>();

            for (int i = 0, len = arr.length(); i < len; i++){
                JSONObject data = arr.getJSONObject(i);
                Version version = Version.valueOf(data.getString("name"));
                versionList.add(version);
            }

            Collections.sort(versionList, new VersionComparator());

        } catch (JSONException e){
            L.e(e);
        }

        return null;
    }

    public interface Listener {
        void onSuccess(Version version);
    }

    public interface ErrorListener {
        void onError(Throwable e);
    }
}
