package tw.skyarrow.ehreader.app;

import android.app.ActionBar;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;

import tw.skyarrow.ehreader.R;

/**
 * Created by SkyArrow on 2014/2/26.
 */
public abstract class DrawerActivity extends FragmentActivity {
    private DrawerLayout drawerLayout;
    private View drawerView;
    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    protected void setupDrawer() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerView = findViewById(R.id.drawer);
        ActionBar actionBar = getActionBar();

        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.drawable.ic_navigation_drawer,
                R.string.menu_open_drawer,
                R.string.menu_close_drawer
        ) {
            @Override
            public void onDrawerStateChanged(int newState) {
                super.onDrawerStateChanged(newState);
                DrawerActivity.this.onDrawerStateChanged(newState);
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                DrawerActivity.this.onDrawerSlide(drawerView, slideOffset);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                DrawerActivity.this.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                DrawerActivity.this.onDrawerClosed(drawerView);
            }
        };

        drawerLayout.setDrawerListener(drawerToggle);
        drawerLayout.setDrawerShadow(R.drawable.navigation_drawer_shadow, Gravity.START);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public DrawerLayout getDrawerLayout() {
        return drawerLayout;
    }

    public ActionBarDrawerToggle getDrawerToggle() {
        return drawerToggle;
    }

    public View getDrawerView() {
        return drawerView;
    }

    public boolean isDrawerOpened() {
        return drawerLayout.isDrawerOpen(drawerView);
    }

    public void openDrawer() {
        drawerLayout.openDrawer(drawerView);
    }

    public void closeDrawer() {
        drawerLayout.closeDrawer(drawerView);
    }

    public void onDrawerOpened(View view) {
        //
    }

    public void onDrawerClosed(View view) {
        //
    }

    public void onDrawerSlide(View view, float offset) {
        //
    }

    public void onDrawerStateChanged(int newState) {
        //
    }

    public void setDrawerIndicatorEnabled(boolean enabled) {
        drawerToggle.setDrawerIndicatorEnabled(enabled);
    }
}
