package tw.skyarrow.ehreader.app.entry;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.facebook.drawee.backends.pipeline.Fresco;

import butterknife.ButterKnife;
import butterknife.InjectView;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.app.pref.PrefActivity;
import tw.skyarrow.ehreader.app.search.SearchActivity;
import tw.skyarrow.ehreader.util.FabricHelper;

/**
 * Created by SkyArrow on 2015/9/24.
 */
public class EntryActivity extends AppCompatActivity {
    public static final String EXTRA_TAB = "TAB";

    public static final int TAB_LATEST = 0;
    public static final int TAB_FAVORITES = 1;
    public static final int TAB_HISTORY = 2;
    public static final int TAB_DOWNLOAD = 3;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @InjectView(R.id.view_pager)
    ViewPager viewPager;

    @InjectView(R.id.tab_layout)
    TabLayout tabLayout;

    @InjectView(R.id.coordinator)
    CoordinatorLayout coordinatorLayout;

    public static Intent intent(Context context, int tab){
        Intent intent = new Intent(context, EntryActivity.class);
        Bundle args = bundle(tab);

        intent.putExtras(args);

        return intent;
    }

    public static Bundle bundle(int tab){
        Bundle bundle = new Bundle();

        bundle.putLong(EXTRA_TAB, tab);

        return bundle;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FabricHelper.setupFabric(this);
        Fresco.initialize(this);
        setContentView(R.layout.activity_entry);
        ButterKnife.inject(this);

        setSupportActionBar(toolbar);

        viewPager.setAdapter(new EntryPagerAdapter(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);

        Intent intent = getIntent();
        Bundle args = intent.getExtras();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String launchPagePref = prefs.getString(getString(R.string.pref_launch_page), getString(R.string.pref_launch_page_default));
        int defaultTab = Integer.parseInt(launchPagePref, 10);
        int tab = args != null ? args.getInt(EXTRA_TAB, defaultTab) : defaultTab;
        viewPager.setCurrentItem(tab, false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.entry, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_search:
                showSearch();
                return true;

            case R.id.action_settings:
                showSettings();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showSearch(){
        Intent intent = SearchActivity.intent(this, "");
        startActivity(intent);
    }

    private void showSettings(){
        Intent intent = PrefActivity.intent(this);
        startActivity(intent);
    }

    private class EntryPagerAdapter extends FragmentPagerAdapter {
        private final String tabTitles[] = new String[]{"Latest", "Favorites", "History", "Downloads"};

        public EntryPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
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

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }
    }
}
