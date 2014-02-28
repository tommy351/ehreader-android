package tw.skyarrow.ehreader.app.download;

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

    public static final String EXTRA_GALLERY = "id";

    private long galleryId;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        Bundle args = getArguments();
        galleryId = args.getLong(EXTRA_GALLERY);

        dialog.setTitle(R.string.delete_gallery_title)
                .setMessage(R.string.delete_gallery_msg)
                .setPositiveButton(R.string.delete, onSubmitClick)
                .setNegativeButton(R.string.cancel, null);

        return dialog.create();
    }

    private DialogInterface.OnClickListener onSubmitClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            DialogFragment dialog = new DownloadDeleteDialog();
            Bundle args = new Bundle();

            args.putLong(DownloadDeleteDialog.EXTRA_GALLERY, galleryId);

            dialog.setArguments(args);
            dialog.show(getActivity().getSupportFragmentManager(), DownloadDeleteDialog.TAG);
        }
    };
}
