package tw.skyarrow.ehreader.util;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import tw.skyarrow.ehreader.Constant;

public class EHAPIRequest extends JsonObjectRequest {
    private Context mContext;

    public EHAPIRequest(int method, String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
    }

    public EHAPIRequest(int method, String url, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    public EHAPIRequest(int method, String url, String requestBody, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, requestBody, listener, errorListener);
    }

    public EHAPIRequest(String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        this(Method.POST, url, jsonRequest, listener, errorListener);
    }

    public EHAPIRequest(String url, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        this(Method.POST, url, listener, errorListener);
    }

    public EHAPIRequest(Context context, JSONObject json, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener){
        this(Method.POST, Constant.API_URL, json, listener, errorListener);
        mContext = context;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> params = new HashMap<>();
        LoginHelper loginHelper = LoginHelper.getInstance(mContext);

        params.put("Accept", "application/json");
        params.put("Content-Type", "application/json");
        params.put("Cookie", loginHelper.getCookieString());

        return params;
    }

    @Override
    public String getUrl() {
        LoginHelper loginHelper = LoginHelper.getInstance(mContext);
        return loginHelper.isLoggedIn() ? Constant.API_URL_EX : Constant.API_URL;
    }
}
