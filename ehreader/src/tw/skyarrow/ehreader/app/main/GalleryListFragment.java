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

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.app.gallery.GalleryActivity;
import tw.skyarrow.ehreader.model.Gallery;
import tw.skyarrow.ehreader.view.RecyclerViewItemClickListener;

public abstract class GalleryListFragment extends Fragment implements RecyclerViewItemClickListener.OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener {

    @InjectView(R.id.list)
    RecyclerView mRecyclerView;

    @InjectView(R.id.container)
    SwipeRefreshLayout mSwipeLayout;

    private List<Gallery> mGalleryList;
    private GalleryListAdapter mListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gallery_list, container, false);
        ButterKnife.inject(this, view);

        // Set up layout manger
        int orientation = getResources().getConfiguration().orientation;
        int columns = orientation == Configuration.ORIENTATION_LANDSCAPE ? 3 : 2;
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(columns, StaggeredGridLayoutManager.VERTICAL);

        // Set up RecyclerView
        mListAdapter = new GalleryListAdapter(getActivity(), mGalleryList);
        mRecyclerView.setAdapter(mListAdapter);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        mRecyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(getActivity(), this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // Set up SwipeRefreshLayout
        mSwipeLayout.setOnRefreshListener(this);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
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

    @Override
    public void onItemClick(View childView, int position) {
        if (mGalleryList == null) return;

        Gallery data = mGalleryList.get(position);
        if (data == null) return;

        Intent intent = new Intent(getActivity(), GalleryActivity.class);

        intent.putExtra(GalleryActivity.EXTRA_ID, data.getId());
        intent.putExtra(GalleryActivity.EXTRA_TOKEN, data.getToken());

        startActivity(intent);
    }

    @Override
    public void onItemLongPress(View childView, int position) {

    }

    @Override
    public void onRefresh() {

    }
}
