package tw.skyarrow.ehreader.network;

import android.content.Context;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.UnsupportedEncodingException;

public class EHStringRequest extends EHRequest<String> {
    public EHStringRequest(Context context, int method, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(context, method, url, listener, errorListener);
    }

    public EHStringRequest(Context context, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(context, url, listener, errorListener);
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        try {
            String result = stringifyResponse(response);
            return Response.success(result, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e){
            return Response.error(new ParseError(e));
        }
    }
}
