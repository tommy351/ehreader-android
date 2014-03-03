package tw.skyarrow.ehreader.app.photo;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

import java.io.File;

import de.greenrobot.event.EventBus;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.db.DaoMaster;
import tw.skyarrow.ehreader.db.DaoSession;
import tw.skyarrow.ehreader.db.Photo;
import tw.skyarrow.ehreader.db.PhotoDao;
import tw.skyarrow.ehreader.event.PhotoDownloadEvent;
import tw.skyarrow.ehreader.util.DatabaseHelper;

/**
 * Created by SkyArrow on 2014/2/28.
 */
public class PhotoDeleteDialog extends DialogFragment {
    public static final String TAG = "PhotoDeleteDialog";

    public static final String EXTRA_PHOTO = "photo";

    private PhotoDao photoDao;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog dialog = new ProgressDialog(getActivity());
        Bundle args = getArguments();
        long id = args.getLong(EXTRA_PHOTO);

        DatabaseHelper helper = DatabaseHelper.getInstance(getActivity());
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();
        photoDao = daoSession.getPhotoDao();

        dialog.setTitle(getString(R.string.photo_deleting));
        dialog.setMessage(getString(R.string.photo_deleting));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.setIndeterminate(true);

        new PhotoDeleteTask().execute(id);

        return dialog;
    }

    private class PhotoDeleteTask extends AsyncTask<Long, Integer, Boolean> {
        private long id;
        private Photo photo;

        @Override
        protected Boolean doInBackground(Long... longs) {
            id = longs[0];
            photo = photoDao.load(id);

            if (photo == null) return false;

            File file = photo.getFile();

            if (file.exists()) {
                return file.delete();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            String msg;

            if (success) {
                msg = getString(R.string.photo_delete_success);
                photo.setDownloaded(false);
                photoDao.updateInTx(photo);
                EventBus.getDefault().post(new PhotoDownloadEvent(id, false));
            } else {
                msg = getString(R.string.photo_delete_failed);
            }

            Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
            dismiss();
        }
    }
}
