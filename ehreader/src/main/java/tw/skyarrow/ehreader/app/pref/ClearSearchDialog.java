package tw.skyarrow.ehreader.app.pref;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.DialogFragment;

import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.provider.SearchSuggestionProvider;

public class ClearSearchDialog extends DialogFragment {
    public static final String TAG = ClearSearchDialog.class.getSimpleName();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.clear_search_title)
                .setMessage(R.string.clear_search_msg)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        clearSearch();
                    }
                })
                .setNegativeButton(R.string.cancel, null);

        return builder.create();
    }

    private void clearSearch(){
        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getActivity(),
                SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);

        suggestions.clearHistory();
    }
}
