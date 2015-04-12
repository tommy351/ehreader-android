package tw.skyarrow.ehreader.app.main;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.app.gallery.GalleryActivity;
import tw.skyarrow.ehreader.model.Gallery;
import tw.skyarrow.ehreader.view.RecyclerViewItemClickListener;

public abstract class GalleryListFragment extends Fragment implements RecyclerViewItemClickListener.OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener {

    private static final int LOAD_THRESHOLD = 3;

    @InjectView(R.id.list)
    RecyclerView mRecyclerView;

    @InjectView(R.id.container)
    SwipeRefreshLayout mSwipeLayout;

    @InjectView(R.id.loading)
    ProgressBar mLoadingView;

    private List<Gallery> mGalleryList;
    private GalleryListAdapter mListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mGalleryList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gallery_list, container, false);
        ButterKnife.inject(this, view);

        // Set up layout manger
        int orientation = getResources().getConfiguration().orientation;
        int columns = orientation == Configuration.ORIENTATION_LANDSCAPE ? 3 : 2;
        final StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(columns, StaggeredGridLayoutManager.VERTICAL);

        // Set up RecyclerView
        mListAdapter = new GalleryListAdapter(getActivity(), mGalleryList);
        mListAdapter.setHasStableIds(true);
        layoutManager.setOrientation(StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mListAdapter);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int totalCount = layoutManager.getItemCount();
                int[] visibleItems = layoutManager.findLastVisibleItemPositions(null);
                Arrays.sort(visibleItems);
                int lastVisibleItem = visibleItems[visibleItems.length - 1];

                if (dy > 0 && lastVisibleItem > totalCount - LOAD_THRESHOLD){
                    onScrollToBottom();
                }
            }
        });
        mRecyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(getActivity(), this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // Set up SwipeRefreshLayout
        mSwipeLayout.setOnRefreshListener(this);

        return view;
    }

    protected List<Gallery> getGalleryList(){
        return mGalleryList;
    }

    protected void setGalleryList(List<Gallery> galleryList){
        mGalleryList = galleryList;
        getListAdapter().setGalleryList(galleryList);
    }

    protected void notifyDataSetChanged(){
        getListAdapter().notifyDataSetChanged();
    }

    protected void addGallery(Gallery gallery){
        getGalleryList().add(gallery);
    }

    protected void addGallery(int position, Gallery gallery){
        getGalleryList().add(position, gallery);
    }

    protected void addGalleryList(List<Gallery> galleryList){
        getGalleryList().addAll(galleryList);
    }

    protected SwipeRefreshLayout getSwipeRefreshLayout(){
        return mSwipeLayout;
    }

    protected GalleryListAdapter getListAdapter(){
        return mListAdapter;
    }

    protected ProgressBar getLoadingView(){
        return mLoadingView;
    }

    @Override
    public void onItemClick(View childView, int position) {
        if (mGalleryList == null || position >= mGalleryList.size()) return;

        Gallery data = mGalleryList.get(position);
        if (data == null) return;

        Intent intent = GalleryActivity.newIntent(getActivity(), data.getId(), data.getToken());
        startActivity(intent);
    }

    @Override
    public void onItemLongPress(View childView, int position) {

    }

    @Override
    public void onRefresh() {
        //
    }

    public void onScrollToBottom(){
        //
    }

    public boolean isLoading(){
        return mListAdapter.isLoading();
    }
}
