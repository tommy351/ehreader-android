package tw.skyarrow.ehreader.app.main;

import android.os.Bundle;
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

public class MainActivity extends ActionBarActivity implements DrawerActivity {
    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    @InjectView(R.id.container)
    DrawerLayout mDrawerLayout;

    @InjectView(R.id.drawer)
    View mDrawer;

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerFragment mDrawerFragment;

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

        if (mDrawerFragment == null){
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            mDrawerFragment = DrawerFragment.newInstance();
            ft.replace(R.id.drawer, mDrawerFragment, DrawerFragment.TAG);
            ft.commit();
        }

        if (savedInstanceState == null){
            mDrawerFragment.setCurrentPage(DrawerFragment.TAB_LATEST);
        }
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
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void closeDrawer(){
        mDrawerLayout.closeDrawer(mDrawer);
    }
}
