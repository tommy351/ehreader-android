package tw.skyarrow.ehreader.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.androidquery.util.AQUtility;

import tw.skyarrow.ehreader.R;

/**
 * Created by SkyArrow on 2014/2/3.
 */
public class ClearCacheDialog extends DialogFragment {
    public static final String TAG = "ClearCacheDialog";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.clear_cache_title)
                .setMessage(R.string.clear_cache_msg)
                .setPositiveButton(R.string.ok, onSubmitClick)
                .setNegativeButton(R.string.cancel, null);

        return builder.create();
    }

    private DialogInterface.OnClickListener onSubmitClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            AQUtility.cleanCacheAsync(getActivity());
        }
    };
}
