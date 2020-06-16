package tw.skyarrow.ehreader.app.login;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.util.L;
import tw.skyarrow.ehreader.util.LoginHelper;

public class LoginDialog extends DialogFragment {
    public static final String TAG = LoginDialog.class.getSimpleName();

    public static final String EXTRA_USERNAME = "username";
    public static final String EXTRA_PASSWORD = "password";

    public static LoginDialog newInstance(String username, String password){
        LoginDialog dialog = new LoginDialog();
        Bundle args = new Bundle();

        args.putString(EXTRA_USERNAME, username);
        args.putString(EXTRA_PASSWORD, password);
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog dialog = new ProgressDialog(getActivity());
        Bundle args = getArguments();
        String username = args.getString(EXTRA_USERNAME);
        String password = args.getString(EXTRA_PASSWORD);

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage(getString(R.string.logging_in));
        dialog.setIndeterminate(true);

        if (savedInstanceState == null){
            login(username, password);
        }

        return dialog;
    }

    private void login(String username, String password){
        LoginHelper.getInstance(getActivity()).login(username, password, new LoginHelper.Listener() {
            @Override
            public void onSuccess() {
                dismiss();
            }
        }, new LoginHelper.ErrorListener() {
            @Override
            public void onError(Throwable e) {
                L.e(e);
                dismiss();
            }
        });
    }
}
