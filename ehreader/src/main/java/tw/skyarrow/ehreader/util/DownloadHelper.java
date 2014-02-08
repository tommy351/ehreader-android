package tw.skyarrow.ehreader.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.preference.PreferenceManager;

import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.greenrobot.dao.DaoException;
import de.greenrobot.dao.query.QueryBuilder;
import tw.skyarrow.ehreader.BaseApplication;
import tw.skyarrow.ehreader.Constant;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.db.DaoMaster;
import tw.skyarrow.ehreader.db.DaoSession;
import tw.skyarrow.ehreader.db.Download;
import tw.skyarrow.ehreader.db.DownloadDao;
import tw.skyarrow.ehreader.db.Gallery;
import tw.skyarrow.ehreader.db.GalleryDao;
import tw.skyarrow.ehreader.db.Photo;
import tw.skyarrow.ehreader.db.PhotoDao;
import tw.skyarrow.ehreader.service.GalleryDownloadService;

/**
 * Created by SkyArrow on 2014/1/30.
 */
public class DownloadHelper {
    private static final Pattern pPhotoUrl = Pattern.compile("http://(g\\.e-|ex)hentai\\.org/s/(\\w+?)/(\\d+)-(\\d+)");
    private static final Pattern pShowkey = Pattern.compile("var showkey.*=.*\"([\\w-]+?)\";");
    private static final Pattern pImageSrc = Pattern.compile("<img id=\"img\" src=\"(.+)/(.+?)\"");
    private static final Pattern pGalleryURL = Pattern.compile("<a href=\"http://(g\\.e-|ex)hentai\\.org/g/(\\d+)/(\\w+)/\" onmouseover");

    private Context context;

    private SQLiteDatabase db;
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private GalleryDao galleryDao;
    private PhotoDao photoDao;
    private DownloadDao downloadDao;
    private HttpContext httpContext;

    public DownloadHelper(Context context) {
        this.context = context;

        initDb();
        httpContext = setupHttpContext(context);
    }

    private void initDb() {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, Constant.DB_NAME, null);
        db = helper.getWritableDatabase();
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
        galleryDao = daoSession.getGalleryDao();
        photoDao = daoSession.getPhotoDao();
        downloadDao = daoSession.getDownloadDao();
    }

    public boolean isLoggedIn() {
        return BaseApplication.isLoggedIn();
    }

    public static HttpContext setupHttpContext(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean loggedIn = preferences.getBoolean(context.getString(R.string.pref_logged_in), false);
        String memberid = preferences.getString(context.getString(R.string.pref_login_memberid), "");
        String passhash = preferences.getString(context.getString(R.string.pref_login_passhash), "");
        String sessionId = preferences.getString(context.getString(R.string.pref_login_sessionid), "");

        HttpContext httpContext = new BasicHttpContext();
        CookieStore cookieStore = new BasicCookieStore();

        cookieStore.addCookie(new Cookie(Constant.IPB_MEMBER_ID, memberid, loggedIn));
        cookieStore.addCookie(new Cookie(Constant.IPB_PASS_HASH, passhash, loggedIn));
        cookieStore.addCookie(new Cookie(Constant.IPB_SESSION_ID, sessionId, loggedIn));
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        return httpContext;
    }

    private static class Cookie extends BasicClientCookie {
        private Cookie(String name, String value, boolean loggedIn) {
            super(name, value);

            setPath("/");
            setDomain(loggedIn ? "exhentai.org" : "e-hentai.org");
        }
    }

    private JSONObject getAPIResponse(JSONObject json) throws IOException, JSONException {
        HttpClient client = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(isLoggedIn() ? Constant.API_URL_EX : Constant.API_URL);

        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setEntity(new StringEntity(json.toString()));

        HttpResponse response = client.execute(httpPost, httpContext);
        String result = HttpRequestHelper.readResponse(response);

        return new JSONObject(result);
    }

    private JSONObject getAPIResponse(String method, JSONObject json) throws IOException, JSONException {
        json.put("method", method);

        return getAPIResponse(json);
    }

    public List<Photo> getPhotoList(long galleryId, int page) throws IOException {
        Gallery gallery = galleryDao.load(galleryId);

        if (gallery == null) {
            return null;
        } else {
            return getPhotoList(gallery, page);
        }
    }

    public List<Photo> getPhotoList(Gallery gallery, int page) throws IOException {
        String url = gallery.getUrl(page, isLoggedIn());

        L.d("Get photo list: %s", url);

        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = client.execute(httpGet, httpContext);
        String content = HttpRequestHelper.readResponse(response);

        List<Photo> list = new ArrayList<Photo>();
        long galleryId = gallery.getId();
        Matcher matcher = pPhotoUrl.matcher(content);

        while (matcher.find()) {
            Photo photo = new Photo();
            String token = matcher.group(2);
            int photoPage = Integer.parseInt(matcher.group(4));

            L.d("Photo found: {galleryId: %d, token: %s, page: %d}", galleryId, token, photoPage);

            photo.setGalleryId(galleryId);
            photo.setToken(token);
            photo.setPage(photoPage);
            list.add(photo);

            QueryBuilder qb = photoDao.queryBuilder();
            qb.where(qb.and(PhotoDao.Properties.GalleryId.eq(galleryId), PhotoDao.Properties.Page.eq(photoPage)));
            qb.limit(1);

            if (qb.count() > 0) {
                Photo qPhoto = (Photo) qb.list().get(0);

                qPhoto.setToken(token);
                photoDao.updateInTx(qPhoto);
            } else {
                photo.setDownloaded(false);
                photo.setBookmarked(false);
                photo.setInvalid(false);

                photoDao.insertInTx(photo);
            }
        }

        return list;
    }

    public Photo getPhotoInfo(Gallery gallery, int page) throws IOException, JSONException {
        QueryBuilder qb = photoDao.queryBuilder();
        qb.where(qb.and(PhotoDao.Properties.GalleryId.eq(gallery.getId()), PhotoDao.Properties.Page.eq(page)));
        List<Photo> list = qb.list();

        if (list.size() > 0) {
            return getPhotoInfo(gallery, list.get(0));
        }

        int galleryPage = page / Constant.PHOTO_PER_PAGE;

        getPhotoList(gallery, galleryPage);

        list = qb.list();

        if (list.size() > 0) {
            return getPhotoInfo(gallery, list.get(0));
        } else {
            return null;
        }
    }

    public Photo getPhotoInfo(Gallery gallery, Photo photo) throws IOException, JSONException {
        if (photo.getSrc() != null && !photo.getSrc().isEmpty() && !photo.getInvalid()) {
            return photo;
        }

        JSONObject result = getPhotoInfoRaw(gallery, photo);

        if (result.has("error")) {
            String error = result.getString("error");

            L.e("Show page callback error: %s", error);

            if (error.equals("Key mismatch")) {
                getShowkey(gallery);
                return getPhotoInfo(gallery, photo);
            }

            return null;
        }

        Matcher matcher = pImageSrc.matcher(result.getString("i3"));
        String src = "";
        String filename = "";

        while (matcher.find()) {
            filename = matcher.group(2);
            src = matcher.group(1) + "/" + filename;
        }

        photo.setFilename(filename);
        photo.setSrc(src);
        photo.setWidth(Integer.parseInt(result.getString("x")));
        photo.setHeight(Integer.parseInt(result.getString("y")));
        photo.setInvalid(false);
        photoDao.updateInTx(photo);

        return photo;
    }

    public JSONObject getPhotoInfoRaw(Gallery gallery, Photo photo) throws IOException, JSONException {
        String showkey = gallery.getShowkey();

        if (showkey == null || showkey.isEmpty()) {
            showkey = getShowkey(gallery);
        }

        JSONObject json = new JSONObject();

        json.put("gid", gallery.getId());
        json.put("page", photo.getPage());
        json.put("imgkey", photo.getToken());
        json.put("showkey", showkey);

        L.d("Show page request: %s", json.toString());

        JSONObject result = getAPIResponse("showpage", json);

        L.d("Show page callback: %s", result.toString());

        return result;
    }

    public String getShowkey(long galleryId) throws IOException {
        Gallery gallery = galleryDao.load(galleryId);

        if (gallery == null) {
            return null;
        } else {
            return getShowkey(gallery);
        }
    }

    public String getShowkey(Gallery gallery) throws IOException {
        QueryBuilder qb = photoDao.queryBuilder();
        qb.where(PhotoDao.Properties.GalleryId.eq(gallery.getId())).limit(1);
        Photo photo = (Photo) qb.list().get(0);
        String url = photo.getUrl(isLoggedIn());

        L.d("Get show key: %s", url);

        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = client.execute(httpGet, httpContext);
        String content = HttpRequestHelper.readResponse(response);
        Matcher matcher = pShowkey.matcher(content);
        String showkey = "";

        while (matcher.find()) {
            showkey = matcher.group(1);
        }

        L.d("Show key found: %s", showkey);

        if (!showkey.isEmpty()) {
            gallery.setShowkey(showkey);
            galleryDao.updateInTx(gallery);
        }

        return showkey;
    }

    public List<Gallery> getGalleryList(String base) throws IOException, JSONException {
        return getGalleryList(base, 0);
    }

    public List<Gallery> getGalleryList(String base, int page) throws IOException, JSONException {
        String url = getGalleryListURL(base, page);
        List<Gallery> galleryList = new ArrayList<Gallery>();

        L.d("Get gallery list: %s", url);

        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = client.execute(httpGet, httpContext);
        String html = HttpRequestHelper.readResponse(response);
        Matcher matcher = pGalleryURL.matcher(html);
        JSONArray gidlist = new JSONArray();

        while (matcher.find()) {
            long id = Long.parseLong(matcher.group(2));
            String token = matcher.group(3);
            JSONArray arr = new JSONArray();

            arr.put(id);
            arr.put(token);

            L.d("Gallery found: {id: %d, token: %s}", id, token);
            gidlist.put(arr);
        }

        if (gidlist.length() == 0) return galleryList;

        JSONObject obj = new JSONObject();

        obj.put("gidlist", gidlist);

        JSONObject json = getAPIResponse("gdata", obj);

        if (json.has("error")) {
            L.e("Get gallery list error: %s", json.getString("error"));
            return null;
        }

        JSONArray gmetadata = json.getJSONArray("gmetadata");

        for (int i = 0, len = gmetadata.length(); i < len; i++) {
            JSONObject data = gmetadata.getJSONObject(i);
            long id = data.getLong("gid");

            if (data.getBoolean("expunged")) continue;


            Gallery gallery = galleryDao.load(id);
            boolean isNew = gallery == null;

            if (isNew) {
                gallery = new Gallery();

                gallery.setStarred(false);
                gallery.setProgress(0);
            }

            gallery.setId(id);
            gallery.setToken(data.getString("token"));
            gallery.setTitle(data.getString("title"));
            gallery.setSubtitle(data.getString("title_jpn"));
            gallery.setCategory(data.getString("category"));
            gallery.setThumbnail(data.getString("thumb"));
            gallery.setCount(data.getInt("filecount"));
            gallery.setRating((float) data.getDouble("rating"));
            gallery.setUploader(data.getString("uploader"));
            gallery.setTags(data.getJSONArray("tags").toString());
            gallery.setCreated(new Date(data.getLong("posted") * 1000));
            gallery.setSize(Long.parseLong(data.getString("filesize")));

            if (isNew) {
                galleryDao.insertInTx(gallery);
            } else {
                galleryDao.updateInTx(gallery);
            }

            galleryList.add(gallery);
        }

        return galleryList;
    }

    private String getGalleryListURL(String base, int page) {
        Uri.Builder builder = Uri.parse(base).buildUpon();
        builder.appendQueryParameter("page", Integer.toString(page));

        return builder.build().toString();
    }

    public void startAllDownload() {
        QueryBuilder qb = downloadDao.queryBuilder();
        qb.where(DownloadDao.Properties.Status.notIn(Download.STATUS_SUCCESS, Download.STATUS_ERROR));
        List<Download> downloadList = qb.list();

        for (Download download : downloadList) {
            Intent intent = new Intent(context, GalleryDownloadService.class);

            intent.setAction(GalleryDownloadService.ACTION_START);
            intent.putExtra(GalleryDownloadService.GALLERY_ID, download.getId());
            context.startService(intent);
        }
    }

    public void pauseAllDownload() {
        Intent intent = new Intent(context, GalleryDownloadService.class);

        intent.setAction(GalleryDownloadService.ACTION_STOP);
        context.startService(intent);
    }

    public boolean isServiceRunning() {
        return isServiceRunning(context);
    }

    // http://stackoverflow.com/a/5921190
    public static boolean isServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        String className = GalleryDownloadService.class.getName();

        for (ActivityManager.RunningServiceInfo info : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (className.equals(info.service.getClassName())) {
                return true;
            }
        }

        return false;
    }
}
