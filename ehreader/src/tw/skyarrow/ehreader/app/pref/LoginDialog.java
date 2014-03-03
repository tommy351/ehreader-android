package tw.skyarrow.ehreader.app.pref;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.google.analytics.tracking.android.MapBuilder;

import java.io.IOException;

import tw.skyarrow.ehreader.BaseApplication;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.util.LoginHelper;

/**
 * Created by SkyArrow on 2014/2/7.
 */
public class LoginDialog extends DialogFragment {
    public static final String TAG = "LoginDialog";

    public static final String EXTRA_USERNAME = "username";
    public static final String EXTRA_PASSWORD = "password";

    private String username;
    private String password;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog dialog = new ProgressDialog(getActivity());
        Bundle args = getArguments();
        username = args.getString(EXTRA_USERNAME);
        password = args.getString(EXTRA_PASSWORD);

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setTitle(R.string.logging_in);
        dialog.setMessage(getString(R.string.logging_in));
        dialog.setIndeterminate(true);

        new LoginTask().execute("");

        return dialog;
    }

    private class LoginTask extends AsyncTask<String, Integer, Boolean> {
        private long startLoadAt;

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                return LoginHelper.getInstance(getActivity()).login(username, password);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPreExecute() {
            startLoadAt = System.currentTimeMillis();
        }

        @Override
        protected void onPostExecute(Boolean isSuccess) {
            if (!isSuccess) {
                DialogFragment dialog = new LoginErrorDialog();
                dialog.show(getActivity().getSupportFragmentManager(), LoginErrorDialog.TAG);
            }

            BaseApplication.getTracker().send(MapBuilder.createTiming(
                    "resources", System.currentTimeMillis() - startLoadAt, "login", null
            ).build());

            dismiss();
        }
    }
}
