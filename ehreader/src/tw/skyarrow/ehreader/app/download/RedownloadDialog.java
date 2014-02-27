package tw.skyarrow.ehreader.app.download;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.service.GalleryDownloadService;

/**
 * Created by SkyArrow on 2014/2/1.
 */
public class RedownloadDialog extends DialogFragment {
    public static final String TAG = "RedownloadDialog";

    public static final String EXTRA_GALLERY = "id";

    private long galleryId;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        Bundle args = getArguments();
        galleryId = args.getLong(EXTRA_GALLERY);

        dialog.setTitle(getString(R.string.download_confirm_title))
                .setMessage(R.string.redownload_msg)
                .setPositiveButton(R.string.download_redownload, onSubmitClick)
                .setNegativeButton(R.string.cancel, null);

        return dialog.create();
    }

    private DialogInterface.OnClickListener onSubmitClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            Intent intent = new Intent(getActivity(), GalleryDownloadService.class);

            intent.setAction(GalleryDownloadService.ACTION_RETRY);
            intent.putExtra(GalleryDownloadService.EXTRA_GALLERY, galleryId);
            getActivity().startService(intent);
        }
    };
}
