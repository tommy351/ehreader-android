package tw.skyarrow.ehreader.util;

import android.content.Context;
import android.os.AsyncTask;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import tw.skyarrow.ehreader.Constant;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.event.LoginEvent;

public class LoginHelper {
    public static final String TAG = LoginHelper.class.getSimpleName();

    private static final String IPB_MEMBER_ID = "ipb_member_id";
    private static final String IPB_PASS_HASH = "ipb_pass_hash";
    private static final String IPB_SESSION_ID = "ipb_session_id";

    private static final String FIELD_USERNAME = "UserName";
    private static final String FIELD_PASSWORD = "PassWord";
    private static final String FIELD_COOKIE_DATE = "CookieDate";

    private static final String DEFAULT_ENCODING = "UTF-8";

    private static LoginHelper mInstance;

    private final Context mContext;
    private boolean mLoggedIn;
    private String mMemberID;
    private String mPassHash;
    private String mSessionID;
    private String mUsername;
    private String mAvatarSrc;
    private final PrefHelper prefHelper;

    private LoginHelper(Context context){
        mContext = context;
        prefHelper = PrefHelper.newInstance(context);

        mLoggedIn = prefHelper.getBoolean(R.string.pref_logged_in);
        mMemberID = prefHelper.getString(R.string.pref_login_memberid);
        mPassHash = prefHelper.getString(R.string.pref_login_passhash);
        mSessionID = prefHelper.getString(R.string.pref_login_sessionid);
        mUsername = prefHelper.getString(R.string.pref_login_username);
        mAvatarSrc = prefHelper.getString(R.string.pref_login_avatar);
    }

    public static synchronized LoginHelper getInstance(Context context){
        if (mInstance == null){
            mInstance = new LoginHelper(context.getApplicationContext());
        }

        return mInstance;
    }

    public void login(final String username, String password, Listener listener, ErrorListener errorListener){
        L.d("Logging in: %s", username);
        new LoginTask(username, password, listener, errorListener).execute();
    }

    public void logout(){
        prefHelper.edit()
                .putBoolean(R.string.pref_logged_in, false)
                .remove(R.string.pref_login_memberid)
                .remove(R.string.pref_login_passhash)
                .remove(R.string.pref_login_sessionid)
                .remove(R.string.pref_login_username)
                .remove(R.string.pref_login_avatar)
                .apply();

        mLoggedIn = false;
        mMemberID = "";
        mPassHash = "";
        mSessionID = "";
        mUsername = "";
        mAvatarSrc = "";

        EventBus.getDefault().post(new LoginEvent(LoginEvent.EVENT_LOGOUT));
    }

    public boolean isLoggedIn(){
        return mLoggedIn;
    }

    public String getCookieString(){
        if (!isLoggedIn()) return "";

        return IPB_MEMBER_ID + "=" + mMemberID + ";" +
                IPB_PASS_HASH + "=" + mPassHash + ";" +
                IPB_SESSION_ID + "=" + mSessionID + ";" +
                "Path=/;" +
                "Domain=exhentai.org;";
    }

    public String getMemberID(){
        return mMemberID;
    }

    public void setMemberID(String memberID){
        mMemberID = memberID;
    }

    public String getUsername(){
        return mUsername;
    }

    public void setUsername(String username){
        mUsername = username;
    }

    public String getAvatar() {
        return mAvatarSrc;
    }

    public void setAvatar(String avatar) {
        mAvatarSrc = avatar;

        prefHelper.edit()
                .putString(R.string.pref_login_avatar, mAvatarSrc)
                .apply();
    }

    public interface Listener {
        void onSuccess();
    }

    public interface ErrorListener {
        void onError(Throwable e);
    }

    private class LoginTask extends AsyncTask<String, Integer, Integer> {
        private String username;
        private String password;
        private Listener listener;
        private ErrorListener errorListener;
        private List<HttpCookie> cookieList;

        public LoginTask(String username, String password, Listener listener, ErrorListener errorListener){
            this.username = username;
            this.password = password;
            this.listener = listener;
            this.errorListener = errorListener;
        }

        @Override
        protected Integer doInBackground(String... strings) {
            try {
                URL url = new URL(Constant.LOGIN_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                Map<String, String> params = new HashMap<>();
                cookieList = new ArrayList<>();

                params.put(FIELD_USERNAME, username);
                params.put(FIELD_PASSWORD, password);
                params.put(FIELD_COOKIE_DATE, "1");

                byte[] body = encodeParameters(params);

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("Charset", "UTF-8");
                conn.setRequestProperty("Content-Length", Integer.toString(body.length));
                conn.setUseCaches(false);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setInstanceFollowRedirects(true);

                DataOutputStream out = new DataOutputStream(conn.getOutputStream());
                out.write(body);
                out.close();

                for (Map.Entry<String, List<String>> header : conn.getHeaderFields().entrySet()){
                    if ("Set-Cookie".equals(header.getKey())){
                        for (String str : header.getValue()){
                            cookieList.addAll(HttpCookie.parse(str));
                        }
                    }
                }

                return 1;
            } catch (IOException e){
                L.e(e);
            }

            return 0;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            String memberId = "";
            String passHash = "";
            String sessionId = "";

            for (HttpCookie cookie : cookieList){
                L.d("Cookie {name: %s, value: %s}", cookie.getName(), cookie.getValue());

                if (IPB_MEMBER_ID.equals(cookie.getName())){
                    memberId = cookie.getValue();
                } else if (IPB_PASS_HASH.equals(cookie.getName())){
                    passHash = cookie.getValue();
                } else if (IPB_SESSION_ID.equals(cookie.getName())){
                    sessionId = cookie.getValue();
                }
            }

            if (memberId.isEmpty() || passHash.isEmpty() || sessionId.isEmpty()){
                handleError(null);
                return;
            }

            mLoggedIn = true;
            mMemberID = memberId;
            mPassHash = passHash;
            mSessionID = sessionId;
            mUsername = username;

            prefHelper.edit()
                    .putBoolean(R.string.pref_logged_in, true)
                    .putString(R.string.pref_login_memberid, memberId)
                    .putString(R.string.pref_login_passhash, passHash)
                    .putString(R.string.pref_login_sessionid, sessionId)
                    .putString(R.string.pref_login_username, username)
                    .apply();

            listener.onSuccess();
            EventBus.getDefault().post(new LoginEvent(LoginEvent.EVENT_SUCCESS));
        }

        private void handleError(Throwable e){
            errorListener.onError(e);
            EventBus.getDefault().post(new LoginEvent(LoginEvent.EVENT_FAILED));
        }

        private byte[] encodeParameters(Map<String, String> params) throws UnsupportedEncodingException{
            String str = "";

            for (Map.Entry<String, String> entry : params.entrySet()){
                str += URLEncoder.encode(entry.getKey(), DEFAULT_ENCODING) + "=" +
                        URLEncoder.encode(entry.getValue(), DEFAULT_ENCODING) + "&";
            }

            return str.getBytes(DEFAULT_ENCODING);
        }
    }
}
