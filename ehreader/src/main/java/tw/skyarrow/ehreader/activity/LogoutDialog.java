package tw.skyarrow.ehreader.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;

import de.greenrobot.event.EventBus;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.event.LoginEvent;

/**
 * Created by SkyArrow on 2014/2/4.
 */
public class LogoutDialog extends DialogFragment {
    public static final String TAG = "LogoutDialog";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.logout_title)
                .setMessage(R.string.logout_msg)
                .setPositiveButton(R.string.ok, onSubmitClick)
                .setNegativeButton(R.string.cancel, null);

        return builder.create();
    }

    private DialogInterface.OnClickListener onSubmitClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor editor = preferences.edit();

            editor.putBoolean(getString(R.string.pref_logged_in), false);
            editor.remove(getString(R.string.pref_login_memberid));
            editor.remove(getString(R.string.pref_login_passhash));
            editor.commit();

            EventBus.getDefault().post(new LoginEvent(LoginEvent.LOGOUT));
        }
    };
}
