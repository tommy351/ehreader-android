package tw.skyarrow.ehreader.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.support.v4.app.NotificationCompat;

import com.androidquery.AQuery;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;

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
import tw.skyarrow.ehreader.Constant;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.activity.GalleryActivity;
import tw.skyarrow.ehreader.activity.MainActivity;
import tw.skyarrow.ehreader.db.DaoMaster;
import tw.skyarrow.ehreader.db.DaoSession;
import tw.skyarrow.ehreader.db.Download;
import tw.skyarrow.ehreader.db.DownloadDao;
import tw.skyarrow.ehreader.db.Gallery;
import tw.skyarrow.ehreader.db.GalleryDao;
import tw.skyarrow.ehreader.db.Photo;
import tw.skyarrow.ehreader.db.PhotoDao;
import tw.skyarrow.ehreader.event.GalleryDownloadEvent;
import tw.skyarrow.ehreader.util.DownloadHelper;
import tw.skyarrow.ehreader.util.L;

/**
 * Created by SkyArrow on 2014/2/4.
 */
public class GalleryDownloadService extends Service {
    public static final String TAG = "GalleryDownloadService";

    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_RETRY = "ACTION_RETRY";
    public static final String ACTION_STOP = "ACTION_STOP";

    public static final int EVENT_DOWNLOADING = 0;
    public static final int EVENT_PAUSED = 1;
    public static final int EVENT_SUCCESS = 2;
    public static final int EVENT_PENDING = 3;
    public static final int EVENT_ERROR = 4;
    public static final int EVENT_SERVICE_START = 10;
    public static final int EVENT_SERVICE_STOP = 11;

    public static final String GALLERY_ID = "galleryId";
    public static final int MAX_RETRY = 2;

    private SQLiteDatabase db;
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private PhotoDao photoDao;
    private GalleryDao galleryDao;
    private DownloadDao downloadDao;

    private EventBus bus;

    private Handler handler;
    private DownloadHelper downloadHelper;
    private AQuery aq;
    private NotificationManager nm;
    private Map<Long, Download> downloadMap;
    private DownloadRunnable runnable;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        bus = EventBus.getDefault();

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, Constant.DB_NAME, null);
        db = helper.getWritableDatabase();
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
        galleryDao = daoSession.getGalleryDao();
        photoDao = daoSession.getPhotoDao();
        downloadDao = daoSession.getDownloadDao();

        downloadHelper = new DownloadHelper(this);
        aq = new AQuery(this);
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        downloadMap = new HashMap<Long, Download>();

        HandlerThread thread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        Looper looper = thread.getLooper();
        handler = new Handler(looper);
        bus.post(new GalleryDownloadEvent(EVENT_SERVICE_START, null));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();

        if (action == null) {
            L.e("Action is undefined.");
        } else if (action.equals(ACTION_START)) {
            startAction(intent, startId);
        } else if (action.equals(ACTION_PAUSE)) {
            pauseAction(intent, startId);
        } else if (action.equals(ACTION_RETRY)) {
            retryAction(intent, startId);
        } else if (action.equals(ACTION_STOP)) {
            stopAction(intent, startId);
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        bus.post(new GalleryDownloadEvent(EVENT_SERVICE_STOP, null));
        db.close();
        super.onDestroy();
    }

    private void startAction(Intent intent, int startId) {
        long id = intent.getLongExtra(GALLERY_ID, 0);

        if (downloadMap.containsKey(id)) {
            L.e("Download %d is already in the download queue.", id);
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
            download.setStatus(Download.STATUS_PENDING);
            download.setCreated(new Date(System.currentTimeMillis()));
            download.setProgress(0);
            downloadDao.insertInTx(download);
        } else {
            download.setStatus(Download.STATUS_PENDING);
            downloadDao.updateInTx(download);
        }

        addDownload(download, startId);
    }

    private void pauseAction(Intent intent, int startId) {
        long id = intent.getLongExtra(GALLERY_ID, 0);

        pauseDownload(id);
    }

    private void retryAction(Intent intent, int startId) {
        long id = intent.getLongExtra(GALLERY_ID, 0);

        if (downloadMap.containsKey(id)) {
            L.e("Download %d is already in the download queue.", id);
            return;
        }

        Download download = downloadDao.load(id);

        if (download == null) {
            L.e("Download %d does not exist.", id);
            return;
        }

        QueryBuilder qb = photoDao.queryBuilder();
        qb.where(qb.and(
                PhotoDao.Properties.GalleryId.eq(id),
                PhotoDao.Properties.Downloaded.notEq(true)
        ));
        List<Photo> photoList = qb.list();

        for (Photo photo : photoList) {
            photo.setDownloaded(false);
            photoDao.updateInTx(photo);
        }

        download.setStatus(Download.STATUS_PENDING);
        download.setProgress(0);
        downloadDao.updateInTx(download);

        addDownload(download, startId);
    }

    private void stopAction(Intent intent, int startId) {
        for (Long key : downloadMap.keySet()) {
            pauseDownload(key);
        }
    }

    private void addDownload(Download download, int startId) {
        downloadMap.put(download.getId(), download);
        bus.post(new GalleryDownloadEvent(EVENT_PENDING, download));
        handler.post(new DownloadRunnable(download, startId));
    }

    private void pauseDownload(long id) {

        Download download = downloadMap.get(id);

        if (download == null) return;

        if (runnable.getId() == id) {
            runnable.stop();
        } else {
            download.setStatus(Download.STATUS_PAUSED);
            downloadDao.updateInTx(download);
            downloadMap.remove(id);
            bus.post(new GalleryDownloadEvent(EVENT_PAUSED, download));
        }
    }

    private class DownloadRunnable implements Runnable {
        private Download download;
        private int id;
        private int startId;
        private Gallery gallery;
        private int total;
        private NotificationCompat.Builder builder;
        private File galleryFolder;
        private boolean isTerminated = false;

        public DownloadRunnable(Download download, int startId) {
            this.download = download;
            this.startId = startId;
        }

        public long getId() {
            return id;
        }

        @Override
        public void run() {
            runnable = this;

            gallery = download.getGallery();
            id = gallery.getId().intValue();
            total = gallery.getCount();
            galleryFolder = gallery.getFolder();

            if (!galleryFolder.exists()) galleryFolder.mkdirs();

            builder = new NotificationCompat.Builder(GalleryDownloadService.this);
            Intent intent = new Intent(GalleryDownloadService.this, MainActivity.class);
            Bundle args = new Bundle();

            args.putInt("tab", MainActivity.TAB_DOWNLOAD);
            intent.putExtras(args);

            PendingIntent pendingIntent = PendingIntent.getActivity(GalleryDownloadService.this, 0,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);

            builder.setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle(gallery.getTitle())
                    .setContentIntent(pendingIntent)
                    .setProgress(0, 0, true)
                    .setContentText(getString(R.string.download_in_progress));

            sendNotification();
            bus.post(new GalleryDownloadEvent(EVENT_DOWNLOADING, download));

            for (int i = 1; i <= total; i++) {
                int status = download.getStatus();

                if (isTerminated) {
                    break;
                } else if (status == Download.STATUS_PAUSED) {
                    stop();
                    break;
                } else {
                    fetchPhoto(i);
                }
            }

            if (!isTerminated) success();
        }

        private void fetchPhoto(int page) {
            Photo photo = null;
            boolean isSuccess = false;
            boolean skip = false;

            for (int i = 0; i < MAX_RETRY; i++) {
                try {
                    photo = downloadHelper.getPhotoInfo(gallery, page);

                    if (photo.getDownloaded()) {
                        isSuccess = true;
                        skip = true;
                        break;
                    }

                    String src = photo.getSrc();
                    File dest = photo.getFile();
                    File cache = aq.getCachedFile(src);

                    if (cache == null) {
                        HttpClient httpClient = new DefaultHttpClient();
                        HttpGet httpGet = new HttpGet(src);
                        HttpResponse response = httpClient.execute(httpGet);
                        int statusCode = response.getStatusLine().getStatusCode();

                        if (statusCode != 200) {
                            markInvalid(photo);
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
                } catch (IOException e) {
                    e.printStackTrace();
                    markInvalid(photo);
                } catch (JSONException e) {
                    e.printStackTrace();
                    markInvalid(photo);
                }
            }

            if (isSuccess) {
                if (!skip) progress(page);
            } else {
                fail();
            }
        }

        private void markInvalid(Photo photo) {
            if (photo == null) return;

            photo.setInvalid(true);
            photoDao.updateInTx(photo);
        }

        private void success() {
            if (isTerminated) return;

            isTerminated = true;

            Intent intent = new Intent(GalleryDownloadService.this, GalleryActivity.class);
            Bundle args = new Bundle();

            args.putLong("id", id);
            intent.putExtras(args);

            PendingIntent pendingIntent = PendingIntent.getActivity(GalleryDownloadService.this, 0,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);

            builder.setContentText(getString(R.string.download_success))
                    .setProgress(0, 0, false)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);

            sendNotification();
            setStatus(Download.STATUS_SUCCESS);
            sendEvent(EVENT_SUCCESS);
            terminate();
        }

        private void fail() {
            if (isTerminated) return;

            isTerminated = true;

            builder.setContentText(getString(R.string.download_failed))
                    .setProgress(0, 0, false)
                    .setAutoCancel(true);

            sendNotification();
            setStatus(Download.STATUS_ERROR);
            sendEvent(EVENT_ERROR);
            terminate();
        }

        private void progress(int progress) {
            if (isTerminated) return;

            String progressText = String.format("%d / %d (%.2f%%)", progress, total, progress * 100f / total);

            builder.setProgress(total, progress, false)
                    .setContentText(progressText);

            download.setProgress(progress);
            downloadDao.updateInTx(download);

            sendNotification();
            setStatus(Download.STATUS_DOWNLOADING);
            sendEvent(EVENT_DOWNLOADING);
        }

        public void stop() {
            if (isTerminated) return;

            isTerminated = true;

            builder.setContentText(getString(R.string.download_paused))
                    .setProgress(0, 0, false)
                    .setAutoCancel(true);

            sendNotification();
            setStatus(Download.STATUS_PAUSED);
            sendEvent(EVENT_PAUSED);
            terminate();
        }

        private void terminate() {
            runnable = null;

            downloadMap.remove(id);
            stopSelf(startId);
        }

        private void sendNotification() {
            nm.notify(id, builder.build());
        }

        private void sendEvent(int event) {
            bus.post(new GalleryDownloadEvent(event, download));
        }

        private void setStatus(int status) {
            if (download.getStatus() == status) return;

            download.setStatus(status);
            downloadDao.updateInTx(download);
        }
    }
}
