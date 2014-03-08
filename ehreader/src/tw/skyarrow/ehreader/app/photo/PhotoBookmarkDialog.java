package tw.skyarrow.ehreader.app.photo;

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
import de.greenrobot.event.EventBus;
import tw.skyarrow.ehreader.BaseApplication;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.db.DaoMaster;
import tw.skyarrow.ehreader.db.DaoSession;
import tw.skyarrow.ehreader.db.Photo;
import tw.skyarrow.ehreader.db.PhotoDao;
import tw.skyarrow.ehreader.event.PhotoBookmarkDialogEvent;
import tw.skyarrow.ehreader.util.DatabaseHelper;

/**
 * Created by SkyArrow on 2014/2/2.
 */
public class PhotoBookmarkDialog extends DialogFragment {
    public static final String TAG = "PhotoBookmarkDialog";

    public static final String EXTRA_GALLERY = "id";

    private long galleryId;
    private List<Photo> photoList;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        Bundle args = getArguments();
        galleryId = args.getLong(EXTRA_GALLERY);

        DatabaseHelper helper = DatabaseHelper.getInstance(getActivity());
        SQLiteDatabase db = helper.getReadableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();
        PhotoDao photoDao = daoSession.getPhotoDao();

        QueryBuilder<Photo> qb = photoDao.queryBuilder();
        qb.where(qb.and(
                PhotoDao.Properties.GalleryId.eq(galleryId),
                PhotoDao.Properties.Bookmarked.eq(true)
        ));
        photoList = qb.list();

        if (photoList.size() > 0) {
            int size = photoList.size();
            String[] menuItems = new String[size];

            for (int i = 0; i < size; i++) {
                menuItems[i] = String.format(getString(R.string.bookmark_list_item),
                        photoList.get(i).getPage());
            }

            dialog.setItems(menuItems, onItemClick);
        } else {
            dialog.setMessage(R.string.no_bookmarks);
        }

        dialog.setTitle(R.string.bookmark_list);

        MapBuilder builder = MapBuilder.createAppView();
        builder.set(Fields.SCREEN_NAME, TAG);

        BaseApplication.getTracker().send(builder.build());

        return dialog.create();
    }

    private DialogInterface.OnClickListener onItemClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            Photo photo = photoList.get(i);
            if (photo == null) return;

            EventBus.getDefault().post(new PhotoBookmarkDialogEvent(galleryId, photo.getPage()));
        }
    };
}
