package tw.skyarrow.ehreader.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.models_old.Gallery;

public abstract class GalleryListFragment extends Fragment {
    private List<Gallery> mGalleryList;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private GalleryListAdapter mListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGalleryList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery_list, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.list);
        mListAdapter = new GalleryListAdapter(mGalleryList);
        mLayoutManager = new LinearLayoutManager(getActivity());

        mRecyclerView.setAdapter(mListAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        return view;
    }

    public List<Gallery> getGalleryList(){
        return mGalleryList;
    }

    public void setGalleryList(List<Gallery> galleryList){
        mGalleryList = galleryList;
    }

    public void notifyDataSetChanged(){
        mListAdapter.notifyDataSetChanged();
    }

    public void addGallery(Gallery gallery){
        mGalleryList.add(gallery);
    }

    public void addGallery(int position, Gallery gallery){
        mGalleryList.add(position, gallery);
    }

    public void addGalleryList(List<Gallery> galleryList){
        mGalleryList.addAll(galleryList);
    }

    public void onScrollToBottom(){
        //
    }
}
