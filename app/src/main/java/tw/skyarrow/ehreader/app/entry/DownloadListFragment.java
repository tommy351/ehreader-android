package tw.skyarrow.ehreader.app.entry;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
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
import rx.Subscription;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.app.gallery.GalleryActivity;
import tw.skyarrow.ehreader.model.DaoSession;
import tw.skyarrow.ehreader.model.Download;
import tw.skyarrow.ehreader.model.DownloadDao;
import tw.skyarrow.ehreader.model.Gallery;
import tw.skyarrow.ehreader.service.GalleryDownloadService;
import tw.skyarrow.ehreader.util.DatabaseHelper;
import tw.skyarrow.ehreader.view.RecyclerViewItemClickListener;

/**
 * Created by SkyArrow on 2015/9/24.
 */
public class DownloadListFragment extends Fragment implements RecyclerViewItemClickListener.OnItemClickListener {
    @InjectView(R.id.list)
    RecyclerView recyclerView;

    private DownloadListAdapter listAdapter;
    private List<Download> downloadList;
    private DatabaseHelper dbHelper;
    private DownloadDao downloadDao;
    private Subscription subscription;

    public static DownloadListFragment create(){
        return new DownloadListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        dbHelper = DatabaseHelper.get(getActivity());
        DaoSession daoSession = dbHelper.open();
        downloadDao = daoSession.getDownloadDao();
        downloadList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_download_list, container, false);
        ButterKnife.inject(this, view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        listAdapter = new DownloadListAdapter(getActivity(), downloadList);
        listAdapter.setHasStableIds(true);
        recyclerView.setAdapter(listAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(getActivity(), this));

        subscription = GalleryDownloadService.getBus()
                .subscribe(download -> {
                    int index = downloadList.indexOf(download);
                    downloadList.set(index, download);
                    listAdapter.notifyItemChanged(index);
                });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null){
            QueryBuilder<Download> qb = downloadDao.queryBuilder();
            qb.orderDesc(DownloadDao.Properties.Created);
            downloadList.addAll(qb.list());
            listAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        subscription.unsubscribe();
        super.onDestroyView();
    }

    @Override
    public void onItemClick(View view, int position) {
        Download download = downloadList.get(position);
        Intent intent = GalleryActivity.intent(getActivity(), download.getId());

        startActivity(intent);
    }

    @Override
    public void onItemLongPress(View view, int position) {

    }
}
