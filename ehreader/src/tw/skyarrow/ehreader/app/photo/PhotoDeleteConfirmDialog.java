package tw.skyarrow.ehreader.app.photo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;

import tw.skyarrow.ehreader.BaseApplication;
import tw.skyarrow.ehreader.R;

/**
 * Created by SkyArrow on 2014/2/28.
 */
public class PhotoDeleteConfirmDialog extends DialogFragment {
    public static final String TAG = "PhotoSaveDialog";

    public static final String EXTRA_PHOTO = "photo";

    private long id;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        Bundle args = getArguments();
        id = args.getLong(EXTRA_PHOTO);

        dialog.setTitle(getString(R.string.photo_delete_title))
                .setMessage(getString(R.string.photo_delete_msg))
                .setPositiveButton(R.string.delete, onSubmitClick)
                .setNegativeButton(R.string.cancel, null);

        return dialog.create();
    }

    private DialogInterface.OnClickListener onSubmitClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            DialogFragment dialog = new PhotoDeleteDialog();
            Bundle args = new Bundle();

            args.putLong(PhotoDeleteDialog.EXTRA_PHOTO, id);
            dialog.setArguments(args);
            dialog.show(getActivity().getSupportFragmentManager(), PhotoDeleteDialog.TAG);
        }
    };
}
