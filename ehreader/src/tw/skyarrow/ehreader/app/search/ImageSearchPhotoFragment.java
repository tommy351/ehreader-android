package tw.skyarrow.ehreader.app.search;

import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.analytics.tracking.android.MapBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import tw.skyarrow.ehreader.BaseApplication;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.api.ApiCallException;
import tw.skyarrow.ehreader.api.ApiErrorCode;
import tw.skyarrow.ehreader.api.DataLoader;
import tw.skyarrow.ehreader.db.DaoMaster;
import tw.skyarrow.ehreader.db.DaoSession;
import tw.skyarrow.ehreader.db.Gallery;
import tw.skyarrow.ehreader.db.GalleryDao;
import tw.skyarrow.ehreader.db.Photo;
import tw.skyarrow.ehreader.db.PhotoDao;
import tw.skyarrow.ehreader.util.DatabaseHelper;
import tw.skyarrow.ehreader.util.NetworkHelper;

/**
 * Created by SkyArrow on 2014/2/4.
 */
public class ImageSearchPhotoFragment extends Fragment {
    @InjectView(R.id.loading)
    ProgressBar loadingView;

    @InjectView(R.id.error)
    TextView errorView;

    @InjectView(R.id.retry)
    Button retryBtn;

    private static final Pattern pSearchUrl = Pattern.compile("<a href=\"http://(g.e-|ex)hentai.org/\\?f_shash=(.+?)\">");

    private GalleryDao galleryDao;
    private PhotoDao photoDao;
    private DataLoader dataLoader;
    private NetworkHelper network;

    private long photoId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_intent, container, false);
        ButterKnife.inject(this, view);

        Bundle args = getArguments();
        photoId = args.getLong("photo");

        DatabaseHelper helper = DatabaseHelper.getInstance(getActivity());
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();
        galleryDao = daoSession.getGalleryDao();
        photoDao = daoSession.getPhotoDao();
        dataLoader = DataLoader.getInstance(getActivity());
        network = NetworkHelper.getInstance(getActivity());

        searchPhoto();

        return view;
    }

    private void searchPhoto() {
        if (network.isAvailable()) {
            loadingView.setVisibility(View.VISIBLE);
            new SearchPhotoTask().execute(photoId);
        } else {
            showError(R.string.error_no_network, true);
        }
    }

    private class SearchPhotoTask extends AsyncTask<Long, Integer, String> {
        private long startLoadAt;

        @Override
        protected String doInBackground(Long... longs) {
            Photo photo = photoDao.load(longs[0]);
            Gallery gallery = galleryDao.load(photo.getGalleryId());
            JSONObject json;

            try {
                json = dataLoader.getPhotoRaw(gallery, photo);
            } catch (ApiCallException e) {
                if (e.getCode() == ApiErrorCode.SHOWKEY_EXPIRED || e.getCode() == ApiErrorCode.SHOWKEY_INVALID) {
                    gallery.setShowkey(null);
                    galleryDao.updateInTx(gallery);
                    json = dataLoader.getPhotoRaw(gallery, photo);
                } else {
                    return null;
                }
            }

            if (json == null) return null;

            try {
                Matcher matcher = pSearchUrl.matcher(json.getString("i6"));
                String prefix = "";
                String hash = "";

                while (matcher.find()) {
                    prefix = matcher.group(1);
                    hash = matcher.group(2);
                }

                if (hash.isEmpty()) {
                    return null;
                } else {
                    return "http://" + prefix + "hentai.org/?f_shash=" + hash;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            startLoadAt = System.currentTimeMillis();
        }

        @Override
        protected void onPostExecute(String url) {
            if (url == null) {
                loadingView.setVisibility(View.GONE);
                showError(R.string.error_load_gallery_list, true);
                return;
            }

            Uri.Builder builder = Uri.parse(url).buildUpon();
            builder.appendQueryParameter("fs_similar", "1");

            BaseApplication.getTracker().send(MapBuilder.createTiming(
                    "resources", System.currentTimeMillis() - startLoadAt, "load image search url of photo", null
            ).build());

            ((ImageSearchActivity) getActivity()).displayPhotoResult(builder.build().toString());
        }
    }

    private void showError(int res, boolean retry) {
        errorView.setVisibility(View.VISIBLE);
        errorView.setText(res);

        if (retry) {
            retryBtn.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.retry)
    void onRetryBtnClick() {
        errorView.setVisibility(View.GONE);
        retryBtn.setVisibility(View.GONE);
        searchPhoto();

        BaseApplication.getTracker().send(MapBuilder.createEvent(
                "UI", "button", "retry image search", null
        ).build());
    }
}
