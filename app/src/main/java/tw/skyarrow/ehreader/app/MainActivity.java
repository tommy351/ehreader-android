package tw.skyarrow.ehreader.app;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.crashlytics.android.Crashlytics;
import com.facebook.drawee.backends.pipeline.Fresco;

import butterknife.ButterKnife;
import butterknife.InjectView;
import com.mopub.common.MoPub;
import io.fabric.sdk.android.Fabric;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.app.entry.WebFragment;
import tw.skyarrow.ehreader.util.FabricHelper;

/**
 * Created by SkyArrow on 2015/9/24.
 */
public class MainActivity extends AppCompatActivity {
    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @InjectView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FabricHelper.setupFabric(this);
        Fresco.initialize(this);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.menu_open_drawer, R.string.menu_close_drawer);

        drawerLayout.setDrawerListener(drawerToggle);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment fragment = fm.findFragmentById(R.id.frame);

        if (fragment != null){
            ft.attach(fragment);
        } else {
            ft.replace(R.id.frame, WebFragment.create());
        }

        ft.commit();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
