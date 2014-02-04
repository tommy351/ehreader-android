package tw.skyarrow.ehreader.activity;

import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.InjectView;
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

/**
 * Created by SkyArrow on 2014/2/4.
 */
public class ImageSearchPhotoFragment extends Fragment {
    @InjectView(R.id.loading)
    ProgressBar loadingView;

    @InjectView(R.id.error)
    TextView errorView;

    private static final Pattern pSearchUrl = Pattern.compile("<a href=\"http://(g.e-|ex)hentai.org/\\?f_shash=(.+?)\">");

    private SQLiteDatabase db;
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private GalleryDao galleryDao;
    private PhotoDao photoDao;
    private DownloadHelper downloadHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.image_search_photo, container, false);
        ButterKnife.inject(this, view);

        Bundle args = getArguments();
        long photoId = args.getLong("photo");

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(getActivity(), Constant.DB_NAME, null);
        db = helper.getWritableDatabase();
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
        galleryDao = daoSession.getGalleryDao();
        photoDao = daoSession.getPhotoDao();
        downloadHelper = new DownloadHelper(getActivity());

        new GetPhotoSearchURLTask().execute(photoId);

        return view;
    }

    private class GetPhotoSearchURLTask extends AsyncTask<Long, Integer, String> {

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
                errorView.setText(R.string.error_load_gallery_list);
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
}
