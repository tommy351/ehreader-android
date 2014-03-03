package tw.skyarrow.ehreader.app.photo;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.greenrobot.event.EventBus;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.db.DaoMaster;
import tw.skyarrow.ehreader.db.DaoSession;
import tw.skyarrow.ehreader.db.Photo;
import tw.skyarrow.ehreader.db.PhotoDao;
import tw.skyarrow.ehreader.event.PhotoDownloadEvent;
import tw.skyarrow.ehreader.provider.PhotoProvider;
import tw.skyarrow.ehreader.util.DatabaseHelper;

/**
 * Created by SkyArrow on 2014/2/27.
 */
public class PhotoSaveDialog extends DialogFragment {
    public static final String TAG = "PhotoSaveDialog";

    public static final String EXTRA_PHOTO = "photo";

    private PhotoDao photoDao;
    private PhotoSaveTask task;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog dialog = new ProgressDialog(getActivity());
        Bundle args = getArguments();

        DatabaseHelper helper = DatabaseHelper.getInstance(getActivity());
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();
        photoDao = daoSession.getPhotoDao();

        dialog.setTitle(getString(R.string.photo_saving));
        dialog.setMessage(getString(R.string.photo_saving));
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), onCancelClick);

        task = new PhotoSaveTask();
        task.execute(args.getLong(EXTRA_PHOTO));

        return dialog;
    }

    private DialogInterface.OnClickListener onCancelClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            task.cancel(true);
            dismiss();
        }
    };

    private class PhotoSaveTask extends AsyncTask<Long, Integer, String> {
        private long id;
        private Photo photo;

        @Override
        protected String doInBackground(Long... longs) {
            id = longs[0];
            photo = photoDao.load(id);
            Uri uri = ContentUris.withAppendedId(PhotoProvider.PHOTO_URI, id);

            if (photo == null) return null;
            if (uri == null) return null;

            File dest = photo.getFile();
            File parentDir = dest.getParentFile();

            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }

            try {
                InputStream in = getActivity().getContentResolver().openInputStream(uri);
                OutputStream out = new FileOutputStream(dest);

                byte[] buf = new byte[1024];
                int len;

                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                in.close();
                out.close();

                return dest.getCanonicalPath();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String path) {
            String msg;

            if (path == null) {
                msg = getString(R.string.photo_save_failed);
            } else {
                msg = String.format(getString(R.string.photo_save_success), path);
                photo.setDownloaded(true);
                photoDao.updateInTx(photo);
                EventBus.getDefault().post(new PhotoDownloadEvent(id, true));
            }

            Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
            dismiss();
        }
    }
}
