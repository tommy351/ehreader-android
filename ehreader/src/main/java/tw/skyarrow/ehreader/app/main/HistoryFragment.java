package tw.skyarrow.ehreader.app.main;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import de.greenrobot.dao.query.QueryBuilder;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.model.DaoMaster;
import tw.skyarrow.ehreader.model.DaoSession;
import tw.skyarrow.ehreader.model.Gallery;
import tw.skyarrow.ehreader.model.GalleryDao;
import tw.skyarrow.ehreader.util.DatabaseHelper;

public class HistoryFragment extends GalleryListFragment {
    public static final String TAG = HistoryFragment.class.getSimpleName();

    public static HistoryFragment newInstance(){
        return new HistoryFragment();
    }

    private DatabaseHelper dbHelper;
    private GalleryDao galleryDao;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get database instance
        dbHelper = DatabaseHelper.getInstance(getActivity());
        DaoMaster daoMaster = new DaoMaster(dbHelper.open());
        DaoSession daoSession = daoMaster.newSession();
        galleryDao = daoSession.getGalleryDao();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getSwipeRefreshLayout().setEnabled(false);

        // Set toolbar title
        ActionBarActivity activity = (ActionBarActivity) getActivity();
        activity.getSupportActionBar().setTitle(getString(R.string.label_history));

        if (savedInstanceState == null){
            QueryBuilder<Gallery> qb = galleryDao.queryBuilder();
            qb.where(GalleryDao.Properties.Lastread.isNotNull());
            qb.orderDesc(GalleryDao.Properties.Lastread);
            addGalleryList(qb.list());
            notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}
