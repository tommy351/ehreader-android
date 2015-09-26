package tw.skyarrow.ehreader.app.entry;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.facebook.drawee.backends.pipeline.Fresco;

import butterknife.ButterKnife;
import butterknife.InjectView;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.model.Download;
import tw.skyarrow.ehreader.util.FabricHelper;

/**
 * Created by SkyArrow on 2015/9/24.
 */
public class EntryActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static final String EXTRA_TAB = "TAB";

    public static final int TAB_LATEST = 0;
    public static final int TAB_FAVORITES = 1;
    public static final int TAB_HISTORY = 2;
    public static final int TAB_DOWNLOAD = 3;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @InjectView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @InjectView(R.id.nav_view)
    NavigationView navigationView;

    ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FabricHelper.setupFabric(this);
        Fresco.initialize(this);
        setContentView(R.layout.activity_entry);
        ButterKnife.inject(this);

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.menu_open_drawer, R.string.menu_close_drawer);

        drawerLayout.setDrawerListener(drawerToggle);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        navigationView.setNavigationItemSelectedListener(this);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment fragment = fm.findFragmentById(R.id.frame);

        if (fragment != null) {
            ft.attach(fragment);
        } else {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String launchPagePref = prefs.getString(getString(R.string.pref_launch_page), getString(R.string.pref_launch_page_default));
            int tab = Integer.parseInt(launchPagePref, 10);

            ft.replace(R.id.frame, getTabFragment(tab));
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.entry, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()){
            case R.id.action_search:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        boolean setBackStack = false;
        Fragment fragment;

        switch (menuItem.getItemId()){
            case R.id.drawer_item_latest:
                fragment = getTabFragment(TAB_LATEST);
                break;

            case R.id.drawer_item_favorites:
                fragment = getTabFragment(TAB_FAVORITES);
                break;

            case R.id.drawer_item_history:
                fragment = getTabFragment(TAB_HISTORY);
                break;

            case R.id.drawer_item_download:
                fragment = getTabFragment(TAB_DOWNLOAD);
                break;

            case R.id.drawer_item_settings:
                fragment = SettingFragment.create();
                setBackStack = true;
                break;

            default:
                return false;
        }

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        ft.replace(R.id.frame, fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        if (setBackStack){
            ft.addToBackStack(null);
        }

        ft.commit();
        drawerLayout.closeDrawers();

        return true;
    }

    private Fragment getTabFragment(int tab){
        switch (tab){
            case TAB_LATEST:
                return WebFragment.create();

            case TAB_FAVORITES:
                return FavoritesFragment.create();

            case TAB_HISTORY:
                return HistoryFragment.create();

            case TAB_DOWNLOAD:
                return DownloadListFragment.create();
        }

        return null;
    }

    public void setDrawerIndicatorEnabled(boolean enabled){
        drawerToggle.setDrawerIndicatorEnabled(enabled);
    }
}
