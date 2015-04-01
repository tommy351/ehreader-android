package tw.skyarrow.ehreader.app.main;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import tw.skyarrow.ehreader.Constant;
import tw.skyarrow.ehreader.model.DaoMaster;
import tw.skyarrow.ehreader.model.DaoSession;
import tw.skyarrow.ehreader.model.Gallery;
import tw.skyarrow.ehreader.model.GalleryDao;
import tw.skyarrow.ehreader.model.GalleryHelper;
import tw.skyarrow.ehreader.util.DatabaseHelper;
import tw.skyarrow.ehreader.util.EHAPIRequest;
import tw.skyarrow.ehreader.util.L;
import tw.skyarrow.ehreader.util.RequestHelper;

public class SearchFragment extends GalleryListFragment {

    public static final String TAG = SearchFragment.class.getSimpleName();

    public static final String EXTRA_BASE_URL = "base_url";
    public static final String EXTRA_TOOLBAR_TITLE = "toolbar_title";

    private GalleryDao galleryDao;
    private int mPage;
    private String mBaseUrl;
    private String mToolbarTitle;
    private boolean isLoading;
    private boolean isRefreshing;

    public static SearchFragment newInstance(String baseUrl){
        return newInstance(baseUrl, null);
    }

    public static SearchFragment newInstance(String baseUrl, String toolbarTitle){
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_BASE_URL, baseUrl);
        args.putString(EXTRA_TOOLBAR_TITLE, toolbarTitle);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get database instance
        SQLiteDatabase db = DatabaseHelper.getWritableDatabase(getActivity());
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();
        galleryDao = daoSession.getGalleryDao();

        // Get arguments
        Bundle args = getArguments();
        mBaseUrl = args.getString(EXTRA_BASE_URL);
        mToolbarTitle = args.getString(EXTRA_TOOLBAR_TITLE, "Latest");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set toolbar title
        ActionBarActivity activity = (ActionBarActivity) getActivity();
        activity.getSupportActionBar().setTitle(mToolbarTitle);

        // Load the first page
        if (getGalleryList() == null){
            List<Gallery> galleryList = new ArrayList<>();
            mPage = 0;
            isLoading = false;
            isRefreshing = false;

            setGalleryList(galleryList);
            loadIndex(mPage);
        }
    }

    @Override
    public void onDestroy() {
        RequestHelper.getInstance(getActivity()).cancelAllRequests(TAG);
        super.onDestroy();
    }

    @Override
    public void onRefresh() {
        isRefreshing = true;
        loadIndex(0);
    }

    private void loadIndex(int page){
        if (isLoading) return;

        String url = GalleryHelper.getIndexUrl(mBaseUrl, page);
        isLoading = true;

        StringRequest req = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                JSONArray list = GalleryHelper.findIdInIndex(s);
                makeAPIRequest(list);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                handleRequestError(volleyError);
            }
        });

        addToRequestQueue(req);
        mSwipeLayout.setRefreshing(true);
        mSwipeLayout.setEnabled(false);
    }

    private void makeAPIRequest(JSONArray gidlist){
        try {
            JSONObject obj = new JSONObject();
            obj.put("method", "gdata");
            obj.put("gidlist", gidlist);

            EHAPIRequest req = new EHAPIRequest(Constant.API_URL, obj, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    handleAPIResponse(jsonObject);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    L.e(volleyError);
                }
            });

            addToRequestQueue(req);
        } catch (JSONException e){
            handleRequestError(e);
        }
    }

    private void handleAPIResponse(JSONObject json){
        try {
            JSONArray list = json.getJSONArray("gmetadata");
            List<Gallery> result = new ArrayList<>();

            for (int i = 0, len = list.length(); i < len; i++){
                JSONObject data = list.getJSONObject(i);
                long id = data.getLong("gid");

                if (data.has("error")){
                    continue;
                }

                Gallery gallery = galleryDao.load(id);
                boolean isNew = gallery == null;

                if (isNew) {
                    gallery = new Gallery();

                    gallery.setStarred(false);
                    gallery.setProgress(0);
                }

                gallery.fromJSON(data);

                if (isNew){
                    galleryDao.insertInTx(gallery);
                } else {
                    galleryDao.updateInTx(gallery);
                }

                result.add(gallery);
            }

            if (isRefreshing){
                isRefreshing = false;
                mPage = 0;
                getGalleryList().clear();
            }

            isLoading = false;
            mPage++;
            getGalleryList().addAll(result);
            notifyDataSetChanged();
            getSwipeRefreshLayout().setRefreshing(false);
            getSwipeRefreshLayout().setEnabled(true);
        } catch (JSONException e){
            handleRequestError(e);
        }
    }

    private void handleRequestError(Exception error){
        L.e(error);
        getSwipeRefreshLayout().setRefreshing(false);
        getSwipeRefreshLayout().setEnabled(true);
        isLoading = false;
        isRefreshing = false;
    }

    private void addToRequestQueue(Request req){
        RequestHelper.getInstance(getActivity()).addToRequestQueue(req, TAG);
    }
}
