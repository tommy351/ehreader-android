package tw.skyarrow.ehreader.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import tw.skyarrow.ehreader.R;

/**
 * Created by SkyArrow on 2014/2/1.
 */
public class DownloadContextMenu extends DialogFragment {
    public static final String TAG = "DownloadContextMenu";

    private long galleryId;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Bundle args = getArguments();
        galleryId = args.getLong("id");

        builder.setTitle(args.getString("title"))
                .setItems(R.array.download_context_menu, onItemClick);

        return builder.create();
    }

    private DialogInterface.OnClickListener onItemClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            switch (i) {
                case 0:
                    openGallery();
                    break;

                case 1:
                    downloadAgain();
                    break;

                case 2:
                    deleteGallery();
                    break;
            }
        }
    };

    private void openGallery() {
        Intent intent = new Intent(getActivity(), GalleryActivity.class);
        Bundle args = new Bundle();

        args.putLong("id", galleryId);
        intent.putExtras(args);

        startActivity(intent);
    }

    private void downloadAgain() {
        Bundle args = new Bundle();
        DialogFragment dialog = new DownloadAgainDialog();

        args.putLong("id", galleryId);

        dialog.setArguments(args);
        dialog.show(getActivity().getSupportFragmentManager(), DownloadAgainDialog.TAG);
    }

    private void deleteGallery() {
        DialogFragment dialog = new DownloadDeleteConfirmDialog();
        Bundle args = new Bundle();

        args.putLong("id", galleryId);

        dialog.setArguments(args);
        dialog.show(getActivity().getSupportFragmentManager(), DownloadDeleteConfirmDialog.TAG);
    }
}
