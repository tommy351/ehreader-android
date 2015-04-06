package tw.skyarrow.ehreader.app.photo;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;

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
import tw.skyarrow.ehreader.model.APIFetcher;
import tw.skyarrow.ehreader.model.DaoMaster;
import tw.skyarrow.ehreader.model.DaoSession;
import tw.skyarrow.ehreader.model.EHCrawler;
import tw.skyarrow.ehreader.model.Gallery;
import tw.skyarrow.ehreader.model.GalleryDao;
import tw.skyarrow.ehreader.model.Photo;
import tw.skyarrow.ehreader.model.PhotoDao;
import tw.skyarrow.ehreader.util.DatabaseHelper;
import tw.skyarrow.ehreader.util.L;

public class PhotoFragment extends Fragment {
    public static final String TAG = PhotoFragment.class.getSimpleName();

    public static final String EXTRA_GALLERY_ID = "gallery_id";
    public static final String EXTRA_PHOTO_TOKEN = "photo_token";
    public static final String EXTRA_PHOTO_PAGE = "photo_page";

    @InjectView(R.id.list)
    RecyclerView mRecyclerView;

    @InjectView(R.id.slider)
    SeekBar mSlider;

    private long galleryId;
    private String photoToken;
    private int photoPage;
    private List<Photo> mPhotoList;
    private PhotoListAdapter mListAdapter;
    private SQLiteDatabase mDatabase;
    private GalleryDao galleryDao;
    private PhotoDao photoDao;
    private Gallery mGallery;
    private LinearLayoutManager mLayoutManager;
    private Set<Integer> mPhotoListReqQueue;
    private Set<Integer> mPhotoPageReqQueue;
    private APIFetcher apiFetcher;
    private EHCrawler ehCrawler;
    private int lastProgress;

    public static PhotoFragment newInstance(long galleryId){
        PhotoFragment fragment = new PhotoFragment();
        Bundle args = new Bundle();

        args.putLong(EXTRA_GALLERY_ID, galleryId);
        fragment.setArguments(args);

        return fragment;
    }

    public static PhotoFragment newInstance(long galleryId, String photoToken, int photoPage){
        PhotoFragment fragment = new PhotoFragment();
        Bundle args = new Bundle();

        args.putLong(EXTRA_GALLERY_ID, galleryId);
        args.putString(EXTRA_PHOTO_TOKEN, photoToken);
        args.putInt(EXTRA_PHOTO_PAGE, photoPage);
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
        lastProgress = 0;

        // Read arguments
        Bundle args = getArguments();
        galleryId = args.getLong(EXTRA_GALLERY_ID);
        photoToken = args.getString(EXTRA_PHOTO_TOKEN);
        photoPage = args.getInt(EXTRA_PHOTO_PAGE);

        // Set up database
        mDatabase = DatabaseHelper.getWritableDatabase(getActivity());
        DaoMaster daoMaster = new DaoMaster(mDatabase);
        DaoSession daoSession = daoMaster.newSession();
        galleryDao = daoSession.getGalleryDao();
        photoDao = daoSession.getPhotoDao();

        // Load data
        mGallery = galleryDao.load(galleryId);
        mPhotoList = new ArrayList<>();
        mListAdapter = new PhotoListAdapter(getActivity(), mPhotoList);
        apiFetcher = new APIFetcher(getActivity());
        ehCrawler = new EHCrawler(getActivity());

        if (mGallery == null){
            loadGallery();
        } else {
            if (mGallery.getPhotoPerPage() == null){
                getPhotoPerPage();
            } else {
                loadPhotos();
            }
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
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                updateProgress();
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
        EventBus.getDefault().unregister(this);
        apiFetcher.close();
        ehCrawler.close();
        mDatabase.close();
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
        if (mPhotoList.size() == 0) return;

        inflater.inflate(R.menu.photo, menu);

        Photo photo = mPhotoList.get(getProgress());
        if (photo == null) return;

        MenuItem bookmark = menu.findItem(R.id.action_bookmark);
        MenuItem unbookmark = menu.findItem(R.id.action_unbookmark);

        if (photo.getBookmarked()){
            bookmark.setVisible(false);
        } else {
            unbookmark.setVisible(false);
        }

        MenuItem share = menu.findItem(R.id.action_share);
        ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(share);
        shareActionProvider.setShareIntent(getShareIntent());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_bookmark:
                addBookmark();
                return true;

            case R.id.action_unbookmark:
                removeBookmark();
                return true;

            case R.id.action_download:
                return true;

            case R.id.action_retry:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void addBookmark(){
        Photo photo = mPhotoList.get(getProgress());
        photo.setBookmarked(true);
        photoDao.updateInTx(photo);
        invalidateOptionsMenu();
        showToast(getString(R.string.toast_bookmark_added));
    }

    private void removeBookmark(){
        Photo photo = mPhotoList.get(getProgress());
        photo.setBookmarked(false);
        photoDao.updateInTx(photo);
        invalidateOptionsMenu();
        showToast(getString(R.string.toast_bookmark_removed));
    }

    private Intent getShareIntent(){
        Intent intent = new Intent(Intent.ACTION_SEND);

        // TODO: photo sharing

        return intent;
    }

    private void loadPhotos(){
        for (int i = 1, len = mGallery.getCount(); i <= len; i++){
            Photo photo = Photo.findPhoto(photoDao, galleryId, i);

            if (photo == null){
                photo = new Photo();
                photo.setGalleryId(galleryId);
                photo.setPage(i);
                photo.setToken("");
                photo.setBookmarked(false);
                photo.setDownloaded(false);
                photo.setInvalid(false);
            }

            mPhotoList.add(photo);
        }

        photoDao.insertOrReplaceInTx(mPhotoList);
        mListAdapter.notifyDataSetChanged();
    }

    private void handleNeedSrcEvent(final int position) {
        Photo photo = mPhotoList.get(position);

        if (photo.getToken().isEmpty()){
            loadPhotoList(photo.getPage() / mGallery.getPhotoPerPage());
        } else {
            loadPhotoSrc(photo);
        }
    }

    private void getPhotoPerPage(){
        ehCrawler.getPhotoList(mGallery.getURL(), 0, new EHCrawler.Listener() {
            @Override
            public void onPhotoListResponse(List<Photo> photoList) {
                mGallery.setPhotoPerPage(photoList.size());
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

    // FIXME: loadPhotoList called too many times
    private void loadPhotoList(final int galleryPage){
        if (mPhotoListReqQueue.contains(galleryPage)) return;

        mPhotoListReqQueue.add(galleryPage);
        ehCrawler.getPhotoList(mGallery.getURL(), galleryPage, new EHCrawler.Listener() {
            @Override
            public void onPhotoListResponse(List<Photo> photoList) {
                if (photoList.size() == 0){
                    // TODO: error handling
                    return;
                }

                mPhotoListReqQueue.remove(galleryPage);
                mListAdapter.notifyItemRangeChanged(photoList.get(0).getPage() - 1, photoList.size());
            }

            @Override
            public void onError(Exception e) {
                // TODO: error handling
                L.e(e);
            }
        });
    }

    private void loadPhotoSrc(Photo photo){
        final int photoPage = photo.getPage();
        if (mPhotoPageReqQueue.contains(photoPage)) return;

        mPhotoPageReqQueue.add(photoPage);
        ehCrawler.getPhoto(photo, new EHCrawler.Listener() {
            @Override
            public void onPhotoResponse(Photo photo) {
                int position = photo.getPage() - 1;

                mPhotoList.set(position, photo);
                mPhotoPageReqQueue.remove(photoPage);
                mListAdapter.notifyItemChanged(position);
            }

            @Override
            public void onError(Exception e) {
                // TODO: error handling
                L.e(e);
            }
        });
    }

    private int getProgress(){
        return mLayoutManager.findFirstVisibleItemPosition();
    }

    private void updateProgress(){
        int progress = getProgress();
        if (progress == lastProgress) return;

        lastProgress = progress;
        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();

        actionBar.setTitle(String.format("%d / %d", progress + 1, mGallery.getCount()));
        mSlider.setProgress(progress);
        actionBar.invalidateOptionsMenu();
    }

    private void loadGallery(){
        apiFetcher.getGalleryByPhotoInfo(galleryId, photoToken, photoPage, new APIFetcher.Listener() {
            @Override
            public void onGalleryResponse(Gallery gallery) {
                mGallery = gallery;
                getPhotoPerPage();
            }

            @Override
            public void onError(Exception e) {
                // TODO: error handling
                L.e(e);
            }
        });
    }

    private void invalidateOptionsMenu(){
        getActivity().invalidateOptionsMenu();
    }

    private void showToast(String s){
        Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT).show();
    }
}
