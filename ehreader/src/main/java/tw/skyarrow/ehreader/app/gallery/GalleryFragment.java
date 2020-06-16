package tw.skyarrow.ehreader.app.gallery;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.app.download.DownloadConfirmDialog;
import tw.skyarrow.ehreader.app.photo.PhotoActivity;
import tw.skyarrow.ehreader.app.search.SearchActivity;
import tw.skyarrow.ehreader.event.GalleryScrollEvent;
import tw.skyarrow.ehreader.model.DaoMaster;
import tw.skyarrow.ehreader.model.DaoSession;
import tw.skyarrow.ehreader.model.Gallery;
import tw.skyarrow.ehreader.model.GalleryDao;
import tw.skyarrow.ehreader.network.APIFetcher;
import tw.skyarrow.ehreader.util.BitmapHelper;
import tw.skyarrow.ehreader.util.DatabaseHelper;
import tw.skyarrow.ehreader.util.ImageLoaderHelper;
import tw.skyarrow.ehreader.util.L;
import tw.skyarrow.ehreader.util.LoginHelper;
import tw.skyarrow.ehreader.view.ExpandedLinearLayoutManager;
import tw.skyarrow.ehreader.view.ObservableScrollView;
import tw.skyarrow.ehreader.view.RecyclerViewItemClickListener;

public class GalleryFragment extends Fragment implements ObservableScrollView.OnScrollListener {
    public static final String TAG = GalleryFragment.class.getSimpleName();

    public static final String EXTRA_ID = "id";
    public static final String EXTRA_TOKEN = "token";

    @InjectView(R.id.title)
    TextView mTitleView;

    @InjectView(R.id.subtitle)
    TextView mSubtitleView;

    @InjectView(R.id.cover)
    ImageView mCoverView;

    @InjectView(R.id.background)
    ImageView mBackgroundView;

    @InjectView(R.id.category)
    TextView mCategoryView;

    @InjectView(R.id.page)
    TextView mPageView;

    @InjectView(R.id.rating_bar)
    RatingBar mRatingBar;

    @InjectView(R.id.rating_text)
    TextView mRatingText;

    @InjectView(R.id.uploader)
    TextView mUploaderView;

    @InjectView(R.id.scroll_view)
    ObservableScrollView mScrollView;

    @InjectView(R.id.tag_list)
    RecyclerView mTagRecyclerView;

    private DatabaseHelper dbHelper;
    private GalleryDao galleryDao;
    private long galleryId;
    private String galleryToken;
    private Gallery mGallery;
    private ImageLoader.ImageContainer mImageContainer;
    private APIFetcher apiFetcher;
    private int mLastDampedScroll;
    private GalleryTagListAdapter mTagListAdapter;

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

        dbHelper = DatabaseHelper.getInstance(getActivity());
        SQLiteDatabase db = dbHelper.open();
        DaoMaster daoMaster = new DaoMaster(db);
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

        RecyclerViewItemClickListener.OnItemClickListener listener = new RecyclerViewItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View childView, int position) {
                Intent intent = SearchActivity.newIntent(getActivity(), mGallery.getTagList().get(position).getTitle());
                startActivity(intent);
            }

            @Override
            public void onItemLongPress(View childView, int position) {

            }
        };

//        InlineLayoutManager layoutManager = new InlineLayoutManager(getActivity());
        ExpandedLinearLayoutManager layoutManager = new ExpandedLinearLayoutManager(getActivity());
        mTagListAdapter = new GalleryTagListAdapter(getActivity());
        mTagRecyclerView.setLayoutManager(layoutManager);
        mTagRecyclerView.setAdapter(mTagListAdapter);
        mTagRecyclerView.setHasFixedSize(true);
        mTagRecyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(getActivity(), listener));

        mCoverView.setImageBitmap(null);
        mScrollView.setOnScrollListener(this);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        onScrollChanged(mScrollView.getScrollX(), mScrollView.getScrollY(), 0, 0);

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

        mCategoryView.setText(mGallery.getCategoryString());
        mCategoryView.setTextColor(getResources().getColor(mGallery.getCategoryColor()));
        mPageView.setText(" / " + mGallery.getCount() + "P");
        mRatingBar.setRating(mGallery.getRating());
        mUploaderView.setText(mGallery.getUploader());
        mRatingText.setText(String.format("%.1f", mGallery.getRating()));

        mTagListAdapter.setTagList(mGallery.getTagList());
        mTagListAdapter.notifyDataSetChanged();

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

        dbHelper.close();
        apiFetcher.close();
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mGallery == null) return;

        inflater.inflate(R.menu.gallery, menu);

        MenuItem addFavorite = menu.findItem(R.id.action_add_to_favorites);
        MenuItem removeFavorite = menu.findItem(R.id.action_remove_from_favorites);

        if (mGallery.getStarred() != null && mGallery.getStarred()){
            addFavorite.setVisible(false);
        } else {
            removeFavorite.setVisible(false);
        }
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

            case R.id.action_share:
                shareGallery();
                return true;

            case R.id.action_download:
                downloadGallery();
                return true;
        }

        return super.onOptionsItemSelected(item);
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

    private void shareGallery(){
        Intent intent = new Intent(Intent.ACTION_SEND);
        boolean loggedIn = LoginHelper.getInstance(getActivity()).isLoggedIn();

        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, mGallery.getTitle() + " " + mGallery.getURL(loggedIn));

        startActivity(Intent.createChooser(intent, getString(R.string.action_share)));
    }

    private void downloadGallery(){
        DownloadConfirmDialog dialog = DownloadConfirmDialog.newInstance(galleryId);
        dialog.show(getActivity().getSupportFragmentManager(), DownloadConfirmDialog.TAG);
    }

    private void invalidateOptionsMenu(){
        getActivity().invalidateOptionsMenu();
    }

    private void showToast(String s){
        Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT).show();
    }

    private void loadGallery(){
        apiFetcher.getGallery(galleryId, galleryToken, new Response.Listener<Gallery>() {
            @Override
            public void onResponse(Gallery gallery) {
                mGallery = gallery;
                showContent();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {
                L.e(e);
            }
        });
    }

    @OnClick(R.id.read_btn)
    void onReadBtnClick(){
        Intent intent = PhotoActivity.newIntent(getActivity(), mGallery.getId());
        startActivity(intent);
    }

    @OnClick(R.id.uploader_container)
    void onUploaderClick(){
        Intent intent = SearchActivity.newIntent(getActivity(), "uploader:" + mGallery.getUploader());
        startActivity(intent);
    }

    // https://gist.github.com/ManuelPeinado/561748b9fa42d3b25661
    @Override
    public void onScrollChanged(int x, int y, int oldX, int oldY) {
        float damping = 0.5f;
        int dampedScroll = (int) (y * damping);
        int offset = mLastDampedScroll - dampedScroll;
        mBackgroundView.offsetTopAndBottom(offset);

        mLastDampedScroll = dampedScroll;
        EventBus.getDefault().post(new GalleryScrollEvent(y));
    }
}
