package tw.skyarrow.ehreader.network;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;

import java.util.List;
import java.util.Map;

import tw.skyarrow.ehreader.model.DaoMaster;
import tw.skyarrow.ehreader.model.DaoSession;
import tw.skyarrow.ehreader.model.Gallery;
import tw.skyarrow.ehreader.model.GalleryDao;
import tw.skyarrow.ehreader.util.DatabaseHelper;
import tw.skyarrow.ehreader.util.RequestHelper;

public class APIFetcher {
    public static final String TAG = APIFetcher.class.getSimpleName();

    private Context mContext;
    private DatabaseHelper dbHelper;
    private GalleryDao galleryDao;
    private String uniqueTag;

    public APIFetcher(Context context){
        mContext = context;
        uniqueTag = TAG + mContext.hashCode();
        dbHelper = DatabaseHelper.getInstance(context);
        DaoMaster daoMaster = new DaoMaster(dbHelper.open());
        DaoSession daoSession = daoMaster.newSession();
        galleryDao = daoSession.getGalleryDao();
    }

    public void close(){
        dbHelper.close();
        RequestHelper.getInstance(mContext).cancelAllRequests(uniqueTag);
    }

    private void addRequestToQueue(Request request){
        RequestHelper.getInstance(mContext).addToRequestQueue(request, uniqueTag);
    }

    public void getGalleryList(JSONArray gidlist, final Response.Listener<List<Gallery>> listener, final Response.ErrorListener errorListener) {
        EHAPIGalleryDataRequest req = new EHAPIGalleryDataRequest(mContext, gidlist, new Response.Listener<List<Gallery>>() {
            @Override
            public void onResponse(List<Gallery> galleryList) {
                galleryDao.insertOrReplaceInTx(galleryList);
                listener.onResponse(galleryList);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {
                errorListener.onErrorResponse(e);
            }
        });

        req.setShouldCache(false);
        addRequestToQueue(req);
    }

    public void getGallery(long id, String token, final Response.Listener<Gallery> listener, final Response.ErrorListener errorListener){
        JSONArray gidlist = new JSONArray();
        gidlist.put(id);
        gidlist.put(token);

        getGalleryList(gidlist, new Response.Listener<List<Gallery>>() {
            @Override
            public void onResponse(List<Gallery> galleryList) {
                if (galleryList.size() == 0) {
                    errorListener.onErrorResponse(new APIError(APIError.GALLERY_NOT_FOUND));
                } else {
                    listener.onResponse(galleryList.get(0));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {
                errorListener.onErrorResponse(e);
            }
        });
    }

    public void getGalleryTokenList(JSONArray pagelist, final Response.Listener<Map<Long, String>> listener, final Response.ErrorListener errorListener){
        EHAPIGalleryTokenRequest req = new EHAPIGalleryTokenRequest(mContext, pagelist, new Response.Listener<Map<Long, String>>() {
            @Override
            public void onResponse(Map<Long, String> tokenList) {
                listener.onResponse(tokenList);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {
                errorListener.onErrorResponse(e);
            }
        });

        req.setShouldCache(false);
        addRequestToQueue(req);
    }

    public void getGalleryToken(final long galleryId, String photoToken, int photoPage, final Response.Listener<String> listener, final Response.ErrorListener errorListener){
        JSONArray pagelist = new JSONArray();
        JSONArray arr = new JSONArray();

        arr.put(galleryId);
        arr.put(photoToken);
        arr.put(photoPage);
        pagelist.put(arr);

        getGalleryTokenList(pagelist, new Response.Listener<Map<Long, String>>() {
            @Override
            public void onResponse(Map<Long, String> galleryTokens) {
                if (galleryTokens.containsKey(galleryId)) {
                    listener.onResponse(galleryTokens.get(galleryId));
                } else {
                    errorListener.onErrorResponse(new APIError(APIError.TOKEN_NOT_FOUND));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {
                errorListener.onErrorResponse(e);
            }
        });
    }

    public void getGalleryByPhotoInfo(final long id, String photoToken, int photoPage, final Response.Listener<Gallery> listener, final Response.ErrorListener errorListener){
        getGalleryToken(id, photoToken, photoPage, new Response.Listener<String>() {
            @Override
            public void onResponse(String galleryToken) {
                getGallery(id, galleryToken, listener, errorListener);
            }
        }, errorListener);
    }

    public static class APIError extends VolleyError {
        public static final int GALLERY_NOT_FOUND = 1;
        public static final int TOKEN_NOT_FOUND = 2;

        private int code;

        public APIError(int code, String message) {
            super(message);
            this.code = code;
        }

        public APIError(int code){
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }
    }
}
