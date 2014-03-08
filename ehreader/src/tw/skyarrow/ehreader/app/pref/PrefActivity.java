package tw.skyarrow.ehreader.app.pref;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;

import tw.skyarrow.ehreader.BaseApplication;
import tw.skyarrow.ehreader.R;

/**
 * Created by SkyArrow on 2014/1/29.
 */
public class PrefActivity extends FragmentActivity {
    private static final String TAG = "PrefActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pref);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment fragment = new PrefFragment();

        ft.replace(R.id.container, fragment);
        ft.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();

        MapBuilder builder = MapBuilder.createAppView();
        builder.set(Fields.SCREEN_NAME, TAG);

        BaseApplication.getTracker().send(builder.build());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_OK);
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
