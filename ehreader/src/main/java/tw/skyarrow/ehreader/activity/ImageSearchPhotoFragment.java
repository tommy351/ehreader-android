package tw.skyarrow.ehreader.activity;

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

import org.json.JSONException;
import org.json.JSONObject;

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
import tw.skyarrow.ehreader.db.Photo;
import tw.skyarrow.ehreader.db.PhotoDao;
import tw.skyarrow.ehreader.util.DownloadHelper;
import tw.skyarrow.ehreader.util.L;
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

    private SQLiteDatabase db;
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private GalleryDao galleryDao;
    private PhotoDao photoDao;
    private DownloadHelper downloadHelper;
    private NetworkHelper network;

    private long photoId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_intent, container, false);
        ButterKnife.inject(this, view);

        Bundle args = getArguments();
        photoId = args.getLong("photo");

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(getActivity(), Constant.DB_NAME, null);
        db = helper.getWritableDatabase();
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
        galleryDao = daoSession.getGalleryDao();
        photoDao = daoSession.getPhotoDao();
        downloadHelper = new DownloadHelper(getActivity());
        network = new NetworkHelper(getActivity());

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
        @Override
        protected String doInBackground(Long... longs) {
            try {
                Photo photo = photoDao.load(longs[0]);
                Gallery gallery = galleryDao.load(photo.getGalleryId());
                JSONObject json = downloadHelper.getPhotoInfoRaw(gallery, photo);

                if (json.has("error")) {
                    L.e("error: %s", json.getString("error"));
                    return null;
                }

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
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
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

            ((ImageSearchActivity) getActivity()).displayPhotoResult(builder.build().toString());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        db.close();
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
    }
}
