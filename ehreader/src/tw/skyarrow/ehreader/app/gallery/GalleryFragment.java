package tw.skyarrow.ehreader.app.gallery;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
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
import tw.skyarrow.ehreader.model.DaoMaster;
import tw.skyarrow.ehreader.model.DaoSession;
import tw.skyarrow.ehreader.model.Gallery;
import tw.skyarrow.ehreader.model.GalleryDao;
import tw.skyarrow.ehreader.util.BitmapHelper;
import tw.skyarrow.ehreader.util.DatabaseHelper;
import tw.skyarrow.ehreader.util.ImageLoaderHelper;
import tw.skyarrow.ehreader.util.L;

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

    private GalleryDao galleryDao;
    private long galleryId;
    private String galleryToken;
    private Gallery mGallery;
    private ImageLoader.ImageContainer mImageContainer;

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

        SQLiteDatabase db = DatabaseHelper.getWritableDatabase(getActivity());
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();
        galleryDao = daoSession.getGalleryDao();

        Bundle args = getArguments();
        galleryId = args.getLong(EXTRA_ID);
        galleryToken = args.getString(EXTRA_TOKEN);
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

        int orientation = getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            DisplayMetrics metrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
            ViewGroup.LayoutParams layoutParams = mBackgroundView.getLayoutParams();

            layoutParams.height = metrics.heightPixels;
            mBackgroundView.setLayoutParams(layoutParams);
        }

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);

        if (mGallery == null) {
            mGallery = galleryDao.load(galleryId);

            String title = mGallery.getTitle();
            String subtitle = mGallery.getSubtitle();

            mTitleView.setText(title);

            if (subtitle == null || subtitle.isEmpty()) {
                mSubtitleView.setVisibility(View.GONE);
            } else {
                mSubtitleView.setText(subtitle);
            }

            ImageLoader imageLoader = ImageLoaderHelper.getImageLoader(getActivity());
            mImageContainer = imageLoader.get(mGallery.getThumbnail(), new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer imageContainer, boolean isImmediate) {
                    Bitmap bitmap = imageContainer.getBitmap();

                    if (bitmap != null) {
                        mCoverView.setImageBitmap(bitmap);
                        mBackgroundView.setImageBitmap(BitmapHelper.blur(getActivity(), bitmap, 20.f));
                    }
                }

                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    L.e(volleyError);
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mImageContainer != null){
            mImageContainer.cancelRequest();
            mImageContainer = null;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mGallery == null) return;

        inflater.inflate(R.menu.gallery, menu);

        MenuItem star = menu.findItem(R.id.action_star);
        MenuItem unstar = menu.findItem(R.id.action_unstar);

        if (mGallery.getStarred()){
            star.setVisible(false);
        } else {
            unstar.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_star:
                onStarClick();
                return true;

            case R.id.action_unstar:
                onUnstarClick();
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
        args.putInt(PhotoActivity.EXTRA_PAGE, mGallery.getProgress() > 0 ? mGallery.getProgress() : 1);
        intent.putExtras(args);
        startActivity(intent);
    }

    private void onStarClick(){
        mGallery.setStarred(true);
        galleryDao.updateInTx(mGallery);
        invalidateOptionsMenu();
        showToast("Added to collection");
    }

    private void onUnstarClick(){
        mGallery.setStarred(false);
        galleryDao.updateInTx(mGallery);
        invalidateOptionsMenu();
        showToast("Removed from collection");
    }

    private void onDownloadClick(){
        //
    }

    private void invalidateOptionsMenu(){
        getActivity().invalidateOptionsMenu();
    }

    private void showToast(String s){
        Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT).show();
    }
}
