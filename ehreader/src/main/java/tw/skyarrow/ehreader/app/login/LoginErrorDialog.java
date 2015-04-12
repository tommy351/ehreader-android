package tw.skyarrow.ehreader.app.login;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import tw.skyarrow.ehreader.R;

public class LoginErrorDialog extends DialogFragment {
    public static final String TAG = LoginErrorDialog.class.getSimpleName();

    public static LoginErrorDialog newInstance(){
        return new LoginErrorDialog();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.login_failed_title)
                .setMessage(R.string.login_failed_msg)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dismiss();
                    }
                });

        return builder.create();
    }
}
