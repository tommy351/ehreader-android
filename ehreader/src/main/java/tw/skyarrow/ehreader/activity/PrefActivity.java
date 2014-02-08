package tw.skyarrow.ehreader.activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;

import tw.skyarrow.ehreader.BaseApplication;
import tw.skyarrow.ehreader.R;

/**
 * Created by SkyArrow on 2014/1/29.
 */
public class PrefActivity extends ActionBarActivity {
    private static final String TAG = "PrefActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pref);

        ActionBar actionBar = getSupportActionBar();
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
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
