package tw.skyarrow.ehreader.app.main;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.dao.query.QueryBuilder;
import de.greenrobot.event.EventBus;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.app.gallery.GalleryActivity;
import tw.skyarrow.ehreader.event.GalleryDownloadEvent;
import tw.skyarrow.ehreader.model.DaoMaster;
import tw.skyarrow.ehreader.model.DaoSession;
import tw.skyarrow.ehreader.model.Download;
import tw.skyarrow.ehreader.model.DownloadDao;
import tw.skyarrow.ehreader.util.DatabaseHelper;
import tw.skyarrow.ehreader.view.RecyclerViewItemClickListener;

public class DownloadListFragment extends Fragment implements RecyclerViewItemClickListener.OnItemClickListener {
    public static final String TAG = DownloadListFragment.class.getSimpleName();

    @InjectView(R.id.list)
    RecyclerView mRecyclerView;

    private List<Download> mDownloadList;
    private DownloadListAdapter mListAdapter;
    private DatabaseHelper dbHelper;
    private DownloadDao downloadDao;

    public static DownloadListFragment newInstance(){
        return new DownloadListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        dbHelper = DatabaseHelper.getInstance(getActivity());
        SQLiteDatabase db = dbHelper.open();
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();
        downloadDao = daoSession.getDownloadDao();
        mDownloadList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.download_list, container, false);
        ButterKnife.inject(this, view);

        mListAdapter = new DownloadListAdapter(getActivity(), mDownloadList);
        mListAdapter.setHasStableIds(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        mRecyclerView.setAdapter(mListAdapter);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(getActivity(), this));

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set toolbar title
        ActionBarActivity activity = (ActionBarActivity) getActivity();
        activity.getSupportActionBar().setTitle(getString(R.string.label_downloads));

        if (savedInstanceState == null){
            QueryBuilder<Download> qb = downloadDao.queryBuilder();
            qb.orderDesc(DownloadDao.Properties.Created);
            mDownloadList.addAll(qb.list());
            mListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().registerSticky(this);

        GalleryDownloadEvent lastEvent = EventBus.getDefault().getStickyEvent(GalleryDownloadEvent.class);
        if (lastEvent != null) onEventMainThread(lastEvent);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }

    @Override
    public void onItemClick(View childView, int position) {
        Download item = mDownloadList.get(position);
        if (item == null) return;

        Intent intent = GalleryActivity.newIntent(getActivity(), item.getId(), null);
        startActivity(intent);
    }

    @Override
    public void onItemLongPress(View childView, int position) {

    }

    public void onEventMainThread(GalleryDownloadEvent event){
        Download download = event.getDownload();
        if (download == null) return;

        int index = mDownloadList.indexOf(download);
        mDownloadList.set(index, download);
        mListAdapter.notifyItemChanged(index);
    }
}
