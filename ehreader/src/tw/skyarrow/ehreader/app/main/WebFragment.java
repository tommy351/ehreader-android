package tw.skyarrow.ehreader.app.main;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import java.util.List;

import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.model.EHCrawler;
import tw.skyarrow.ehreader.model.Gallery;
import tw.skyarrow.ehreader.util.L;

public class WebFragment extends GalleryListFragment {
    public static final String TAG = WebFragment.class.getSimpleName();

    public static final String EXTRA_BASE_URL = "base_url";

    private static final int GALLERY_PER_PAGE = 25;

    private int mPage;
    private String mBaseUrl;
    private boolean isRefreshing;
    private boolean isEnd;
    private EHCrawler ehCrawler;

    public static WebFragment newInstance(String baseUrl){
        WebFragment fragment = new WebFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_BASE_URL, baseUrl);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get arguments
        Bundle args = getArguments();
        mBaseUrl = args.getString(EXTRA_BASE_URL);
        ehCrawler = new EHCrawler(getActivity());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set toolbar title
        ActionBarActivity activity = (ActionBarActivity) getActivity();
        activity.getSupportActionBar().setTitle(getString(R.string.label_latest));

        // Load the first page
        if (savedInstanceState == null){
            mPage = 0;
            isRefreshing = false;
            isEnd = false;

            loadIndex(mPage);
        }
    }

    @Override
    public void onDestroy() {
        ehCrawler.close();
        super.onDestroy();
    }

    @Override
    public void onRefresh() {
        isRefreshing = true;
        loadIndex(0);
    }

    private void loadIndex(int page){
        if (getListAdapter().isLoading()) return;

        ehCrawler.getGalleryList(mBaseUrl, page, new EHCrawler.Listener() {
            @Override
            public void onGalleryListResponse(List<Gallery> galleryList) {
                if (isRefreshing){
                    isRefreshing = false;
                    isEnd = false;
                    mPage = 0;
                    getGalleryList().clear();
                }

                if (galleryList.size() < GALLERY_PER_PAGE){
                    isEnd = true;
                }

                mPage++;
                addGalleryList(galleryList);
                setLoading(false);
                notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                // TODO: error handling
                L.e(e);
                setLoading(false);
                isRefreshing = false;
            }
        });

        setLoading(true);
    }

    private void setLoading(boolean loading){
        if (loading && getGalleryList().size() == 0){
            getLoadingView().setVisibility(View.VISIBLE);
            getListAdapter().setLoading(false);
        } else {
            getLoadingView().setVisibility(View.GONE);
            getListAdapter().setLoading(loading);
        }

        getSwipeRefreshLayout().setEnabled(!loading);
    }

    @Override
    public void onScrollToBottom() {
        if (!isEnd) loadIndex(mPage);
    }
}
