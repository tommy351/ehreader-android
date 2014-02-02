package tw.skyarrow.ehreader.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import tw.skyarrow.ehreader.util.L;
import tw.skyarrow.ehreader.util.PhotoInfoHelper;
import tw.skyarrow.ehreader.util.UriHelper;

/**
 * Created by SkyArrow on 2014/1/30.
 */
public class GalleryDownloadService extends IntentService {
    private static final String CLASS_NAME = "GalleryDownloadService";

    public static int MAX_RETRY = 2;

    public static final String GALLERY_ID = "galleryId";

    public static final String ACTION_START = "GalleryDownloadService.ACTION_START";
    public static final String ACTION_PAUSE = "GalleryDownloadService.ACTION_PAUSE";
    public static final String ACTION_CANCEL = "GalleryDownloadService.ACTION_CANCEL";
    public static final String ACTION_RETRY = "GalleryDownloadService.ACTION_RETRY";
    public static final String ACTION_START_ALL = "GalleryDownloadService.ACTION_START_ALL";
    public static final String ACTION_PAUSE_ALL = "GalleryDownloadService.ACTION_PAUSE_ALL";

    public static final int EVENT_STARTED = 0;
    public static final int EVENT_PROGRESS = 1;
    public static final int EVENT_PAUSED = 2;
    public static final int EVENT_ERROR = 3;
    public static final int EVENT_SUCCESS = 4;
    public static final int EVENT_SERVICE_START = 10;
    public static final int EVENT_SERVICE_STOP = 11;

    private SQLiteDatabase db;
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private GalleryDao galleryDao;
    private PhotoDao photoDao;
    private DownloadDao downloadDao;

    private PhotoInfoHelper infoHelper;
    private AQuery aq;
    private NotificationManager nm;
    private EventBus bus;

    private File ehFolder;
    private GalleryDownloadTask task;

    public GalleryDownloadService() {
        super(CLASS_NAME);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, Constant.DB_NAME, null);
        db = helper.getWritableDatabase();
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
        galleryDao = daoSession.getGalleryDao();
        photoDao = daoSession.getPhotoDao();
        downloadDao = daoSession.getDownloadDao();

        infoHelper = new PhotoInfoHelper(this);
        aq = new AQuery(this);
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        bus = EventBus.getDefault();

        ehFolder = UriHelper.getPicFolder();

        bus.post(new GalleryDownloadEvent(EVENT_SERVICE_START, null));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        task.terminate();

        bus.post(new GalleryDownloadEvent(EVENT_SERVICE_STOP, null));
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        long galleryId = intent.getLongExtra(GALLERY_ID, 0);
        String action = intent.getAction();

        if (galleryId <= 0) {
            L.e("Gallery ID must be a positive number.");
            return;
        }

        if (action.equals(ACTION_START)) {
            task = new GalleryDownloadTask(galleryId);
            task.run();
        } else if (action.equals(ACTION_RETRY)) {
            Download download = downloadDao.load(galleryId);
            task = new GalleryDownloadTask(galleryId);

            download.setStatus(Download.STATUS_RETRY);
            downloadDao.update(download);
            task.run();
        } else if (action.equals(ACTION_START_ALL)) {
            //
        } else if (action.equals(ACTION_PAUSE_ALL)) {
            stopSelf();
        }
    }

    private class GalleryDownloadTask {
        private long galleryId;
        private int id;
        private int total;
        private Gallery gallery;
        private Download download;
        private boolean isTerminated = false;
        private File galleryFolder;
        private NotificationCompat.Builder builder;

        public GalleryDownloadTask(long galleryId) {
            this.galleryId = galleryId;
            this.id = (int) galleryId;
        }

        public long getGalleryId() {
            return galleryId;
        }

        public void run() {
            gallery = galleryDao.load(galleryId);

            if (gallery == null) {
                L.e("Gallery %d not found.", galleryId);
                return;
            }

            download = downloadDao.load(galleryId);
            total = gallery.getCount();
            galleryFolder = UriHelper.getGalleryFolder(gallery);

            if (download == null) {
                download = new Download();

                download.setId(galleryId);
                download.setCreated(new Date(System.currentTimeMillis()));
                download.setProgress(0);
                download.setStatus(Download.STATUS_DOWNLOADING);
                downloadDao.insert(download);
            } else {
                if (download.getStatus() == Download.STATUS_SUCCESS) {
                    return;
                }

                download.setStatus(Download.STATUS_DOWNLOADING);
                downloadDao.update(download);
            }

            if (!galleryFolder.exists()) {
                galleryFolder.mkdirs();
            }

            bus.post(new GalleryDownloadEvent(EVENT_STARTED, download));
            buildNotification();

            for (int i = 1; i <= total; i++) {
                if (isTerminated) {
                    terminate();
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

        private void fetchPhoto(int page) {
            Photo photo = null;
            boolean isSuccess = false;

            for (int i = 0; i < MAX_RETRY; i++) {
                try {
                    photo = infoHelper.getPhotoInfo(gallery, page);

                    if (photo.getDownloaded() && download.getStatus() != Download.STATUS_RETRY) {
                        isSuccess = true;
                        break;
                    }

                    String src = photo.getSrc();
                    File dest = UriHelper.getPhotoFile(photo);
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

        private void success() {
            isTerminated = true;

            Intent intent = new Intent(GalleryDownloadService.this, GalleryActivity.class);
            Bundle args = new Bundle();

            args.putLong("id", galleryId);
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
            bus.post(new GalleryDownloadEvent(EVENT_PROGRESS, download));
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
