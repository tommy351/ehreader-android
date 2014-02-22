package tw.skyarrow.ehreader.app.download;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;

import tw.skyarrow.ehreader.BaseApplication;
import tw.skyarrow.ehreader.Constant;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.app.gallery.GalleryActivity;
import tw.skyarrow.ehreader.db.DaoMaster;
import tw.skyarrow.ehreader.db.DaoSession;
import tw.skyarrow.ehreader.db.Download;
import tw.skyarrow.ehreader.db.DownloadDao;
import tw.skyarrow.ehreader.service.GalleryDownloadService;

/**
 * Created by SkyArrow on 2014/2/1.
 */
public class DownloadContextMenu extends DialogFragment {
    public static final String TAG = "DownloadContextMenu";

    public static final String EXTRA_GALLERY = "id";
    public static final String EXTRA_TITLE = "title";

    private SQLiteDatabase db;
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private DownloadDao downloadDao;

    private long galleryId;
    private Download download;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        Bundle args = getArguments();
        galleryId = args.getLong(EXTRA_GALLERY);

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(getActivity(), Constant.DB_NAME, null);
        db = helper.getReadableDatabase();
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
        downloadDao = daoSession.getDownloadDao();

        download = downloadDao.load(galleryId);

        String[] menu = {
                getString(R.string.download_open),
                "",
                getString(R.string.download_delete)
        };

        switch (download.getStatus()) {
            case Download.STATUS_SUCCESS:
                menu[1] = getString(R.string.download_redownload);
                break;

            case Download.STATUS_DOWNLOADING:
            case Download.STATUS_PENDING:
                menu[1] = getString(R.string.download_pause);
                break;

            case Download.STATUS_PAUSED:
            case Download.STATUS_ERROR:
                menu[1] = getString(R.string.download_start);
                break;
        }

        dialog.setTitle(args.getString(EXTRA_TITLE))
                .setItems(menu, onItemClick);

        MapBuilder builder = MapBuilder.createAppView();
        builder.set(Fields.SCREEN_NAME, TAG);

        BaseApplication.getTracker().send(builder.build());

        return dialog.create();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        db.close();
    }

    private DialogInterface.OnClickListener onItemClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            switch (i) {
                case 0:
                    openGallery();
                    break;

                case 1:
                    performDownloadOperation();
                    break;

                case 2:
                    deleteGallery();
                    break;
            }
        }
    };

    private void openGallery() {
        Intent intent = new Intent(getActivity(), GalleryActivity.class);
        Bundle args = new Bundle();

        args.putLong(GalleryActivity.EXTRA_GALLERY, galleryId);
        intent.putExtras(args);

        startActivity(intent);
    }

    private void performDownloadOperation() {
        switch (download.getStatus()) {
            case Download.STATUS_SUCCESS:
                redownload();
                break;

            case Download.STATUS_DOWNLOADING:
            case Download.STATUS_PENDING:
                pauseDownload();
                break;

            case Download.STATUS_PAUSED:
            case Download.STATUS_ERROR:
                startDownload();
                break;
        }
    }

    private void redownload() {
        Bundle args = new Bundle();
        DialogFragment dialog = new RedownloadDialog();

        args.putLong(RedownloadDialog.EXTRA_GALLERY, galleryId);

        dialog.setArguments(args);
        dialog.show(getActivity().getSupportFragmentManager(), RedownloadDialog.TAG);
    }

    private void startDownload() {
        Intent intent = new Intent(getActivity(), GalleryDownloadService.class);

        intent.setAction(GalleryDownloadService.ACTION_START);
        intent.putExtra(GalleryDownloadService.GALLERY_ID, download.getId());
        getActivity().startService(intent);
    }

    private void pauseDownload() {
        Intent intent = new Intent(getActivity(), GalleryDownloadService.class);

        intent.setAction(GalleryDownloadService.ACTION_PAUSE);
        intent.putExtra(GalleryDownloadService.GALLERY_ID, download.getId());
        getActivity().startService(intent);
    }

    private void deleteGallery() {
        DialogFragment dialog = new DownloadDeleteConfirmDialog();
        Bundle args = new Bundle();

        args.putLong(DownloadDeleteConfirmDialog.EXTRA_GALLERY, galleryId);

        dialog.setArguments(args);
        dialog.show(getActivity().getSupportFragmentManager(), DownloadDeleteConfirmDialog.TAG);
    }
}
