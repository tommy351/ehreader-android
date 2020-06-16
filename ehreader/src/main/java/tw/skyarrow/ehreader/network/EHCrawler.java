package tw.skyarrow.ehreader.network;

import android.content.Context;
import android.net.Uri;

import com.android.volley.ParseError;
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

import tw.skyarrow.ehreader.model.DaoMaster;
import tw.skyarrow.ehreader.model.DaoSession;
import tw.skyarrow.ehreader.model.Gallery;
import tw.skyarrow.ehreader.model.GalleryDao;
import tw.skyarrow.ehreader.model.Photo;
import tw.skyarrow.ehreader.model.PhotoBase;
import tw.skyarrow.ehreader.model.PhotoDao;
import tw.skyarrow.ehreader.util.DatabaseHelper;
import tw.skyarrow.ehreader.util.L;
import tw.skyarrow.ehreader.util.LoginHelper;
import tw.skyarrow.ehreader.util.RequestHelper;

public class EHCrawler {
    public static final String TAG = EHCrawler.class.getSimpleName();

    private static final Pattern pGalleryURL = Pattern.compile("<a href=\"http://(?:g\\.e-|ex)hentai\\.org/g/(\\d+)/(\\w+)/\" onmouseover");
    private static final Pattern pPhotoURL = Pattern.compile("http://(?:g\\.e-|ex)hentai\\.org/s/(\\w+?)/(\\d+)-(\\d+)");
    private static final Pattern pPhotoVars = Pattern.compile("var +(\\w+) *= *(.+?);");
    private static final Pattern pImageSrc = Pattern.compile("<img id=\"img\" src=\"([^\"]+)\"");

    private Context mContext;
    private DatabaseHelper dbHelper;
    private GalleryDao galleryDao;
    private PhotoDao photoDao;
    private String uniqueTag;
    private APIFetcher apiFetcher;

    public EHCrawler(Context context){
        mContext = context;
        uniqueTag = TAG + mContext.hashCode();
        dbHelper = DatabaseHelper.getInstance(context);
        DaoMaster daoMaster = new DaoMaster(dbHelper.open());
        DaoSession daoSession = daoMaster.newSession();
        galleryDao = daoSession.getGalleryDao();
        photoDao = daoSession.getPhotoDao();
        apiFetcher = new APIFetcher(mContext);
    }

    public void close(){
        dbHelper.close();
        apiFetcher.close();
        RequestHelper.getInstance(mContext).cancelAllRequests(uniqueTag);
    }

    private void addRequestToQueue(Request request){
        RequestHelper.getInstance(mContext).addToRequestQueue(request, uniqueTag);
    }

    public void getGalleryList(String url, final Response.Listener<List<Gallery>> listener, final Response.ErrorListener errorListener){
        L.d("Get gallery list: %s", url);

        EHStringRequest req = new EHStringRequest(mContext, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String html) {
                JSONArray gidlist = new JSONArray();
                Matcher matcher = pGalleryURL.matcher(html);

                while (matcher.find()) {
                    JSONArray arr = new JSONArray();
                    long id = Long.parseLong(matcher.group(1), 10);
                    String token = matcher.group(2);

                    arr.put(id);
                    arr.put(token);
                    gidlist.put(arr);
                }

                if (gidlist.length() == 0) {
                    listener.onResponse(Collections.<Gallery>emptyList());
                    return;
                }

                apiFetcher.getGalleryList(gidlist, listener, errorListener);
            }
        }, errorListener);

        req.setShouldCache(false);
        addRequestToQueue(req);
    }

    public void getGalleryList(String baseUrl, int page, Response.Listener<List<Gallery>> listener, Response.ErrorListener errorListener){
        Uri.Builder builder = Uri.parse(baseUrl).buildUpon();
        builder.appendQueryParameter("page", Integer.toString(page));
        String url = builder.build().toString();

        getGalleryList(url, listener, errorListener);
    }

    public void getPhotoList(String url, final Response.Listener<List<Photo>> listener, Response.ErrorListener errorListener){
        L.d("Get photo list: %s", url);

        EHStringRequest req = new EHStringRequest(mContext, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String html) {
                List<Photo> list = new ArrayList<>();
                Matcher matcher = pPhotoURL.matcher(html);

                while (matcher.find()){
                    String token = matcher.group(1);
                    long galleryId = Long.parseLong(matcher.group(2), 10);
                    int photoPage = Integer.parseInt(matcher.group(3), 10);
                    Photo photo = Photo.findPhoto(photoDao, galleryId, photoPage);

                    if (photo == null){
                        photo = new Photo();
                    }

                    photo.setGalleryId(galleryId);
                    photo.setToken(token);
                    photo.setPage(photoPage);
                    list.add(photo);
                }

                photoDao.insertOrReplaceInTx(list);
                listener.onResponse(list);
            }
        }, errorListener);

        req.setShouldCache(false);
        addRequestToQueue(req);
    }

    public void getPhotoList(String baseUrl, int page, Response.Listener<List<Photo>> listener, Response.ErrorListener errorListener){
        Uri.Builder builder = Uri.parse(baseUrl).buildUpon();
        builder.appendQueryParameter("p", Integer.toString(page));
        String url = builder.build().toString();

        getPhotoList(url, listener, errorListener);
    }

    public void getPhotoList(Gallery gallery, Response.Listener<List<Photo>> listener, Response.ErrorListener errorListener){
        getPhotoList(gallery.getURL(isLoggedIn()), listener, errorListener);
    }

    public void getPhotoList(Gallery gallery, int page, Response.Listener<List<Photo>> listener, Response.ErrorListener errorListener){
        getPhotoList(gallery.getURL(isLoggedIn()), page, listener, errorListener);
    }

    public void getPhoto(final Photo photo, final Response.Listener<Photo> listener, final Response.ErrorListener errorListener){
        String url = photo.getURL(isLoggedIn());
        L.d("Get photo: %s", url);

        EHStringRequest req = new EHStringRequest(mContext, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String html) {
                try {
                    Matcher matcher = pPhotoVars.matcher(html);
                    Gallery gallery = galleryDao.load(photo.getGalleryId());

                    while (matcher.find()) {
                        String key = matcher.group(1);
                        String value = matcher.group(2);

                        if ("showkey".equals(key)) {
                            gallery.setShowkey(value.substring(1, value.length() - 1));
                        } else if ("si".equals(key)) {
                            photo.setRetryId(value);
                        } else if ("x".equals(key)) {
                            photo.setWidth(Integer.parseInt(value, 10));
                        } else if ("y".equals(key)) {
                            photo.setHeight(Integer.parseInt(value, 10));
                        }
                    }

                    matcher = pImageSrc.matcher(html);

                    if (matcher.find()) {
                        photo.setSrc(URLDecoder.decode(matcher.group(1), "UTF-8"));
                    } else {
                        errorListener.onErrorResponse(new CrawlerError(CrawlerError.PHOTO_NOT_FOUND));
                        return;
                    }

                    galleryDao.insertOrReplaceInTx(gallery);
                    photoDao.insertOrReplaceInTx(photo);
                    listener.onResponse(photo);
                } catch (UnsupportedEncodingException e) {
                    errorListener.onErrorResponse(new ParseError(e));
                }
            }
        }, errorListener);

        req.setShouldCache(false);
        addRequestToQueue(req);
    }

    public void getPhoto(long galleryId, int page, Response.Listener<Photo> listener, Response.ErrorListener errorListener){
        Photo photo = PhotoBase.findPhoto(photoDao, galleryId, page);

        if (photo != null){
            getPhoto(photo, listener, errorListener);
        } else {
            errorListener.onErrorResponse(new CrawlerError(CrawlerError.PHOTO_NOT_FOUND));
        }
    }

    private boolean isLoggedIn(){
        return LoginHelper.getInstance(mContext).isLoggedIn();
    }

    public static class CrawlerError extends VolleyError {
        public static final int PHOTO_NOT_FOUND = 1;

        private int code;

        public CrawlerError(int code, String message) {
            super(message);
            this.code = code;
        }

        public CrawlerError(int code){
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
