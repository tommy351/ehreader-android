package tw.skyarrow.ehreader.app.photo;

import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.model.DaoMaster;
import tw.skyarrow.ehreader.model.DaoSession;
import tw.skyarrow.ehreader.model.Gallery;
import tw.skyarrow.ehreader.model.GalleryDao;
import tw.skyarrow.ehreader.model.GalleryHelper;
import tw.skyarrow.ehreader.model.Photo;
import tw.skyarrow.ehreader.model.PhotoDao;
import tw.skyarrow.ehreader.util.DatabaseHelper;
import tw.skyarrow.ehreader.util.L;
import tw.skyarrow.ehreader.util.RequestHelper;

public class PhotoFragment extends Fragment {
    public static final String TAG = PhotoFragment.class.getSimpleName();

    public static final String EXTRA_GALLERY_ID = "gallery_id";
    public static final String EXTRA_PAGE = "page";

    @InjectView(R.id.list)
    RecyclerView mRecyclerView;

    private long galleryId;
    private List<Photo> mPhotoList;
    private PhotoListAdapter mListAdapter;
    private GalleryDao galleryDao;
    private PhotoDao photoDao;
    private Gallery mGallery;
    private LinearLayoutManager mLayoutManager;

    public static PhotoFragment newInstance(long galleryId, int page){
        PhotoFragment fragment = new PhotoFragment();
        Bundle args = new Bundle();

        args.putLong(EXTRA_GALLERY_ID, galleryId);
        args.putInt(EXTRA_PAGE, page);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);

        // Read arguments
        Bundle args = getArguments();
        galleryId = args.getLong(EXTRA_GALLERY_ID);

        // Set up database
        SQLiteDatabase db = DatabaseHelper.getWritableDatabase(getActivity());
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();
        galleryDao = daoSession.getGalleryDao();
        photoDao = daoSession.getPhotoDao();

        mGallery = galleryDao.load(galleryId);
        mPhotoList = new ArrayList<>();
        mListAdapter = new PhotoListAdapter(getActivity(), mGallery, mPhotoList);

        // Get the photos per page of gallery
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

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        // TODO: save progress
    }

    @Override
    public void onDestroy() {
        RequestHelper.getInstance(getActivity()).cancelAllRequests(TAG);
        mListAdapter.cancelAllRequests();
        super.onDestroy();
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
        }

        return super.onOptionsItemSelected(item);
    }

    private void getPhotoPerPage(){
        StringRequest req = new StringRequest(mGallery.getURL(), new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                handlePhotoListResponse(s);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                L.e(volleyError);
            }
        });

        RequestHelper.getInstance(getActivity()).addToRequestQueue(req, TAG);
    }

    private void handlePhotoListResponse(String html){
        List<Photo> list = GalleryHelper.findPhotosInGallery(html);

        // TODO: error handling
        if (list.size() == 0) return;

        // Insert photos into the database
        photoDao.insertInTx(list);

        // Update the photos per page of gallery
        mGallery.setPhotoPerPage(list.size());
        galleryDao.updateInTx(mGallery);

        // Start loading photos
        loadPhotos();
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
}
