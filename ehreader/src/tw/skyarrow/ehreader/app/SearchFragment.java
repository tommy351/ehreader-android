package tw.skyarrow.ehreader.app;

import android.os.Bundle;

import tw.skyarrow.ehreader.services.GalleryFetchService;
import tw.skyarrow.ehreader.util.GalleryHelper;

public class SearchFragment extends GalleryListFragment {
    public static final String TAG = "SearchFragment";

    public static final String ARG_BASE_URL = "ARG_BASE_URL";

    private int indexPage;
    private String baseUrl;
    private String currentUrl;

    public static SearchFragment newInstance(String baseUrl){
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_BASE_URL, baseUrl);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        baseUrl = args.getString(ARG_BASE_URL);
        indexPage = 0;
    }

    @Override
    public void onStart() {
        super.onStart();
//        EventBus.getDefault().register(this);

        loadIndex(indexPage);
    }

    @Override
    public void onStop() {
//        EventBus.getDefault().unregister(this);
        super.onStop();
    }
/*
    public void onEventMainThread(FetchIndexEvent event){
        if (event.getUrl() != currentUrl || event.getEvent() == FetchIndexEvent.EVENT_FAILED) return;

        List<Gallery> list = event.getGalleryList();
        if (list == null) return;

        addGalleryList(list);
        notifyDataSetChanged();
    }
*/
    @Override
    public void onScrollToBottom() {
        //
    }

    private void loadIndex(int page){
        currentUrl = GalleryHelper.getIndexUrl(baseUrl, page);
        GalleryFetchService.startFetchIndex(getActivity(), currentUrl);
    }
}
