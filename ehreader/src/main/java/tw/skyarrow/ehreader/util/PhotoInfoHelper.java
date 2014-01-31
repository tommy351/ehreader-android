package tw.skyarrow.ehreader.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.greenrobot.dao.query.QueryBuilder;
import tw.skyarrow.ehreader.Constant;
import tw.skyarrow.ehreader.db.DaoMaster;
import tw.skyarrow.ehreader.db.DaoSession;
import tw.skyarrow.ehreader.db.Gallery;
import tw.skyarrow.ehreader.db.GalleryDao;
import tw.skyarrow.ehreader.db.Photo;
import tw.skyarrow.ehreader.db.PhotoDao;

/**
 * Created by SkyArrow on 2014/1/30.
 */
public class PhotoInfoHelper {
    private static final Pattern pPhotoUrl = Pattern.compile("http://(g.e-|ex)hentai.org/s/(\\w+?)/(\\d+)-(\\d+)");
    private static final Pattern pShowkey = Pattern.compile("var showkey.*=.*\"([\\w-]+?)\";");
    private static final Pattern pImageSrc = Pattern.compile("<img id=\"img\" src=\"(.+)/(.+?)\"");

    private Context context;

    private SQLiteDatabase db;
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private GalleryDao galleryDao;
    private PhotoDao photoDao;

    public PhotoInfoHelper(Context context) {
        this.context = context;

        initDb();
    }

    private void initDb() {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, Constant.DB_NAME, null);
        db = helper.getWritableDatabase();
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
        galleryDao = daoSession.getGalleryDao();
        photoDao = daoSession.getPhotoDao();

        QueryBuilder.LOG_SQL = true;
        QueryBuilder.LOG_VALUES = true;
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
        List<Photo> list = new ArrayList<Photo>();
        long galleryId = gallery.getId();
        String content = HttpRequestHelper.getString(UriHelper.getGalleryUrlString(gallery, page));
        Matcher matcher = pPhotoUrl.matcher(content);

        while (matcher.find()) {
            Photo photo = new Photo();
            String token = matcher.group(2);
            int photoPage = Integer.parseInt(matcher.group(4));

            L.v("Photo found: {galleryId: %d, token: %s, page: %d}", galleryId, token, photoPage);

            photo.setGalleryId(galleryId);
            photo.setToken(matcher.group(2));
            photo.setPage(photoPage);
            list.add(photo);

            QueryBuilder qb = photoDao.queryBuilder();
            qb.where(qb.and(PhotoDao.Properties.GalleryId.eq(galleryId), PhotoDao.Properties.Page.eq(photoPage)));
            long count = qb.count();

            L.v("qb count: %d", count);

            // TODO Error: Cannot update entity without key - was it inserted before?
            if (count > 0) {
                photoDao.updateInTx(photo);
            } else {
                photo.setDownloaded(false);
                photo.setBookmarked(false);

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
        String showkey = gallery.getShowkey();

        if (showkey == null || showkey.isEmpty()) {
            showkey = getShowkey(gallery);
        }

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(Constant.API_URL);
        JSONObject json = new JSONObject();

        json.put("method", "showpage");
        json.put("gid", gallery.getId());
        json.put("page", photo.getPage());
        json.put("imgkey", photo.getToken());
        json.put("showkey", showkey);

        L.v("Show page request: %s", json.toString());

        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setEntity(GalleryAjaxCallback.jsonToEntity(json));

        HttpResponse response = httpClient.execute(httpPost);
        String content = HttpRequestHelper.readResponse(response);
        JSONObject result = new JSONObject(content);

        L.v("Show page callback: %s", content);

        if (result.has("error")) {
            String error = result.getString("error");

            L.e("Show page callback error: %s", error);

            if (error.equals("Key mismatch")) {
                getShowkey(gallery);
                getPhotoInfo(gallery, photo);
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
        photoDao.updateInTx(photo);

        return photo;
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

        String content = HttpRequestHelper.getString(UriHelper.getPhotoUrlString(photo));
        Matcher matcher = pShowkey.matcher(content);
        String showkey = "";

        while (matcher.find()) {
            showkey = matcher.group(1);
        }

        if (!showkey.isEmpty()) {
            gallery.setShowkey(showkey);
            galleryDao.updateInTx(gallery);
        }

        return showkey;
    }
}
