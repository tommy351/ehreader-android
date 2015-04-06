package tw.skyarrow.ehreader.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.net.HttpCookie;
import java.util.HashMap;
import java.util.Map;

import tw.skyarrow.ehreader.R;

public class LoginHelper {
    public static final String TAG = LoginHelper.class.getSimpleName();

    private static final String IPB_MEMBER_ID = "ipb_member_id";
    private static final String IPB_PASS_HASH = "ipb_pass_hash";
    private static final String IPB_SESSION_ID = "ipb_session_id";

    private static String PREF_LOGGED_IN;
    private static String PREF_LOGIN_MEMBERID;
    private static String PREF_LOGIN_PASSHASH;
    private static String PREF_LOGIN_SESSIONID;

    private static LoginHelper mInstance;

    private Context mContext;
    private boolean mLoggedIn;
    private Map<String, String> mCookies;

    public LoginHelper(Context context){
        mContext = context;
        mCookies = new HashMap<>();
        SharedPreferences preferences = getPreferences();

        if (PREF_LOGGED_IN == null) PREF_LOGGED_IN = context.getString(R.string.pref_logged_in);
        if (PREF_LOGIN_MEMBERID == null) PREF_LOGIN_MEMBERID = context.getString(R.string.pref_login_memberid);
        if (PREF_LOGIN_PASSHASH == null) PREF_LOGIN_PASSHASH = context.getString(R.string.pref_login_passhash);
        if (PREF_LOGIN_SESSIONID == null) PREF_LOGIN_SESSIONID = context.getString(R.string.pref_login_sessionid);

        mLoggedIn = preferences.getBoolean(PREF_LOGGED_IN, false);
        mCookies.put(IPB_MEMBER_ID, preferences.getString(PREF_LOGIN_MEMBERID, ""));
        mCookies.put(IPB_PASS_HASH, preferences.getString(PREF_LOGIN_PASSHASH, ""));
        mCookies.put(IPB_SESSION_ID, preferences.getString(PREF_LOGIN_SESSIONID, ""));
    }

    public static synchronized LoginHelper getInstance(Context context){
        if (mInstance == null){
            mInstance = new LoginHelper(context.getApplicationContext());
        }

        return mInstance;
    }

    public void login(String username, String password, final Listener listener){
        EHLoginRequest req = new EHLoginRequest(username, password, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                if (getCookies() != null){
                    return;
                }

                Map<String, String> cookies = getCookies();
                String memberId = cookies.get(IPB_MEMBER_ID);
                String passHash = cookies.get(IPB_PASS_HASH);
                String sessionId = cookies.get(IPB_SESSION_ID);

                if (memberId == null || passHash == null || sessionId == null){
                    return;
                }

                SharedPreferences.Editor editor = getPreferences().edit();

                editor.putBoolean(PREF_LOGGED_IN, true);
                editor.putString(PREF_LOGIN_MEMBERID, memberId);
                editor.putString(PREF_LOGIN_PASSHASH, passHash);
                editor.putString(PREF_LOGIN_SESSIONID, sessionId);
                editor.apply();

                mCookies = cookies;

                listener.onSuccess();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                listener.onError(volleyError);
            }
        });

        RequestHelper.getInstance(mContext).addToRequestQueue(req, TAG);
    }

    public void logout(){
        SharedPreferences.Editor editor = getPreferences().edit();

        editor.putBoolean(PREF_LOGGED_IN, false);
        editor.remove(PREF_LOGIN_MEMBERID);
        editor.remove(PREF_LOGIN_PASSHASH);
        editor.remove(PREF_LOGIN_SESSIONID);

        editor.apply();
    }

    public boolean isLoggedIn(){
        return mLoggedIn;
    }

    public Map<String, String> getCookies(){
        return mCookies;
    }

    public String getCookieString(){
        StringBuilder builder = new StringBuilder();

        if (mCookies != null){
            for (Map.Entry<String, String> entry : mCookies.entrySet()){
                HttpCookie cookie = new HttpCookie(entry.getKey(), entry.getValue());

                cookie.setPath("/");
                cookie.setDomain("exhentai.org");

                builder.append(cookie.toString()).append(";");
            }
        }

        return builder.toString();
    }

    private SharedPreferences getPreferences(){
        return PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public interface Listener {
        void onSuccess();
        void onError(Exception e);
    }
}
