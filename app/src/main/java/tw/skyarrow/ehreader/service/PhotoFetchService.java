package tw.skyarrow.ehreader.service;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Observable;
import tw.skyarrow.ehreader.api.API;
import tw.skyarrow.ehreader.api.APIService;
import tw.skyarrow.ehreader.model.DaoSession;
import tw.skyarrow.ehreader.model.Gallery;
import tw.skyarrow.ehreader.model.GalleryDao;
import tw.skyarrow.ehreader.model.Photo;
import tw.skyarrow.ehreader.model.PhotoDao;
import tw.skyarrow.ehreader.util.DatabaseHelper;
import tw.skyarrow.ehreader.util.L;
import tw.skyarrow.ehreader.util.RxBus;

/**
 * Created by SkyArrow on 2015/9/26.
 */
public class PhotoFetchService extends PriorityQueueService {
    private static final String TAG = PhotoFetchService.class.getSimpleName();

    private static final String GALLERY_ID = "GALLERY_ID";
    private static final String PHOTO_PAGE = "PHOTO_PAGE";

    private static final Pattern pPhotoUrl = Pattern.compile("http://(?:g\\.e-|ex)hentai\\.org/s/(\\w+?)/(\\d+)-(\\d+)");
    private static final Pattern pPhotoVar = Pattern.compile("var +(.+?) *= *(.+?);");
    private static final Pattern pImageSrc = Pattern.compile("<img id=\"img\" src=\"([^\"]+)\"");

    private APIService api;
    private DatabaseHelper dbHelper;
    private GalleryDao galleryDao;
    private PhotoDao photoDao;
    private static final RxBus<Photo> bus = new RxBus<>();

    public static Intent intent(Context context, long galleryId, int photoPage){
        Intent intent = new Intent(context, PhotoFetchService.class);

        intent.putExtra(GALLERY_ID, galleryId);
        intent.putExtra(PHOTO_PAGE, photoPage);

        return intent;
    }

    public PhotoFetchService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        api = API.getService(this);
        dbHelper = DatabaseHelper.get(this);
        DaoSession daoSession = dbHelper.open();
        galleryDao = daoSession.getGalleryDao();
        photoDao = daoSession.getPhotoDao();
    }

    @Override
    public void onHandleIntent(Intent intent) {
        long galleryId = intent.getLongExtra(GALLERY_ID, 0);
        int photoPage = intent.getIntExtra(PHOTO_PAGE, 0);

        if (galleryId == 0 || photoPage == 0) return;

        L.d("onHandleIntent: %d - %d", galleryId, photoPage);

        Photo photo = findOrCreatePhoto(galleryId, photoPage);
        Observable<Photo> observable = Observable.just(photo);

        if (!photo.shouldReload()){
            L.d("Photo don't need to be loaded: %d - %d", galleryId, photoPage);
            return;
        }

        if (TextUtils.isEmpty(photo.getToken())){
            observable = getPhotoIndex(galleryId, photoPage / 40)
                    .filter(p -> p.getGalleryId() == galleryId && p.getPage() == photoPage)
                    .first();
        }

        try {
            Future<Photo> future = observable.flatMap(this::getPhotoPage)
                    .toBlocking()
                    .toFuture();

            bus.send(future.get(15, TimeUnit.SECONDS));
        } catch (InterruptedException|ExecutionException|TimeoutException e){
            L.e(e);
        }
    }

    @Override
    public void onDestroy() {
        L.d("onDestroy");

        dbHelper.close();
        super.onDestroy();
    }

    private Observable<Photo> getPhotoIndex(long galleryId, int galleryPage){
        Gallery gallery = galleryDao.load(galleryId);

        L.d("getPhotoIndex: %d - %d", galleryId, galleryPage);

        return api.getGalleryPage(galleryId, gallery.getToken(), galleryPage)
                .flatMap(html -> {
                    Matcher matcher = pPhotoUrl.matcher(html);
                    List<Photo> photoList = new ArrayList<>();

                    while (matcher.find()){
                        String token = matcher.group(1);
                        int photoPage = Integer.parseInt(matcher.group(3), 10);
                        Photo p = findOrCreatePhoto(galleryId, photoPage);

                        p.setToken(token);
                        photoDao.insertOrReplaceInTx(p);
                        photoList.add(p);
                    }

                    return Observable.from(photoList);
                });
    }

    private Observable<Photo> getPhotoPage(final Photo photo){
        L.d("getPhotoPage: %d - %s - %d", photo.getGalleryId(), photo.getToken(), photo.getPage());

        return api.getPhotoPage(photo.getGalleryId(), photo.getToken(), photo.getPage())
                .map(html -> {
                    Matcher matcher = pPhotoVar.matcher(html);

                    while (matcher.find()) {
                        String key = matcher.group(1);
                        String value = matcher.group(2);

                        if (TextUtils.equals(key, "si")) {
                            photo.setRetryId(value);
                        } else if (TextUtils.equals(key, "x")) {
                            photo.setWidth(Integer.parseInt(value, 10));
                        } else if (TextUtils.equals(key, "y")) {
                            photo.setHeight(Integer.parseInt(value, 10));
                        }
                    }

                    matcher = pImageSrc.matcher(html);

                    if (matcher.find()) {
                        try {
                            photo.setSrc(URLDecoder.decode(matcher.group(1), "UTF-8"));
                        } catch (UnsupportedEncodingException e) {
                            L.e(e, "Unable to find photo src: %d - %s - %d", photo.getGalleryId(), photo.getToken(), photo.getPage());
                        }
                    }

                    photoDao.insertOrReplaceInTx(photo);
                    return photo;
                });

    }

    public static Observable<Photo> getBus(){
        return bus.tObservable();
    }

    private Photo findOrCreatePhoto(long galleryId, int photoPage){
        Photo photo = Photo.findPhoto(photoDao, galleryId, photoPage);

        if (photo == null){
            photo = new Photo();
            photo.setDefaultFields();
            photo.setGalleryId(galleryId);
            photo.setPage(photoPage);
        }

        return photo;
    }
}
