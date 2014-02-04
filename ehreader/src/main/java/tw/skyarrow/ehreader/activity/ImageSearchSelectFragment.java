package tw.skyarrow.ehreader.activity;

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

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import tw.skyarrow.ehreader.Constant;
import tw.skyarrow.ehreader.R;

/**
 * Created by SkyArrow on 2014/1/29.
 */
public class ImageSearchSelectFragment extends Fragment {
    private static final int PHOTO_SELECT = 200;

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

    private MultiPartPostTask uploadTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.image_search_select, container, false);
        ButterKnife.inject(this, view);

        return view;
    }

    @OnClick(R.id.select)
    void onSelectBtnClick() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");

        startActivityForResult(intent, PHOTO_SELECT);
    }

    @OnClick(R.id.cancel)
    void onCancelBtnClick() {
        if (uploadTask != null && !uploadTask.isCancelled() && uploadTask.getStatus() != AsyncTask.Status.FINISHED) {
            uploadTask.cancel(true);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case PHOTO_SELECT:
                if (resultCode == Activity.RESULT_OK) {
                    fileUpload(data.getData());
                }
                break;
        }
    }

    private void fileUpload(Uri image) {
        uploadTask = new MultiPartPostTask();
        uploadTask.execute(image);
    }

    private class MultiPartPostTask extends AsyncTask<Uri, Integer, String> {
        @Override
        protected String doInBackground(Uri... uris) {
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(Constant.IMAGE_SEARCH_URL);
                HttpParams params = httpPost.getParams();
                MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();

                Uri uri = uris[0];
                String[] projection = {MediaStore.Images.ImageColumns.DATA};
                Cursor cursor = getActivity().getContentResolver().query(uri, projection, null, null, null);
                cursor.moveToFirst();
                String path = cursor.getString(0);
                cursor.close();

                File file = new File(path);

                entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                entityBuilder.addBinaryBody("sfile", file);

                HttpEntity entity = entityBuilder.build();
                httpPost.setEntity(entity);
                params.setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);

                HttpResponse response = httpClient.execute(httpPost);
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
            showProgressBar();
        }

        @Override
        protected void onPostExecute(String url) {
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

            ((ImageSearchActivity) getActivity()).displaySelectResult(builder.build().toString());
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
}
