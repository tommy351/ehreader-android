package tw.skyarrow.ehreader.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.github.zafarkhaja.semver.Version;

import org.json.JSONException;

import java.io.IOException;

import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.util.UpdateChecker;

/**
 * Created by SkyArrow on 2014/2/17.
 */
public class CheckUpdateDialog extends DialogFragment {
    public static final String TAG = "CheckUpdateDialog";

    private UpdateCheckTask task;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog dialog = new ProgressDialog(getActivity());

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setTitle(R.string.checking_update);
        dialog.setMessage(getString(R.string.checking_update));
        dialog.setIndeterminate(true);

        task = new UpdateCheckTask();
        task.execute("");

        return dialog;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (!task.isCancelled()) task.cancel(true);
    }

    private class UpdateCheckTask extends AsyncTask<String, Integer, Version> {
        private UpdateChecker updateChecker;

        public UpdateCheckTask() {
            this.updateChecker = new UpdateChecker(getActivity());
        }

        @Override
        protected Version doInBackground(String... strings) {
            try {
                return updateChecker.getLatestVersion();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Version version) {
            DialogFragment dialog;
            String tag;
            Bundle args = new Bundle();

            if (version == null) {
                dialog = new CheckUpdateErrorDialog();
                tag = CheckUpdateErrorDialog.TAG;
            } else {
                args.putString("version", version.toString());

                if (updateChecker.compare(version)) {
                    dialog = new CheckUpdateAvailableDialog();
                    tag = CheckUpdateAvailableDialog.TAG;

                    args.putString("current", updateChecker.getVersionCode());
                } else {
                    dialog = new CheckUpdateLatestDialog();
                    tag = CheckUpdateLatestDialog.TAG;
                }
            }

            dialog.setArguments(args);
            dialog.show(getActivity().getSupportFragmentManager(), tag);
            dismiss();
        }
    }
}
