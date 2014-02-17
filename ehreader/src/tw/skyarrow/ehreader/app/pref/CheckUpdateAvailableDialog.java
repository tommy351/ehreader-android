package tw.skyarrow.ehreader.app.pref;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;

import tw.skyarrow.ehreader.BaseApplication;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.util.UpdateChecker;

/**
 * Created by SkyArrow on 2014/2/17.
 */
public class CheckUpdateAvailableDialog extends DialogFragment {
    public static final String TAG = "CheckUpdateAvailableDialog";

    private String version;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        Bundle args = getArguments();
        version = args.getString("version");

        dialog.setTitle(R.string.check_update_available_title)
                .setMessage(getString(R.string.check_update_available_msg, version))
                .setPositiveButton(R.string.update, onSubmitClick)
                .setNegativeButton(R.string.cancel, null);

        MapBuilder builder = MapBuilder.createAppView();
        builder.set(Fields.SCREEN_NAME, TAG);

        BaseApplication.getTracker().send(builder.build());

        return dialog.create();
    }

    private DialogInterface.OnClickListener onSubmitClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            Intent intent = new Intent(Intent.ACTION_VIEW);

            intent.setData(Uri.parse(UpdateChecker.getDownloadUrl(version)));
            startActivity(intent);
        }
    };
}
