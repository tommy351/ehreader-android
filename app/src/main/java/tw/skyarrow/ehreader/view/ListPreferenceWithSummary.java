package tw.skyarrow.ehreader.view;

import android.content.Context;
import android.support.v7.preference.ListPreference;
import android.util.AttributeSet;

/**
 * Created by SkyArrow on 2015/9/27.
 */
public class ListPreferenceWithSummary extends ListPreference {
    public ListPreferenceWithSummary(Context context) {
        this(context, null);
    }

    public ListPreferenceWithSummary(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOnPreferenceChangeListener((pref, o) -> {
            updateSummary();
            return true;
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
