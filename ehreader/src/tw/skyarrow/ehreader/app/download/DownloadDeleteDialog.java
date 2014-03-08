package tw.skyarrow.ehreader.app.download;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import java.io.File;
import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;
import de.greenrobot.event.EventBus;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.db.DaoMaster;
import tw.skyarrow.ehreader.db.DaoSession;
import tw.skyarrow.ehreader.db.Download;
import tw.skyarrow.ehreader.db.DownloadDao;
import tw.skyarrow.ehreader.db.Gallery;
import tw.skyarrow.ehreader.db.Photo;
import tw.skyarrow.ehreader.db.PhotoDao;
import tw.skyarrow.ehreader.event.GalleryDeleteEvent;
import tw.skyarrow.ehreader.service.GalleryDownloadService;
import tw.skyarrow.ehreader.util.DatabaseHelper;

/**
 * Created by SkyArrow on 2014/2/3.
 */
public class DownloadDeleteDialog extends DialogFragment {
    public static final String TAG = "DownloadDeleteDialog";

    public static final String EXTRA_GALLERY = "id";

    private PhotoDao photoDao;
    private DownloadDao downloadDao;

    private long galleryId;
    private ProgressDialog dialog;
    private Download download;
    private Gallery gallery;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialog = new ProgressDialog(getActivity());
        Bundle args = getArguments();
        galleryId = args.getLong(EXTRA_GALLERY);

        DatabaseHelper helper = DatabaseHelper.getInstance(getActivity());
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();
        photoDao = daoSession.getPhotoDao();
        downloadDao = daoSession.getDownloadDao();

        download = downloadDao.load(galleryId);
        gallery = download.getGallery();

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setTitle(R.string.gallery_deleting);
        dialog.setMax(gallery.getCount());
        dialog.setProgress(0);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        Intent intent = new Intent(getActivity(), GalleryDownloadService.class);

        intent.setAction(GalleryDownloadService.ACTION_PAUSE);
        intent.putExtra(GalleryDownloadService.EXTRA_GALLERY, galleryId);

        getActivity().startService(intent);

        new GalleryDeleteTask().execute();

        return dialog;
    }

    private class GalleryDeleteTask extends AsyncTask<Integer, Integer, String> {
        @Override
        protected String doInBackground(Integer... integers) {
            QueryBuilder<Photo> qb = photoDao.queryBuilder();
            qb.where(PhotoDao.Properties.GalleryId.eq(galleryId));
            List<Photo> photos = qb.list();
            File galleryFolder = gallery.getFolder();

            for (Photo photo : photos) {
                File file = photo.getFile();

                if (file.exists()) {
                    file.delete();
                }

                photo.setDownloaded(false);
                photoDao.updateInTx(photo);
                publishProgress(1);
            }

            if (galleryFolder.exists()) {
                galleryFolder.delete();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            dialog.incrementProgressBy(values[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            downloadDao.delete(download);

            EventBus.getDefault().post(new GalleryDeleteEvent(galleryId));
            dismiss();
        }
    }
}
