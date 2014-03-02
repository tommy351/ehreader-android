package tw.skyarrow.ehreader.app.search;

import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;

import tw.skyarrow.ehreader.BaseApplication;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.app.main.AdActivity;
import tw.skyarrow.ehreader.app.main.MainFragmentWeb;
import tw.skyarrow.ehreader.provider.SearchSuggestionProvider;
import tw.skyarrow.ehreader.util.ActionBarHelper;

/**
 * Created by SkyArrow on 2014/1/28.
 */
public class SearchActivity extends AdActivity {
    private static final String TAG = "SearchActivity";

    private String query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupDrawer();
        setDrawerIndicatorEnabled(false);

        Intent intent = getIntent();
        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean loggedIn = preferences.getBoolean(getString(R.string.pref_logged_in), false);

        if (Intent.ACTION_SEARCH.equals(intent.getAction()) && savedInstanceState == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Fragment fragment = new MainFragmentWeb();
            Bundle bundle = new Bundle();
            query = intent.getStringExtra(SearchManager.QUERY);
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);

            bundle.putString("base", ActionBarHelper.buildSearchUrl(query, loggedIn));
            fragment.setArguments(bundle);
            suggestions.saveRecentQuery(query, null);
            actionBar.setTitle(query);

            ft.replace(R.id.container, fragment);
            ft.commit();
            setupAd();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        MapBuilder builder = MapBuilder.createAppView();
        builder.set(Fields.SCREEN_NAME, TAG);

        BaseApplication.getTracker().send(builder.build());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ActionBarHelper.upNavigation(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
