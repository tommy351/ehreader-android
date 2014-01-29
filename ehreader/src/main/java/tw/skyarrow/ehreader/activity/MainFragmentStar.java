package tw.skyarrow.ehreader.activity;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.androidquery.AQuery;

import org.w3c.dom.Text;

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
public class MainFragmentStar extends Fragment implements AdapterView.OnItemClickListener {
    @InjectView(R.id.list)
    ListView listView;

    @InjectView(R.id.error)
    TextView errorView;

    private SQLiteDatabase db;
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private GalleryDao galleryDao;

    private AQuery aq;
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

        aq = new AQuery(view);
        galleryList = qb.list();
        adapter = new GalleryListAdapter(context, galleryList);

        aq.id(R.id.loading).gone();
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
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Gallery gallery = galleryList.get(i);
        Intent intent = new Intent(getActivity(), GalleryActivity.class);
        Bundle args = new Bundle();

        args.putLong("id", gallery.getId());
        intent.putExtras(args);
        startActivity(intent);
    }
}
