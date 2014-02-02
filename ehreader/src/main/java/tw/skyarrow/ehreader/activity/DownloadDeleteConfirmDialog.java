package tw.skyarrow.ehreader.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import tw.skyarrow.ehreader.R;

/**
 * Created by SkyArrow on 2014/2/2.
 */
public class DownloadDeleteConfirmDialog extends DialogFragment {
    public static final String TAG = "DownloadDeleteConfirmDialog";

    private long galleryId;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Bundle args = getArguments();
        galleryId = args.getLong("id");

        builder.setTitle(R.string.delete_gallery_title)
                .setMessage(R.string.delete_gallery_msg)
                .setPositiveButton(R.string.ok, onSubmitClick)
                .setNegativeButton(R.string.cancel, onCancelClick);

        return builder.create();
    }

    private DialogInterface.OnClickListener onSubmitClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            DialogFragment dialog = new DownloadDeleteDialog();
            Bundle args = new Bundle();

            args.putLong("id", galleryId);

            dialog.setArguments(args);
            dialog.show(getActivity().getSupportFragmentManager(), DownloadDeleteDialog.TAG);
        }
    };

    private DialogInterface.OnClickListener onCancelClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            dismiss();
        }
    };
}
