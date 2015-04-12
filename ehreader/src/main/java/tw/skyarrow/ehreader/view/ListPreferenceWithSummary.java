package tw.skyarrow.ehreader.view;

import android.content.Context;
import android.preference.ListPreference;
import android.preference.Preference;
import android.util.AttributeSet;

public class ListPreferenceWithSummary extends ListPreference {
    public ListPreferenceWithSummary(Context context) {
        this(context, null);
    }

    public ListPreferenceWithSummary(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                updateSummary();
                return true;
            }
        });
    }

    @Override
    public CharSequence getSummary() {
        return super.getEntry();
    }

    public void updateSummary(){
        setSummary(getEntry());
    }
}
