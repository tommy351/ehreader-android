package tw.skyarrow.ehreader.app.entry;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.api.API;
import tw.skyarrow.ehreader.api.APIService;
import tw.skyarrow.ehreader.model.DaoSession;
import tw.skyarrow.ehreader.model.Gallery;
import tw.skyarrow.ehreader.model.GalleryDao;
import tw.skyarrow.ehreader.model.GalleryDataRequest;
import tw.skyarrow.ehreader.model.GalleryId;
import tw.skyarrow.ehreader.util.DatabaseHelper;
import tw.skyarrow.ehreader.view.InfiniteScrollListener;
import tw.skyarrow.ehreader.view.RecyclerViewItemClickListener;

/**
 * Created by SkyArrow on 2015/9/24.
 */
public class WebFragment extends GalleryListFragment implements InfiniteScrollListener.ScrollListener {
    private static final Pattern pGalleryUrl = Pattern.compile("<a href=\"http://(?:g\\.e-|ex)hentai\\.org/g/(\\d+)/(\\w+)/\" onmouseover");

    @InjectView(R.id.container)
    SwipeRefreshLayout refreshLayout;

    @InjectView(R.id.list)
    RecyclerView recyclerView;

    private APIService api;
    private GalleryListAdapter listAdapter;
    private DatabaseHelper dbHelper;
    private GalleryDao galleryDao;
    private BehaviorSubject<Integer> refreshSubject = BehaviorSubject.create();
    private Subscription subscription;
    private int currentPage = 0;
    private boolean fragmentCreated = false;

    public static WebFragment create() {
        return new WebFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        api = API.getService(getActivity());
        dbHelper = DatabaseHelper.get(getActivity());
        DaoSession daoSession = dbHelper.open();
        galleryDao = daoSession.getGalleryDao();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery_list_web, container, false);
        ButterKnife.inject(this, view);

        subscription = refreshSubject
                .distinct()
                .subscribe(this::loadGalleryList);

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        listAdapter = new GalleryListAdapter(getActivity(), galleryList);
        listAdapter.setHasStableIds(true);
        recyclerView.setAdapter(listAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(getActivity(), this));
        refreshLayout.setOnRefreshListener(() -> refreshSubject.onNext(0));
        //recyclerView.addOnScrollListener(new InfiniteScrollListener(this));

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setTitle(R.string.label_latest);

        if (!fragmentCreated) {
            fragmentCreated = true;

            refreshLayout.post(() -> {
                refreshLayout.setRefreshing(true);
                refreshSubject.onNext(0);
            });
        }
    }

    @Override
    public void onDestroyView() {
        subscription.unsubscribe();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }

    private void loadGalleryList(int page) {
        api.getIndex(page)
                .flatMap(html -> {
                    List<GalleryId> list = new ArrayList<>();
                    Matcher matcher = pGalleryUrl.matcher(html);

                    while (matcher.find()) {
                        long id = Long.parseLong(matcher.group(1), 10);
                        GalleryId galleryId = new GalleryId(id, matcher.group(2));
                        list.add(galleryId);
                    }

                    GalleryId[] arr = list.toArray(new GalleryId[list.size()]);
                    GalleryDataRequest req = new GalleryDataRequest(arr);

                    return api.getGalleryData(req);
                })
                .flatMap(res -> Observable.from(res.getData()))
                .map(metaData -> {
                    Gallery gallery = galleryDao.load(metaData.getId());

                    if (gallery == null) {
                        gallery = new Gallery();
                    }

                    gallery.setId(metaData.getId());
                    gallery.setToken(metaData.getToken());
                    gallery.setTitle(metaData.getTitle());
                    gallery.setSubtitle(metaData.getTitleJpn());
                    gallery.setCategory(metaData.getCategory());
                    gallery.setThumbnail(metaData.getThumb());
                    gallery.setCount(metaData.getFileCount());
                    gallery.setRating(metaData.getRating());
                    gallery.setUploader(metaData.getUploader());
                    gallery.setCreated(metaData.getPosted());
                    gallery.setSize(metaData.getFileSize());
                    gallery.setTags(metaData.getTags());

                    galleryDao.insertOrReplaceInTx(gallery);
                    galleryList.add(gallery);

                    return gallery;
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Gallery>() {
                    @Override
                    public void onCompleted() {
                        currentPage++;
                        listAdapter.notifyDataSetChanged();
                        refreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        refreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onNext(Gallery gallery) {

                    }
                });
    }

    @Override
    public void onScrollToEnd() {
        if (galleryList.isEmpty() || galleryList.size() < 25) return;

        refreshSubject.onNext(currentPage);
    }
}
