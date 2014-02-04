package tw.skyarrow.ehreader.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

import tw.skyarrow.ehreader.Constant;
import tw.skyarrow.ehreader.R;

/**
 * Created by SkyArrow on 2014/2/3.
 */
public class PrefFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref);

        /*
        Preference loginPref = findPreferenceByResource(R.string.pref_login);
        loginPref.setOnPreferenceClickListener(
                new OpenDialogPreference(new LoginDialog(), LoginDialog.TAG));

        Preference logoutPref = findPreferenceByResource(R.string.pref_logout);
        logoutPref.setOnPreferenceClickListener(
                new OpenDialogPreference(new LogoutDialog(), LogoutDialog.TAG));*/

        Preference clearCachePref = findPreferenceByResource(R.string.pref_clear_cache);
        clearCachePref.setOnPreferenceClickListener(
                new OpenDialogPreference(new ClearCacheDialog(), ClearCacheDialog.TAG));

        Preference clearHistoryPref = findPreferenceByResource(R.string.pref_clear_history);
        clearHistoryPref.setOnPreferenceClickListener(
                new OpenDialogPreference(new ClearHistoryDialog(), ClearHistoryDialog.TAG));

        Preference clearSearchPref = findPreferenceByResource(R.string.pref_clear_search);
        clearSearchPref.setOnPreferenceClickListener(
                new OpenDialogPreference(new ClearSearchDialog(), ClearSearchDialog.TAG));

        Preference aboutPref = findPreferenceByResource(R.string.pref_about);
        aboutPref.setOnPreferenceClickListener(onAboutClick);
        aboutPref.setTitle(String.format(getString(R.string.pref_about_ver),
                getString(R.string.version)));

        Preference licensePref = findPreferenceByResource(R.string.pref_open_source_license);
        licensePref.setOnPreferenceClickListener(
                new OpenDialogPreference(new LicenseDialog(), LicenseDialog.TAG));
    }

    private Preference.OnPreferenceClickListener onAboutClick = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            Intent intent = new Intent(Intent.ACTION_VIEW);

            intent.setData(Uri.parse(Constant.HOMEPAGE));
            startActivity(intent);

            return true;
        }
    };

    private class OpenDialogPreference implements Preference.OnPreferenceClickListener {
        private DialogFragment dialog;
        private String tag;

        public OpenDialogPreference(DialogFragment dialog, String tag) {
            this.dialog = dialog;
            this.tag = tag;
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            dialog.show(((FragmentActivity) getActivity()).getSupportFragmentManager(), tag);
            return true;
        }
    };

    private Preference findPreferenceByResource(int id) {
        return findPreference(getString(id));
    }
}
