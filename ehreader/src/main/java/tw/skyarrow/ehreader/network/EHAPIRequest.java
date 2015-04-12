package tw.skyarrow.ehreader.network;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;

import java.util.Map;

import tw.skyarrow.ehreader.Constant;

public abstract class EHAPIRequest<T> extends EHRequest<T> {
    protected static final String CONTENT_TYPE = "application/json; charset=" + CHARSET;

    public EHAPIRequest(Context context, Response.Listener<T> listener, Response.ErrorListener errorListener){
        super(context, Method.POST, Constant.API_URL, listener, errorListener);
    }

    @Override
    protected abstract Response<T> parseNetworkResponse(NetworkResponse response);

    @Override
    public String getUrl() {
        return isLoggedIn() ? Constant.API_URL_EX : Constant.API_URL;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = super.getHeaders();

        headers.put("Accept", "application/json");
        headers.put("Content-Type", CONTENT_TYPE);

        return headers;
    }

    @Override
    public String getBodyContentType() {
        return CONTENT_TYPE;
    }
}