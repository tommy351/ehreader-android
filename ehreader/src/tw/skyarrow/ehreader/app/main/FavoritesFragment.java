package tw.skyarrow.ehreader.app.main;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import de.greenrobot.dao.query.QueryBuilder;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.model.DaoMaster;
import tw.skyarrow.ehreader.model.DaoSession;
import tw.skyarrow.ehreader.model.Gallery;
import tw.skyarrow.ehreader.model.GalleryDao;
import tw.skyarrow.ehreader.util.DatabaseHelper;

public class FavoritesFragment extends GalleryListFragment {
    public static final String TAG = FavoritesFragment.class.getSimpleName();

    public static FavoritesFragment newInstance(){
        return new FavoritesFragment();
    }

    private GalleryDao galleryDao;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get database instance
        SQLiteDatabase db = DatabaseHelper.getWritableDatabase(getActivity());
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();
        galleryDao = daoSession.getGalleryDao();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set toolbar title
        ActionBarActivity activity = (ActionBarActivity) getActivity();
        activity.getSupportActionBar().setTitle(getResources().getString(R.string.label_favorites));

        if (savedInstanceState == null){
            QueryBuilder<Gallery> qb = galleryDao.queryBuilder();
            qb.where(GalleryDao.Properties.Starred.eq(true));
            qb.orderDesc(GalleryDao.Properties.Lastread);
            addGalleryList(qb.list());
            notifyDataSetChanged();
        }
    }
}
