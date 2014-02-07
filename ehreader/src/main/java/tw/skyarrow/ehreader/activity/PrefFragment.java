package tw.skyarrow.ehreader.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

import de.greenrobot.event.EventBus;
import tw.skyarrow.ehreader.Constant;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.event.LoginEvent;

/**
 * Created by SkyArrow on 2014/2/3.
 */
public class PrefFragment extends PreferenceFragment {
    private PreferenceCategory accountCategory;
    private Preference loginPref;
    private Preference logoutPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref);
        EventBus.getDefault().register(this);

        accountCategory = (PreferenceCategory) findPreferenceByResource(R.string.pref_account);

        loginPref = findPreferenceByResource(R.string.pref_login);
        loginPref.setOnPreferenceClickListener(
                new OpenDialogPreference(new LoginPromptDialog(), LoginPromptDialog.TAG));

        logoutPref = findPreferenceByResource(R.string.pref_logout);
        logoutPref.setOnPreferenceClickListener(
                new OpenDialogPreference(new LogoutDialog(), LogoutDialog.TAG));

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

        SharedPreferences preferences = getPreferenceManager().getSharedPreferences();
        boolean loggedIn = preferences.getBoolean(getString(R.string.pref_logged_in), false);

        if (loggedIn) {
            hideLoginPref();
        } else {
            hideLogoutPref();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onEvent(LoginEvent event) {
        switch (event.getCode()) {
            case LoginEvent.LOGIN:
                hideLoginPref();
                break;

            case LoginEvent.LOGOUT:
                hideLogoutPref();
                break;
        }
    }

    private void hideLoginPref() {
        accountCategory.removePreference(loginPref);
        accountCategory.addPreference(logoutPref);
    }

    private void hideLogoutPref() {
        accountCategory.removePreference(logoutPref);
        accountCategory.addPreference(loginPref);
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
