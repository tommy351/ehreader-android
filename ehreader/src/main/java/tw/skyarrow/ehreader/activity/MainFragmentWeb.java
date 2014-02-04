package tw.skyarrow.ehreader.activity;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.adapter.GalleryListAdapter;
import tw.skyarrow.ehreader.db.Gallery;
import tw.skyarrow.ehreader.util.DownloadHelper;
import tw.skyarrow.ehreader.util.InfiniteScrollListener;

/**
 * Created by SkyArrow on 2014/1/26.
 */
public class MainFragmentWeb extends MainFragmentBase implements InfiniteScrollListener.OnScrollToEndListener {
    @InjectView(R.id.list)
    ListView listView;

    @InjectView(R.id.loading)
    ProgressBar progressBar;

    @InjectView(R.id.error)
    TextView errorView;

    public static final String TAG = "MainFragmentWeb";

    private String baseUrl;
    private InfiniteScrollListener scrollListener;
    private List<Long> galleryIndex;
    private List<Gallery> galleryList;
    private GalleryListAdapter adapter;
    private DownloadHelper infoHelper;

    private View footer;
    private ProgressBar footerProgressBar;
    private TextView footerError;
    private boolean firstLoaded = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.inject(this, view);
        setHasOptionsMenu(true);

        Context context = getActivity();
        infoHelper = new DownloadHelper(getActivity());

        Bundle args = getArguments();
        baseUrl = args.getString("base");

        scrollListener = new InfiniteScrollListener();
        scrollListener.setOnScrollToEndListener(this);
        scrollListener.setLoading(true);

        galleryIndex = new ArrayList<Long>();
        galleryList = new ArrayList<Gallery>();
        adapter = new GalleryListAdapter(context, galleryList);

        footer = getActivity().getLayoutInflater().inflate(R.layout.gallery_list_footer, null);
        footerProgressBar = (ProgressBar) footer.findViewById(R.id.loading);
        footerError = (TextView) footer.findViewById(R.id.error);

        listView.addFooterView(footer);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        listView.setOnScrollListener(scrollListener);

        if (savedInstanceState != null) {
            listView.setSelection(savedInstanceState.getInt("position"));
        }

        getGalleryList(0);

        return view;
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

        outState.putInt("position", listView.getSelectedItemPosition());
    }

    private void getGalleryList(int page) {
        startLoading();
        //new Thread(new GalleryListRunnable(baseUrl, page)).start();
        new GalleryListTask().execute(page);
    }

    private class GalleryListTask extends AsyncTask<Integer, Integer, List<Gallery>> {
        @Override
        protected List<Gallery> doInBackground(Integer... integers) {
            int page = integers[0];

            try {
                return infoHelper.getGalleryList(baseUrl, page);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<Gallery> list) {
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
        }
    }

    private void showError(int res) {
        if (galleryList.size() == 0) {
            errorView.setVisibility(View.VISIBLE);
            errorView.setText(res);
        } else {
            footerError.setVisibility(View.VISIBLE);
            footerError.setText(res);
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

        getGalleryList(0);
    }
}
