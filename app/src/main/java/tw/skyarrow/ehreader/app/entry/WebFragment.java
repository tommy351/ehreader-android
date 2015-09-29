package tw.skyarrow.ehreader.app.entry;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
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
import tw.skyarrow.ehreader.util.L;
import tw.skyarrow.ehreader.view.InfiniteScrollListener;
import tw.skyarrow.ehreader.view.RecyclerViewItemClickListener;

/**
 * Created by SkyArrow on 2015/9/24.
 */
public class WebFragment extends GalleryListFragment implements InfiniteScrollListener.ScrollListener {
    private static final Pattern pGalleryUrl = Pattern.compile("<a href=\"http://(?:g\\.e-|ex)hentai\\.org/g/(\\d+)/(\\w+)/\" onmouseover");

    public static final String BASE_URL = "BASE_URL";

    @InjectView(R.id.container)
    SwipeRefreshLayout refreshLayout;

    @InjectView(R.id.list)
    RecyclerView recyclerView;

    private APIService api;
    private GalleryListAdapter listAdapter;
    private DatabaseHelper dbHelper;
    private GalleryDao galleryDao;
    private BehaviorSubject<Integer> subject;
    private Subscription subscription;
    private int currentPage = 0;
    private boolean fragmentCreated = false;
    private String baseUrl;
    private boolean loading = false;
    private boolean noMoreGallery = false;
    private boolean refreshList = false;

    public static WebFragment create() {
        return create("/");
    }

    public static WebFragment create(String baseUrl){
        WebFragment fragment = new WebFragment();
        Bundle args = new Bundle();

        args.putString(BASE_URL, baseUrl);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        api = API.getService(getActivity());
        dbHelper = DatabaseHelper.get(getActivity());
        DaoSession daoSession = dbHelper.open();
        galleryDao = daoSession.getGalleryDao();
        subject = BehaviorSubject.create();

        Bundle args = getArguments();
        baseUrl = args.getString(BASE_URL);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery_list_web, container, false);
        ButterKnife.inject(this, view);

        subscription = subject
                .filter(i -> !loading && !noMoreGallery)
                .flatMap(this::loadGalleryList)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        listAdapter = new GalleryListAdapter(getActivity(), galleryList);
        listAdapter.setHasStableIds(true);
        recyclerView.setAdapter(listAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(getActivity(), this));
        refreshLayout.setOnRefreshListener(() -> subject.onNext(0));
        recyclerView.addOnScrollListener(new InfiniteScrollListener(this));

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (!fragmentCreated){
            fragmentCreated = true;

            refreshLayout.post(() -> {
                refreshLayout.setRefreshing(true);
                subject.onNext(0);
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

    private Observable loadGalleryList(int page){
        loading = true;
        L.d("loadGalleryList: %s - %d", baseUrl, page);

        return api.getIndex(baseUrl, page)
                .flatMap(html -> {
                    List<GalleryId> list = new ArrayList<>();
                    Matcher matcher = pGalleryUrl.matcher(html);

                    while (matcher.find()) {
                        long id = Long.parseLong(matcher.group(1), 10);
                        String token = matcher.group(2);

                        L.d("Gallery found: %d - %s", id, token);
                        list.add(new GalleryId(id, token));
                    }

                    GalleryId[] arr = list.toArray(new GalleryId[list.size()]);
                    GalleryDataRequest req = new GalleryDataRequest(arr);

                    return api.getGalleryData(req);
                })
                .flatMap(res -> Observable.from(res.getData()))
                .map(metaData -> {
                    L.d("Gallery data retrieved: %d", metaData.getId());

                    Gallery gallery = galleryDao.load(metaData.getId());

                    if (gallery == null) {
                        gallery = new Gallery();
                        gallery.setDefaultFields();
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
                    return gallery;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(g -> {
                    int index = galleryList.indexOf(g);

                    if (index > -1) {
                        galleryList.set(index, g);
                    } else if (refreshList) {
                        galleryList.add(0, g);
                    } else {
                        galleryList.add(g);
                    }
                })
                .doOnCompleted(() -> {
                    if (!refreshList) currentPage++;
                    refreshLayout.setRefreshing(false);
                    listAdapter.notifyDataSetChanged();
                })
                .doOnError(L::e)
                .doOnTerminate(() -> {
                    loading = false;
                    refreshList = false;
                })
                .count()
                .doOnNext(count -> {
                    if (count < 25) noMoreGallery = true;
                });
    }

    @Override
    public void onScrollToEnd() {
        refreshLayout.setRefreshing(true);
        subject.onNext(currentPage);
    }
}
