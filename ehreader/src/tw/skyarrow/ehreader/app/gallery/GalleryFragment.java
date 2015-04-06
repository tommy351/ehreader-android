package tw.skyarrow.ehreader.app.gallery;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;

import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import butterknife.ButterKnife;
import butterknife.InjectView;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.app.photo.PhotoActivity;
import tw.skyarrow.ehreader.model.APIFetcher;
import tw.skyarrow.ehreader.model.DaoMaster;
import tw.skyarrow.ehreader.model.DaoSession;
import tw.skyarrow.ehreader.model.Gallery;
import tw.skyarrow.ehreader.model.GalleryDao;
import tw.skyarrow.ehreader.util.BitmapHelper;
import tw.skyarrow.ehreader.util.DatabaseHelper;
import tw.skyarrow.ehreader.util.ImageLoaderHelper;
import tw.skyarrow.ehreader.util.L;
import tw.skyarrow.ehreader.util.LoginHelper;

public class GalleryFragment extends Fragment {
    public static final String TAG = GalleryFragment.class.getSimpleName();

    public static final String EXTRA_ID = "id";
    public static final String EXTRA_TOKEN = "token";

    @InjectView(R.id.title)
    TextView mTitleView;

    @InjectView(R.id.subtitle)
    TextView mSubtitleView;

    @InjectView(R.id.cover)
    ImageView mCoverView;

    @InjectView(R.id.read_btn)
    ImageButton mReadBtn;

    @InjectView(R.id.background)
    ImageView mBackgroundView;

    private SQLiteDatabase mDatabase;
    private GalleryDao galleryDao;
    private long galleryId;
    private String galleryToken;
    private Gallery mGallery;
    private ImageLoader.ImageContainer mImageContainer;
    private APIFetcher apiFetcher;

    public static GalleryFragment newInstance(long id, String token){
        GalleryFragment fragment = new GalleryFragment();
        Bundle args = new Bundle();

        args.putLong(EXTRA_ID, id);
        args.putString(EXTRA_TOKEN, token);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);

        mDatabase = DatabaseHelper.getWritableDatabase(getActivity());
        DaoMaster daoMaster = new DaoMaster(mDatabase);
        DaoSession daoSession = daoMaster.newSession();
        galleryDao = daoSession.getGalleryDao();
        apiFetcher = new APIFetcher(getActivity());

        Bundle args = getArguments();
        galleryId = args.getLong(EXTRA_ID);
        galleryToken = args.getString(EXTRA_TOKEN);
        mGallery = galleryDao.load(galleryId);

        if (mGallery == null){
            loadGallery();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
        ButterKnife.inject(this, view);

        mCoverView.setImageBitmap(null);
        mReadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onReadBtnClick();
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mGallery != null){
            showContent();
        }
    }

    private void showContent(){
        String title = mGallery.getTitle();
        String subtitle = mGallery.getSubtitle();

        mTitleView.setText(title);

        if (subtitle == null || subtitle.isEmpty()) {
            mSubtitleView.setVisibility(View.GONE);
        } else {
            mSubtitleView.setText(subtitle);
        }

        if (mImageContainer == null){
            ImageLoader imageLoader = ImageLoaderHelper.getImageLoader(getActivity());
            mImageContainer = imageLoader.get(mGallery.getThumbnail(), new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer imageContainer, boolean isImmediate) {
                    setCoverImage(imageContainer);
                }

                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    L.e(volleyError);
                }
            });
        } else {
            setCoverImage(mImageContainer);
        }
    }

    private void setCoverImage(ImageLoader.ImageContainer container){
        Bitmap bitmap = container.getBitmap();

        if (bitmap != null) {
            mCoverView.setImageBitmap(bitmap);
            mBackgroundView.setImageBitmap(BitmapHelper.blur(getActivity(), bitmap, 20.f));
        }
    }

    @Override
    public void onDestroy() {
        if (mImageContainer != null){
            mImageContainer.cancelRequest();
            mImageContainer = null;
        }

        mDatabase.close();
        apiFetcher.close();
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mGallery == null) return;

        inflater.inflate(R.menu.gallery, menu);

        MenuItem addFavorite = menu.findItem(R.id.action_add_to_favorites);
        MenuItem removeFavorite = menu.findItem(R.id.action_remove_from_favorites);

        if (mGallery.getStarred()){
            addFavorite.setVisible(false);
        } else {
            removeFavorite.setVisible(false);
        }

        MenuItem share = menu.findItem(R.id.action_share);
        ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(share);
        shareActionProvider.setShareIntent(getShareIntent());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_add_to_favorites:
                addToFavorites();
                return true;

            case R.id.action_remove_from_favorites:
                removeFromFavorites();
                return true;

            case R.id.action_download:
                onDownloadClick();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onReadBtnClick(){
        Intent intent = new Intent(getActivity(), PhotoActivity.class);
        Bundle args = new Bundle();

        args.putLong(PhotoActivity.EXTRA_GALLERY_ID, mGallery.getId());
        intent.putExtras(args);
        startActivity(intent);
    }

    private void addToFavorites(){
        mGallery.setStarred(true);
        galleryDao.updateInTx(mGallery);
        invalidateOptionsMenu();
        showToast(getResources().getString(R.string.toast_added_to_favorites));
    }

    private void removeFromFavorites(){
        mGallery.setStarred(false);
        galleryDao.updateInTx(mGallery);
        invalidateOptionsMenu();
        showToast(getResources().getString(R.string.toast_removed_from_favorites));
    }

    private void onDownloadClick(){
        //
    }

    private Intent getShareIntent(){
        Intent intent = new Intent(Intent.ACTION_SEND);
        boolean loggedIn = LoginHelper.getInstance(getActivity()).isLoggedIn();

        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, mGallery.getTitle() + " " + mGallery.getURL(loggedIn));

        return intent;
    }

    private void invalidateOptionsMenu(){
        getActivity().invalidateOptionsMenu();
    }

    private void showToast(String s){
        Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT).show();
    }

    private void loadGallery(){
        apiFetcher.getGallery(galleryId, galleryToken, new APIFetcher.Listener() {
            @Override
            public void onGalleryResponse(Gallery gallery) {
                mGallery = gallery;
                showContent();
            }

            @Override
            public void onError(Exception e) {
                // TODO: error handling
                L.e(e);
            }
        });
    }
}
