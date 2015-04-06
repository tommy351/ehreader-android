package tw.skyarrow.ehreader.util;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.net.HttpCookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EHLoginRequest extends StringRequest {
    private static final String LOGIN_URL = "https://forums.e-hentai.org/index.php?act=Login&CODE=01";
    private static final String HEADER_SET_COOKIE = "Set-Cookie";
    private static final String FIELD_USERNAME = "UserName";
    private static final String FIELD_PASSWORD = "PassWord";
    private static final String FIELD_COOKIE_DATE = "CookieDate";

    private String mUsername;
    private String mPassword;
    private Map<String, String> mCookies;

    public EHLoginRequest(int method, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    public EHLoginRequest(String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(url, listener, errorListener);
    }

    public EHLoginRequest(String username, String password, Response.Listener<String> listener, Response.ErrorListener errorListener){
        this(Method.POST, LOGIN_URL, listener, errorListener);

        mUsername = username;
        mPassword = password;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String password) {
        mPassword = password;
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        Map<String, String> params = new HashMap<>();

        params.put(FIELD_USERNAME, mUsername);
        params.put(FIELD_PASSWORD, mPassword);
        params.put(FIELD_COOKIE_DATE, "1");

        return params;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>();

        headers.put("Content-Type","application/x-www-form-urlencoded");

        return headers;
    }

    public Map<String, String> getCookies() {
        return mCookies;
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        Map<String, String> headers = response.headers;

        if (headers.containsKey(HEADER_SET_COOKIE)){
            String rawCookie = headers.get(HEADER_SET_COOKIE);
            List<HttpCookie> cookieList = HttpCookie.parse(rawCookie);
            mCookies = new HashMap<>();

            for (HttpCookie cookie : cookieList){
                mCookies.put(cookie.getName(), cookie.getValue());
            }
        }

        return super.parseNetworkResponse(response);
    }
}
