package tw.skyarrow.ehreader.app.pref;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.model.DaoMaster;
import tw.skyarrow.ehreader.model.DaoSession;
import tw.skyarrow.ehreader.model.DownloadDao;
import tw.skyarrow.ehreader.model.GalleryDao;
import tw.skyarrow.ehreader.model.PhotoDao;
import tw.skyarrow.ehreader.util.DatabaseHelper;

public class ClearHistoryDialog extends DialogFragment {
    public static final String TAG = ClearHistoryDialog.class.getSimpleName();

    private GalleryDao galleryDao;
    private DownloadDao downloadDao;
    private PhotoDao photoDao;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        DatabaseHelper helper = DatabaseHelper.getInstance(getActivity());
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();
        galleryDao = daoSession.getGalleryDao();
        downloadDao = daoSession.getDownloadDao();
        photoDao = daoSession.getPhotoDao();

        builder.setTitle(R.string.clear_history_title)
                .setMessage(R.string.clear_history_msg)
                .setPositiveButton(R.string.ok, onSubmitClick)
                .setNegativeButton(R.string.cancel, null);

        return builder.create();
    }

    private DialogInterface.OnClickListener onSubmitClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            // TODO: clear history
        }
    };
}
