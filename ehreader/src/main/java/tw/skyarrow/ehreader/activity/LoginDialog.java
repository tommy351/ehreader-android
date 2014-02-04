package tw.skyarrow.ehreader.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import tw.skyarrow.ehreader.R;

/**
 * Created by SkyArrow on 2014/2/4.
 */
public class LoginDialog extends DialogFragment {
    @InjectView(R.id.username)
    TextView usernameText;

    @InjectView(R.id.password)
    TextView passwordText;

    public static final String TAG = "LoginDialog";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_login, null);
        ButterKnife.inject(this, view);

        builder.setTitle(R.string.login_title)
                .setView(view)
                .setPositiveButton(R.string.login_btn, onSubmitClick)
                .setNegativeButton(R.string.cancel, null);

        return builder.create();
    }

    private DialogInterface.OnClickListener onSubmitClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            //
        }
    };
}
