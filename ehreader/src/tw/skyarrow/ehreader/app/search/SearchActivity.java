package tw.skyarrow.ehreader.app.search;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteCursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import tw.skyarrow.ehreader.Constant;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.app.main.SearchFragment;
import tw.skyarrow.ehreader.provider.SearchSuggestionProvider;
import tw.skyarrow.ehreader.util.ActionBarHelper;
import tw.skyarrow.ehreader.util.L;

public class SearchActivity extends ActionBarActivity implements SearchView.OnQueryTextListener, SearchView.OnSuggestionListener {

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    @InjectView(R.id.search_view)
    SearchView mSearchView;

    private String lastQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.inject(this);

        // Set up toolbar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Set up SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(getComponentName());
        lastQuery = "";

        mSearchView.setSearchableInfo(searchableInfo);
        mSearchView.setSubmitButtonEnabled(false);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setOnSuggestionListener(this);

        // Find the last fragment
        FragmentManager fm = getSupportFragmentManager();
        Fragment lastFragment = fm.findFragmentByTag(SearchFragment.TAG);
        Intent intent = getIntent();

        if (lastFragment != null){
            FragmentTransaction ft = fm.beginTransaction();
            ft.attach(lastFragment);
            ft.commit();
        } else if (Intent.ACTION_SEARCH.equals(intent.getAction())){
            mSearchView.setQuery(intent.getStringExtra(SearchManager.QUERY), true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                ActionBarHelper.upNavigation(this);
                return true;

            case R.id.action_filter:
                openFilterDialog();
                return true;

            case R.id.action_image_search:
                searchByImage();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void doSearch(String query){
        if (lastQuery.equals(query)) return;

        lastQuery = query;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
        Uri.Builder builder = Uri.parse(Constant.BASE_URL).buildUpon();

        builder.appendQueryParameter("f_search", query);
        suggestions.saveRecentQuery(query, null);
        ft.replace(R.id.frame, SearchFragment.newInstance(builder.toString()), SearchFragment.TAG);
        ft.commit();
    }

    private void openFilterDialog(){
        //
    }

    private void searchByImage(){
        //
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        doSearch(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        return false;
    }

    @Override
    public boolean onSuggestionSelect(int position) {
        return false;
    }

    @Override
    public boolean onSuggestionClick(int position) {
        // TODO: customized suggestion view
        L.d("onSuggestionClick: %d", position);
        return true;
    }
}
