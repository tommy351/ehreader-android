package tw.skyarrow.ehreader.app.main;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.dao.query.QueryBuilder;
import tw.skyarrow.ehreader.BaseApplication;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.db.DaoMaster;
import tw.skyarrow.ehreader.db.DaoSession;
import tw.skyarrow.ehreader.db.Gallery;
import tw.skyarrow.ehreader.db.GalleryDao;
import tw.skyarrow.ehreader.util.DatabaseHelper;

/**
 * Created by SkyArrow on 2014/2/2.
 */
public class MainFragmentHistory extends MainFragmentBase {
    @InjectView(R.id.error)
    TextView errorView;

    public static final String TAG = "MainFragmentHistory";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        ButterKnife.inject(this, view);

        DatabaseHelper helper = DatabaseHelper.getInstance(getActivity());
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();
        GalleryDao galleryDao = daoSession.getGalleryDao();

        QueryBuilder<Gallery> qb = galleryDao.queryBuilder();
        qb.where(GalleryDao.Properties.Lastread.isNotNull());
        qb.orderDesc(GalleryDao.Properties.Lastread);
        getList().addAll(qb.list());
        notifyDataSetChanged();

        if (getCount() == 0) {
            errorView.setText(R.string.error_no_history);
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        MapBuilder builder = MapBuilder.createAppView();
        builder.set(Fields.SCREEN_NAME, TAG);

        BaseApplication.getTracker().send(builder.build());
    }
}
