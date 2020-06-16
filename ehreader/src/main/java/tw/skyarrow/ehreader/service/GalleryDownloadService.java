package tw.skyarrow.ehreader.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.IBinder;
import android.os.Process;
import android.support.v4.app.NotificationCompat;

import com.android.volley.toolbox.RequestFuture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import de.greenrobot.event.EventBus;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.app.drawer.DrawerFragment;
import tw.skyarrow.ehreader.app.gallery.GalleryActivity;
import tw.skyarrow.ehreader.app.main.MainActivity;
import tw.skyarrow.ehreader.event.GalleryDownloadEvent;
import tw.skyarrow.ehreader.model.DaoMaster;
import tw.skyarrow.ehreader.model.DaoSession;
import tw.skyarrow.ehreader.model.Download;
import tw.skyarrow.ehreader.model.DownloadDao;
import tw.skyarrow.ehreader.model.Gallery;
import tw.skyarrow.ehreader.model.GalleryDao;
import tw.skyarrow.ehreader.model.Photo;
import tw.skyarrow.ehreader.model.PhotoDao;
import tw.skyarrow.ehreader.network.EHCrawler;
import tw.skyarrow.ehreader.util.DatabaseHelper;
import tw.skyarrow.ehreader.util.L;

public class GalleryDownloadService extends Service {
    public static final String TAG = GalleryDownloadService.class.getSimpleName();

    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_STOP = "ACTION_STOP";

    public static final String EXTRA_ID = "id";

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private final BlockingQueue<DownloadTask> mTaskQueue = new LinkedBlockingQueue<>();
    private volatile DownloadTask mActiveTask;

    private DatabaseHelper dbHelper;
    private GalleryDao galleryDao;
    private DownloadDao downloadDao;
    private PhotoDao photoDao;
    private EHCrawler ehCrawler;
    private NotificationManager notificationManager;

    public static Intent newIntent(Context context, String action, long id){
        Intent intent = new Intent(context, GalleryDownloadService.class);

        intent.setAction(action);
        intent.putExtra(EXTRA_ID, id);

        return intent;
    }

    public static Intent newIntent(Context context, String action){
        return newIntent(context, action, 0);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        dbHelper = DatabaseHelper.getInstance(this);
        DaoMaster daoMaster = new DaoMaster(dbHelper.open());
        DaoSession daoSession = daoMaster.newSession();
        galleryDao = daoSession.getGalleryDao();
        downloadDao = daoSession.getDownloadDao();
        photoDao = daoSession.getPhotoDao();
        ehCrawler = new EHCrawler(this);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        long id = intent.getLongExtra(EXTRA_ID, 0);

        L.d("Start command: {action: %s, id: %d}", action, id);

        if (ACTION_START.equals(action)){
            addTask(id, startId);
        } else if (ACTION_PAUSE.equals(action)){
            removeTask(id);
        } else if (ACTION_STOP.equals(action)){
            stopAll();
        }

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        stopAll();
        mExecutor.shutdown();
        ehCrawler.close();
        dbHelper.close();
        super.onDestroy();
    }

    private synchronized void addTask(long id, int startId){
        DownloadTask task = new DownloadTask(id, startId);

        // Do nothing if the task is running
        if (mActiveTask != null && mActiveTask == task){
            return;
        }

        // Do nothing if the task is in the queue
        if (!mTaskQueue.isEmpty() && mTaskQueue.contains(task)) {
            return;
        }

        if (task.prepare()){
            mTaskQueue.add(task);
            if (mActiveTask == null) next();
        }
    }

    private synchronized void removeTask(long id){
        if (mActiveTask != null && mActiveTask.getId() == id){
            // Stop the current task
            mActiveTask.cancel();
            mActiveTask = null;

            // Do the next task
            next();
        } else if (!mTaskQueue.isEmpty()){
            // Find the task in the queue
            Iterator<DownloadTask> iterator = mTaskQueue.iterator();
            DownloadTask task;

            while ((task = iterator.next()) != null){
                if (task.getId() == id){
                    task.cancel();
                    iterator.remove();
                    break;
                }
            }
        }
    }

    private synchronized void stopAll(){
        // Stop the current task
        if (mActiveTask != null){
            mActiveTask.cancel();
            mActiveTask = null;
        }

        // Cancel all tasks in the queue
        if (!mTaskQueue.isEmpty()){
            DownloadTask task;

            while ((task = mTaskQueue.poll()) != null){
                task.cancel();
            }
        }
    }

    private synchronized void next(){
        if ((mActiveTask = mTaskQueue.poll()) != null){
            mActiveTask.execute();
        }
    }

    private final class DownloadTask {
        private final long id;
        private final AtomicBoolean mCancelled = new AtomicBoolean(false);
        private final AtomicBoolean mTaskInvoked = new AtomicBoolean(false);
        private final FutureTask<Download> mFutureTask;
        private final AtomicInteger mProgress = new AtomicInteger(0);
        private Gallery mGallery;
        private Download mDownload;

        public DownloadTask(final long id, final int startId){
            this.id = id;

            mFutureTask = new FutureTask<Download>(new Callable<Download>() {
                @Override
                public Download call() throws Exception {
                    mTaskInvoked.set(true);
                    android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                    return doInBackground();
                }
            }){
                @Override
                protected void done() {
                    mActiveTask = null;

                    L.d("Download done: %d", id);

                    try {
                        get();
                        mDownload.setStatus(Download.STATUS_SUCCESS);
                    } catch (ExecutionException e){
                        L.e(e.getCause());
                        mDownload.setStatus(Download.STATUS_ERROR);
                    } catch (InterruptedException e){
                        L.e(e);
                        mDownload.setStatus(Download.STATUS_ERROR);
                    }

                    mDownload.setProgress(mProgress.get());
                    downloadDao.updateInTx(mDownload);
                    postEvent();
                    stopSelf(startId);
                    next();
                }
            };
        }

        public long getId() {
            return id;
        }

        public boolean prepare(){
            L.d("Prepare: %d", id);

            mGallery = galleryDao.load(id);
            if (mGallery == null) return false;

            mDownload = downloadDao.load(id);

            if (mDownload == null){
                mDownload = new Download(id);
                mDownload.setCreated(new Date(System.currentTimeMillis()));
                mDownload.setProgress(0);
            } else if (mDownload.getStatus() == Download.STATUS_SUCCESS){
                return false;
            }

            mDownload.setStatus(Download.STATUS_PENDING);
            downloadDao.insertOrReplaceInTx(mDownload);
            postEvent();

            return true;
        }

        public void execute(){
            if (!mTaskInvoked.get() && !mCancelled.get()){
                mExecutor.execute(mFutureTask);
            }
        }

        public void cancel(){
            mCancelled.set(true);
            mFutureTask.cancel(true);
            mDownload.setStatus(Download.STATUS_PAUSED);
            downloadDao.updateInTx(mDownload);
            postEvent();
        }

        public Download get() throws InterruptedException, ExecutionException {
            return mFutureTask.get();
        }

        public Download doInBackground() throws InterruptedException, ExecutionException, TimeoutException {
            L.d("Start downloading: %d", id);

            mGallery = galleryDao.load(id);
            mDownload = downloadDao.load(id);

            mDownload.setStatus(Download.STATUS_DOWNLOADING);
            downloadDao.updateInTx(mDownload);
            postEvent();

            if (mGallery.getPhotoPerPage() == null){
                L.d("Get photo per page: %s", mGallery.getURL());
                List<Photo> photoList = getPhotoList();
                mGallery.setPhotoPerPage(photoList.size());
                galleryDao.updateInTx(mGallery);
            }

            while (!mCancelled.get() && mProgress.incrementAndGet() <= mGallery.getCount()){
                downloadPhoto(mProgress.get());
            }

            return mDownload;
        }

        private void downloadPhoto(final int i) throws InterruptedException, ExecutionException, TimeoutException {
            L.d("Download photo: %d-%d", mGallery.getId(), i);

            Photo photo = Photo.findPhoto(photoDao, id, i);
            File photoFile;

            if (photo != null){
                photoFile = photo.getFile(GalleryDownloadService.this);

                if (photo.getDownloaded() != null && photo.getDownloaded() && photoFile.exists()){
                    mDownload.setProgress(i);
                    postEvent();
                    return;
                }
            } else {
                int photoPerPage = mGallery.getPhotoPerPage();
                int galleryPage = i / photoPerPage;
                List<Photo> photoList = getPhotoList(galleryPage);
                Photo newPhoto = photoList.get(i % photoPerPage);

                if (newPhoto == null){
                    throw new RuntimeException(String.format("Failed to load photo: %d-%d", mGallery.getId(), i));
                }

                photo = newPhoto;
            }

            if (photo.getSrc() == null || photo.getSrc().isEmpty()){
                RequestFuture<Photo> future = RequestFuture.newFuture();
                ehCrawler.getPhoto(photo, future, future);
                Photo newPhoto = future.get(20, TimeUnit.SECONDS);

                if (newPhoto == null || newPhoto.getSrc() == null || newPhoto.getSrc().isEmpty()){
                    throw new RuntimeException(String.format("Failed to load photo src: %s", photo.getURL()));
                }

                photo = newPhoto;
            }

            try {
                photoFile = photo.getFile(GalleryDownloadService.this);
                File parentDir = photoFile.getParentFile();

                if (!parentDir.exists()){
                    if (!parentDir.mkdirs()){
                        throw new RuntimeException("Failed to create directory: " + parentDir.getAbsolutePath());
                    }
                }

                URL url = new URL(photo.getSrc());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(30 * 1000); // 30 sec timeout
                conn.connect();

                InputStream input = conn.getInputStream();
                OutputStream output = new FileOutputStream(photoFile);
                byte[] buf = new byte[4096];
                int len;

                while ((len = input.read(buf)) > 0){
                    output.write(buf, 0, len);
                }

                input.close();
                output.close();

                photo.setDownloaded(true);
                photoDao.updateInTx(photo);
                mDownload.setProgress(i);
                postEvent();
            } catch (IOException e){
                throw new ExecutionException(e);
            }
        }

        private List<Photo> getPhotoList() throws InterruptedException, ExecutionException, TimeoutException {
            return getPhotoList(0);
        }

        private List<Photo> getPhotoList(int page) throws InterruptedException, ExecutionException, TimeoutException {
            RequestFuture<List<Photo>> future = RequestFuture.newFuture();
            ehCrawler.getPhotoList(mGallery, page, future, future);
            List<Photo> photoList = future.get(10, TimeUnit.SECONDS);

            if (photoList.size() == 0){
                throw new RuntimeException("Photo per page load failed: " + mGallery.getURL());
            } else {
                return photoList;
            }
        }

        private void postEvent(){
            int progress = mDownload.getProgress();
            EventBus.getDefault().postSticky(new GalleryDownloadEvent(mDownload));

            NotificationCompat.Builder builder = new NotificationCompat.Builder(GalleryDownloadService.this);

            builder.setSmallIcon(R.drawable.notification_download)
                    .setContentIntent(getDefaultPendingIntent());

            switch (mDownload.getStatus()){
                case Download.STATUS_DOWNLOADING:
                    buildDownloadingNotification(builder);
                    break;

                case Download.STATUS_PENDING:
                    buildPendingNotification(builder);
                    break;

                case Download.STATUS_SUCCESS:
                    buildSuccessNotification(builder);
                    break;

                case Download.STATUS_PAUSED:
                    buildPausedNotification(builder);
                    break;

                case Download.STATUS_ERROR:
                    buildErrorNotification(builder);
                    break;
            }

            notificationManager.notify(TAG, hashCode(), builder.build());
        }

        private void buildDownloadingNotification(NotificationCompat.Builder builder){
            builder.setContentTitle(getString(R.string.download_in_progress))
                    .setTicker(getString(R.string.download_in_progress))
                    .setProgress(mGallery.getCount(), mProgress.get(), false)
                    .setContentText(String.format("%d / %d", mProgress.get(), mGallery.getCount()));
        }

        private void buildPendingNotification(NotificationCompat.Builder builder){
            builder.setContentTitle(getString(R.string.download_in_progress))
                    .setTicker(getString(R.string.download_in_progress))
                    .setProgress(0, 0, false)
                    .setContentText(getString(R.string.download_pending));
        }

        private void buildSuccessNotification(NotificationCompat.Builder builder){
            Intent intent = GalleryActivity.newIntent(GalleryDownloadService.this, mGallery.getId(), mGallery.getToken());
            PendingIntent pendingIntent;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
                pendingIntent = TaskStackBuilder.create(GalleryDownloadService.this)
                        .addNextIntentWithParentStack(intent)
                        .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            } else {
                pendingIntent = PendingIntent.getActivity(GalleryDownloadService.this, 0,
                        intent, PendingIntent.FLAG_CANCEL_CURRENT);
            }

            builder.setContentTitle(mGallery.getTitle())
                    .setTicker(getString(R.string.download_success))
                    .setContentText(getString(R.string.download_success))
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);

            Photo cover = Photo.findPhoto(photoDao, mGallery.getId(), 1);

            if (cover != null){
                File coverFile = cover.getFile(GalleryDownloadService.this);

                if (coverFile != null && coverFile.exists()){
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                    Bitmap bitmap = BitmapFactory.decodeFile(coverFile.getAbsolutePath(), options);
                    NotificationCompat.BigPictureStyle style = new NotificationCompat.BigPictureStyle()
                            .bigPicture(bitmap);

                    builder.setLargeIcon(getNotificationLargeIcon(bitmap))
                            .setStyle(style);
                }
            }
        }

        private void buildPausedNotification(NotificationCompat.Builder builder){
            builder.setContentTitle(getString(R.string.download_paused))
                    .setTicker(getString(R.string.download_paused))
                    .setContentText(mGallery.getTitle())
                    .setAutoCancel(true);
        }

        private void buildErrorNotification(NotificationCompat.Builder builder){
            builder.setContentTitle(getString(R.string.download_failed))
                    .setTicker(getString(R.string.download_failed))
                    .setContentText(mGallery.getTitle())
                    .setAutoCancel(true);
        }

        private PendingIntent getDefaultPendingIntent(){
            Intent intent = MainActivity.newIntent(GalleryDownloadService.this, DrawerFragment.TAB_DOWNLOAD);
            PendingIntent pendingIntent = PendingIntent.getActivity(GalleryDownloadService.this, 0,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);

            return pendingIntent;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DownloadTask task = (DownloadTask) o;
            return id == task.getId();
        }

        @Override
        public int hashCode() {
            return (int) id;
        }
    }

    private Bitmap getNotificationLargeIcon(Bitmap bitmap){
        Resources res = getResources();
        int reqWidth = (int) res.getDimension(android.R.dimen.notification_large_icon_width);
        int reqHeight = (int) res.getDimension(android.R.dimen.notification_large_icon_height);

        return ThumbnailUtils.extractThumbnail(bitmap, reqWidth, reqHeight);
    }
}
