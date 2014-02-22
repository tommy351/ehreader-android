package tw.skyarrow.ehreader.app.pref;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;

import com.google.analytics.tracking.android.MapBuilder;

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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import tw.skyarrow.ehreader.BaseApplication;
import tw.skyarrow.ehreader.Constant;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.event.LoginEvent;
import tw.skyarrow.ehreader.util.L;

/**
 * Created by SkyArrow on 2014/2/7.
 */
public class LoginDialog extends DialogFragment {
    public static final String TAG = "LoginDialog";

    public static final String EXTRA_USERNAME = "username";
    public static final String EXTRA_PASSWORD = "password";

    private static final String LOGIN_URL = "https://forums.e-hentai.org/index.php?act=Login&CODE=01";

    private String username;
    private String password;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog dialog = new ProgressDialog(getActivity());
        Bundle args = getArguments();
        username = args.getString(EXTRA_USERNAME);
        password = args.getString(EXTRA_PASSWORD);

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setTitle(R.string.logging_in);
        dialog.setMessage(getString(R.string.logging_in));
        dialog.setIndeterminate(true);

        new LoginTask().execute("");

        return dialog;
    }

    private class LoginTask extends AsyncTask<String, Integer, Boolean> {
        private long startLoadAt;

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                HttpClient client = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(LOGIN_URL);
                HttpContext httpContext = new BasicHttpContext();
                CookieStore cookieStore = new BasicCookieStore();

                List<NameValuePair> params = new ArrayList<NameValuePair>();

                params.add(new BasicNameValuePair("UserName", username));
                params.add(new BasicNameValuePair("PassWord", password));
                params.add(new BasicNameValuePair("CookieDate", "1"));

                httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

                HttpResponse response = client.execute(httpPost, httpContext);
                List<Cookie> cookies = cookieStore.getCookies();
                String memberId = "";
                String passHash = "";
                String sessionId = "";

                for (Cookie cookie : cookies) {
                    L.d("Cookie: {name: %s, value: %s}", cookie.getName(), cookie.getValue());

                    if (cookie.getName().equals(Constant.IPB_MEMBER_ID)) {
                        memberId = cookie.getValue();
                    } else if (cookie.getName().equals(Constant.IPB_PASS_HASH)) {
                        passHash = cookie.getValue();
                    } else if (cookie.getName().equals(Constant.IPB_SESSION_ID)) {
                        sessionId = cookie.getValue();
                    }
                }

                if (memberId.isEmpty() || passHash.isEmpty() || sessionId.isEmpty()) {
                    return false;
                }

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = preferences.edit();

                editor.putBoolean(getString(R.string.pref_logged_in), true);
                editor.putString(getString(R.string.pref_login_memberid), memberId);
                editor.putString(getString(R.string.pref_login_passhash), passHash);
                editor.putString(getString(R.string.pref_login_sessionid), sessionId);
                editor.commit();

                return true;

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPreExecute() {
            startLoadAt = System.currentTimeMillis();
        }

        @Override
        protected void onPostExecute(Boolean isSuccess) {
            if (isSuccess) {
                EventBus.getDefault().post(new LoginEvent(LoginEvent.LOGIN));
            } else {
                DialogFragment dialog = new LoginErrorDialog();

                dialog.show(getActivity().getSupportFragmentManager(), LoginErrorDialog.TAG);
            }

            BaseApplication.getTracker().send(MapBuilder.createTiming(
                    "resources", System.currentTimeMillis() - startLoadAt, "login", null
            ).build());

            BaseApplication.setLoggedIn(isSuccess);
            dismiss();
        }
    }
}
