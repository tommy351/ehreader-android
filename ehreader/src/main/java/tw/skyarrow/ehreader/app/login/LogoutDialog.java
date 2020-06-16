package tw.skyarrow.ehreader.app.login;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.util.LoginHelper;

public class LogoutDialog extends DialogFragment {
    public static final String TAG = LogoutDialog.class.getSimpleName();

    public static LogoutDialog newInstance(){
        return new LogoutDialog();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LoginHelper loginHelper = LoginHelper.getInstance(getActivity());

        builder.setTitle(R.string.logout_title)
                .setMessage(R.string.logout_msg)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        logout();
                    }
                })
                .setNegativeButton(R.string.cancel, null);

        return builder.create();
    }

    private void logout(){
        LoginHelper.getInstance(getActivity()).logout();
    }
}
