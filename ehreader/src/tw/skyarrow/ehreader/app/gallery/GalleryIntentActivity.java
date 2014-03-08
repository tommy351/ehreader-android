package tw.skyarrow.ehreader.app.gallery;

import android.app.ActionBar;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.analytics.tracking.android.MapBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import tw.skyarrow.ehreader.BaseApplication;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.api.ApiCallException;
import tw.skyarrow.ehreader.api.DataLoader;
import tw.skyarrow.ehreader.db.DaoMaster;
import tw.skyarrow.ehreader.db.DaoSession;
import tw.skyarrow.ehreader.db.Gallery;
import tw.skyarrow.ehreader.db.GalleryDao;
import tw.skyarrow.ehreader.util.ActionBarHelper;
import tw.skyarrow.ehreader.util.DatabaseHelper;
import tw.skyarrow.ehreader.util.L;

/**
 * Created by SkyArrow on 2014/2/9.
 */
public class GalleryIntentActivity extends FragmentActivity {
    @InjectView(R.id.loading)
    ProgressBar loadingView;

    @InjectView(R.id.error)
    TextView errorText;

    @InjectView(R.id.retry)
    Button retryBtn;

    public static final String TAG = "GalleryIntentActivity";

    private static final Pattern pGalleryUrl = DataLoader.pGalleryUrl;

    private GalleryDao galleryDao;

    private DataLoader dataLoader;
    private long id = 0;
    private String token = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intent);
        ButterKnife.inject(this);

        DatabaseHelper helper = DatabaseHelper.getInstance(this);
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();
        galleryDao = daoSession.getGalleryDao();
        dataLoader = DataLoader.getInstance(this);

        ActionBar actionBar = getActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        Intent intent = getIntent();

        if (Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null) {
            String url = intent.getData().toString();
            Matcher matcher = pGalleryUrl.matcher(url);

            L.d("Intent url: %s", url);

            while (matcher.find()) {
                id = Long.parseLong(matcher.group(2));
                token = matcher.group(3);
            }

            if (id == 0 || token.isEmpty()) {
                errorText.setText(R.string.error_token_invalid);
            } else {
                getGalleryInfo();
            }
        } else {
            errorText.setText(R.string.error_no_data);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ActionBarHelper.upNavigation(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getGalleryInfo() {
        if (token == null) {
            errorText.setText(R.string.error_token_invalid);
            return;
        }

        Gallery gallery = galleryDao.load(id);

        if (gallery == null) {
            loadingView.setVisibility(View.VISIBLE);
            errorText.setText("");
            retryBtn.setVisibility(View.GONE);
            new GalleryInfoTask(id, token).execute();
        } else {
            showGallery(gallery);
        }
    }

    private class GalleryInfoTask extends AsyncTask<Integer, String, Gallery> {
        private long id;
        private String token;
        private long startLoadAt;

        public GalleryInfoTask(long id, String token) {
            this.id = id;
            this.token = token;
        }

        @Override
        protected Gallery doInBackground(Integer... integers) {
            try {
                return dataLoader.getGallery(id, token);
            } catch (ApiCallException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            startLoadAt = System.currentTimeMillis();
        }

        @Override
        protected void onPostExecute(Gallery gallery) {
            if (gallery == null) {
                loadingView.setVisibility(View.GONE);
                errorText.setText(R.string.error_fetch_gallery);
                retryBtn.setVisibility(View.VISIBLE);
            } else {
                showGallery(gallery);

                BaseApplication.getTracker().send(MapBuilder.createTiming(
                        "resources", System.currentTimeMillis() - startLoadAt, "load gallery info", null
                ).build());
            }
        }
    }

    private void showGallery(Gallery gallery) {
        Intent intent = new Intent(this, GalleryActivity.class);

        intent.putExtra(GalleryActivity.EXTRA_GALLERY, gallery.getId());
        startActivity(intent);
    }

    @OnClick(R.id.retry)
    void onRetryBtnClick() {
        getGalleryInfo();

        BaseApplication.getTracker().send(MapBuilder.createEvent(
                "UI", "button", "retry loading gallery info", null
        ).build());
    }
}
