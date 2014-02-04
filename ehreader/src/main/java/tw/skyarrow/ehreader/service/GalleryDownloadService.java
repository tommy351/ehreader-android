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
import android.os.Message;
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
import java.util.List;

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
    private DownloadDao downloadDao;

    private EventBus bus;
    private Looper looper;
    private Handler handler;
    private DownloadHelper infoHelper;
    private AQuery aq;
    private NotificationManager nm;

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
        photoDao = daoSession.getPhotoDao();
        downloadDao = daoSession.getDownloadDao();

        infoHelper = new DownloadHelper(this);
        aq = new AQuery(this);
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        HandlerThread thread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        looper = thread.getLooper();
        handler = new GalleryDownloadHandler(looper);
        bus.post(new GalleryDownloadEvent(EVENT_SERVICE_START, null));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();

        if (action.equals(ACTION_STOP)) {
            handler.removeCallbacksAndMessages(null);
            stopSelf();
            return START_STICKY;
        }

        long galleryId = intent.getLongExtra(GALLERY_ID, 0);
        int what = (int) galleryId;
        boolean inQueue = handler.hasMessages(what);

        if (galleryId <= 0) {
            L.e("Gallery ID must be a positive number.");
            return START_STICKY;
        }

        if (action.equals(ACTION_START)) {
            if (inQueue) {
                L.e("Download %d has been already started.", galleryId);
            } else {
                Download download = downloadDao.load(galleryId);

                if (download == null) {
                    download = new Download();

                    download.setId(galleryId);
                    download.setCreated(new Date(System.currentTimeMillis()));
                    download.setProgress(0);
                    download.setStatus(Download.STATUS_PENDING);

                    downloadDao.insert(download);
                } else {
                    download.setStatus(Download.STATUS_PENDING);
                    downloadDao.update(download);
                }

                addDownloadToQueue(download, startId);
            }
        } else if (action.equals(ACTION_PAUSE)) {
            if (inQueue) {
                handler.removeMessages(what);
            } else {
                L.e("Download %d is not in the queue.", galleryId);
            }
        } else if (action.equals(ACTION_RETRY)) {
            if (inQueue) {
                L.e("Download %d has been already started.", galleryId);
            } else {
                Download download = downloadDao.load(galleryId);

                if (download == null) {
                    download = new Download();

                    download.setId(galleryId);
                    download.setCreated(new Date(System.currentTimeMillis()));
                    download.setProgress(0);
                    download.setStatus(Download.STATUS_PENDING);

                    downloadDao.insert(download);
                } else {
                    QueryBuilder qb = photoDao.queryBuilder();
                    qb.where(PhotoDao.Properties.GalleryId.eq(galleryId));
                    List<Photo> photoList = qb.list();

                    for (Photo photo : photoList) {
                        photo.setDownloaded(false);
                        photoDao.updateInTx(photo);
                    }

                    download.setStatus(Download.STATUS_PENDING);
                    download.setProgress(0);
                    downloadDao.update(download);
                }

                addDownloadToQueue(download, startId);
            }
        }


        return START_STICKY;
    }

    private void addDownloadToQueue(Download download, int startId) {
        Message msg = handler.obtainMessage();
        msg.what = download.getId().intValue();
        msg.arg1 = startId;
        handler.sendMessage(msg);
        bus.post(new GalleryDownloadEvent(EVENT_PENDING, download));
    }

    @Override
    public void onDestroy() {
        QueryBuilder qb = downloadDao.queryBuilder();
        qb.where(DownloadDao.Properties.Status.in(Download.STATUS_PENDING, Download.STATUS_DOWNLOADING));
        List<Download> downloadList = qb.list();

        for (Download download : downloadList) {
            download.setStatus(Download.STATUS_PAUSED);
            downloadDao.updateInTx(download);
        }

        super.onDestroy();
        bus.post(new GalleryDownloadEvent(EVENT_SERVICE_STOP, null));
        db.close();
    }

    private final class GalleryDownloadHandler extends Handler {
        public GalleryDownloadHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            new GalleryDownloadTask(msg.what);
            stopSelf(msg.arg1);
        }
    }

    private class GalleryDownloadTask {
        private int id;
        private Gallery gallery;
        private Download download;
        private File galleryFolder;
        private NotificationCompat.Builder builder;
        private int total;
        private boolean isTerminated = false;

        public GalleryDownloadTask(int id) {
            this.id = id;

            download = downloadDao.load((long) id);
            gallery = download.getGallery();
            total = gallery.getCount();

            // Create the gallery folder if not exists
            galleryFolder = gallery.getFolder();
            if (!galleryFolder.exists()) galleryFolder.mkdirs();

            bus.post(new GalleryDownloadEvent(EVENT_DOWNLOADING, download));
            buildNotification();

            for (int i = 1; i <= total; i++) {
                if (isTerminated) {
                    return;
                } else {
                    fetchPhoto(i);
                }
            }

            success();
        }

        public void terminate() {
            if (isTerminated) return;

            isTerminated = true;

            builder.setContentText(getResources().getString(R.string.download_paused))
                    .setProgress(0, 0, false)
                    .setAutoCancel(true);

            sendNotification();
            setStatus(Download.STATUS_PAUSED);
            bus.post(new GalleryDownloadEvent(EVENT_PAUSED, download));
        }

        private void success() {
            isTerminated = true;

            Intent intent = new Intent(GalleryDownloadService.this, GalleryActivity.class);
            Bundle args = new Bundle();

            args.putLong("id", id);
            intent.putExtras(args);

            PendingIntent pendingIntent = PendingIntent.getActivity(GalleryDownloadService.this, 0,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);

            builder.setContentText(getResources().getString(R.string.download_success))
                    .setProgress(0, 0, false)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);

            sendNotification();
            setStatus(Download.STATUS_SUCCESS);
            bus.post(new GalleryDownloadEvent(EVENT_SUCCESS, download));
        }

        private void fail() {
            isTerminated = true;

            builder.setContentText(getResources().getString(R.string.download_failed))
                    .setProgress(0, 0, false)
                    .setAutoCancel(true);

            sendNotification();
            setStatus(Download.STATUS_ERROR);
            bus.post(new GalleryDownloadEvent(EVENT_ERROR, download));
        }

        private void progress(int progress) {
            String progressText = String.format("%d / %d (%.2f%%)", progress, total, progress * 100f / total);

            builder.setProgress(total, progress, false)
                    .setContentText(progressText);

            download.setProgress(progress);
            downloadDao.update(download);

            sendNotification();
            setStatus(Download.STATUS_DOWNLOADING);
            bus.post(new GalleryDownloadEvent(EVENT_DOWNLOADING, download));
        }

        private void fetchPhoto(int page) {
            Photo photo = null;
            boolean isSuccess = false;

            for (int i = 0; i < MAX_RETRY; i++) {
                try {
                    photo = infoHelper.getPhotoInfo(gallery, page);

                    if (photo.getDownloaded()) {
                        isSuccess = true;
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
                progress(page);
            } else {
                fail();
            }
        }

        private void markInvalid(Photo photo) {
            if (photo == null) return;

            photo.setInvalid(true);
            photoDao.update(photo);
        }

        private void buildNotification() {
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
                    .setContentText(getResources().getString(R.string.download_in_progress));

            sendNotification();
        }

        private void sendNotification() {
            nm.notify(id, builder.build());
        }

        private void setStatus(int status) {
            if (download.getStatus() == status) return;

            download.setStatus(status);
            downloadDao.update(download);
        }
    }
}
