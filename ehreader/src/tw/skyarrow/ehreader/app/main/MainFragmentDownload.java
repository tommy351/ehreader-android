package tw.skyarrow.ehreader.app.main;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.dao.query.QueryBuilder;
import de.greenrobot.event.EventBus;
import tw.skyarrow.ehreader.BaseApplication;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.db.DaoMaster;
import tw.skyarrow.ehreader.db.DaoSession;
import tw.skyarrow.ehreader.db.Download;
import tw.skyarrow.ehreader.db.DownloadDao;
import tw.skyarrow.ehreader.event.GalleryDeleteEvent;
import tw.skyarrow.ehreader.event.GalleryDownloadEvent;
import tw.skyarrow.ehreader.event.ListUpdateEvent;
import tw.skyarrow.ehreader.service.GalleryDownloadService;
import tw.skyarrow.ehreader.util.DatabaseHelper;
import tw.skyarrow.ehreader.util.DownloadHelper;
import tw.skyarrow.ehreader.widget.InfiniteScrollListener;

/**
 * Created by SkyArrow on 2014/1/26.
 */
public class MainFragmentDownload extends Fragment implements AbsListView.OnScrollListener {
    @InjectView(R.id.list)
    ListView listView;

    @InjectView(R.id.error)
    TextView errorView;

    @InjectView(R.id.loading)
    ProgressBar loadingView;

    public static final String TAG = "MainFragmentDownload";

    public static final String EXTRA_POSITION = "position";

    private List<Download> downloadList;
    private DownloadListAdapter adapter;
    private EventBus bus;
    private DownloadHelper downloadHelper;
    private boolean isDownloading = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.inject(this, view);
        setHasOptionsMenu(true);

        bus = EventBus.getDefault();
        bus.register(this);

        DatabaseHelper helper = DatabaseHelper.getInstance(getActivity());
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();
        DownloadDao downloadDao = daoSession.getDownloadDao();
        downloadHelper = DownloadHelper.getInstance(getActivity());

        QueryBuilder<Download> qb = downloadDao.queryBuilder();
        qb.orderDesc(DownloadDao.Properties.Created);

        downloadList = qb.list();
        adapter = new DownloadListAdapter(getActivity(), downloadList);
        isDownloading = downloadHelper.isServiceRunning();

        listView.setAdapter(adapter);
        listView.setOnScrollListener(this);
        loadingView.setVisibility(View.GONE);

        if (downloadList.size() == 0) {
            errorView.setText(R.string.error_no_download);
        }

        if (savedInstanceState != null) {
            listView.setSelection(savedInstanceState.getInt(EXTRA_POSITION));
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bus.unregister(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.download, menu);

        if (isDownloading) {
            menu.findItem(R.id.menu_start_all).setVisible(false);
            menu.findItem(R.id.menu_pause_all).setVisible(true);
        } else {
            menu.findItem(R.id.menu_start_all).setVisible(true);
            menu.findItem(R.id.menu_pause_all).setVisible(false);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_start_all:
                startAll();
                return true;

            case R.id.menu_pause_all:
                pauseAll();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(EXTRA_POSITION, listView.getSelectedItemPosition());
    }

    public void onEventMainThread(GalleryDownloadEvent event) {
        switch (event.getCode()) {
            case GalleryDownloadService.EVENT_DOWNLOADING:
            case GalleryDownloadService.EVENT_PAUSED:
            case GalleryDownloadService.EVENT_SUCCESS:
            case GalleryDownloadService.EVENT_PENDING:
            case GalleryDownloadService.EVENT_ERROR:
                Download eDownload = event.getDownload();
                long id = eDownload.getId();
                boolean exist = false;

                for (int i = 0, len = downloadList.size(); i < len; i++) {
                    Download download = downloadList.get(i);

                    if (download.getId() == id) {
                        exist = true;
                        downloadList.set(i, eDownload);
                        break;
                    }
                }

                if (!exist) {
                    downloadList.add(0, eDownload);
                }

                adapter.notifyDataSetChanged();
                break;

            case GalleryDownloadService.EVENT_SERVICE_START:
                isDownloading = true;
                invalidateOptionsMenu();
                break;

            case GalleryDownloadService.EVENT_SERVICE_STOP:
                isDownloading = false;
                invalidateOptionsMenu();
                break;
        }
    }

    public void onEvent(GalleryDeleteEvent event) {
        for (int i = 0; i < downloadList.size(); i++) {
            if (downloadList.get(i).getId() == event.getId()) {
                downloadList.remove(i);
                break;
            }
        }

        adapter.notifyDataSetChanged();
    }

    public void onEvent(ListUpdateEvent event) {
        adapter.notifyDataSetChanged();
    }

    private void startAll() {
        downloadHelper.startAll();
    }

    private void pauseAll() {
        downloadHelper.pauseAll();
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int state) {
        if (state == InfiniteScrollListener.SCROLL_STATE_FLING) {
            adapter.setScrolling(true);
        } else {
            adapter.setScrolling(false);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onScroll(AbsListView absListView, int i, int i2, int i3) {

    }

    private void invalidateOptionsMenu() {
        getActivity().invalidateOptionsMenu();
    }
}
