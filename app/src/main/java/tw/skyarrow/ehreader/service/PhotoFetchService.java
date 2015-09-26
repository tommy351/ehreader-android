package tw.skyarrow.ehreader.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import tw.skyarrow.ehreader.api.API;
import tw.skyarrow.ehreader.api.APIService;
import tw.skyarrow.ehreader.model.DaoSession;
import tw.skyarrow.ehreader.model.Gallery;
import tw.skyarrow.ehreader.model.GalleryDao;
import tw.skyarrow.ehreader.model.Photo;
import tw.skyarrow.ehreader.model.PhotoDao;
import tw.skyarrow.ehreader.util.DatabaseHelper;
import tw.skyarrow.ehreader.util.RxBus;

/**
 * Created by SkyArrow on 2015/9/26.
 */
public class PhotoFetchService extends Service {
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
    private Subscription subscription;
    private BehaviorSubject<Command> commandSubject = BehaviorSubject.create();
    private static final RxBus<Photo> photoBus = new RxBus<>();
    private Map<GalleryPage, Observable<Photo>> galleryPageMap;

    public static Intent intent(Context context, long galleryId, int photoPage){
        Intent intent = new Intent(context, PhotoFetchService.class);

        intent.putExtra(GALLERY_ID, galleryId);
        intent.putExtra(PHOTO_PAGE, photoPage);

        return intent;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        api = API.getService(this);
        dbHelper = DatabaseHelper.get(this);
        DaoSession daoSession = dbHelper.open();
        galleryDao = daoSession.getGalleryDao();
        photoDao = daoSession.getPhotoDao();
        galleryPageMap = new HashMap<>();

        subscription = commandSubject
                .filter(command -> command.galleryId > 0 && command.photoPage > 0)
                .distinct()
                .flatMap(this::getPhoto)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        photoBus::send,
                        e -> e.printStackTrace());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        long galleryId = intent.getLongExtra(GALLERY_ID, 0);
        int photoPage = intent.getIntExtra(PHOTO_PAGE, 0);

        Log.d(TAG, String.format("onStartCommand: %d - %d", galleryId, photoPage));

        commandSubject.onNext(new Command(galleryId, photoPage));

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        subscription.unsubscribe();
        dbHelper.close();
        super.onDestroy();
    }

    public static Observable<Photo> getPhotoBus(){
        return photoBus.tObservable();
    }

    private Observable<Photo> getPhoto(Command command){
        long galleryId = command.galleryId;
        int photoPage = command.photoPage;
        Photo photo = findOrCreatePhoto(galleryId, photoPage);
        Observable<Photo> observable = Observable.just(photo);

        if (!photo.shouldReload()){
            return observable;
        }

        if (TextUtils.isEmpty(photo.getToken())){
            GalleryPage galleryPage = new GalleryPage(galleryId, photoPage / 40);

            if (galleryPageMap.containsKey(galleryPage)){
                observable = galleryPageMap.get(galleryPage);
            } else {
                observable = getPhotoIndex(galleryPage);
                galleryPageMap.put(galleryPage, observable);
            }
        }

        return observable
                .filter(p -> p.getGalleryId() == galleryId && p.getPage() == photoPage)
                .first()
                .flatMap(this::getPhotoPage);
    }

    private Observable<Photo> getPhotoIndex(GalleryPage galleryPage){
        Gallery gallery = galleryDao.load(galleryPage.galleryId);

        Log.d(TAG, String.format("getPhotoIndex: %d - %d", galleryPage.galleryId, galleryPage.page));

        return api.getGalleryPage(galleryPage.galleryId, gallery.getToken(), galleryPage.page)
                .flatMap(html -> {
                    Matcher matcher = pPhotoUrl.matcher(html);
                    List<Photo> photoList = new ArrayList<>();

                    while (matcher.find()){
                        String token = matcher.group(1);
                        long galleryId = Long.parseLong(matcher.group(2), 10);
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
        Log.d(TAG, String.format("getPhotoPage: %d - %s - %d", photo.getGalleryId(), photo.getToken(), photo.getPage()));

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
                            e.printStackTrace();
                        }
                    }

                    photoDao.insertOrReplaceInTx(photo);
                    return photo;
                });

    }

    private Photo findOrCreatePhoto(long galleryId, int photoPage){
        Photo photo = Photo.findPhoto(photoDao, galleryId, photoPage);

        if (photo == null){
            photo = new Photo();
            photo.setGalleryId(galleryId);
            photo.setPage(photoPage);
        }

        return photo;
    }

    private static class Command {
        public final long galleryId;
        public final int photoPage;

        public Command(long galleryId, int photoPage){
            this.galleryId = galleryId;
            this.photoPage = photoPage;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Command command = (Command) o;

            if (galleryId != command.galleryId) return false;
            return photoPage == command.photoPage;

        }
    }

    private static class GalleryPage {
        public final long galleryId;
        public final int page;

        public GalleryPage(long galleryId, int page) {
            this.galleryId = galleryId;
            this.page = page;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            GalleryPage that = (GalleryPage) o;

            if (galleryId != that.galleryId) return false;
            return page == that.page;
        }
    }
}
