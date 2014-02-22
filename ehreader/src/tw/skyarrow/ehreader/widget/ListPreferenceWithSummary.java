package tw.skyarrow.ehreader.widget;

import android.content.Context;
import android.preference.ListPreference;
import android.preference.Preference;
import android.util.AttributeSet;

/**
 * Created by SkyArrow on 2014/2/3.
 */
// http://stackoverflow.com/a/8004498
public class ListPreferenceWithSummary extends ListPreference {
    public ListPreferenceWithSummary(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ListPreferenceWithSummary(Context context) {
        super(context);
        init();
    }

    private void init() {
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

    public void updateSummary() {
        setSummary(getEntry());
    }
}
