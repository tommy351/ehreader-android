package tw.skyarrow.ehreader.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NotificationCompat;

import com.nostra13.universalimageloader.cache.disc.DiscCacheAware;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.DiscCacheUtil;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.dao.query.QueryBuilder;
import de.greenrobot.event.EventBus;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.api.DataLoader;
import tw.skyarrow.ehreader.app.gallery.GalleryActivity;
import tw.skyarrow.ehreader.app.main.MainActivity;
import tw.skyarrow.ehreader.db.DaoMaster;
import tw.skyarrow.ehreader.db.DaoSession;
import tw.skyarrow.ehreader.db.Download;
import tw.skyarrow.ehreader.db.DownloadDao;
import tw.skyarrow.ehreader.db.Gallery;
import tw.skyarrow.ehreader.db.GalleryDao;
import tw.skyarrow.ehreader.db.Photo;
import tw.skyarrow.ehreader.db.PhotoDao;
import tw.skyarrow.ehreader.event.GalleryDownloadEvent;
import tw.skyarrow.ehreader.util.DatabaseHelper;
import tw.skyarrow.ehreader.util.L;

/**
 * Created by SkyArrow on 2014/2/4.
 */
public class GalleryDownloadService extends Service {
    public static final String TAG = "GalleryDownloadService";

    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_RETRY = "ACTION_RETRY";

    public static final String EXTRA_GALLERY = "galleryId";

    public static final int EVENT_DOWNLOADING = 0;
    public static final int EVENT_PAUSED = 1;
    public static final int EVENT_SUCCESS = 2;
    public static final int EVENT_PENDING = 3;
    public static final int EVENT_ERROR = 4;
    public static final int EVENT_SERVICE_START = 10;
    public static final int EVENT_SERVICE_STOP = 11;

    private static final int MAX_RETRY = 2;
    private static final int GLOBAL_NOTIFICATION_ID = 0;

    private volatile Looper serviceLooper;
    private volatile ServiceHandler serviceHandler;
    private Map<Long, Download> pendingDownloads;
    private DownloadTask downloadTask;

    private GalleryDao galleryDao;
    private PhotoDao photoDao;
    private DownloadDao downloadDao;
    private EventBus bus;
    private DataLoader dataLoader;
    private DiscCacheAware discCache;

    private NotificationCompat.Builder notification;
    private NotificationManager nm;
    private int notificationCount = 0;

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            L.i("Download %d is started", msg.what);
            downloadTask = new DownloadTask(msg.what);
            downloadTask.start();
            stopSelf(msg.arg1);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread thread = new HandlerThread(TAG);
        thread.start();

        L.i("%s is created", TAG);

        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);
        pendingDownloads = new HashMap<Long, Download>();

        DatabaseHelper helper = DatabaseHelper.getInstance(this);
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();
        galleryDao = daoSession.getGalleryDao();
        photoDao = daoSession.getPhotoDao();
        downloadDao = daoSession.getDownloadDao();
        bus = EventBus.getDefault();
        dataLoader = DataLoader.getInstance(this);
        discCache = ImageLoader.getInstance().getDiscCache();

        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        buildNotification();
        bus.post(new GalleryDownloadEvent(EVENT_SERVICE_START, null));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();

        if (action == null) {
            L.e("Intent action is required!");
        } else if (action.equals(ACTION_START)) {
            handleStartAction(intent, startId);
        } else if (action.equals(ACTION_PAUSE)) {
            handlePauseAction(intent, startId);
        } else if (action.equals(ACTION_RETRY)) {
            handleRetryAction(intent, startId);
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        boolean hasPendings = downloadTask != null || pendingDownloads.size() > 0;

        if (downloadTask != null) {
            downloadTask.pause();
        }

        for (Long id : pendingDownloads.keySet()) {
            pauseDownload(id);
        }

        if (hasPendings) {
            notification.setAutoCancel(true)
                    .setContentText(getString(R.string.download_paused))
                    .setTicker(getString(R.string.download_paused))
                    .setProgress(0, 0, false);

            sendNotification();
        } else {
            nm.cancel(TAG, GLOBAL_NOTIFICATION_ID);
        }

        bus.post(new GalleryDownloadEvent(EVENT_SERVICE_STOP, null));
        serviceLooper.quit();

        L.d("%s is destroyed", TAG);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void handleStartAction(Intent intent, int startId) {
        long id = intent.getLongExtra(EXTRA_GALLERY, 0);

        if (pendingDownloads.containsKey(id)) {
            L.e("Download %d is downloading or pending", id);
            return;
        }

        Download download = downloadDao.load(id);

        if (download == null) {
            Gallery gallery = galleryDao.load(id);

            if (gallery == null) {
                L.e("Gallery %d does not exist.", id);
                return;
            }

            download = new Download();
            download.setId(id);
            download.setCreated(new Date(System.currentTimeMillis()));
            download.setProgress(0);
            downloadDao.insertInTx(download);

            if (!gallery.getStarred()) {
                gallery.setStarred(true);
                galleryDao.updateInTx(gallery);
            }
        }

        addDownload(download, startId);
    }

    private void handlePauseAction(Intent intent, int startId) {
        long id = intent.getLongExtra(EXTRA_GALLERY, 0);

        if (downloadTask != null) {
            if (downloadTask.getId() == id) {
                downloadTask.pause();
            }
        } else {
            pauseDownload(id);
        }

        stopSelf(startId);
    }

    private void handleRetryAction(Intent intent, int startId) {
        long id = intent.getLongExtra(EXTRA_GALLERY, 0);

        if (pendingDownloads.containsKey(id)) {
            L.e("Download %d is pending or downloading", id);
            return;
        }

        Download download = downloadDao.load(id);

        if (download == null) {
            handleStartAction(intent, startId);
        } else {
            QueryBuilder<Photo> qb = photoDao.queryBuilder();
            qb.where(PhotoDao.Properties.GalleryId.eq(id));
            List<Photo> photoList = qb.list();

            for (Photo photo : photoList) {
                photo.setDownloaded(false);
                photoDao.updateInTx(photo);
            }

            download.setProgress(0);
            downloadDao.updateInTx(download);
            addDownload(download, startId);
        }
    }

    private void addDownload(Download download, int startId) {
        Message msg = serviceHandler.obtainMessage();
        msg.what = download.getId().intValue();
        msg.arg1 = startId;

        L.i("Download %d is added to the queue", download.getId());

        download.setStatus(Download.STATUS_PENDING);
        downloadDao.updateInTx(download);
        pendingDownloads.put(download.getId(), download);
        serviceHandler.sendMessage(msg);
        bus.post(new GalleryDownloadEvent(EVENT_PENDING, download));
        updateNotificationCount(notificationCount + 1);
    }

    private void pauseDownload(long id) {
        serviceHandler.removeMessages((int) id);

        if (pendingDownloads.containsKey(id)) {
            Download download = pendingDownloads.get(id);

            download.setStatus(Download.STATUS_PAUSED);
            downloadDao.updateInTx(download);
            pendingDownloads.remove(id);
            bus.post(new GalleryDownloadEvent(EVENT_PAUSED, download));
            updateNotificationCount(notificationCount - 1);
        }

        L.i("Download %d is paused", id);
    }

    private void buildNotification() {
        notification = new NotificationCompat.Builder(this);
        Intent intent = new Intent(this, MainActivity.class);
        Bundle args = new Bundle();

        args.putInt("tab", MainActivity.TAB_DOWNLOAD);
        intent.putExtras(args);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        notification.setContentTitle(getString(R.string.download_in_progress))
                .setTicker(getString(R.string.download_in_progress))
                .setSmallIcon(R.drawable.ic_notification_download)
                .setProgress(0, 0, true)
                .setContentText(getString(R.string.download_in_progress))
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true);
    }

    private void updateNotificationCount(int count) {
        notificationCount = count;
        notification.setNumber(count);
        sendNotification();
    }

    private void sendNotification() {
        nm.notify(TAG, GLOBAL_NOTIFICATION_ID, notification.build());
    }

    private final class DownloadTask {
        private long id;
        private Download download;
        private Gallery gallery;
        private int total;
        private File galleryFolder;
        private boolean isTerminated = false;
        private Photo cover;

        private DownloadTask(long id) {
            this.id = id;
        }

        public long getId() {
            return id;
        }

        public void start() {
            download = pendingDownloads.get(id);
            gallery = download.getGallery();
            total = gallery.getCount();
            galleryFolder = gallery.getFolder();

            if (!galleryFolder.exists()) galleryFolder.mkdirs();

            notification.setContentTitle(gallery.getTitles(GalleryDownloadService.this)[0])
                    .setProgress(0, 0, true)
                    .setContentText(getString(R.string.download_in_progress));

            sendNotification();
            setStatus(Download.STATUS_DOWNLOADING);
            sendEvent(EVENT_DOWNLOADING);

            for (int i = 1; i <= total; i++) {
                if (isTerminated) {
                    break;
                } else {
                    fetchPhoto(i);
                }
            }

            if (!isTerminated) success();
        }

        public void pause() {
            if (isTerminated) return;

            isTerminated = true;

            setStatus(Download.STATUS_PAUSED);
            sendEvent(EVENT_PAUSED);
            terminate();
        }

        private void success() {
            if (isTerminated) return;

            isTerminated = true;
            Intent intent = new Intent(GalleryDownloadService.this, GalleryActivity.class);
            Bundle args = new Bundle();
            PendingIntent pendingIntent;

            args.putLong("id", id);
            intent.putExtras(args);

            if (Build.VERSION.SDK_INT >= 16) {
                pendingIntent = TaskStackBuilder.create(GalleryDownloadService.this)
                        .addNextIntentWithParentStack(intent)
                        .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            } else {
                pendingIntent = PendingIntent.getActivity(GalleryDownloadService.this, 0,
                        intent, PendingIntent.FLAG_UPDATE_CURRENT);
            }


            NotificationCompat.Builder builder = new NotificationCompat.Builder(GalleryDownloadService.this);

            builder.setContentTitle(gallery.getTitles(GalleryDownloadService.this)[0])
                    .setTicker(getString(R.string.download_success))
                    .setContentText(getString(R.string.download_success))
                    .setSmallIcon(R.drawable.ic_notification_download)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            if (cover != null) {
                try {
                    File file = cover.getFile();

                    if (file.exists()) {
                        FileInputStream in = new FileInputStream(cover.getFile());
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = getSampleSize(cover.getWidth(), cover.getHeight());
                        Rect rect = new Rect(0, 0, 0, 0);
                        Bitmap bitmap = BitmapFactory.decodeStream(in, rect, options);

                        builder.setLargeIcon(getNotificationLargeIcon(bitmap));
                        bitmap.recycle();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            L.i("Download %d download success", id);

            setStatus(Download.STATUS_SUCCESS);
            sendEvent(EVENT_SUCCESS);
            nm.notify(TAG, (int) id, builder.build());
            terminate();
        }

        private void fail() {
            if (isTerminated) return;

            isTerminated = true;

            L.i("Download %d download failed", id);

            setStatus(Download.STATUS_ERROR);
            sendEvent(EVENT_ERROR);
            terminate();
        }

        private void terminate() {
            downloadTask = null;
            pendingDownloads.remove(id);
            updateNotificationCount(notificationCount - 1);
        }

        private void progress(int progress) {
            if (isTerminated) return;

            String progressText = String.format("%d / %d (%.2f%%)", progress, total, progress * 100f / total);

            notification.setProgress(total, progress, false)
                    .setContentText(progressText);

            download.setProgress(progress);
            downloadDao.updateInTx(download);

            L.i("Download %d progress %d / %d", id, progress, total);

            sendNotification();
            sendEvent(EVENT_DOWNLOADING);
        }

        private void fetchPhoto(int page) {
            Photo photo = null;
            boolean isSuccess = false;
            boolean skip = false;

            for (int i = 0; i < MAX_RETRY; i++) {
                try {
                    photo = dataLoader.getPhotoInfo(gallery, page);

                    if (page == 1) {
                        cover = photo;
                    }

                    if (photo.getDownloaded()) {
                        isSuccess = true;
                        skip = true;
                        break;
                    }

                    String src = photo.getSrc();
                    File dest = photo.getFile();
                    File cache = DiscCacheUtil.findInCache(src, discCache);

                    if (cache == null) {
                        HttpClient client = new DefaultHttpClient();
                        HttpGet httpGet = new HttpGet(src);
                        HttpResponse response = client.execute(httpGet);
                        int statusCode = response.getStatusLine().getStatusCode();

                        if (statusCode != 200) {
                            invalidatePhoto(photo);
                            continue;
                        }

                        HttpEntity entity = response.getEntity();
                        OutputStream out = new FileOutputStream(dest);

                        entity.writeTo(out);
                        out.close();
                    } else {
                        InputStream in = new FileInputStream(cache);
                        OutputStream out = new FileOutputStream(dest);
                        byte[] buf = new byte[1024];
                        int len;

                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }

                        in.close();
                        out.close();
                    }

                    isSuccess = true;
                    photo.setDownloaded(true);
                    photoDao.updateInTx(photo);
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    invalidatePhoto(photo);
                }
            }

            if (isSuccess) {
                if (!skip) progress(page);
            } else {
                fail();
            }
        }

        private void invalidatePhoto(Photo photo) {
            if (photo == null) return;

            photo.setInvalid(true);
            photoDao.updateInTx(photo);
        }

        private void setStatus(int status) {
            if (download.getStatus() == status) return;

            download.setStatus(status);
            downloadDao.updateInTx(download);
        }

        private void sendEvent(int event) {
            bus.post(new GalleryDownloadEvent(event, download));
        }

        private Bitmap getNotificationLargeIcon(Bitmap bitmap) {
            Resources res = getResources();
            int width = (int) res.getDimension(android.R.dimen.notification_large_icon_width);
            int height = (int) res.getDimension(android.R.dimen.notification_large_icon_height);

            return ThumbnailUtils.extractThumbnail(bitmap, width, height);
        }

        // http://developer.android.com/training/displaying-bitmaps/load-bitmap.html#load-bitmap
        private int getSampleSize(int width, int height) {
            Resources res = getResources();
            int reqWidth = (int) res.getDimension(android.R.dimen.notification_large_icon_width);
            int reqHeight = (int) res.getDimension(android.R.dimen.notification_large_icon_height);
            int scale = 1;

            if (height > reqHeight || width > reqWidth) {
                final int halfHeight = height / 2;
                final int halfWidth = width / 2;

                while ((halfHeight / scale) > reqHeight && (halfWidth / scale) > reqWidth) {
                    scale *= 2;
                }
            }

            return scale;
        }
    }
}
