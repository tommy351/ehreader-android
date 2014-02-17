package tw.skyarrow.ehreader.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;

import de.greenrobot.event.EventBus;
import tw.skyarrow.ehreader.BaseApplication;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.event.LoginEvent;

/**
 * Created by SkyArrow on 2014/2/4.
 */
public class LogoutDialog extends DialogFragment {
    public static final String TAG = "LogoutDialog";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());

        dialog.setTitle(R.string.logout_title)
                .setMessage(R.string.logout_msg)
                .setPositiveButton(R.string.ok, onSubmitClick)
                .setNegativeButton(R.string.cancel, null);

        MapBuilder builder = MapBuilder.createAppView();
        builder.set(Fields.SCREEN_NAME, TAG);

        BaseApplication.getTracker().send(builder.build());

        return dialog.create();
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
