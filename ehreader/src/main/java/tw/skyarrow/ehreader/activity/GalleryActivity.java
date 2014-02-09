package tw.skyarrow.ehreader.activity;

import android.app.SearchManager;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.DateFormat;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import tw.skyarrow.ehreader.BaseApplication;
import tw.skyarrow.ehreader.Constant;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.db.DaoMaster;
import tw.skyarrow.ehreader.db.DaoSession;
import tw.skyarrow.ehreader.db.Download;
import tw.skyarrow.ehreader.db.DownloadDao;
import tw.skyarrow.ehreader.db.Gallery;
import tw.skyarrow.ehreader.db.GalleryDao;
import tw.skyarrow.ehreader.util.ActionBarHelper;
import tw.skyarrow.ehreader.util.BitmapHelper;

/**
 * Created by SkyArrow on 2014/1/27.
 */
public class GalleryActivity extends ActionBarActivity {
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

    @InjectView(R.id.read)
    Button readBtn;

    public static final String TAG = "GalleryActivity";

    private SQLiteDatabase db;
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private GalleryDao galleryDao;
    private DownloadDao downloadDao;

    private AQuery aq;
    private Gallery gallery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        ButterKnife.inject(this);

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, Constant.DB_NAME, null);
        db = helper.getWritableDatabase();
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
        galleryDao = daoSession.getGalleryDao();
        downloadDao = daoSession.getDownloadDao();

        aq = new AQuery(this);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        Bundle args = getIntent().getExtras();
        long galleryId = args.getLong("id");
        gallery = galleryDao.load(galleryId);

        if (gallery != null) {
            showGallery();
            invalidateOptionsMenu();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        MapBuilder builder = MapBuilder.createAppView();
        builder.set(Fields.SCREEN_NAME, TAG);
        builder.set(Fields.TITLE, gallery.getTitle());
        builder.set(Fields.DESCRIPTION, gallery.getId() + "/" + gallery.getToken());

        BaseApplication.getTracker().send(builder.build());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
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
        boolean isLoggedIn = BaseApplication.isLoggedIn();

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

            case R.id.menu_download:
                downloadGallery();
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

        metaView.setText(meta);
        titleView.setText(gallery.getTitle());
        ratingBar.setRating(gallery.getRating());

        if (gallery.getSubtitle().isEmpty()) {
            subtitleView.setVisibility(View.GONE);
        } else {
            subtitleView.setText(gallery.getSubtitle());
        }

        displayCover(gallery.getThumbnail());
        displayTags(gallery.getTags());
        displayUploader(gallery.getUploader());
        displayCreated(gallery.getCreated());
    }

    private void displayCover(String url) {
        aq.ajax(url, Bitmap.class, 1000 * 60 * 60 * 12, new AjaxCallback<Bitmap>() {
            @Override
            public void callback(String url, Bitmap bm, AjaxStatus status) {
                // http://www.sherif.mobi/2013/01/how-to-get-widthheight-of-view.html
                coverArea.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                int coverWidth = coverArea.getMeasuredWidth();
                int coverHeight = coverArea.getMeasuredHeight();

                new Thread(new BlurCoverRunnable(bm, coverWidth, coverHeight)).start();
            }
        });
    }

    private class BlurCoverRunnable implements Runnable {
        private Bitmap bitmap;
        private int coverWidth;
        private int coverHeight;

        public BlurCoverRunnable(Bitmap bitmap, int coverWidth, int coverHeight) {
            this.bitmap = bitmap;
            this.coverWidth = coverWidth;
            this.coverHeight = coverHeight;
        }

        @Override
        public void run() {
            int bmWidth = bitmap.getWidth();
            int bmHeight= bitmap.getHeight();
            float scale;

            if (bmWidth * coverHeight > bmHeight * coverWidth) {
                scale = (float) coverHeight / (float) bmHeight;
            } else {
                scale = (float) coverWidth / (float) bmWidth;
            }

            Bitmap bg = Bitmap.createScaledBitmap(BitmapHelper.blur(bitmap, 10), (int) (bmWidth * scale), (int) (bmHeight * scale), true);
            runOnUiThread(new UpdateCoverRunnable(bg, bitmap));
        }
    };

    private class UpdateCoverRunnable implements Runnable {
        private Bitmap background;
        private Bitmap foreground;

        public UpdateCoverRunnable(Bitmap background, Bitmap foreground) {
            this.background = background;
            this.foreground = foreground;
        }

        @Override
        public void run() {
            Animation fadeIn = AnimationUtils.loadAnimation(GalleryActivity.this, R.anim.cover_fade_in);

            coverBackground.setImageBitmap(background);
            coverBackground.startAnimation(fadeIn);

            coverForeground.setImageBitmap(foreground);
            coverForeground.startAnimation(fadeIn);
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
                "ui_action", "button_press", "star_button", null
        ).build());
    }

    private void showToast(int res) {
        Toast.makeText(this, res, Toast.LENGTH_SHORT).show();
    }

    private void downloadGallery() {
        Download download = downloadDao.load(gallery.getId());
        Bundle args = new Bundle();
        DialogFragment dialog;
        String tag;

        args.putLong("id", gallery.getId());
        args.putLong("size", gallery.getSize());

        if (download == null) {
            dialog = new DownloadConfirmDialog();
            tag = DownloadConfirmDialog.TAG;
        } else {
            dialog = new RedownloadDialog();
            tag = RedownloadDialog.TAG;
        }

        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), tag);
    }

    private void openInBrowser() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        boolean isLoggedIn = BaseApplication.isLoggedIn();

        intent.setData(gallery.getUri(isLoggedIn));
        startActivity(intent);
    }

    @OnClick(R.id.read)
    void onReadBtnClick() {
        Intent intent = new Intent(this, PhotoActivity.class);
        Bundle args = new Bundle();

        args.putLong("id", gallery.getId());

        intent.putExtras(args);
        startActivity(intent);

    }
}
