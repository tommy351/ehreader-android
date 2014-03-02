package tw.skyarrow.ehreader.app.gallery;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.app.photo.PhotoActivity;

/**
 * Created by SkyArrow on 2014/3/2.
 */
public class GalleryPageDialog extends DialogFragment {
    @InjectView(R.id.picker)
    NumberPicker picker;

    @InjectView(R.id.picker_prefix)
    TextView pickerPrefix;

    @InjectView(R.id.picker_suffix)
    TextView pickerSuffix;

    public static final String TAG = "GalleryPageDialog";

    public static final String EXTRA_GALLERY = "id";
    public static final String EXTRA_DEFAULT_PAGE = "defaultPage";
    public static final String EXTRA_TOTAL_PAGE = "total";

    private long galleryId;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.choose_page, null);
        ButterKnife.inject(this, view);

        Bundle args = getArguments();
        galleryId = args.getLong(EXTRA_GALLERY);
        int defaultPage = args.getInt(EXTRA_DEFAULT_PAGE);
        int totalPage = args.getInt(EXTRA_TOTAL_PAGE);

        if (defaultPage <= 0) defaultPage = 1;

        picker.setMinValue(1);
        picker.setMaxValue(totalPage);
        picker.setValue(defaultPage);

        checkTextViewEmpty(pickerPrefix);
        checkTextViewEmpty(pickerSuffix);

        dialog.setTitle(getString(R.string.choose_page_title))
                .setView(view)
                .setPositiveButton(R.string.ok, onSubmitClick)
                .setNegativeButton(R.string.cancel, null);

        return dialog.create();
    }

    private void checkTextViewEmpty(TextView textView) {
        if (textView.getText().length() == 0) {
            textView.setVisibility(View.INVISIBLE);
        }
    }

    private DialogInterface.OnClickListener onSubmitClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            Intent intent = new Intent(getActivity(), PhotoActivity.class);
            Bundle args = new Bundle();

            args.putLong(PhotoActivity.EXTRA_GALLERY, galleryId);
            args.putInt(PhotoActivity.EXTRA_PAGE, picker.getValue() - 1);

            intent.putExtras(args);
            startActivity(intent);
        }
    };
}
