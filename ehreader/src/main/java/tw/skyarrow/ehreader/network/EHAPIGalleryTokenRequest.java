package tw.skyarrow.ehreader.network;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import tw.skyarrow.ehreader.util.L;

public class EHAPIGalleryTokenRequest extends EHAPIRequest<Map<Long, String>> {
    private JSONArray mPageList;

    public EHAPIGalleryTokenRequest(Context context, JSONArray pagelist, Response.Listener<Map<Long, String>> listener, Response.ErrorListener errorListener) {
        super(context, listener, errorListener);
        mPageList = pagelist;
    }

    @Override
    protected Response<Map<Long, String>> parseNetworkResponse(NetworkResponse response) {
        try {
            JSONObject json = parseJSONObject(response);
            JSONArray list = json.getJSONArray("tokenlist");
            Map<Long, String> result = new HashMap<>();

            for (int i = 0, len = list.length(); i < len; i++){
                JSONObject data = list.getJSONObject(i);
                if (data.has("error") || !data.has("token")) continue;

                result.put(data.getLong("gid"), data.getString("token"));
            }

            return Response.success(result, HttpHeaderParser.parseCacheHeaders(response));
        } catch (JSONException|UnsupportedEncodingException e){
            return Response.error(new ParseError(e));
        }
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        try {
            JSONObject obj = new JSONObject();
            obj.put("method", "gtoken");
            obj.put("pagelist", mPageList);

            return obj.toString().getBytes(CHARSET);
        } catch (JSONException|UnsupportedEncodingException e){
            L.e(e);
        }

        return null;
    }
}
