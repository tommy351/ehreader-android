package tw.skyarrow.ehreader.app.download;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.model.DaoMaster;
import tw.skyarrow.ehreader.model.DaoSession;
import tw.skyarrow.ehreader.model.Gallery;
import tw.skyarrow.ehreader.model.GalleryDao;
import tw.skyarrow.ehreader.service.GalleryDownloadService;
import tw.skyarrow.ehreader.util.DatabaseHelper;
import tw.skyarrow.ehreader.util.FileHelper;

public class DownloadConfirmDialog extends DialogFragment {
    public static final String TAG = DownloadConfirmDialog.class.getSimpleName();

    public static final String EXTRA_ID = "id";

    private Gallery mGallery;
    private DatabaseHelper dbHelper;
    private GalleryDao galleryDao;

    public static DownloadConfirmDialog newInstance(long id){
        DownloadConfirmDialog dialog = new DownloadConfirmDialog();
        Bundle args = new Bundle();

        args.putLong(EXTRA_ID, id);
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHelper = DatabaseHelper.getInstance(getActivity());
        SQLiteDatabase db = dbHelper.open();
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();
        galleryDao = daoSession.getGalleryDao();

        Bundle args = getArguments();
        long id = args.getLong(EXTRA_ID);
        mGallery = galleryDao.load(id);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String message = String.format(getString(R.string.download_confirm_msg), FileHelper.toBytes(mGallery.getSize()));

        builder.setTitle(getString(R.string.download_confirm_title))
                .setMessage(message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startDownload();
                    }
                })
                .setNegativeButton(R.string.cancel, null);

        return builder.create();
    }

    @Override
    public void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }

    private void startDownload(){
        Intent intent = GalleryDownloadService.newIntent(getActivity(), GalleryDownloadService.ACTION_START, mGallery.getId());
        getActivity().startService(intent);
    }
}
