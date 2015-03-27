package tw.skyarrow.ehreader.app;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import tw.skyarrow.ehreader.Constant;
import tw.skyarrow.ehreader.R;

public class MainActivity extends ActionBarActivity {
    private Toolbar mToolbar;
    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;
    private RecyclerView mMainMenu;
    private RecyclerView mSubMenu;
    private LinearLayoutManager mMainMenuLayoutManager;
    private LinearLayoutManager mSubMenuLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mDrawer = (DrawerLayout) findViewById(R.id.drawer);
        mMainMenu = (RecyclerView) findViewById(R.id.main_menu);
        mSubMenu = (RecyclerView) findViewById(R.id.sub_menu);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawer, mToolbar, R.string.drawer_open, R.string.drawer_close);
        mMainMenuLayoutManager = new LinearLayoutManager(this);
        mSubMenuLayoutManager = new LinearLayoutManager(this);

        mToolbar.setTitle(R.string.app_name);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mDrawerToggle.syncState();
        mDrawer.setDrawerListener(mDrawerToggle);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frame, SearchFragment.newInstance(Constant.BASE_URL));
        ft.commit();

//        mMainMenu.setHasFixedSize(true);
//        mMainMenu.setLayoutManager(mMainMenuLayoutManager);

//        mSubMenu.setHasFixedSize(true);
//        mSubMenu.setLayoutManager(mSubMenuLayoutManager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

            case R.id.action_settings:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
