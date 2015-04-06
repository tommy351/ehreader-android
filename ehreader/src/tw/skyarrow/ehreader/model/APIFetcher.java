package tw.skyarrow.ehreader.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tw.skyarrow.ehreader.util.DatabaseHelper;
import tw.skyarrow.ehreader.util.EHAPIRequest;
import tw.skyarrow.ehreader.util.L;
import tw.skyarrow.ehreader.util.RequestHelper;

public class APIFetcher {
    public static final String TAG = APIFetcher.class.getSimpleName();

    private Context mContext;
    private SQLiteDatabase mDatabase;
    private GalleryDao galleryDao;
    private String uniqueTag;

    public APIFetcher(Context context){
        mContext = context;
        uniqueTag = TAG + mContext.hashCode();
        mDatabase = DatabaseHelper.getWritableDatabase(context);
        DaoMaster daoMaster = new DaoMaster(mDatabase);
        DaoSession daoSession = daoMaster.newSession();
        galleryDao = daoSession.getGalleryDao();
    }

    public void close(){
        mDatabase.close();
        RequestHelper.getInstance(mContext).cancelAllRequests(uniqueTag);
    }

    private void addRequestToQueue(Request request){
        RequestHelper.getInstance(mContext).addToRequestQueue(request, uniqueTag);
    }

    public void getGalleryList(JSONArray gidlist, final Listener listener){
        try {
            JSONObject obj = new JSONObject();
            obj.put("method", "gdata");
            obj.put("gidlist", gidlist);

            L.d("Request gdata: gidlist %s", gidlist.toString());

            EHAPIRequest req = new EHAPIRequest(mContext, obj, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject json) {
                    try {
                        JSONArray list = json.getJSONArray("gmetadata");
                        List<Gallery> result = new ArrayList<>();

                        for (int i = 0, len = list.length(); i < len; i++){
                            JSONObject data = list.getJSONObject(i);
                            if (data.has("error")) continue;

                            long id = data.getLong("gid");
                            Gallery gallery = galleryDao.load(id);

                            if (gallery == null){
                                gallery = new Gallery();

                                gallery.setStarred(false);
                                gallery.setProgress(0);
                            }

                            gallery.fromJSON(data);
                            result.add(gallery);
                        }

                        galleryDao.insertOrReplaceInTx(result);
                        listener.onGalleryListResponse(result);
                    } catch (JSONException e){
                        listener.onError(e);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    listener.onError(volleyError);
                }
            });

            req.setShouldCache(false);
            addRequestToQueue(req);
        } catch (JSONException e){
            listener.onError(e);
        }
    }

    public void getGallery(long id, String token, final Listener listener){
        JSONArray gidlist = new JSONArray();
        gidlist.put(id);
        gidlist.put(token);

        getGalleryList(gidlist, new Listener() {
            @Override
            public void onGalleryListResponse(List<Gallery> galleryList) {
                if (galleryList.size() == 0){
                    listener.onError(new APIException(APIException.GALLERY_NOT_FOUND));
                } else {
                    listener.onGalleryResponse(galleryList.get(0));
                }
            }

            @Override
            public void onError(Exception e) {
                listener.onError(e);
            }
        });
    }

    public void getGalleryTokenList(JSONArray pagelist, final Listener listener){
        try {
            JSONObject obj = new JSONObject();
            obj.put("method", "gtoken");
            obj.put("pagelist", pagelist);

            L.d("Request gtoken: %s", pagelist.toString());

            EHAPIRequest req = new EHAPIRequest(mContext, obj, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject json) {
                    try {
                        JSONArray tokenlist = json.getJSONArray("tokenlist");
                        Map<Long, String> result = new HashMap<>();

                        for (int i = 0, len = tokenlist.length(); i < len; i++){
                            JSONObject data = tokenlist.getJSONObject(i);

                            if (data.has("error") || !data.has("token")){
                                continue;
                            }

                            result.put(data.getLong("gid"), data.getString("token"));
                        }

                        listener.onGalleryTokenListResponse(result);
                    } catch (JSONException e){
                        listener.onError(e);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    listener.onError(volleyError);
                }
            });

            req.setShouldCache(false);
            addRequestToQueue(req);
        } catch (JSONException e){
            listener.onError(e);
        }
    }

    public void getGalleryToken(final long galleryId, String photoToken, int photoPage, final Listener listener){
        JSONArray pagelist = new JSONArray();
        JSONArray arr = new JSONArray();

        arr.put(galleryId);
        arr.put(photoToken);
        arr.put(photoPage);
        pagelist.put(arr);

        getGalleryTokenList(pagelist, new Listener() {
            @Override
            public void onGalleryTokenListResponse(Map<Long, String> galleryTokens) {
                if (galleryTokens.containsKey(galleryId)) {
                    listener.onGalleryTokenResponse(galleryTokens.get(galleryId));
                } else {
                    listener.onError(new APIException(APIException.TOKEN_NOT_FOUND));
                }
            }

            @Override
            public void onError(Exception e) {
                listener.onError(e);
            }
        });
    }

    public void getGalleryByPhotoInfo(final long id, String photoToken, int photoPage, final Listener listener){
        getGalleryToken(id, photoToken, photoPage, new Listener() {
            @Override
            public void onGalleryTokenResponse(String galleryToken) {
                getGallery(id, galleryToken, listener);
            }

            @Override
            public void onError(Exception e) {
                listener.onError(e);
            }
        });
    }

    public static abstract class Listener {
        public void onGalleryListResponse(List<Gallery> galleryList){
            //
        }

        public void onGalleryResponse(Gallery gallery){
            //
        }

        public void onGalleryTokenListResponse(Map<Long, String> galleryTokens){
            //
        }

        public void onGalleryTokenResponse(String galleryToken){
            //
        }

        public void onError(Exception e){
            //
        }
    }
}
