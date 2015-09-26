package tw.skyarrow.ehreader.app.entry;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.MenuItem;

import tw.skyarrow.ehreader.R;

/**
 * Created by SkyArrow on 2015/9/26.
 */
public class SettingFragment extends PreferenceFragmentCompat {
    public static SettingFragment create(){
        return new SettingFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.pref);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        EntryActivity activity = (EntryActivity) getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setTitle(R.string.label_settings);
        activity.setDrawerIndicatorEnabled(false);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        EntryActivity activity = (EntryActivity) getActivity();
        activity.setDrawerIndicatorEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                FragmentManager fm = getActivity().getSupportFragmentManager();
                fm.popBackStack();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Preference findPreferenceByResource(int id){
        return findPreference(getString(id));
    }
}
