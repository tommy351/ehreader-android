package tw.skyarrow.ehreader.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import tw.skyarrow.ehreader.R;

/**
 * Created by SkyArrow on 2014/2/7.
 */
public class LoginErrorDialog extends DialogFragment {
    public static final String TAG = "LoginErrorDialog";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.login_failed_title)
                .setMessage(R.string.login_failed_msg)
                .setPositiveButton(R.string.retry, onSubmitClick)
                .setNegativeButton(R.string.cancel, null);

        return builder.create();
    }

    private DialogInterface.OnClickListener onSubmitClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            DialogFragment dialog = new LoginPromptDialog();

            dialog.show(getActivity().getSupportFragmentManager(), LoginPromptDialog.TAG);
        }
    };
}
