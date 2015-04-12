package tw.skyarrow.ehreader.network;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import tw.skyarrow.ehreader.util.LoginHelper;

public abstract class EHRequest<T> extends Request<T> {
    protected static final String CHARSET = "UTF-8";

    private Context mContext;
    private Response.Listener<T> mListener;
    private LoginHelper mLoginHelper;

    public EHRequest(Context context, int method, String url, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        mContext = context;
        mListener = listener;
        mLoginHelper = LoginHelper.getInstance(context);
    }

    public EHRequest(Context context, String url, Response.Listener<T> listener, Response.ErrorListener errorListener){
        this(context, Method.GET, url, listener, errorListener);
    }

    public Context getContext(){
        return mContext;
    }

    public Response.Listener<T> getListener() {
        return mListener;
    }

    protected boolean isLoggedIn(){
        return mLoginHelper.isLoggedIn();
    }

    @Override
    protected void deliverResponse(T t) {
        if (mListener != null){
            mListener.onResponse(t);
        }
    }

    @Override
    protected abstract Response<T> parseNetworkResponse(NetworkResponse response);

    protected String stringifyResponse(NetworkResponse response) throws UnsupportedEncodingException {
        return new String(response.data, HttpHeaderParser.parseCharset(response.headers, CHARSET));
    }

    protected JSONArray parseJSONArray(NetworkResponse response) throws UnsupportedEncodingException, JSONException {
        String str = stringifyResponse(response);
        return new JSONArray(str);
    }

    protected JSONObject parseJSONObject(NetworkResponse response) throws UnsupportedEncodingException, JSONException {
        String str = stringifyResponse(response);
        return new JSONObject(str);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>();
        headers.put("Cookie", mLoginHelper.getCookieString());
        return headers;
    }
}
