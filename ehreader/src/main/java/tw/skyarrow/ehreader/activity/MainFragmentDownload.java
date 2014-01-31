package tw.skyarrow.ehreader.activity;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.androidquery.AQuery;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.dao.query.QueryBuilder;
import de.greenrobot.event.EventBus;
import tw.skyarrow.ehreader.Constant;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.adapter.DownloadListAdapter;
import tw.skyarrow.ehreader.db.DaoMaster;
import tw.skyarrow.ehreader.db.DaoSession;
import tw.skyarrow.ehreader.db.Gallery;
import tw.skyarrow.ehreader.db.GalleryDao;
import tw.skyarrow.ehreader.db.Photo;
import tw.skyarrow.ehreader.db.PhotoDao;
import tw.skyarrow.ehreader.event.GalleryDownloadEvent;
import tw.skyarrow.ehreader.service.GalleryDownloadService;
import tw.skyarrow.ehreader.util.GalleryDownload;

/**
 * Created by SkyArrow on 2014/1/26.
 */
public class MainFragmentDownload extends Fragment implements AdapterView.OnItemClickListener {
    @InjectView(R.id.list)
    ListView listView;

    @InjectView(R.id.error)
    TextView errorView;

    @InjectView(R.id.loading)
    ProgressBar loadingView;

    private SQLiteDatabase db;
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private GalleryDao galleryDao;

    private List<GalleryDownload> galleryList;
    private DownloadListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.inject(this, view);
        setHasOptionsMenu(true);

        Context context = getActivity();
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, Constant.DB_NAME, null);
        db = helper.getWritableDatabase();
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
        galleryDao = daoSession.getGalleryDao();

        galleryList = new ArrayList<GalleryDownload>();
        adapter = new DownloadListAdapter(context, galleryList);

        QueryBuilder galleryQb = galleryDao.queryBuilder();
        galleryQb.where(GalleryDao.Properties.DownloadStatus.notEq(GalleryDownloadService.STATUS_NOT_DOWNLOADED));
        galleryQb.orderDesc(GalleryDao.Properties.Downloaded);
        List<Gallery> tmpList = galleryQb.list();

        loadingView.setVisibility(View.GONE);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        if (tmpList.size() > 0) {
            for (Gallery gallery : tmpList) {
                int count = 0;

                for (Photo photo : gallery.getPhotos()) {
                    if (photo.getDownloaded()) count++;
                }

                galleryList.add(new GalleryDownload(gallery, count));
            }

            adapter.notifyDataSetChanged();
        } else {
            errorView.setText(R.string.error_no_download);
        }

        if (savedInstanceState != null) {
            listView.setSelection(savedInstanceState.getInt("position"));
        }

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.download, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_start_all:
                return true;

            case R.id.menu_pause_all:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("position", listView.getSelectedItemPosition());
    }

    public void onEventMainThread(GalleryDownloadEvent event) {
        Gallery eGallery = event.getGallery();
        boolean exist = false;
        int progress = 0;

        if (event.getCode() == GalleryDownloadService.EVENT_PROGRESS) {
            progress = (Integer) event.getExtra();
        }

        for (GalleryDownload gallery : galleryList) {
            if (gallery.getGallery().getId() == eGallery.getId()) {
                gallery.setGallery(eGallery);
                gallery.setDownloadProgress(progress);

                exist = true;
                break;
            }
        }

        if (!exist) {
            galleryList.add(0, new GalleryDownload(eGallery, progress));
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        GalleryDownload gallery = (GalleryDownload) adapterView.getAdapter().getItem(i);

        if (gallery == null) return;

        Intent intent = new Intent(getActivity(), GalleryActivity.class);
        Bundle args = new Bundle();

        args.putLong("id", gallery.getGallery().getId());
        intent.putExtras(args);
        startActivity(intent);
    }
}
