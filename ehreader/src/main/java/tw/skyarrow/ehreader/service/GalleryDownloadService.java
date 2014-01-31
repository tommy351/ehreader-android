package tw.skyarrow.ehreader.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.androidquery.AQuery;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Date;

import de.greenrobot.event.EventBus;
import tw.skyarrow.ehreader.Constant;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.activity.GalleryActivity;
import tw.skyarrow.ehreader.db.DaoMaster;
import tw.skyarrow.ehreader.db.DaoSession;
import tw.skyarrow.ehreader.db.Gallery;
import tw.skyarrow.ehreader.db.GalleryDao;
import tw.skyarrow.ehreader.db.Photo;
import tw.skyarrow.ehreader.db.PhotoDao;
import tw.skyarrow.ehreader.event.GalleryDownloadEvent;
import tw.skyarrow.ehreader.util.L;
import tw.skyarrow.ehreader.util.PhotoInfoHelper;

/**
 * Created by SkyArrow on 2014/1/30.
 */
public class GalleryDownloadService extends IntentService {
    private static final String CLASS_NAME = "GalleryDownloadService";

    public static final String GALLERY_ID = "galleryId";

    public static final String ACTION_START = "GalleryDownloadService.ACTION_START";
    public static final String ACTION_PAUSE = "GalleryDownloadService.ACTION_PAUSE";
    public static final String ACTION_CANCEL = "GalleryDownloadService.ACTION_CANCEL";

    public static final int EVENT_STARTED = 0;
    public static final int EVENT_PROGRESS = 1;
    public static final int EVENT_PAUSED = 2;
    public static final int EVENT_ERROR = 3;
    public static final int EVENT_SUCCESS = 4;

    public static final int STATUS_NOT_DOWNLOADED = -1;
    public static final int STATUS_DOWNLOADING = 0;
    public static final int STATUS_PAUSED = 1;
    public static final int STATUS_ERROR = 2;
    public static final int STATUS_SUCCESS = 3;

    private SQLiteDatabase db;
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private GalleryDao galleryDao;
    private PhotoDao photoDao;

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

        infoHelper = new PhotoInfoHelper(this);
        aq = new AQuery(this);
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        bus = EventBus.getDefault();

        ehFolder = new File(Environment.getExternalStorageDirectory(), Constant.FOLDER_NAME);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        task.terminate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        long galleryId = intent.getLongExtra(GALLERY_ID, 0);

        if (galleryId <= 0) {
            L.e("Gallery ID must be a positive number.");
            return;
        }

        task = new GalleryDownloadTask(galleryId);
        task.run();
    }

    private class GalleryDownloadTask {
        private long galleryId;
        private int id;
        private int total;
        private Gallery gallery;
        private NotificationCompat.Builder builder;
        private boolean isTerminated = false;
        private File galleryFolder;

        public GalleryDownloadTask(long galleryId) {
            this.galleryId = galleryId;
            this.id = (int) galleryId;
        }

        public void run() {
            gallery = galleryDao.load(galleryId);

            if (gallery == null) {
                L.e("Gallery %d not found.", galleryId);
                return;
            }

            if (gallery.getDownloadStatus() == STATUS_SUCCESS) {
                success();
                return;
            }

            total = gallery.getCount();
            galleryFolder = new File(ehFolder, Long.toString(galleryId));

            if (!galleryFolder.exists()) {
                galleryFolder.mkdirs();
            }

            if (gallery.getDownloaded() == null) {
                gallery.setDownloaded(new Date(System.currentTimeMillis()));
                galleryDao.update(gallery);
            }

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
            if (this.isTerminated) return;

            this.isTerminated = true;

            builder.setContentText("Download paused")
                    .setProgress(0, 0, false)
                    .setAutoCancel(true);

            sendNotification();
            setStatus(STATUS_PAUSED);
            bus.post(new GalleryDownloadEvent(EVENT_PAUSED, gallery));
        }

        private void success() {
            this.isTerminated = true;

            builder.setContentText("Download success")
                    .setProgress(0, 0, false)
                    .setAutoCancel(true);

            sendNotification();
            setStatus(STATUS_SUCCESS);
            bus.post(new GalleryDownloadEvent(EVENT_SUCCESS, gallery));
        }

        private void progress(int progress) {
            String progressText = String.format("%d / %d (%.2f%%)", progress, total, progress * 100f / total);

            builder.setProgress(total, progress, false)
                    .setContentText(progressText);

            sendNotification();
            setStatus(STATUS_DOWNLOADING);
            bus.post(new GalleryDownloadEvent(EVENT_PROGRESS, gallery, progress));
        }

        private void error() {
            this.isTerminated = true;

            builder.setContentText("Download failed")
                    .setProgress(0, 0, false)
                    .setAutoCancel(true);

            sendNotification();
            setStatus(STATUS_ERROR);
            bus.post(new GalleryDownloadEvent(EVENT_ERROR, gallery));
        }

        private void setStatus(int status) {
            if (gallery.getDownloadStatus() == status) return;

            gallery.setDownloadStatus(status);
            galleryDao.update(gallery);
        }

        private void sendNotification() {
            nm.notify(id, builder.build());
        }

        private void buildNotification() {
            builder = new NotificationCompat.Builder(GalleryDownloadService.this);
            Intent intent = new Intent(GalleryDownloadService.this, GalleryActivity.class);
            Bundle args = new Bundle();

            args.putLong("id", galleryId);
            intent.putExtras(args);

            PendingIntent pendingIntent = PendingIntent.getActivity(GalleryDownloadService.this, 0,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);

            builder.setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle(gallery.getTitle())
                    .setContentIntent(pendingIntent)
                    .setProgress(0, 0, true)
                    .setContentText("Download in progress");

            sendNotification();
        }

        private void fetchPhoto(int page) {
            try {
                Photo photo = infoHelper.getPhotoInfo(gallery, page);

                if (photo.getDownloaded()) {
                    progress(page);
                    return;
                }

                File dest = new File(galleryFolder, photo.getFilename());
                File cache = aq.getCachedFile(photo.getSrc());
                InputStream in;
                OutputStream out = new FileOutputStream(dest);
                byte[] buf = new byte[1024];
                int len;

                if (cache == null) {
                    URL url = new URL(photo.getSrc());
                    in = new BufferedInputStream(url.openStream());
                } else {
                    in = new FileInputStream(cache);
                }

                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                in.close();
                out.close();

                photo.setDownloaded(true);
                photoDao.updateInTx(photo);
                progress(page);
            } catch (IOException e) {
                e.printStackTrace();
                error();
            } catch (JSONException e) {
                e.printStackTrace();
                error();
            }
        }
    }
}
