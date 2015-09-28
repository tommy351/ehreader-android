package tw.skyarrow.ehreader.app.pref;

import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import tw.skyarrow.ehreader.R;

/**
 * Created by SkyArrow on 2015/9/27.
 */
public class PrefFragment extends PreferenceFragmentCompat {
    public static PrefFragment create(){
        return new PrefFragment();
    }
    
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.pref);
    }

    private Preference findPreferenceByResource(int id){
        return findPreference(getString(id));
    }
}
