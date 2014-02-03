package tw.skyarrow.ehreader.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import tw.skyarrow.ehreader.R;

/**
 * Created by SkyArrow on 2014/2/2.
 */
public class FilterDialog extends DialogFragment {
    public static final String TAG = "FilterDialog";

    private boolean[] filter;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        int[] categories = {
                R.string.category_doujinshi,
                R.string.category_manga,
                R.string.category_artistcg,
                R.string.category_gamecg,
                R.string.category_western,
                R.string.category_non_h,
                R.string.category_imageset,
                R.string.category_cosplay,
                R.string.category_asianporn,
                R.string.category_misc
        };

        filter = new boolean[categories.length];

        for (int i = 0; i < categories.length; i++) {
            filter[i] = false;
        }

        builder.setMultiChoiceItems(buildResourceArray(categories), null, onChoose)
                .setPositiveButton(R.string.ok, onSubmitClick)
                .setNegativeButton(R.string.cancel, null);

        return builder.create();
    }

    private String[] buildResourceArray(int[] arr) {
        String[] result = new String[arr.length];

        for (int i = 0; i < arr.length; i++) {
            result[i] = getString(arr[i]);
        }

        return result;
    };

    private DialogInterface.OnMultiChoiceClickListener onChoose = new DialogInterface.OnMultiChoiceClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i, boolean b) {
            filter[i] = b;
        }
    };

    private DialogInterface.OnClickListener onSubmitClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            Intent intent = new Intent(getActivity(), FilterActivity.class);
            Bundle args = new Bundle();

            args.putBooleanArray("filter", filter);
            intent.putExtras(args);

            startActivity(intent);
        }
    };
}
