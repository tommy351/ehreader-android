package tw.skyarrow.ehreader.activity;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.dao.query.QueryBuilder;
import tw.skyarrow.ehreader.Constant;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.adapter.GalleryListAdapter;
import tw.skyarrow.ehreader.db.DaoMaster;
import tw.skyarrow.ehreader.db.DaoSession;
import tw.skyarrow.ehreader.db.Gallery;
import tw.skyarrow.ehreader.db.GalleryDao;

/**
 * Created by SkyArrow on 2014/1/26.
 */
public class MainFragmentStar extends MainFragmentBase {
    @InjectView(R.id.list)
    ListView listView;

    @InjectView(R.id.error)
    TextView errorView;

    @InjectView(R.id.loading)
    ProgressBar loadingView;

    public static final String TAG = "MainFragmentStar";

    private SQLiteDatabase db;
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private GalleryDao galleryDao;

    private List<Gallery> galleryList;
    private GalleryListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.inject(this, view);

        Context context = getActivity();
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, Constant.DB_NAME, null);
        db = helper.getWritableDatabase();
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
        galleryDao = daoSession.getGalleryDao();

        QueryBuilder qb = galleryDao.queryBuilder();
        qb.where(GalleryDao.Properties.Starred.eq(true));
        qb.orderDesc(GalleryDao.Properties.Lastread);

        galleryList = qb.list();
        adapter = new GalleryListAdapter(context, galleryList);

        loadingView.setVisibility(View.GONE);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        if (galleryList.size() == 0) {
            errorView.setText(R.string.error_no_starred);
        }

        if (savedInstanceState != null) {
            listView.setSelection(savedInstanceState.getInt("position"));
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("position", listView.getSelectedItemPosition());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        db.close();
    }
}
