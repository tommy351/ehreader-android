package tw.skyarrow.ehreader.app.photo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;

import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.api.ApiErrorCode;

/**
 * Created by SkyArrow on 2014/3/3.
 */
public class PhotoErrorDialog extends DialogFragment {
    public static final String TAG = "PhotoErrorDialog";

    public static final String EXTRA_ERROR_CODE = "error";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        Bundle args = getArguments();
        int error = args.getInt(EXTRA_ERROR_CODE);

        dialog.setTitle(getString(R.string.photo_error_title));

        switch (error) {
            case ApiErrorCode.GALLERY_PINNED:
                dialog.setMessage(R.string.photo_error_pinned)
                        .setPositiveButton(R.string.ok, null);
                break;

            case ApiErrorCode.IO_ERROR:
                dialog.setMessage(R.string.photo_error_network)
                        .setPositiveButton(R.string.network_config, onSubmitClick)
                        .setNegativeButton(R.string.cancel, null);

                break;

            case ApiErrorCode.TOKEN_OR_PAGE_INVALID:
                dialog.setMessage(R.string.photo_error_not_found)
                        .setPositiveButton(R.string.ok, null);

                break;

            default:
                dialog.setMessage(R.string.photo_error_unknown);
        }

        return dialog.create();
    }

    private DialogInterface.OnClickListener onSubmitClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
        }
    };
}
