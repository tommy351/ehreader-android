package tw.skyarrow.ehreader.activity;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.InjectView;
import tw.skyarrow.ehreader.Constant;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.adapter.GalleryListAdapter;
import tw.skyarrow.ehreader.db.DaoMaster;
import tw.skyarrow.ehreader.db.DaoSession;
import tw.skyarrow.ehreader.db.Gallery;
import tw.skyarrow.ehreader.db.GalleryDao;
import tw.skyarrow.ehreader.service.GalleryDownloadService;
import tw.skyarrow.ehreader.util.GalleryAjaxCallback;
import tw.skyarrow.ehreader.util.CategoryHelper;
import tw.skyarrow.ehreader.util.InfiniteScrollListener;
import tw.skyarrow.ehreader.util.L;

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

    private static final Pattern pGalleryURL = Pattern.compile("<a href=\"http://(g.e-|ex)hentai.org/g/(\\d+)/(\\w+)/\" onmouseover");

    private SQLiteDatabase db;
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private GalleryDao galleryDao;

    private String baseUrl;
    private InfiniteScrollListener scrollListener;
    private AQuery aq;
    private List<Long> galleryIndex;
    private List<Gallery> galleryList;
    private GalleryListAdapter adapter;

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
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, Constant.DB_NAME, null);
        db = helper.getWritableDatabase();
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
        galleryDao = daoSession.getGalleryDao();

        Bundle args = getArguments();
        baseUrl = args.getString("base");

        scrollListener = new InfiniteScrollListener();
        scrollListener.setOnScrollToEndListener(this);
        scrollListener.setLoading(true);

        aq = new AQuery(view);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        db.close();
    }

    private AjaxCallback<String> getRawHtmlCallback = new AjaxCallback<String>() {
        @Override
        public void callback(String url, String html, AjaxStatus status) {
            // Find all url matching the pattern
            List<Gallery> list = new ArrayList<Gallery>();
            Matcher matcher = pGalleryURL.matcher(html);

            while (matcher.find()) {
                String id = matcher.group(2);
                String token = matcher.group(3);
                Gallery gallery = new Gallery();

                gallery.setId(Long.parseLong(id));
                gallery.setToken(token);

                L.v("Gallery found: {id: %s, token: %s}", id, token);
                list.add(gallery);
            }

            if (list.size() > 0) {
                getJsonCallback.setGalleryList(list);
                aq.ajax(getJsonCallback);
            } else {
                L.v("No galleries found.");
                stopLoading();
                scrollListener.setEnd(true);

                if (galleryList.size() == 0) {
                    errorView.setText(R.string.error_no_results);
                } else {
                    footerError.setText(R.string.error_no_more_results);
                }
            }
        }
    };

    private GalleryAjaxCallback getJsonCallback = new GalleryAjaxCallback() {
        @Override
        public void callback(String html, JSONObject json, AjaxStatus status) {
            try {
                L.v("Gallery json callback: %s", json.toString());

                if (json.has("error")) {
                    L.e("Gallery json callback error: %s", json.getString("error"));
                    stopLoading();
                }

                JSONArray gmetadata = json.getJSONArray("gmetadata");

                for (int i = 0, len = gmetadata.length(); i < len; i++) {
                    JSONObject data = gmetadata.getJSONObject(i);
                    long id = data.getLong("gid");

                    if (galleryIndex.contains(id) || data.getBoolean("expunged")) continue;

                    Gallery gallery = galleryDao.load(id);
                    boolean isNew = gallery == null;

                    if (isNew){
                        gallery = new Gallery();

                        gallery.setStarred(false);
                        gallery.setProgress(0);
                    }

                    gallery.setId(data.getLong("gid"));
                    gallery.setToken(data.getString("token"));
                    gallery.setTitle(data.getString("title"));
                    gallery.setSubtitle(data.getString("title_jpn"));
                    gallery.setCategory(CategoryHelper.toCategoryId(data.getString("category")));
                    gallery.setThumbnail(data.getString("thumb"));
                    gallery.setCount(data.getInt("filecount"));
                    gallery.setRating((float) data.getDouble("rating"));
                    gallery.setUploader(data.getString("uploader"));
                    gallery.setTags(data.getJSONArray("tags").toString());
                    gallery.setCreated(new Date(data.getLong("posted") * 1000));
                    gallery.setSize(Long.parseLong(data.getString("filesize")));

                    if (isNew) {
                        galleryDao.insertInTx(gallery);
                    } else {
                        galleryDao.updateInTx(gallery);
                    }

                    galleryList.add(gallery);
                    galleryIndex.add(id);
                }

                adapter.notifyDataSetChanged();
                stopLoading();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private void getGalleryList(int page) {
        String url = getGalleryListURL(page);

        L.v("Get gallery list: %s", url);
        startLoading();

        aq.ajax(url, String.class, getRawHtmlCallback);
    }

    private String getGalleryListURL(int page) {
        Uri.Builder builder = Uri.parse(baseUrl).buildUpon();
        builder.appendQueryParameter("page", Integer.toString(page));

        return builder.build().toString();
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
        ((MainActivity) getActivity()).refreshFragment();
    }
}
