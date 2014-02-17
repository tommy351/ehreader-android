package tw.skyarrow.ehreader.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.TextView;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;

import butterknife.ButterKnife;
import butterknife.InjectView;
import tw.skyarrow.ehreader.BaseApplication;
import tw.skyarrow.ehreader.R;

/**
 * Created by SkyArrow on 2014/2/4.
 */
public class LoginPromptDialog extends DialogFragment {
    @InjectView(R.id.username)
    TextView usernameText;

    @InjectView(R.id.password)
    TextView passwordText;

    public static final String TAG = "LoginPromptDialog";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_login, null);
        ButterKnife.inject(this, view);

        builder.setTitle(R.string.login_title)
                .setView(view)
                .setPositiveButton(R.string.login_btn, null)
                .setNegativeButton(R.string.cancel, null);

        AlertDialog dialog = builder.create();

        dialog.show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(onSubmitClick);

        MapBuilder mapBuilder = MapBuilder.createAppView();
        mapBuilder.set(Fields.SCREEN_NAME, TAG);

        BaseApplication.getTracker().send(mapBuilder.build());

        return dialog;
    }

    private View.OnClickListener onSubmitClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String username = usernameText.getText().toString();
            String password = passwordText.getText().toString();

            if (username.isEmpty()) {
                usernameText.setError(getString(R.string.login_username_required));
                return;
            }

            if (password.isEmpty()) {
                passwordText.setError(getString(R.string.login_password_required));
                return;
            }

            DialogFragment dialog = new LoginDialog();
            Bundle args = new Bundle();

            args.putString("username", username);
            args.putString("password", password);

            dialog.setArguments(args);
            dialog.show(getActivity().getSupportFragmentManager(), LoginDialog.TAG);
            dismiss();
        }
    };
}
