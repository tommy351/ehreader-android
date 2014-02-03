package tw.skyarrow.ehreader.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.DialogFragment;

import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.provider.SearchSuggestionProvider;

/**
 * Created by SkyArrow on 2014/2/3.
 */
public class ClearSearchDialog extends DialogFragment {
    public static final String TAG = "ClearSearchDialog";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.clear_search_title)
                .setMessage(R.string.clear_search_msg)
                .setPositiveButton(R.string.ok, onSubmitClick)
                .setNegativeButton(R.string.cancel, null);

        return builder.create();
    }

    private DialogInterface.OnClickListener onSubmitClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getActivity(),
                    SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);

            suggestions.clearHistory();
        }
    };
}
