package tw.skyarrow.ehreader.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import rx.Observable;
import rx.Subscription;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.app.entry.EntryActivity;
import tw.skyarrow.ehreader.app.gallery.GalleryActivity;
import tw.skyarrow.ehreader.model.DaoSession;
import tw.skyarrow.ehreader.model.Download;
import tw.skyarrow.ehreader.model.DownloadDao;
import tw.skyarrow.ehreader.model.Gallery;
import tw.skyarrow.ehreader.model.GalleryDao;
import tw.skyarrow.ehreader.model.Photo;
import tw.skyarrow.ehreader.model.PhotoDao;
import tw.skyarrow.ehreader.util.DatabaseHelper;
import tw.skyarrow.ehreader.util.L;
import tw.skyarrow.ehreader.util.RxBus;

/**
 * Created by SkyArrow on 2015/9/27.
 */
public class GalleryDownloadService extends PriorityQueueService {
    private static final String TAG = GalleryDownloadService.class.getSimpleName();

    private static final String GALLERY_ID = "GALLERY_ID";

    private DatabaseHelper dbHelper;
    private GalleryDao galleryDao;
    private PhotoDao photoDao;
    private DownloadDao downloadDao;
    private OkHttpClient client;
    private static final RxBus<Download> bus = new RxBus<>();
    private Subscription subscription;

    public static Intent intent(Context context, long galleryId){
        Intent intent = new Intent(context, GalleryDownloadService.class);

        intent.putExtra(GALLERY_ID, galleryId);

        return intent;
    }

    public GalleryDownloadService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        dbHelper = DatabaseHelper.get(this);
        DaoSession daoSession = dbHelper.open();
        galleryDao = daoSession.getGalleryDao();
        photoDao = daoSession.getPhotoDao();
        downloadDao = daoSession.getDownloadDao();
        client = new OkHttpClient();

        setupNotificationSubscription();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        long galleryId = intent.getLongExtra(GALLERY_ID, 0);
        Download download = findOrCreateDownload(galleryId);
        download.setStatus(Download.STATUS_PENDING);
        downloadDao.updateInTx(download);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onHandleIntent(Intent intent) {
        long galleryId = intent.getLongExtra(GALLERY_ID, 0);
        if (galleryId == 0) return;

        L.d("onHandleIntent: %d", galleryId);

        Gallery gallery = galleryDao.load(galleryId);
        Download download = findOrCreateDownload(galleryId);
        download.setStatus(Download.STATUS_DOWNLOADING);
        downloadDao.updateInTx(download);
        bus.send(download);

        try {
            for (int i = 1; i <= gallery.getCount(); i++){
                Photo photo = getPhoto(download, i);
                downloadPhoto(photo);
                download.setProgress(i);
                downloadDao.updateInTx(download);
                bus.send(download);
            }

            download.setStatus(Download.STATUS_SUCCESS);
            downloadDao.updateInTx(download);
            bus.send(download);
        } catch (IOException e) {
            L.e(e, "Download failed: %d", galleryId);
            download.setStatus(Download.STATUS_ERROR);
            downloadDao.updateInTx(download);
            bus.send(download);
        }
    }

    @Override
    public void onDestroy() {
        L.d("onDestroy");

        subscription.unsubscribe();
        dbHelper.close();
        super.onDestroy();
    }

    public static Observable<Download> getBus(){
        return bus.tObservable();
    }

    private Download findOrCreateDownload(long galleryId){
        Download download = downloadDao.load(galleryId);

        if (download == null){
            download = new Download();
            download.setDefaultFields();
            download.setId(galleryId);
            downloadDao.insertInTx(download);
        }

        return download;
    }

    private Photo getPhoto(Download download, int page){
        long galleryId = download.getId();
        Photo photo = Photo.findPhoto(photoDao, galleryId, page);

        L.d("getPhoto: %d - %d", galleryId, page);

        if (photo == null || photo.shouldReload()){
            L.d("Call PhotoFetchService to load photo: %d - %d", galleryId, page);

            Intent intent = PhotoFetchService.intent(this, galleryId, page);
            startService(intent);
            photo = PhotoFetchService.getBus()
                    .filter(p -> p.getGalleryId() == galleryId && p.getPage() == page)
                    .toBlocking()
                    .first();
        }

        return photo;
    }

    private void downloadPhoto(Photo photo) throws IOException {
        L.d("downloadPhoto: %d - %d", photo.getGalleryId(), photo.getPage());

        File file = photo.getFile(this);

        if (photo.getDownloaded() != null && photo.getDownloaded() && file.exists()) {
            L.d("Skip download photo: %d - %d", photo.getGalleryId(), photo.getPage());
            return;
        }

        File parentDir = file.getParentFile();

        if (!parentDir.exists()){
            parentDir.mkdirs();
        }

        L.d("Download from %s to %s", photo.getSrc(), file.getAbsolutePath());

        Request request = new Request.Builder()
                .url(photo.getSrc())
                .build();

        Response response = client.newCall(request).execute();
        InputStream input = response.body().byteStream();
        OutputStream output = new FileOutputStream(file);
        byte[] buf = new byte[2048];
        int len;

        while ((len = input.read(buf)) > 0){
            output.write(buf, 0, len);
        }

        input.close();
        output.close();

        photo.setDownloaded(true);
        photoDao.updateInTx(photo);
    }

    private void setupNotificationSubscription(){
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        subscription = getBus()
                .subscribe(download -> {
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                    PendingIntent pendingIntent = null;
                    Gallery gallery = galleryDao.load(download.getId());
                    Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

                    builder.setSmallIcon(R.drawable.notification_download)
                            .setLargeIcon(icon);

                    switch (download.getStatus()) {
                        case Download.STATUS_DOWNLOADING:
                            int progress = download.getProgress();
                            int total = gallery.getCount();

                            builder.setContentTitle(getString(R.string.download_in_progress))
                                    .setTicker(getString(R.string.download_in_progress))
                                    .setProgress(total, progress, false)
                                    .setContentText(String.format("%d / %d", progress, total));
                            break;

                        case Download.STATUS_PENDING:
                            builder.setContentTitle(getString(R.string.download_in_progress))
                                    .setTicker(getString(R.string.download_in_progress))
                                    .setContentText(getString(R.string.download_pending));
                            break;

                        case Download.STATUS_SUCCESS:
                            Intent intent = GalleryActivity.intent(this, gallery.getId());

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
                                pendingIntent = TaskStackBuilder.create(this)
                                        .addNextIntentWithParentStack(intent)
                                        .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                            } else {
                                pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                            }

                            builder.setContentTitle(getString(R.string.download_success))
                                    .setTicker(getString(R.string.download_success))
                                    .setContentText(gallery.getTitle())
                                    .setAutoCancel(true);
                            break;

                        case Download.STATUS_PAUSED:
                            builder.setContentTitle(getString(R.string.download_paused))
                                    .setTicker(getString(R.string.download_paused))
                                    .setAutoCancel(true)
                                    .setContentText(gallery.getTitle());
                            break;

                        case Download.STATUS_ERROR:
                            builder.setContentTitle(getString(R.string.download_failed))
                                    .setTicker(getString(R.string.download_failed))
                                    .setAutoCancel(true)
                                    .setContentText(gallery.getTitle());
                            break;

                        default:
                            return;
                    }

                    if (pendingIntent == null){
                        Intent intent = EntryActivity.intent(this, EntryActivity.TAB_DOWNLOAD);
                        pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    }

                    builder.setContentIntent(pendingIntent);
                    manager.notify(TAG, download.getId().intValue(), builder.build());
                });
    }
}
