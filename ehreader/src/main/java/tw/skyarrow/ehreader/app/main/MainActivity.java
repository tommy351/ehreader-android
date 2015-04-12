package tw.skyarrow.ehreader.app.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.InjectView;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.app.drawer.DrawerActivity;
import tw.skyarrow.ehreader.app.drawer.DrawerFragment;
import tw.skyarrow.ehreader.app.search.SearchActivity;
import tw.skyarrow.ehreader.util.PrefHelper;

public class MainActivity extends ActionBarActivity implements DrawerActivity {
    public static final String EXTRA_TAB = "tab";

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    @InjectView(R.id.container)
    DrawerLayout mDrawerLayout;

    @InjectView(R.id.drawer)
    View mDrawer;

    private ActionBarDrawerToggle mDrawerToggle;

    public static Intent newIntent(Context context, int tab){
        Intent intent = new Intent(context, MainActivity.class);

        intent.putExtra(EXTRA_TAB, tab);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close);
        mToolbar.setTitle(R.string.app_name);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        DrawerFragment drawerFragment = (DrawerFragment) fm.findFragmentByTag(DrawerFragment.TAG);

        if (drawerFragment == null){
            drawerFragment = DrawerFragment.newInstance();
            ft.replace(R.id.drawer, drawerFragment, DrawerFragment.TAG);
        } else {
            ft.attach(drawerFragment);
        }

        ft.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }

        switch (item.getItemId()){
            case R.id.action_search:
                openSearch();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void closeDrawer(){
        mDrawerLayout.closeDrawer(mDrawer);
    }

    private void openSearch(){
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
    }
}
