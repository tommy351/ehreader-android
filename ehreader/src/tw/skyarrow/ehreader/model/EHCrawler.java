package tw.skyarrow.ehreader.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tw.skyarrow.ehreader.util.DatabaseHelper;
import tw.skyarrow.ehreader.util.EHStringRequest;
import tw.skyarrow.ehreader.util.L;
import tw.skyarrow.ehreader.util.RequestHelper;

public class EHCrawler {
    public static final String TAG = EHCrawler.class.getSimpleName();

    private static final Pattern pGalleryURL = Pattern.compile("<a href=\"http://(?:g\\.e-|ex)hentai\\.org/g/(\\d+)/(\\w+)/\" onmouseover");
    private static final Pattern pPhotoURL = Pattern.compile("http://(?:g\\.e-|ex)hentai\\.org/s/(\\w+?)/(\\d+)-(\\d+)");
    private static final Pattern pPhotoVars = Pattern.compile("var +(\\w+) *= *(.+?);");
    private static final Pattern pImageSrc = Pattern.compile("<img id=\"img\" src=\"([^\"]+)\"");

    private Context mContext;
    private SQLiteDatabase mDatabase;
    private GalleryDao galleryDao;
    private PhotoDao photoDao;
    private String uniqueTag;
    private APIFetcher apiFetcher;

    public EHCrawler(Context context){
        mContext = context;
        uniqueTag = TAG + mContext.hashCode();
        mDatabase = DatabaseHelper.getWritableDatabase(context);
        DaoMaster daoMaster = new DaoMaster(mDatabase);
        DaoSession daoSession = daoMaster.newSession();
        galleryDao = daoSession.getGalleryDao();
        photoDao = daoSession.getPhotoDao();
        apiFetcher = new APIFetcher(mContext);
    }

    public void close(){
        mDatabase.close();
        apiFetcher.close();
        RequestHelper.getInstance(mContext).cancelAllRequests(uniqueTag);
    }

    private void addRequestToQueue(Request request){
        RequestHelper.getInstance(mContext).addToRequestQueue(request, uniqueTag);
    }

    public void getGalleryList(String baseUrl, Listener listener){
        getGalleryList(baseUrl, 0, listener);
    }

    public void getGalleryList(String baseUrl, int page, final Listener listener){
        Uri.Builder builder = Uri.parse(baseUrl).buildUpon();
        builder.appendQueryParameter("page", Integer.toString(page));
        String url = builder.build().toString();

        L.d("Get gallery list: %s", url);

        EHStringRequest req = new EHStringRequest(mContext, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String html) {
                JSONArray gidlist = new JSONArray();
                Matcher matcher = pGalleryURL.matcher(html);

                while (matcher.find()){
                    JSONArray arr = new JSONArray();
                    long id = Long.parseLong(matcher.group(1), 10);
                    String token = matcher.group(2);

                    arr.put(id);
                    arr.put(token);
                    gidlist.put(arr);
                }

                if (gidlist.length() == 0){
                    listener.onGalleryListResponse(Collections.<Gallery>emptyList());
                    return;
                }

                apiFetcher.getGalleryList(gidlist, new APIFetcher.Listener() {
                    @Override
                    public void onGalleryListResponse(List<Gallery> galleryList) {
                        listener.onGalleryListResponse(galleryList);
                    }

                    @Override
                    public void onError(Exception e) {
                        listener.onError(e);
                    }
                });
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                listener.onError(volleyError);
            }
        });

        req.setShouldCache(false);
        addRequestToQueue(req);
    }

    public void getPhotoList(String baseUrl, Listener listener){
        getPhotoList(baseUrl, 0, listener);
    }

    public void getPhotoList(String baseUrl, int page, final Listener listener){
        Uri.Builder builder = Uri.parse(baseUrl).buildUpon();
        builder.appendQueryParameter("p", Integer.toString(page));
        String url = builder.build().toString();

        L.d("Get photo list: %s", url);

        EHStringRequest req = new EHStringRequest(mContext, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String html) {
                List<Photo> list = new ArrayList<>();
                Matcher matcher = pPhotoURL.matcher(html);

                while (matcher.find()){
                    String token = matcher.group(1);
                    long galleryId = Long.parseLong(matcher.group(2), 10);
                    int page = Integer.parseInt(matcher.group(3), 10);
                    Photo photo = Photo.findPhoto(photoDao, galleryId, page);

                    if (photo == null){
                        photo = new Photo();
                    }

                    photo.setGalleryId(galleryId);
                    photo.setToken(token);
                    photo.setPage(page);
                    list.add(photo);
                }

                photoDao.insertOrReplaceInTx(list);
                listener.onPhotoListResponse(list);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                listener.onError(volleyError);
            }
        });

        req.setShouldCache(false);
        addRequestToQueue(req);
    }

    public void getPhoto(final Photo photo, final Listener listener){
        L.d("Get photo: %s", photo.getURL());

        EHStringRequest req = new EHStringRequest(mContext, photo.getURL(), new Response.Listener<String>() {
            @Override
            public void onResponse(String html) {
                try {
                    Matcher matcher = pPhotoVars.matcher(html);
                    Gallery gallery = galleryDao.load(photo.getGalleryId());

                    while (matcher.find()){
                        String key = matcher.group(1);
                        String value = matcher.group(2);

                        if ("showkey".equals(key)){
                            gallery.setShowkey(value.substring(1, value.length() - 1));
                        } else if ("si".equals(key)){
                            photo.setRetryId(value);
                        } else if ("x".equals(key)){
                            photo.setWidth(Integer.parseInt(value, 10));
                        } else if ("y".equals(key)){
                            photo.setHeight(Integer.parseInt(value, 10));
                        }
                    }

                    matcher = pImageSrc.matcher(html);

                    if (matcher.find()){
                        photo.setSrc(URLDecoder.decode(matcher.group(1), "UTF-8"));
                    } else {
                        listener.onError(new APIException(APIException.PHOTO_NOT_FOUND));
                        return;
                    }

                    galleryDao.insertOrReplaceInTx(gallery);
                    photoDao.insertOrReplaceInTx(photo);
                    listener.onPhotoResponse(photo);
                } catch (UnsupportedEncodingException e){
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
    }

    public void getPhoto(long galleryId, int page, Listener listener){
        Photo photo = PhotoBase.findPhoto(photoDao, galleryId, page);

        if (photo != null){
            getPhoto(photo, listener);
        }
    }

    public static abstract class Listener {
        public void onGalleryListResponse(List<Gallery> galleryList){
            //
        }

        public void onPhotoListResponse(List<Photo> photoList){
            //
        }

        public void onPhotoResponse(Photo photo){
            //
        }

        public void onError(Exception e){
            //
        }
    }
}
