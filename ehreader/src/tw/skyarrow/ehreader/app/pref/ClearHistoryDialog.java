package tw.skyarrow.ehreader.app.pref;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;

import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;
import tw.skyarrow.ehreader.BaseApplication;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.db.DaoMaster;
import tw.skyarrow.ehreader.db.DaoSession;
import tw.skyarrow.ehreader.db.DownloadDao;
import tw.skyarrow.ehreader.db.Gallery;
import tw.skyarrow.ehreader.db.GalleryDao;
import tw.skyarrow.ehreader.db.Photo;
import tw.skyarrow.ehreader.db.PhotoDao;
import tw.skyarrow.ehreader.util.DatabaseHelper;

/**
 * Created by SkyArrow on 2014/2/3.
 */
public class ClearHistoryDialog extends DialogFragment {
    public static final String TAG = "ClearHistoryDialog";

    private GalleryDao galleryDao;
    private DownloadDao downloadDao;
    private PhotoDao photoDao;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());

        DatabaseHelper helper = DatabaseHelper.getInstance(getActivity());
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();
        galleryDao = daoSession.getGalleryDao();
        downloadDao = daoSession.getDownloadDao();
        photoDao = daoSession.getPhotoDao();

        dialog.setTitle(R.string.clear_history_title)
                .setMessage(R.string.clear_history_msg)
                .setPositiveButton(R.string.ok, onSubmitClick)
                .setNegativeButton(R.string.cancel, null);

        MapBuilder builder = MapBuilder.createAppView();
        builder.set(Fields.SCREEN_NAME, TAG);

        BaseApplication.getTracker().send(builder.build());

        return dialog.create();
    }

    private DialogInterface.OnClickListener onSubmitClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int n) {
            QueryBuilder<Gallery> galleryQb = galleryDao.queryBuilder();
            galleryQb.where(GalleryDao.Properties.Starred.notEq(true));
            List<Gallery> galleryList = galleryQb.list();

            for (Gallery gallery : galleryList) {
                if (!gallery.getStarred() && downloadDao.load(gallery.getId()) == null) {
                    QueryBuilder<Photo> photoQb = photoDao.queryBuilder();
                    photoQb.where(PhotoDao.Properties.GalleryId.eq(gallery.getId()));
                    List<Photo> photoList = photoQb.list();

                    for (Photo photo : photoList) {
                        photoDao.deleteInTx(photo);
                    }

                    galleryDao.deleteInTx(gallery);
                }
            }
        }
    };
}
