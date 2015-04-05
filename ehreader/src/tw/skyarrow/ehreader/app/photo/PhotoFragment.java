package tw.skyarrow.ehreader.app.photo;

import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.event.PhotoListAdapterEvent;
import tw.skyarrow.ehreader.model.DaoMaster;
import tw.skyarrow.ehreader.model.DaoSession;
import tw.skyarrow.ehreader.model.Gallery;
import tw.skyarrow.ehreader.model.GalleryDao;
import tw.skyarrow.ehreader.model.GalleryHelper;
import tw.skyarrow.ehreader.model.Photo;
import tw.skyarrow.ehreader.model.PhotoDao;
import tw.skyarrow.ehreader.model.PhotoHelper;
import tw.skyarrow.ehreader.model.PhotoPageData;
import tw.skyarrow.ehreader.util.DatabaseHelper;
import tw.skyarrow.ehreader.util.L;
import tw.skyarrow.ehreader.util.RequestHelper;

public class PhotoFragment extends Fragment {
    public static final String TAG = PhotoFragment.class.getSimpleName();

    public static final String EXTRA_GALLERY_ID = "gallery_id";

    @InjectView(R.id.list)
    RecyclerView mRecyclerView;

    @InjectView(R.id.slider)
    SeekBar mSlider;

    private long galleryId;
    private List<Photo> mPhotoList;
    private PhotoListAdapter mListAdapter;
    private GalleryDao galleryDao;
    private PhotoDao photoDao;
    private Gallery mGallery;
    private LinearLayoutManager mLayoutManager;
    private Set<Integer> mPhotoListReqQueue;
    private Set<Integer> mPhotoPageReqQueue;

    public static PhotoFragment newInstance(long galleryId){
        PhotoFragment fragment = new PhotoFragment();
        Bundle args = new Bundle();

        args.putLong(EXTRA_GALLERY_ID, galleryId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        EventBus.getDefault().register(this);

        mPhotoListReqQueue = new HashSet<>();
        mPhotoPageReqQueue = new HashSet<>();

        // Read arguments
        Bundle args = getArguments();
        galleryId = args.getLong(EXTRA_GALLERY_ID);

        // Set up database
        SQLiteDatabase db = DatabaseHelper.getWritableDatabase(getActivity());
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();
        galleryDao = daoSession.getGalleryDao();
        photoDao = daoSession.getPhotoDao();

        // Load data
        mGallery = galleryDao.load(galleryId);
        mPhotoList = new ArrayList<>();
        mListAdapter = new PhotoListAdapter(getActivity(), mPhotoList);

        if (mGallery.getPhotoPerPage() == null){
            getPhotoPerPage();
        } else {
            loadPhotos();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo, container, false);
        ButterKnife.inject(this, view);

        // Set up RecyclerView
        mLayoutManager = new LinearLayoutManager(getActivity());
        int orientation = getActivity().getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_LANDSCAPE){
            mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        } else {
            mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        }

        mRecyclerView.setAdapter(mListAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int progress = getProgress();
                ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();

                actionBar.setTitle(String.format("%d / %d", progress + 1, mGallery.getCount()));
                mSlider.setProgress(progress);
                actionBar.invalidateOptionsMenu();
            }
        });

        // Set up slider
        mSlider.setMax(mGallery.getCount() - 1);
        mSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();

        mGallery.setProgress(getProgress());
        mGallery.setLastread(new Date(System.currentTimeMillis()));
        galleryDao.updateInTx(mGallery);
    }

    @Override
    public void onDestroy() {
        RequestHelper.getInstance(getActivity()).cancelAllRequests(TAG);
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public void onEvent(PhotoListAdapterEvent event){
        switch (event.getAction()){
            case PhotoListAdapterEvent.ACTION_NEED_SRC:
                handleNeedSrcEvent(event.getPosition());
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.photo, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_bookmark:
                return true;

            case R.id.action_unbookmark:
                return true;

            case R.id.action_download:
                return true;

            case R.id.action_retry:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadPhotos(){
        for (int i = 1, len = mGallery.getCount(); i <= len; i++){
            Photo photo = Photo.findPhoto(photoDao, galleryId, i);

            if (photo == null){
                photo = new Photo();
                photo.setGalleryId(galleryId);
                photo.setPage(i);
            }

            mPhotoList.add(photo);
        }

        mListAdapter.notifyDataSetChanged();
    }

    private void handleNeedSrcEvent(final int position){
        Photo photo = mPhotoList.get(position);

        if (photo.getId() == null){
            int galleryPage = photo.getPage() / mGallery.getPhotoPerPage();

            loadPhotoList(galleryPage, new LoadPhotoListCallback() {
                @Override
                public void onSuccess(List<Photo> list) {
                    for (Photo photo : list){
                        mPhotoList.set(photo.getPage() - 1, photo);
                    }

                    loadPhotoSrc(mPhotoList.get(position));
                }

                @Override
                public void onError(Exception e) {
                    // TODO: error handling
                    L.e(e);
                }
            });
        } else {
            loadPhotoSrc(photo);
        }
    }

    private void addRequestToQueue(Request req){
        RequestHelper.getInstance(getActivity()).addToRequestQueue(req, PhotoFragment.TAG);
    }

    private void getPhotoPerPage(){
        loadPhotoList(0, new LoadPhotoListCallback() {
            @Override
            public void onSuccess(List<Photo> list) {
                mGallery.setPhotoPerPage(list.size());
                galleryDao.updateInTx(mGallery);
                loadPhotos();
            }

            @Override
            public void onError(Exception e) {
                // TODO: error handling
                L.e(e);
            }
        });
    }


    private void loadPhotoList(final int galleryPage, final LoadPhotoListCallback callback){
        if (mPhotoListReqQueue.contains(galleryPage)) return;

        String url = mGallery.getURL(galleryPage);
        StringRequest req = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String html) {
                List<Photo> list = GalleryHelper.findPhotosInGallery(html);

                // TODO: error handling
                if (list.size() == 0) return;

                photoDao.insertInTx(list);
                mPhotoListReqQueue.remove(galleryPage);
                callback.onSuccess(list);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                callback.onError(volleyError);
            }
        });

        mPhotoListReqQueue.add(galleryPage);
        addRequestToQueue(req);
    }

    private interface LoadPhotoListCallback {
        void onSuccess(List<Photo> list);
        void onError(Exception e);
    }

    private void loadPhotoSrc(final Photo photo) {
        if (mPhotoPageReqQueue.contains(photo.getPage())) return;

        String url = photo.getURL();
        StringRequest req = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String html) {
                try {
                    PhotoPageData data = PhotoHelper.readPhotoPage(html);

                    photo.setRetryId(data.getRetryId());
                    photo.setSrc(data.getSrc());
                    photo.setWidth(data.getWidth());
                    photo.setHeight(data.getHeight());
                    photoDao.updateInTx(photo);

                    mGallery.setShowkey(data.getShowkey());
                    galleryDao.updateInTx(mGallery);

                    mListAdapter.notifyItemChanged(photo.getPage() - 1);
                } catch (UnsupportedEncodingException e) {
                    // TODO: error handling
                    L.e(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                // TODO: error handling
                L.e(volleyError);
            }
        });

        mPhotoPageReqQueue.add(photo.getPage());
        addRequestToQueue(req);
    }

    private int getProgress(){
        return mLayoutManager.findFirstVisibleItemPosition();
    }
}
