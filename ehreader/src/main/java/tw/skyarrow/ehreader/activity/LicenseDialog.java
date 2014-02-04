package tw.skyarrow.ehreader.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.webkit.WebView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import tw.skyarrow.ehreader.R;

/**
 * Created by SkyArrow on 2014/2/3.
 */
public class LicenseDialog extends DialogFragment {
    public static final String TAG = "LicenseDialog";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final WebView webView = new WebView(getActivity());

        builder.setTitle(R.string.open_source_license)
                .setView(webView)
                .setPositiveButton(R.string.ok, null);

        new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream in = getResources().openRawResource(R.raw.license);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                int len;

                try {
                    while ((len = in.read(buf)) != -1) {
                        out.write(buf, 0, len);
                    }

                    in.close();
                    out.close();

                    webView.loadDataWithBaseURL("file:///android_res/raw/", out.toString(), "text/html", "UTF-8", null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).run();

        return builder.create();
    }
}
