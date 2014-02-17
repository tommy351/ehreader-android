package tw.skyarrow.ehreader.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

    private static final int CLICK_THRESHOLD = 5;
    private static final long RESET_DELAY = 500;

    private int clickCount = 0;

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

        Preference versionPref = findPreferenceByResource(R.string.pref_version);
        versionPref.setOnPreferenceClickListener(onVersionClick);

        try {
            PackageManager pm = getActivity().getPackageManager();
            String appVer = pm.getPackageInfo(getActivity().getPackageName(), 0).versionName;
            versionPref.setSummary(appVer);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        Preference authorPref = findPreferenceByResource(R.string.pref_author);
        authorPref.setOnPreferenceClickListener(onAuthorClick);

        Preference sourceCodePref = findPreferenceByResource(R.string.pref_source_code);
        sourceCodePref.setOnPreferenceClickListener(onSourceCodeClick);

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

    private Preference.OnPreferenceClickListener onVersionClick = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            versionClickHandler.removeMessages(0);

            if (clickCount < CLICK_THRESHOLD) {
                clickCount++;
                versionClickHandler.sendEmptyMessageDelayed(0, RESET_DELAY);
            } else {
                Intent intent = new Intent(getActivity(), AboutActivity.class);
                clickCount = 0;

                versionClickHandler.removeMessages(0);
                startActivity(intent);
            }

            return true;
        }
    };

    private Handler versionClickHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            versionClickHandler.removeMessages(0);
            clickCount = 0;
        }
    };

    private Preference.OnPreferenceClickListener onAuthorClick = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            Intent intent = new Intent(Intent.ACTION_VIEW);

            intent.setData(Uri.parse(Constant.AUTHOR_PAGE));
            startActivity(intent);

            return true;
        }
    };

    private Preference.OnPreferenceClickListener onSourceCodeClick = new Preference.OnPreferenceClickListener() {
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
