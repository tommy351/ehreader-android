package tw.skyarrow.ehreader.activity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import tw.skyarrow.ehreader.Constant;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.db.DaoMaster;
import tw.skyarrow.ehreader.db.DaoSession;
import tw.skyarrow.ehreader.db.Gallery;
import tw.skyarrow.ehreader.db.GalleryDao;
import tw.skyarrow.ehreader.util.ActionBarHelper;
import tw.skyarrow.ehreader.util.DownloadHelper;
import tw.skyarrow.ehreader.util.L;

/**
 * Created by SkyArrow on 2014/2/9.
 */
public class GalleryIntentActivity extends ActionBarActivity {
    @InjectView(R.id.loading)
    ProgressBar loadingView;

    @InjectView(R.id.error)
    TextView errorText;

    @InjectView(R.id.retry)
    Button retryBtn;

    public static final String TAG = "GalleryActivity";

    private static final Pattern pGalleryUrl = DownloadHelper.pGalleryUrl;

    private SQLiteDatabase db;
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private GalleryDao galleryDao;

    private DownloadHelper downloadHelper;
    private long id = 0;
    private String token = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intent);
        ButterKnife.inject(this);

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, Constant.DB_NAME, null);
        db = helper.getWritableDatabase();
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
        galleryDao = daoSession.getGalleryDao();
        downloadHelper = new DownloadHelper(this);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        Intent intent = getIntent();

        if (intent.getData() != null) {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
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

        public GalleryInfoTask(long id, String token) {
            this.id = id;
            this.token = token;
        }

        @Override
        protected Gallery doInBackground(Integer... integers) {
            try {
                Gallery gallery = downloadHelper.getGalleryInfo(id, token);

                return gallery;
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Gallery gallery) {
            if (gallery == null) {
                loadingView.setVisibility(View.GONE);
                errorText.setText(R.string.error_fetch_gallery);
                retryBtn.setVisibility(View.VISIBLE);
            } else {
                showGallery(gallery);
            }
        }
    }

    private void showGallery(Gallery gallery) {
        Intent intent = new Intent(this, GalleryActivity.class);
        Bundle args = new Bundle();

        args.putLong("id", gallery.getId());
        intent.putExtras(args);
        startActivity(intent);
    }

    @OnClick(R.id.retry)
    void onRetryBtnClick() {
        getGalleryInfo();
    }
}
