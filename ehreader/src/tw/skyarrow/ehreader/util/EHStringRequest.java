package tw.skyarrow.ehreader.util;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class EHStringRequest extends StringRequest {
    private Context mContext;

    public EHStringRequest(int method, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    public EHStringRequest(String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(url, listener, errorListener);
    }

    public EHStringRequest(Context context, String url, Response.Listener<String> listener, Response.ErrorListener errorListener){
        this(url, listener, errorListener);
        mContext = context;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>();
        LoginHelper loginHelper = LoginHelper.getInstance(mContext);

        headers.put("Cookie", loginHelper.getCookieString());

        return headers;
    }
}
