package tw.skyarrow.ehreader.app.pref;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import de.greenrobot.event.EventBus;
import tw.skyarrow.ehreader.Constant;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.event.LoginEvent;
import tw.skyarrow.ehreader.event.UpdateCheckEvent;
import tw.skyarrow.ehreader.service.UpdateCheckService;
import tw.skyarrow.ehreader.util.DownloadHelper;
import tw.skyarrow.ehreader.util.UpdateHelper;
import tw.skyarrow.ehreader.widget.ListPreferenceWithSummary;

/**
 * Created by SkyArrow on 2014/2/3.
 */
public class PrefFragment extends PreferenceFragment {
    public static final String TAG = "PrefFragment";

    private SharedPreferences preferences;
    private PreferenceCategory accountCategory;
    private Preference loginPref;
    private Preference logoutPref;
    private Preference checkUpdatePref;

    private static final int CLICK_THRESHOLD = 5;
    private static final long RESET_DELAY = 500;

    private int clickCount = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref);
        EventBus.getDefault().register(this);

        preferences = getPreferenceManager().getSharedPreferences();

        accountCategory = (PreferenceCategory) findPreferenceByResource(R.string.pref_account);

        loginPref = findPreferenceByResource(R.string.pref_login);
        loginPref.setOnPreferenceClickListener(
                new OpenDialogPreference(new LoginPromptDialog(), LoginPromptDialog.TAG));

        logoutPref = findPreferenceByResource(R.string.pref_logout);
        logoutPref.setOnPreferenceClickListener(
                new OpenDialogPreference(new LogoutDialog(), LogoutDialog.TAG));

        boolean loggedIn = preferences.getBoolean(getString(R.string.pref_logged_in), false);

        if (loggedIn) {
            hideLoginPref();
        } else {
            hideLogoutPref();
        }

        Preference hideFilesPref = findPreferenceByResource(R.string.pref_hide_files);
        hideFilesPref.setOnPreferenceChangeListener(onHideFileChange);

        Preference clearCachePref = findPreferenceByResource(R.string.pref_clear_cache);
        clearCachePref.setOnPreferenceClickListener(
                new OpenDialogPreference(new ClearCacheDialog(), ClearCacheDialog.TAG));

        Preference clearHistoryPref = findPreferenceByResource(R.string.pref_clear_history);
        clearHistoryPref.setOnPreferenceClickListener(
                new OpenDialogPreference(new ClearHistoryDialog(), ClearHistoryDialog.TAG));

        Preference clearSearchPref = findPreferenceByResource(R.string.pref_clear_search);
        clearSearchPref.setOnPreferenceClickListener(
                new OpenDialogPreference(new ClearSearchDialog(), ClearSearchDialog.TAG));

        checkUpdatePref = findPreferenceByResource(R.string.pref_check_update);
        checkUpdatePref.setOnPreferenceClickListener(
                new OpenDialogPreference(new CheckUpdateDialog(), CheckUpdateDialog.TAG));
        updateLastCheckedAt();

        final ListPreferenceWithSummary autoUpdatePref =
                (ListPreferenceWithSummary) findPreferenceByResource(R.string.pref_auto_check_update);
        final UpdateHelper updateHelper = new UpdateHelper(getActivity());

        autoUpdatePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                autoUpdatePref.updateSummary();
                updateHelper.cancelAlarm();

                if (Integer.parseInt((String) value) > 0) {
                    updateHelper.setupAlarm();
                }

                return true;
            }
        });

        Preference versionPref = findPreferenceByResource(R.string.pref_version);
        versionPref.setOnPreferenceClickListener(onVersionClick);
        versionPref.setSummary(updateHelper.getVersionCode());

        Preference authorPref = findPreferenceByResource(R.string.pref_author);
        authorPref.setOnPreferenceClickListener(onAuthorClick);

        Preference sourceCodePref = findPreferenceByResource(R.string.pref_source_code);
        sourceCodePref.setOnPreferenceClickListener(onSourceCodeClick);
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

    public void onEventMainThread(UpdateCheckEvent event) {
        switch (event.getCode()) {
            case UpdateCheckService.EVENT_AVAILABLE:
            case UpdateCheckService.EVENT_LATEST:
                updateLastCheckedAt();
                break;
        }
    }

    private void updateLastCheckedAt() {
        long lastCheckedAt = preferences.getLong(getString(R.string.pref_update_checked_at), 0);

        if (lastCheckedAt > 0) {
            DateFormat dateFormat = DateFormat.getDateInstance();
            String dateString = dateFormat.format(new Date(lastCheckedAt));
            checkUpdatePref.setSummary(String.format(getString(R.string.pref_check_update_summary), dateString));
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

    private Preference.OnPreferenceChangeListener onHideFileChange = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            try {
                DownloadHelper.setFolderVisibility(!(Boolean) value);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return true;
        }
    };

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
