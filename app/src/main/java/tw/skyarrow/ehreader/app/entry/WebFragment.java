package tw.skyarrow.ehreader.app.entry;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.dao.query.QueryBuilder;
import retrofit.Callback;
import retrofit.Response;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.api.API;
import tw.skyarrow.ehreader.api.APIService;
import tw.skyarrow.ehreader.api.EHCrawler;
import tw.skyarrow.ehreader.model.DaoMaster;
import tw.skyarrow.ehreader.model.DaoSession;
import tw.skyarrow.ehreader.model.Gallery;
import tw.skyarrow.ehreader.model.GalleryDao;
import tw.skyarrow.ehreader.model.GalleryDataRequest;
import tw.skyarrow.ehreader.model.GalleryDataResponse;
import tw.skyarrow.ehreader.model.GalleryId;
import tw.skyarrow.ehreader.util.DatabaseHelper;

/**
 * Created by SkyArrow on 2015/9/24.
 */
public class WebFragment extends Fragment {
    public static final String TAG = WebFragment.class.getSimpleName();

    @InjectView(R.id.container)
    SwipeRefreshLayout refreshLayout;

    @InjectView(R.id.list)
    RecyclerView recyclerView;

    private APIService api;
    private List<Gallery> galleryList;
    private GalleryListAdapter listAdapter;
    private DatabaseHelper dbHelper;
    private GalleryDao galleryDao;

    public static WebFragment create(){
        return new WebFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        api = API.getService(getActivity());
        galleryList = new ArrayList<>();
        dbHelper = DatabaseHelper.get(getActivity());
        DaoSession daoSession = dbHelper.open();
        galleryDao = daoSession.getGalleryDao();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery_list_web, container, false);
        ButterKnife.inject(this, view);

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        listAdapter = new GalleryListAdapter(getActivity(), galleryList);
        listAdapter.setHasStableIds(true);
        recyclerView.setAdapter(listAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null){
            refreshLayout.post(() -> {
                refreshLayout.setRefreshing(true);
                loadGalleryList();
            });
        }
    }

    @Override
    public void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }

    private void loadGalleryList(){
        api.getIndex(0).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Response<String> response) {
                // TODO: error handling
                if (!response.isSuccess()) return;

                GalleryId[] gidlist = EHCrawler.parseGalleryIndex(response.body());
                loadGalleryData(gidlist);
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void loadGalleryData(GalleryId[] gidlist){
        GalleryDataRequest req = new GalleryDataRequest(gidlist);

        api.getGalleryData(req).enqueue(new Callback<GalleryDataResponse>() {
            @Override
            public void onResponse(Response<GalleryDataResponse> response) {
                // TODO: error handling
                if (!response.isSuccess()) return;

                List<Gallery> list = EHCrawler.parseGalleryDataResponse(response.body());
                galleryDao.insertOrReplaceInTx(list);

                int size = galleryList.size();
                galleryList.addAll(list);
                listAdapter.notifyItemRangeInserted(size, list.size());
                refreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
