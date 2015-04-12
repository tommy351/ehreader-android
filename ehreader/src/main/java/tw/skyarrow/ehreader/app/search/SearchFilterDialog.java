package tw.skyarrow.ehreader.app.search;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import de.greenrobot.event.EventBus;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.event.SearchFilterEvent;

public class SearchFilterDialog extends DialogFragment {
    public static final String TAG = SearchFilterDialog.class.getSimpleName();

    public static final String EXTRA_CHOSEN_CATEGORIES = "chosen_categories";

    private boolean[] mChosenCategories;

    public static SearchFilterDialog newInstance(boolean[] chosenCategories){
        SearchFilterDialog dialog = new SearchFilterDialog();
        Bundle args = new Bundle();

        args.putBooleanArray(EXTRA_CHOSEN_CATEGORIES, chosenCategories);
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        mChosenCategories = args.getBooleanArray(EXTRA_CHOSEN_CATEGORIES);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        int[] categoryResources = {
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

        builder.setMultiChoiceItems(buildResourceArray(categoryResources), mChosenCategories, onChoose)
                .setPositiveButton(R.string.ok, onSubmitClick)
                .setNegativeButton(R.string.cancel, null);

        return builder.create();
    }

    private String[] buildResourceArray(int[] arr){
        String[] result = new String[arr.length];

        for (int i = 0; i < arr.length; i++) {
            result[i] = getString(arr[i]);
        }

        return result;
    }

    private DialogInterface.OnMultiChoiceClickListener onChoose = new DialogInterface.OnMultiChoiceClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i, boolean b) {
            mChosenCategories[i] = b;
        }
    };

    private DialogInterface.OnClickListener onSubmitClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            EventBus.getDefault().post(new SearchFilterEvent(mChosenCategories));
        }
    };
}
