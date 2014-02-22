package tw.skyarrow.ehreader.app.pref;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.github.zafarkhaja.semver.Version;

import de.greenrobot.event.EventBus;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.event.UpdateCheckEvent;
import tw.skyarrow.ehreader.service.UpdateCheckService;

/**
 * Created by SkyArrow on 2014/2/17.
 */
public class CheckUpdateDialog extends DialogFragment {
    public static final String TAG = "CheckUpdateDialog";

    private EventBus bus;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog dialog = new ProgressDialog(getActivity());
        bus = EventBus.getDefault();
        bus.register(this);

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setTitle(R.string.checking_update);
        dialog.setMessage(getString(R.string.checking_update));
        dialog.setIndeterminate(true);

        Intent intent = new Intent(getActivity(), UpdateCheckService.class);

        intent.setAction(UpdateCheckService.ACTION_CHECK_UPDATE);
        getActivity().startService(intent);

        return dialog;
    }

    public void onEventMainThread(UpdateCheckEvent event) {
        DialogFragment dialog = null;
        String tag = "";
        Bundle args = new Bundle();
        Version version = event.getVersion();

        switch (event.getCode()) {
            case UpdateCheckService.EVENT_AVAILABLE:
                dialog = new CheckUpdateAvailableDialog();
                tag = CheckUpdateAvailableDialog.TAG;

                args.putString(CheckUpdateAvailableDialog.EXTRA_VERSION, version.toString());
                break;

            case UpdateCheckService.EVENT_LATEST:
                dialog = new CheckUpdateLatestDialog();
                tag = CheckUpdateLatestDialog.TAG;

                args.putString(CheckUpdateLatestDialog.EXTRA_VERSION, version.toString());
                break;

            case UpdateCheckService.EVENT_ERROR:
                dialog = new CheckUpdateErrorDialog();
                tag = CheckUpdateErrorDialog.TAG;

                break;
        }

        if (dialog != null) {
            dialog.setArguments(args);
            dialog.show(getActivity().getSupportFragmentManager(), tag);
            dismiss();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bus.unregister(this);
    }
}
