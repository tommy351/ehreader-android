package tw.skyarrow.ehreader.app.download;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;

import tw.skyarrow.ehreader.BaseApplication;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.service.GalleryDownloadService;
import tw.skyarrow.ehreader.util.FileInfoHelper;

/**
 * Created by SkyArrow on 2014/2/1.
 */
public class DownloadConfirmDialog extends DialogFragment {
    public static final String TAG = "DownloadConfirmDialog";

    public static final String EXTRA_GALLERY = "id";
    public static final String EXTRA_SIZE = "size";

    private long galleryId;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        Bundle args = getArguments();
        galleryId = args.getLong(EXTRA_GALLERY);
        long gallerySize = args.getLong(EXTRA_SIZE);
        String message = String.format(getString(R.string.download_confirm), FileInfoHelper.toBytes(gallerySize));

        dialog.setMessage(message)
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
            Intent intent = new Intent(getActivity(), GalleryDownloadService.class);

            intent.setAction(GalleryDownloadService.ACTION_START);
            intent.putExtra(GalleryDownloadService.GALLERY_ID, galleryId);
            getActivity().startService(intent);
        }
    };
}
