package tw.skyarrow.ehreader.activity;

import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;

import tw.skyarrow.ehreader.BaseApplication;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.provider.SearchSuggestionProvider;
import tw.skyarrow.ehreader.util.SearchHelper;

/**
 * Created by SkyArrow on 2014/1/28.
 */
public class SearchActivity extends ActionBarActivity {
    private static final String TAG = "SearchActivity";

    private String query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

            bundle.putString("base", SearchHelper.buildUrl(query, loggedIn));
            fragment.setArguments(bundle);
            suggestions.saveRecentQuery(query, null);
            actionBar.setTitle(query);

            ft.add(R.id.container, fragment).commit();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        MapBuilder builder = MapBuilder.createAppView();
        builder.set(Fields.SCREEN_NAME, TAG);
        builder.set(Fields.TITLE, query);

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
