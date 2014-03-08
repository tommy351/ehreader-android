package tw.skyarrow.ehreader.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.event.LoginEvent;

/**
 * Created by SkyArrow on 2014/3/3.
 */
public class LoginHelper {
    private static final String LOGIN_URL = "https://forums.e-hentai.org/index.php?act=Login&CODE=01";
    private static final String FIELD_USERNAME = "UserName";
    private static final String FIELD_PASSWORD = "PassWord";
    private static final String FIELD_COOKIE_DATE = "CookieDate";
    private static final String IPB_MEMBER_ID = "ipb_member_id";
    private static final String IPB_PASS_HASH = "ipb_pass_hash";
    private static final String IPB_SESSION_ID = "ipb_session_id";

    private static LoginHelper instance;

    private Context context;
    private SharedPreferences preferences;
    private String PREF_LOGGED_IN;
    private String PREF_LOGIN_MEMBERID;
    private String PREF_LOGIN_PASSHASH;
    private String PREF_LOGIN_SESSIONID;

    private LoginHelper(Context context) {
        this.context = context;

        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        PREF_LOGGED_IN = context.getString(R.string.pref_logged_in);
        PREF_LOGIN_MEMBERID = context.getString(R.string.pref_login_memberid);
        PREF_LOGIN_PASSHASH = context.getString(R.string.pref_login_passhash);
        PREF_LOGIN_SESSIONID = context.getString(R.string.pref_login_sessionid);
    }

    public static LoginHelper getInstance(Context context) {
        if (instance == null) {
            instance = new LoginHelper(context.getApplicationContext());
        }

        return instance;
    }

    public boolean isLoggedIn() {
        return preferences.getBoolean(PREF_LOGGED_IN, false);
    }

    public boolean login(String username, String password) throws IOException {
        HttpClient client = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(LOGIN_URL);
        HttpContext httpContext = new BasicHttpContext();
        CookieStore cookieStore = new BasicCookieStore();

        List<NameValuePair> params = new ArrayList<NameValuePair>();

        params.add(new BasicNameValuePair(FIELD_USERNAME, username));
        params.add(new BasicNameValuePair(FIELD_PASSWORD, password));
        params.add(new BasicNameValuePair(FIELD_COOKIE_DATE, "1"));

        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        HttpResponse response = client.execute(httpPost, httpContext);
        List<Cookie> cookies = cookieStore.getCookies();
        String memberId = "";
        String passHash = "";
        String sessionId = "";

        for (Cookie cookie : cookies) {
            L.d("Cookie: {name: %s, value: %s}", cookie.getName(), cookie.getValue());

            if (cookie.getName().equals(IPB_MEMBER_ID)) {
                memberId = cookie.getValue();
            } else if (cookie.getName().equals(IPB_PASS_HASH)) {
                passHash = cookie.getValue();
            } else if (cookie.getName().equals(IPB_SESSION_ID)) {
                sessionId = cookie.getValue();
            }
        }

        if (memberId.isEmpty() || passHash.isEmpty() || sessionId.isEmpty()) {
            return false;
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean(PREF_LOGGED_IN, true);
        editor.putString(PREF_LOGIN_MEMBERID, memberId);
        editor.putString(PREF_LOGIN_PASSHASH, passHash);
        editor.putString(PREF_LOGIN_SESSIONID, sessionId);
        editor.commit();

        EventBus.getDefault().post(new LoginEvent(LoginEvent.LOGIN));
        return true;
    }

    public void logout() {
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean(PREF_LOGGED_IN, false);
        editor.remove(PREF_LOGIN_MEMBERID);
        editor.remove(PREF_LOGIN_PASSHASH);
        editor.commit();

        EventBus.getDefault().post(new LoginEvent(LoginEvent.LOGOUT));
    }
}
