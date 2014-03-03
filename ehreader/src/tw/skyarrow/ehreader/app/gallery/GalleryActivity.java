package tw.skyarrow.ehreader.app.gallery;

import android.app.SearchManager;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.ShareActionProvider;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.enrique.stackblur.StackBlurManager;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.DateFormat;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnLongClick;
import tw.skyarrow.ehreader.BaseApplication;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.app.download.DownloadConfirmDialog;
import tw.skyarrow.ehreader.app.download.RedownloadDialog;
import tw.skyarrow.ehreader.app.main.MainDrawerActivity;
import tw.skyarrow.ehreader.app.photo.PhotoActivity;
import tw.skyarrow.ehreader.app.search.SearchActivity;
import tw.skyarrow.ehreader.db.DaoMaster;
import tw.skyarrow.ehreader.db.DaoSession;
import tw.skyarrow.ehreader.db.Download;
import tw.skyarrow.ehreader.db.DownloadDao;
import tw.skyarrow.ehreader.db.Gallery;
import tw.skyarrow.ehreader.db.GalleryDao;
import tw.skyarrow.ehreader.util.ActionBarHelper;
import tw.skyarrow.ehreader.util.DatabaseHelper;
import tw.skyarrow.ehreader.util.LoginHelper;

/**
 * Created by SkyArrow on 2014/1/27.
 */
public class GalleryActivity extends MainDrawerActivity {
    @InjectView(R.id.meta)
    TextView metaView;

    @InjectView(R.id.title)
    TextView titleView;

    @InjectView(R.id.rating)
    RatingBar ratingBar;

    @InjectView(R.id.subtitle)
    TextView subtitleView;

    @InjectView(R.id.tags)
    TextView tagView;

    @InjectView(R.id.uploader)
    TextView uploaderView;

    @InjectView(R.id.created)
    TextView createdView;

    @InjectView(R.id.cover_area)
    View coverArea;

    @InjectView(R.id.cover_fg)
    ImageView coverForeground;

    @InjectView(R.id.cover_bg)
    ImageView coverBackground;

    @InjectView(R.id.cover_loading)
    ProgressBar coverLoading;

    public static final String TAG = "GalleryActivity";

    public static final String EXTRA_GALLERY = "id";

    private GalleryDao galleryDao;
    private DownloadDao downloadDao;

    private Gallery gallery;
    private ImageLoader imageLoader;
    private DisplayImageOptions displayOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_wrap);
        setupDrawer();
        ButterKnife.inject(this);
        setDrawerIndicatorEnabled(false);

        DatabaseHelper helper = DatabaseHelper.getInstance(this);
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();
        galleryDao = daoSession.getGalleryDao();
        downloadDao = daoSession.getDownloadDao();

        imageLoader = ImageLoader.getInstance();
        displayOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .build();

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        long galleryId = getIntent().getLongExtra(EXTRA_GALLERY, 0);
        gallery = galleryDao.load(galleryId);

        if (gallery != null) {
            showGallery();
            supportInvalidateOptionsMenu();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        MapBuilder builder = MapBuilder.createAppView();
        builder.set(Fields.SCREEN_NAME, TAG);

        BaseApplication.getTracker().send(builder.build());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gallery, menu);

        if (gallery != null && gallery.getStarred()) {
            menu.findItem(R.id.menu_star).setVisible(false);
        } else {
            menu.findItem(R.id.menu_unstar).setVisible(false);
        }

        MenuItem shareItem = menu.findItem(R.id.menu_share);
        ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        shareActionProvider.setShareIntent(getShareIntent());

        return true;
    }

    private Intent getShareIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        boolean isLoggedIn = LoginHelper.getInstance(this).isLoggedIn();

        intent.putExtra(Intent.EXTRA_TEXT, gallery.getTitle() + " " + gallery.getUrl(isLoggedIn));
        intent.setType("text/plain");

        return intent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ActionBarHelper.upNavigation(this);
                return true;

            case R.id.menu_star:
                starGallery(true);
                return true;

            case R.id.menu_unstar:
                starGallery(false);
                return true;

            case R.id.menu_open_in_browser:
                openInBrowser();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showGallery() {
        int categoryRes = gallery.getCategoryResource();
        String meta = getString(categoryRes) + " / " + gallery.getCount() + "P";

        String[] titles = gallery.getTitles(this);
        String title = titles[0];
        String subtitle = titles[1];

        metaView.setText(meta);
        ratingBar.setRating(gallery.getRating());
        titleView.setText(title);

        if (subtitle.isEmpty()) {
            subtitleView.setVisibility(View.GONE);
        } else {
            subtitleView.setText(subtitle);
        }

        displayCover(gallery.getThumbnail());
        displayTags(gallery.getTags());
        displayUploader(gallery.getUploader());
        displayCreated(gallery.getCreated());
    }

    private void displayCover(String url) {
        imageLoader.loadImage(url, displayOptions, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                coverLoading.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                new Thread(new BlurCoverRunnable(loadedImage)).start();
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                hideLoadingView();
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                hideLoadingView();
            }

            private void hideLoadingView() {
                coverLoading.setVisibility(View.GONE);
            }
        });
    }

    private class BlurCoverRunnable implements Runnable {
        private Bitmap bitmap;

        public BlurCoverRunnable(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        @Override
        public void run() {
            StackBlurManager blurManager = new StackBlurManager(bitmap);
            runOnUiThread(new UpdateCoverRunnable(bitmap, blurManager.processNatively(10)));
        }
    };

    private class UpdateCoverRunnable implements Runnable {
        private Bitmap bitmap;
        private Bitmap background;

        public UpdateCoverRunnable(Bitmap bitmap, Bitmap background) {
            this.bitmap = bitmap;
            this.background = background;
        }

        @Override
        public void run() {
            // http://www.sherif.mobi/2013/01/how-to-get-widthheight-of-view.html
            coverArea.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int coverWidth = coverArea.getMeasuredWidth();
            int coverHeight = coverArea.getMeasuredHeight();
            int bmWidth = bitmap.getWidth();
            int bmHeight = bitmap.getHeight();
            float scale = getScale(bmWidth, bmHeight, coverWidth, coverHeight);

            Animation fadeIn = AnimationUtils.loadAnimation(GalleryActivity.this, R.anim.cover_fade_in);
            Bitmap bg = Bitmap.createScaledBitmap(background, (int) (bmWidth * scale), (int) (bmHeight * scale), true);

            coverForeground.setImageBitmap(bitmap);
            coverForeground.startAnimation(fadeIn);

            coverBackground.setImageBitmap(bg);
            coverBackground.startAnimation(fadeIn);
        }
    }

    private float getScale(int width, int height, int containerWidth, int containerHeight) {
        if (width * containerHeight > height * containerWidth) {
            return (float) height / containerHeight;
        } else {
            return (float) width / containerWidth;
        }
    }

    private void displayTags(String str) {
        try {
            JSONArray arr = new JSONArray(str);

            if (arr.length() == 0) {
                tagView.setVisibility(View.GONE);
                return;
            }

            String[] tags = new String[arr.length()];

            for (int i = 0, len = arr.length(); i < len; i++) {
                tags[i] = arr.getString(i);
            }

            String tagTitle = getString(R.string.meta_tags) + " ";
            String separator = ", ";
            SpannableString sp = new SpannableString(tagTitle + TextUtils.join(separator, tags));
            int length = tagTitle.length();
            int separatorLen = separator.length();

            sp.setSpan(new StyleSpan(Typeface.BOLD), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            for (int i = 0, len = tags.length; i < len; i++) {
                String tag = tags[i];
                int tagLength = tag.length();

                sp.setSpan(new TagSpan(tag), length, length + tagLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                length += tagLength + separatorLen;
            }

            tagView.setText(sp);
            tagView.setMovementMethod(LinkMovementMethod.getInstance());
        } catch (JSONException e) {
            e.printStackTrace();
            tagView.setVisibility(View.GONE);
        }
    }

    private void displayUploader(String uploader) {
        if (uploader == null || uploader.isEmpty()) {
            uploaderView.setVisibility(View.GONE);
            return;
        }

        String title = getString(R.string.meta_uploader) + " ";
        SpannableString sp = new SpannableString(title + uploader);
        int titleLength = title.length();

        sp.setSpan(new StyleSpan(Typeface.BOLD), 0, titleLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sp.setSpan(new TagSpan("uploader:" + uploader), titleLength, titleLength + uploader.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        uploaderView.setText(sp);
        uploaderView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private class TagSpan extends ClickableSpan {
        private String tag;

        public TagSpan(String tag) {
            this.tag = tag;
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(GalleryActivity.this, SearchActivity.class);

            intent.setAction(Intent.ACTION_SEARCH);
            intent.putExtra(SearchManager.QUERY, tag);

            startActivity(intent);
        }
    }

    private void displayCreated(Date date) {
        if (date == null) {
            createdView.setVisibility(View.GONE);
            return;
        }

        DateFormat dateFormat = DateFormat.getDateInstance();
        String dateString = dateFormat.format(date);
        String title = getString(R.string.meta_created) + " ";
        SpannableString sp = new SpannableString(title + dateString);
        int titleLength = title.length();

        sp.setSpan(new StyleSpan(Typeface.BOLD), 0, titleLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        createdView.setText(sp);
    }

    private void starGallery(boolean isStarred) {
        gallery.setStarred(isStarred);
        galleryDao.update(gallery);

        if (isStarred) {
            showToast(R.string.notification_starred);
        } else {
            showToast(R.string.notification_unstarred);
        }

        supportInvalidateOptionsMenu();

        BaseApplication.getTracker().send(MapBuilder.createEvent(
                "UI", "button", "star", null
        ).build());
    }

    private void showToast(int res) {
        Toast.makeText(this, res, Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.download)
    void downloadGallery() {
        Download download = downloadDao.load(gallery.getId());
        Bundle args = new Bundle();
        DialogFragment dialog;
        String tag;

        if (download == null) {
            dialog = new DownloadConfirmDialog();
            tag = DownloadConfirmDialog.TAG;

            args.putLong(DownloadConfirmDialog.EXTRA_GALLERY, gallery.getId());
            args.putLong(DownloadConfirmDialog.EXTRA_SIZE, gallery.getSize());
        } else {
            dialog = new RedownloadDialog();
            tag = RedownloadDialog.TAG;

            args.putLong(RedownloadDialog.EXTRA_GALLERY, gallery.getId());
        }

        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), tag);
    }

    private void openInBrowser() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        boolean isLoggedIn = LoginHelper.getInstance(this).isLoggedIn();

        BaseApplication.getTracker().send(MapBuilder.createEvent(
                "UI", "button", "open in browser", null
        ).build());

        intent.setData(gallery.getUri(isLoggedIn));
        startActivity(intent);
    }

    @OnClick(R.id.read)
    void onReadBtnClick() {
        Intent intent = new Intent(this, PhotoActivity.class);
        Bundle args = new Bundle();

        args.putLong("id", gallery.getId());

        BaseApplication.getTracker().send(MapBuilder.createEvent(
                "UI", "button", "read", null
        ).build());

        intent.putExtras(args);
        startActivity(intent);
    }

    @OnLongClick(R.id.read)
    boolean onReadBtnLongClick() {
        DialogFragment dialog = new GalleryPageDialog();
        Bundle args = new Bundle();

        args.putLong(GalleryPageDialog.EXTRA_GALLERY, gallery.getId());
        args.putInt(GalleryPageDialog.EXTRA_DEFAULT_PAGE, gallery.getProgress());
        args.putInt(GalleryPageDialog.EXTRA_TOTAL_PAGE, gallery.getCount());

        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), GalleryPageDialog.TAG);

        return true;
    }
}
