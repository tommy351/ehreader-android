package tw.skyarrow.ehreader.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.preference.PreferenceManager;

import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
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

import de.greenrobot.dao.query.QueryBuilder;
import tw.skyarrow.ehreader.Constant;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.db.DaoMaster;
import tw.skyarrow.ehreader.db.DaoSession;
import tw.skyarrow.ehreader.db.Gallery;
import tw.skyarrow.ehreader.db.GalleryDao;
import tw.skyarrow.ehreader.db.Photo;
import tw.skyarrow.ehreader.db.PhotoDao;
import tw.skyarrow.ehreader.util.DatabaseHelper;
import tw.skyarrow.ehreader.util.HttpRequestHelper;
import tw.skyarrow.ehreader.util.L;
import tw.skyarrow.ehreader.util.LoginHelper;

/**
 * Created by SkyArrow on 2014/2/19.
 */
public class DataLoader {
    private static DataLoader instance;
    private Context context;
    private SQLiteDatabase db;
    private GalleryDao galleryDao;
    private PhotoDao photoDao;
    private HttpContext httpContext;
    private LoginHelper loginHelper;

    private static final String IPB_MEMBER_ID = "ipb_member_id";
    private static final String IPB_PASS_HASH = "ipb_pass_hash";
    private static final String IPB_SESSION_ID = "ipb_session_id";

    public static final Pattern pGalleryUrl = Pattern.compile("http://(g\\.e-|ex)hentai\\.org/g/(\\d+)/(\\w+)");
    public static final Pattern pPhotoUrl = Pattern.compile("http://(g\\.e-|ex)hentai\\.org/s/(\\w+?)/(\\d+)-(\\d+)");
    public static final Pattern pShowkey = Pattern.compile("var showkey.*=.*\"([\\w-]+?)\";");
    public static final Pattern pImageSrc = Pattern.compile("<img id=\"img\" src=\"(.+)/(.+?)\"");
    public static final Pattern pGalleryURL = Pattern.compile("<a href=\"http://(g\\.e-|ex)hentai\\.org/g/(\\d+)/(\\w+)/\" onmouseover");

    private DataLoader(Context context) {
        this.context = context;
        loginHelper = LoginHelper.getInstance(context);

        setupDatabase();
        setupHttpContext();
    }

    public static DataLoader getInstance(Context context) {
        if (instance == null) {
            instance = new DataLoader(context.getApplicationContext());
        }

        return instance;
    }

    private void setupDatabase() {
        DatabaseHelper helper = DatabaseHelper.getInstance(context);
        db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();
        galleryDao = daoSession.getGalleryDao();
        photoDao = daoSession.getPhotoDao();
    }

    private void setupHttpContext() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String memberId = preferences.getString(context.getString(R.string.pref_login_memberid), "");
        String passhash = preferences.getString(context.getString(R.string.pref_login_passhash), "");
        String sessionid = preferences.getString(context.getString(R.string.pref_login_sessionid), "");
        boolean isLoggedIn = isLoggedIn();

        httpContext = new BasicHttpContext();
        CookieStore cookieStore = new BasicCookieStore();

        cookieStore.addCookie(new Cookie(IPB_MEMBER_ID, memberId, isLoggedIn));
        cookieStore.addCookie(new Cookie(IPB_PASS_HASH, passhash, isLoggedIn));
        cookieStore.addCookie(new Cookie(IPB_SESSION_ID, sessionid, isLoggedIn));
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
    }

    private class Cookie extends BasicClientCookie {
        private Cookie(String name, String value, boolean loggedIn) {
            super(name, value);

            setPath("/");
            setDomain(loggedIn ? "exhentai.org" : "e-hentai.org");
        }
    }

    public boolean isLoggedIn() {
        return loginHelper.isLoggedIn();
    }

    public HttpContext getHttpContext() {
        return httpContext;
    }

    private HttpResponse getHttpResponse(HttpRequestBase httpRequest) throws IOException {
        HttpClient httpClient = new DefaultHttpClient();

        return httpClient.execute(httpRequest, httpContext);
    }

    public JSONObject callApi(JSONObject json) throws ApiCallException {
        String responseStr = "";

        try {
            String url = isLoggedIn() ? Constant.API_URL_EX : Constant.API_URL;
            HttpPost httpPost = new HttpPost(url);

            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity(json.toString()));

            HttpResponse response = getHttpResponse(httpPost);
            responseStr = HttpRequestHelper.readResponse(response);

            L.d("Api callback: %s", responseStr);

            JSONObject result = new JSONObject(responseStr);

            if (result.has("error")) {
                String error = result.getString("error");

                L.e("Api call error: %s", error);

                if (error.equals("Key mismatch")) {
                    throw new ApiCallException(ApiErrorCode.SHOWKEY_INVALID, url, response);
                } else {
                    throw new ApiCallException(ApiErrorCode.API_ERROR, url, response);
                }
            }

            return result;
        } catch (IOException e) {
            throw new ApiCallException(ApiErrorCode.IO_ERROR, e);
        } catch (JSONException e) {
            if (responseStr != null && !responseStr.isEmpty()) {
                L.e("Api call error: %s", responseStr);
            }

            throw new ApiCallException(ApiErrorCode.JSON_ERROR, e);
        }
    }

    public JSONObject callApi(String method, JSONObject json) throws ApiCallException {
        try {
            json.put("method", method);
        } catch (JSONException e) {
            throw new ApiCallException(ApiErrorCode.JSON_ERROR, e);
        }

        return callApi(json);
    }

    public List<Photo> getPhotoList(long galleryId, int page) throws ApiCallException {
        Gallery gallery = galleryDao.load(galleryId);

        if (gallery == null) {
            throw new ApiCallException(ApiErrorCode.GALLERY_NOT_EXIST);
        } else {
            return getPhotoList(gallery, page);
        }
    }

    public List<Photo> getPhotoList(Gallery gallery, int page) throws ApiCallException {
        try {
            String url = gallery.getUrl(page, isLoggedIn());

            L.d("Get photo list: %s", url);

            HttpGet httpGet = new HttpGet(url);
            HttpResponse response = getHttpResponse(httpGet);
            String content = HttpRequestHelper.readResponse(response);

            List<Photo> list = new ArrayList<Photo>();
            long galleryId = gallery.getId();
            Matcher matcher = pPhotoUrl.matcher(content);

            while (matcher.find()) {
                String token = matcher.group(2);
                int photoPage = Integer.parseInt(matcher.group(4));
                Photo photo = getPhotoInDb(galleryId, photoPage);

                L.d("Photo found: {galleryId: %d, token: %s, page: %d}", galleryId, token, photoPage);

                if (photo != null) {
                    photo.setToken(token);
                    photoDao.updateInTx(photo);
                } else {
                    photo = new Photo();

                    photo.setGalleryId(galleryId);
                    photo.setToken(token);
                    photo.setPage(photoPage);
                    photo.setDownloaded(false);
                    photo.setBookmarked(false);
                    photo.setInvalid(false);

                    photoDao.insertInTx(photo);
                }

                list.add(photo);
            }

            return list;
        } catch (IOException e) {
            throw new ApiCallException(ApiErrorCode.IO_ERROR, e);
        }
    }

    public Photo getPhotoInfo(Gallery gallery, int page) throws ApiCallException {
        Photo photo = getPhotoInDb(gallery, page);

        if (photo != null) {
            return getPhotoInfo(gallery, photo);
        }

        int galleryPage = page / Constant.PHOTO_PER_PAGE;
        List<Photo> list = getPhotoList(gallery, galleryPage);
        photo = list.get((page - 1) % Constant.PHOTO_PER_PAGE);

        if (photo != null) {
            return getPhotoInfo(gallery, photo);
        } else {
            throw new ApiCallException(ApiErrorCode.PHOTO_NOT_EXIST);
        }
    }

    public Photo getPhotoInDb(long galleryId, int page) {
        QueryBuilder<Photo> qb = photoDao.queryBuilder();
        qb.where(qb.and(
                PhotoDao.Properties.GalleryId.eq(galleryId),
                PhotoDao.Properties.Page.eq(page)
        ));
        qb.orderDesc(PhotoDao.Properties.Id);

        if (qb.count() > 0) {
            return qb.list().get(0);
        } else {
            return null;
        }
    }

    public Photo getPhotoInDb(Gallery gallery, int page) {
        return getPhotoInDb(gallery.getId(), page);
    }

    public Photo getPhotoInfo(Gallery gallery, Photo photo) throws ApiCallException {
        String src = photo.getSrc();

        if (src != null && !src.isEmpty() && !photo.getInvalid()) {
            return photo;
        }

        try {
            JSONObject json = getPhotoRaw(gallery, photo);
            Matcher matcher = pImageSrc.matcher(json.getString("i3"));
            String filename = "";

            while (matcher.find()) {
                filename = matcher.group(2);
                src = matcher.group(1) + "/" + filename;
            }

            if (src.isEmpty() || filename.isEmpty()) {
                throw new ApiCallException(ApiErrorCode.PHOTO_NOT_FOUND);
            }

            photo.setFilename(filename);
            photo.setSrc(src);
            photo.setWidth(Integer.parseInt(json.getString("x")));
            photo.setHeight(Integer.parseInt(json.getString("y")));
            photo.setInvalid(false);
            photoDao.updateInTx(photo);

            return photo;
        } catch (JSONException e) {
            throw new ApiCallException(ApiErrorCode.JSON_ERROR, e);
        }
    }

    public JSONObject getPhotoRaw(Gallery gallery, Photo photo) throws ApiCallException {
        try {
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

            JSONObject result = callApi("showpage", json);

            L.d("Show page callback: %s", result.toString());

            return result;
        } catch (JSONException e) {
            throw new ApiCallException(ApiErrorCode.JSON_ERROR, e);
        }
    }

    public String getShowkey(Gallery gallery) throws ApiCallException {
        long galleryId = gallery.getId();
        QueryBuilder<Photo> qb = photoDao.queryBuilder();
        qb.where(PhotoDao.Properties.GalleryId.eq(galleryId)).limit(1);
        List<Photo> list = qb.list();
        Photo photo = list.get(0);

        if (photo == null) {
            throw new ApiCallException(ApiErrorCode.PHOTO_DATA_REQUIRED);
        }

        try {
            String url = photo.getUrl(isLoggedIn());

            L.d("Get show key: %s", url);

            HttpGet httpGet = new HttpGet(url);
            HttpResponse response = getHttpResponse(httpGet);
            String content = HttpRequestHelper.readResponse(response);

            L.d("Get show key callback: %s", content);

            if (content.contains("This gallery is pining for the fjords")) {
                throw new ApiCallException(ApiErrorCode.GALLERY_PINNED, url, response);
            } else if (content.equals("Invalid page.")) {
                list = getPhotoList(galleryId, photo.getPage() / Constant.PHOTO_PER_PAGE);
                photo = list.get(0);
                httpGet = new HttpGet(photo.getUrl(isLoggedIn()));
                response = getHttpResponse(httpGet);
                content = HttpRequestHelper.readResponse(response);

                L.d("Get show key callback (retry): %s", content);

                if (content.equals("Invalid page.")) {
                    throw new ApiCallException(ApiErrorCode.SHOWKEY_EXPIRED, url, response);
                }
            }

            Matcher matcher = pShowkey.matcher(content);
            String showkey = "";

            while (matcher.find()) {
                showkey = matcher.group(1);
            }

            if (showkey.isEmpty()) {
                throw new ApiCallException(ApiErrorCode.SHOWKEY_NOT_FOUND, url, response);
            } else {
                L.d("Show key found: %s", showkey);
                gallery.setShowkey(showkey);
                galleryDao.updateInTx(gallery);

                return showkey;
            }
        } catch (IOException e) {
            throw new ApiCallException(ApiErrorCode.IO_ERROR, e);
        }
    }

    public List<Gallery> getGalleryIndex(String base) throws ApiCallException {
        return getGalleryIndex(base, 0);
    }

    public List<Gallery> getGalleryIndex(String base, int page) throws ApiCallException {
        String url = getGalleryIndexUrl(base, page);

        L.d("Get gallery index: %s", url);

        try {
            HttpGet httpGet = new HttpGet(url);
            HttpResponse response = getHttpResponse(httpGet);
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

            return getGalleryList(gidlist);
        } catch (IOException e) {
            throw new ApiCallException(ApiErrorCode.IO_ERROR, e);
        }
    }

    public String getGalleryIndexUrl(String base, int page) {
        Uri.Builder builder = Uri.parse(base).buildUpon();
        builder.appendQueryParameter("page", Integer.toString(page));

        return builder.build().toString();
    }

    public List<Gallery> getGalleryList(JSONArray gidlist) throws ApiCallException {
        List<Gallery> galleryList = new ArrayList<Gallery>();

        if (gidlist.length() == 0) return galleryList;

        try {
            JSONObject obj = new JSONObject();
            obj.put("gidlist", gidlist);

            JSONObject json = callApi("gdata", obj);

            L.d("Get gallery list callback: %s", json.toString());

            JSONArray gmetadata = json.getJSONArray("gmetadata");

            for (int i = 0, len = gmetadata.length(); i < len; i++) {
                JSONObject data = gmetadata.getJSONObject(i);
                long id = data.getLong("gid");

                if (data.getBoolean("expunged")) continue;

                if (data.has("error")) {
                    String error = data.getString("token");

                    if (error.equals("Key missing, or incorrect key provided.")) {
                        throw new ApiCallException(ApiErrorCode.TOKEN_INVALID);
                    } else {
                        throw new ApiCallException(ApiErrorCode.API_ERROR, error);
                    }
                }

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
        } catch (JSONException e) {
            throw new ApiCallException(ApiErrorCode.JSON_ERROR, e);
        }
    }

    public Gallery getGallery(long id, String token) throws ApiCallException {
        JSONArray gidlist = new JSONArray();
        JSONArray arr = new JSONArray();

        L.d("Get gallery info: {id: %d, token: %s}", id, token);

        arr.put(id);
        arr.put(token);
        gidlist.put(arr);

        List<Gallery> galleryList = getGalleryList(gidlist);

        if (galleryList == null) {
            return null;
        } else {
            return galleryList.get(0);
        }
    }

    public JSONArray getGalleryTokenList(JSONArray pagelist) throws ApiCallException {
        try {
            JSONObject obj = new JSONObject();
            obj.put("pagelist", pagelist);

            JSONObject json = callApi("gtoken", obj);

            L.d("Get gallery token callback: %s", json.toString());

            if (json == null) {
                throw new ApiCallException(ApiErrorCode.TOKEN_NOT_FOUND);
            } else {
                if (json.has("tokenlist")) {
                    return json.getJSONArray("tokenlist");
                } else {
                    throw new ApiCallException(ApiErrorCode.TOKEN_NOT_FOUND);
                }
            }
        } catch (JSONException e) {
            throw new ApiCallException(ApiErrorCode.JSON_ERROR, e);
        }
    }

    public String getGalleryToken(long id, String photoToken, int page) throws ApiCallException {
        try {
            JSONArray pagelist = new JSONArray();
            JSONArray arr = new JSONArray();

            arr.put(id);
            arr.put(photoToken);
            arr.put(page);
            pagelist.put(arr);

            JSONArray tokenlist = getGalleryTokenList(pagelist);
            JSONObject tokenObj = tokenlist.getJSONObject(0);

            if (tokenObj == null) {
                throw new ApiCallException(ApiErrorCode.TOKEN_NOT_FOUND);
            } else {
                if (tokenObj.has("token")) {
                    return tokenObj.getString("token");
                } else if (tokenObj.has("error")) {
                    String error = tokenObj.getString("error");

                    if (error.equals("Invalid page.")) {
                        throw new ApiCallException(ApiErrorCode.TOKEN_OR_PAGE_INVALID);
                    } else {
                        throw new ApiCallException(ApiErrorCode.TOKEN_NOT_FOUND, error);
                    }
                } else {
                    throw new ApiCallException(ApiErrorCode.TOKEN_NOT_FOUND);
                }
            }
        } catch (JSONException e) {
            throw new ApiCallException(ApiErrorCode.JSON_ERROR, e);
        }
    }

    public Gallery getGalleryByPhotoInfo(long id, String photoToken, int page) throws ApiCallException {
        String token = getGalleryToken(id, photoToken, page);

        if (token == null || token.isEmpty()) {
            throw new ApiCallException(ApiErrorCode.TOKEN_NOT_FOUND);
        }

        return getGallery(id, token);
    }
}
