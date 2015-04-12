package tw.skyarrow.ehreader.app.pref;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;

import tw.skyarrow.ehreader.Constant;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.util.L;
import tw.skyarrow.ehreader.util.UpdateHelper;

public class PrefFragment extends PreferenceFragment {
    private SharedPreferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref);

        Activity activity = getActivity();
        UpdateHelper updateHelper = new UpdateHelper(activity);

        preferences = getPreferenceManager().getSharedPreferences();

        // Hide files
        Preference hideFilesPref = findPreferenceByResource(R.string.pref_hide_files);
        hideFilesPref.setOnPreferenceClickListener(onHideFileClick);

        // Clear cache
        Preference clearCachePref = findPreferenceByResource(R.string.pref_clear_cache);
        clearCachePref.setOnPreferenceClickListener(new OpenDialogPreference(ClearCacheDialog.class));

        // Clear history
        Preference clearHistoryPref = findPreferenceByResource(R.string.pref_clear_history);
        clearHistoryPref.setOnPreferenceClickListener(new OpenDialogPreference(ClearHistoryDialog.class));

        // Clear search
        Preference clearSearchPref = findPreferenceByResource(R.string.pref_clear_search);
        clearSearchPref.setOnPreferenceClickListener(new OpenDialogPreference(ClearSearchDialog.class));

        // Version
        Preference versionPref = findPreferenceByResource(R.string.pref_version);
        versionPref.setSummary(updateHelper.getVersionCode());

        // Author
        Preference authorPref = findPreferenceByResource(R.string.pref_author);
        authorPref.setOnPreferenceClickListener(new OpenLinkPreference(Constant.AUTHOR_PAGE));

        // Source code
        Preference sourceCodePref = findPreferenceByResource(R.string.pref_source_code);
        sourceCodePref.setOnPreferenceClickListener(new OpenLinkPreference(Constant.HOMEPAGE));
    }

    private Preference.OnPreferenceClickListener onHideFileClick = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            return true;
        }
    };

    private Preference findPreferenceByResource(int id){
        return findPreference(getString(id));
    }

    private class OpenDialogPreference implements Preference.OnPreferenceClickListener {
        private Class mDialogClass;
        private ActionBarActivity mActivity;

        public OpenDialogPreference( Class dialogClass) {
            mDialogClass = dialogClass;
            mActivity = (ActionBarActivity) getActivity();
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            try {
                DialogFragment dialog = (DialogFragment) mDialogClass.newInstance();
                FragmentManager fm = mActivity.getSupportFragmentManager();
                dialog.show(fm, mDialogClass.getSimpleName());
            } catch (Exception e){
                L.e(e);
            }

            return true;
        }
    }

    private class OpenLinkPreference implements Preference.OnPreferenceClickListener {
        private Uri mUri;

        public OpenLinkPreference(String link){
            mUri = Uri.parse(link);
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            Intent intent = new Intent(Intent.ACTION_VIEW);

            intent.setData(mUri);
            getActivity().startActivity(intent);

            return true;
        }
    }
}
