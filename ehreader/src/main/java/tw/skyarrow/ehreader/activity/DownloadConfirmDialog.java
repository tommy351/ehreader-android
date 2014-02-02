package tw.skyarrow.ehreader.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.service.GalleryDownloadService;
import tw.skyarrow.ehreader.util.FileInfoHelper;

/**
 * Created by SkyArrow on 2014/2/1.
 */
public class DownloadConfirmDialog extends DialogFragment {
    public static final String TAG = "DownloadConfirmDialog";

    private long galleryId;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Bundle args = getArguments();
        galleryId = args.getLong("id");
        long gallerySize = args.getLong("size");
        String message = String.format(getResources().getString(R.string.download_confirm), FileInfoHelper.toBytes(gallerySize));

        builder.setMessage(message)
                .setPositiveButton(R.string.ok, onSubmitClick)
                .setNegativeButton(R.string.cancel, onCancelClick);

        return builder.create();
    }

    private DialogInterface.OnClickListener onSubmitClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            Intent intent = new Intent(getActivity(), GalleryDownloadService.class);

            intent.setAction(GalleryDownloadService.ACTION_START);
            intent.putExtra(GalleryDownloadService.GALLERY_ID, galleryId);
            getActivity().startService(intent);
        }
    };

    private DialogInterface.OnClickListener onCancelClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            dismiss();
        }
    };
}
