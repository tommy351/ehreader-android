package tw.skyarrow.ehreader.app.photo;

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

/**
 * Created by SkyArrow on 2014/2/9.
 */
public class PhotoIntentActivity extends FragmentActivity {
    @InjectView(R.id.loading)
    ProgressBar loadingView;

    @InjectView(R.id.error)
    TextView errorText;

    @InjectView(R.id.retry)
    Button retryBtn;

    public static final String TAG = "PhotoIntentActivity";

    private Pattern pPhotoUrl = DataLoader.pPhotoUrl;

    private GalleryDao galleryDao;

    private DataLoader dataLoader;
    private long id = 0;
    private String token = "";
    private int page = 0;

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
            Matcher matcher = pPhotoUrl.matcher(url);

            while (matcher.find()) {
                token = matcher.group(2);
                id = Long.parseLong(matcher.group(3));
                page = Integer.parseInt(matcher.group(4));
            }

            if (id == 0 || token.isEmpty() || page == 0) {
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
            new GalleryInfoTask(id, token, page).execute();
        } else {
            showPhoto();
        }
    }

    private class GalleryInfoTask extends AsyncTask<Integer, String, Gallery> {
        private long id;
        private String token;
        private int page;
        private long startLoadAt;

        public GalleryInfoTask(long id, String token, int page) {
            this.id = id;
            this.token = token;
            this.page = page;
        }

        @Override
        protected Gallery doInBackground(Integer... integers) {
            try {
                return dataLoader.getGalleryByPhotoInfo(id, token, page);
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
                showPhoto();

                BaseApplication.getTracker().send(MapBuilder.createTiming(
                        "resources", System.currentTimeMillis() - startLoadAt, "load gallery info", null
                ).build());
            }
        }
    }

    private void showPhoto() {
        Intent intent = new Intent(this, PhotoActivity.class);
        Bundle args = new Bundle();

        args.putLong("id", id);
        args.putInt("page", page - 1);
        intent.putExtras(args);
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
