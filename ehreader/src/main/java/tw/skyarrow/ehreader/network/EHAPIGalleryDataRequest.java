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
import java.util.ArrayList;
import java.util.List;

import tw.skyarrow.ehreader.model.Gallery;
import tw.skyarrow.ehreader.util.L;

public class EHAPIGalleryDataRequest extends EHAPIRequest<List<Gallery>> {
    private JSONArray mGidList;

    public EHAPIGalleryDataRequest(Context context, JSONArray gidlist, Response.Listener<List<Gallery>> listener, Response.ErrorListener errorListener) {
        super(context, listener, errorListener);
        mGidList = gidlist;
    }

    @Override
    protected Response<List<Gallery>> parseNetworkResponse(NetworkResponse response) {
        try {
            JSONObject json = parseJSONObject(response);
            JSONArray list = json.getJSONArray("gmetadata");
            List<Gallery> result = new ArrayList<>();

            for (int i = 0, len = list.length(); i < len; i++){
                JSONObject data = list.getJSONObject(i);
                if (data.has("error")) continue;

                long id = data.getLong("gid");
                Gallery gallery = new Gallery(id);

                gallery.setStarred(false);
                gallery.setProgress(0);
                gallery.fromJSON(data);

                result.add(gallery);
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
            obj.put("method", "gdata");
            obj.put("gidlist", mGidList);

            return obj.toString().getBytes(CHARSET);
        } catch (JSONException|UnsupportedEncodingException e){
            L.e(e);
        }

        return null;
    }
}
