package tw.skyarrow.ehreader.app.search;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.analytics.tracking.android.MapBuilder;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import tw.skyarrow.ehreader.BaseApplication;
import tw.skyarrow.ehreader.Constant;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.api.DataLoader;
import tw.skyarrow.ehreader.util.LoginHelper;
import tw.skyarrow.ehreader.util.NetworkHelper;
import tw.skyarrow.ehreader.util.ObservableHttpEntity;

/**
 * Created by SkyArrow on 2014/1/29.
 */
public class ImageSearchSelectFragment extends Fragment {
    @InjectView(R.id.select)
    Button selectBtn;

    @InjectView(R.id.progress_view)
    View progressView;

    @InjectView(R.id.progress_bar)
    ProgressBar progressBar;

    @InjectView(R.id.progress_text)
    TextView progressText;

    @InjectView(R.id.cancel)
    Button cancelBtn;

    @InjectView(R.id.similar)
    CheckBox similarSearch;

    @InjectView(R.id.only_cover)
    CheckBox onlyCover;

    @InjectView(R.id.retry)
    Button retryBtn;

    @InjectView(R.id.error)
    TextView errorView;

    public static final String TAG = "ImageSearchSelectFragment";

    public static final String EXTRA_DATA = "data";

    private static final int PHOTO_SELECT = 200;

    private MultiPartPostTask uploadTask;
    private boolean loggedIn;
    private boolean backStack = true;
    private Uri uri;
    private NetworkHelper network;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.image_search_select, container, false);
        ButterKnife.inject(this, view);

        Bundle args = getArguments();
        loggedIn = LoginHelper.getInstance(getActivity()).isLoggedIn();
        network = NetworkHelper.getInstance(getActivity());

        if (args != null && args.getParcelable(EXTRA_DATA) != null) {
            uri = args.getParcelable(EXTRA_DATA);
            backStack = false;

            fileUpload();
        }

        return view;
    }

    @OnClick(R.id.select)
    void onSelectBtnClick() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");

        BaseApplication.getTracker().send(MapBuilder.createEvent(
                "UI", "button", "select image search", null
        ).build());

        startActivityForResult(intent, PHOTO_SELECT);
    }

    @OnClick(R.id.cancel)
    void onCancelBtnClick() {
        if (uploadTask != null && !uploadTask.isCancelled() && uploadTask.getStatus() != AsyncTask.Status.FINISHED) {
            uploadTask.cancel(true);
        }

        BaseApplication.getTracker().send(MapBuilder.createEvent(
                "UI", "button", "cancel image search", null
        ).build());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case PHOTO_SELECT:
                if (resultCode == Activity.RESULT_OK) {
                    uri = data.getData();
                    fileUpload();
                }
                break;
        }
    }

    private void fileUpload() {
        if (network.isAvailable()) {
            uploadTask = new MultiPartPostTask();
            uploadTask.execute(uri);
        } else {
            showError(R.string.error_no_network, true);
        }
    }

    private class MultiPartPostTask extends AsyncTask<Uri, Integer, String> implements ObservableHttpEntity.OnWriteListener {
        private long totalSize = 0;
        private long startLoadAt;

        @Override
        protected String doInBackground(Uri... uris) {
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(loggedIn ? Constant.IMAGE_SEARCH_URL_EX : Constant.IMAGE_SEARCH_URL);
                HttpContext httpContext = DataLoader.getInstance(getActivity()).getHttpContext();
                HttpParams params = httpPost.getParams();
                MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();

                Uri uri = uris[0];
                String[] projection = {MediaStore.Images.ImageColumns.DATA};
                Cursor cursor = getActivity().getContentResolver().query(uri, projection, null, null, null);
                String path = "";

                if (cursor == null) {
                    path = uri.getPath();
                } else {
                    cursor.moveToFirst();
                    int idx = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA);
                    path = cursor.getString(idx);
                    cursor.close();
                }

                if (path.isEmpty()) return null;

                File file = new File(path);
                totalSize = file.length();

                entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                entityBuilder.addBinaryBody("sfile", file);

                ObservableHttpEntity entity = new ObservableHttpEntity(entityBuilder.build());
                entity.setOnWriteListener(this);
                httpPost.setEntity(entity);
                params.setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);

                HttpResponse response = httpClient.execute(httpPost, httpContext);
                Header location = response.getLastHeader("Location");

                if (location != null) {
                    return location.getValue();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            startLoadAt = System.currentTimeMillis();

            showProgressBar();
        }

        @Override
        protected void onPostExecute(String url) {
            if (url == null) {
                showError(R.string.error_upload_image, true);
                return;
            }

            Uri uri = Uri.parse(url);
            String hash = uri.getQueryParameter("f_shash");
            String from = uri.getQueryParameter("fs_from");

            Uri.Builder builder = uri.buildUpon();

            builder.clearQuery();
            builder.appendQueryParameter("f_shash", hash);
            builder.appendQueryParameter("fs_from", from);

            if (similarSearch.isChecked()) {
                builder.appendQueryParameter("fs_similar", "1");
            }

            if (onlyCover.isChecked()) {
                builder.appendQueryParameter("fs_covers", "1");
            }

            BaseApplication.getTracker().send(MapBuilder.createTiming(
                    "resources", System.currentTimeMillis() - startLoadAt, "upload image search", null
            ).build());

            ((ImageSearchActivity) getActivity()).displaySelectResult(builder.build().toString(), backStack);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            int progress = values[0];

            if (progressBar.isIndeterminate()) {
                progressBar.setIndeterminate(false);
            }

            progressBar.setProgress(progress);
            progressText.setText(progress + "%");
        }

        @Override
        protected void onCancelled() {
            hideProgressBar();
        }

        @Override
        public void onWrite(long totalSent) {
            publishProgress((int) (totalSent * 100f / totalSize));
        }
    }

    private void showProgressBar() {
        selectBtn.setVisibility(View.GONE);
        cancelBtn.setVisibility(View.VISIBLE);
        progressView.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        selectBtn.setVisibility(View.VISIBLE);
        cancelBtn.setVisibility(View.GONE);
        progressView.setVisibility(View.GONE);
    }

    private void showError(int res, boolean retry) {
        selectBtn.setVisibility(View.GONE);
        cancelBtn.setVisibility(View.GONE);
        progressView.setVisibility(View.GONE);
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
        fileUpload();

        BaseApplication.getTracker().send(MapBuilder.createEvent(
                "UI", "button", "retry image search", null
        ).build());
    }
}
