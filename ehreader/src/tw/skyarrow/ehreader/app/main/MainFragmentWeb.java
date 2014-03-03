package tw.skyarrow.ehreader.app.main;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import tw.skyarrow.ehreader.BaseApplication;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.api.ApiCallException;
import tw.skyarrow.ehreader.api.DataLoader;
import tw.skyarrow.ehreader.app.gallery.GalleryActivity;
import tw.skyarrow.ehreader.db.Gallery;
import tw.skyarrow.ehreader.event.ListUpdateEvent;
import tw.skyarrow.ehreader.util.NetworkHelper;
import tw.skyarrow.ehreader.widget.InfiniteScrollListener;

/**
 * Created by SkyArrow on 2014/1/26.
 */
public class MainFragmentWeb extends Fragment implements InfiniteScrollListener.OnScrollToEndListener,
        InfiniteScrollListener.OnScrollStateChangedListener, AdapterView.OnItemClickListener {

    @InjectView(R.id.list)
    ListView listView;

    @InjectView(R.id.loading)
    ProgressBar progressBar;

    @InjectView(R.id.error)
    TextView errorView;

    @InjectView(R.id.retry)
    Button retryBtn;

    public static final String TAG = "MainFragmentWeb";

    public static final String EXTRA_BASE = "base";
    public static final String EXTRA_POSITION = "position";

    private String baseUrl;
    private InfiniteScrollListener scrollListener;
    private List<Long> galleryIndex;
    private List<Gallery> galleryList;
    private GalleryListAdapter adapter;
    private DataLoader dataLoader;
    private NetworkHelper network;
    private EventBus bus;

    private View footer;
    private ProgressBar footerProgressBar;
    private TextView footerError;
    private Button footerRetry;
    private boolean firstLoaded = true;
    private GalleryListTask task;
    private int currentPage = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.inject(this, view);
        setHasOptionsMenu(true);
        bus = EventBus.getDefault();
        bus.register(this);

        Context context = getActivity();
        dataLoader = DataLoader.getInstance(getActivity());
        network = NetworkHelper.getInstance(getActivity());

        Bundle args = getArguments();
        baseUrl = args.getString(EXTRA_BASE);

        scrollListener = new InfiniteScrollListener();
        scrollListener.setOnScrollToEndListener(this);
        scrollListener.setOnScrollStateChangedListener(this);
        scrollListener.setLoading(true);

        galleryIndex = new ArrayList<Long>();
        galleryList = new ArrayList<Gallery>();
        adapter = new GalleryListAdapter(context, galleryList);

        footer = getActivity().getLayoutInflater().inflate(R.layout.gallery_list_footer, null);
        footerProgressBar = (ProgressBar) footer.findViewById(R.id.loading);
        footerError = (TextView) footer.findViewById(R.id.error);
        footerRetry = (Button) footer.findViewById(R.id.retry);

        retryBtn.setOnClickListener(onRetryBtnClick);
        footerRetry.setOnClickListener(onRetryBtnClick);

        listView.addFooterView(footer);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        listView.setOnScrollListener(scrollListener);

        if (savedInstanceState != null) {
            listView.setSelection(savedInstanceState.getInt(EXTRA_POSITION));
        }

        getGalleryList(0);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        MapBuilder builder = MapBuilder.createAppView();
        builder.set(Fields.SCREEN_NAME, TAG);

        BaseApplication.getTracker().send(builder.build());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bus.unregister(this);

        if (task != null && !task.isCancelled()) {
            task.cancel(true);
            task = null;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!firstLoaded) {
            inflater.inflate(R.menu.main_web, menu);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                refresh();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(EXTRA_POSITION, listView.getSelectedItemPosition());
    }

    public void onEvent(ListUpdateEvent event) {
        adapter.notifyDataSetChanged();
    }

    private void getGalleryList(int page) {
        if (network.isAvailable()) {
            task = new GalleryListTask();

            startLoading();
            task.execute(page);
        } else {
            currentPage = page;
            scrollListener.setLoading(true);
            showError(R.string.error_no_network, true);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Gallery gallery = (Gallery) adapterView.getAdapter().getItem(i);

        if (gallery == null) return;

        Intent intent = new Intent(getActivity(), GalleryActivity.class);

        intent.putExtra(GalleryActivity.EXTRA_GALLERY, gallery.getId());
        startActivity(intent);
    }

    private class GalleryListTask extends AsyncTask<Integer, Integer, List<Gallery>> {
        private long startLoadAt;

        @Override
        protected List<Gallery> doInBackground(Integer... integers) {
            int page = integers[0];

            try {
                return dataLoader.getGalleryIndex(baseUrl, page);
            } catch (ApiCallException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            startLoadAt = System.currentTimeMillis();
        }

        @Override
        protected void onPostExecute(List<Gallery> list) {
            task = null;

            if (list == null) {
                scrollListener.setEnd(true);
                showError(R.string.error_load_gallery_list);
            } else if (list.size() == 0) {
                scrollListener.setEnd(true);
                showError(R.string.error_no_more_results);
            } else {
                for (Gallery gallery : list) {
                    long id = gallery.getId();

                    if (galleryIndex.contains(id)) continue;

                    galleryList.add(gallery);
                    galleryIndex.add(id);
                }

                adapter.notifyDataSetChanged();
            }

            stopLoading();

            BaseApplication.getTracker().send(MapBuilder.createTiming(
                    "resources", System.currentTimeMillis() - startLoadAt, "load index", null
            ).build());
        }
    }

    private void showError(int res) {
        showError(res, false);
    }

    private void showError(int res, boolean retry) {
        if (galleryList.size() == 0) {
            errorView.setVisibility(View.VISIBLE);
            errorView.setText(res);

            if (retry) retryBtn.setVisibility(View.VISIBLE);
        } else {
            footerError.setVisibility(View.VISIBLE);
            footerError.setText(res);

            if (retry) footerRetry.setVisibility(View.VISIBLE);
        }
    }

    private void startLoading() {
        scrollListener.setLoading(true);

        if (firstLoaded) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            footerProgressBar.setVisibility(View.VISIBLE);
        }
    }

    private void stopLoading() {
        scrollListener.setLoading(false);

        if (firstLoaded) {
            firstLoaded = false;
            progressBar.setVisibility(View.GONE);
            getActivity().supportInvalidateOptionsMenu();
        } else {
            footerProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onScrollToEnd(int page) {
        getGalleryList(page);
    }

    @Override
    public void onScrollStateChanged(int state) {
        if (state == InfiniteScrollListener.SCROLL_STATE_FLING) {
            adapter.setScrolling(true);
        } else {
            adapter.setScrolling(false);
            adapter.notifyDataSetChanged();
        }
    }

    private void refresh() {
        firstLoaded = true;

        scrollListener.setLoading(true);
        scrollListener.setEnd(false);
        scrollListener.setPage(0);

        galleryIndex.clear();
        galleryList.clear();
        adapter.notifyDataSetChanged();
        getActivity().supportInvalidateOptionsMenu();
        errorView.setVisibility(View.GONE);
        footerRetry.setVisibility(View.GONE);

        BaseApplication.getTracker().send(MapBuilder.createEvent(
                "UI", "button", "refresh", null
        ).build());

        getGalleryList(0);
    }

    private View.OnClickListener onRetryBtnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            errorView.setVisibility(View.GONE);
            footerError.setVisibility(View.GONE);
            retryBtn.setVisibility(View.GONE);
            footerRetry.setVisibility(View.GONE);
            getGalleryList(currentPage);
        }
    };
}
