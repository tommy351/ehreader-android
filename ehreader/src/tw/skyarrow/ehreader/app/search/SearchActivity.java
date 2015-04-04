package tw.skyarrow.ehreader.app.search;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import tw.skyarrow.ehreader.Constant;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.app.main.SearchFragment;
import tw.skyarrow.ehreader.event.SearchFilterEvent;
import tw.skyarrow.ehreader.provider.SearchSuggestionProvider;
import tw.skyarrow.ehreader.util.ActionBarHelper;
import tw.skyarrow.ehreader.util.L;

public class SearchActivity extends ActionBarActivity implements SearchView.OnQueryTextListener, SearchView.OnSuggestionListener {

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    @InjectView(R.id.search_view)
    SearchView mSearchView;

    private static final String[] FILTER_PARAMS = new String[]{
            "f_doujinshi", "f_manga", "f_artistcg", "f_gamecg", "f_western",
            "f_non-h", "f_imageset", "f_cosplay", "f_asianporn", "f_misc"
    };

    private String lastQuery = "";
    private boolean[] mChosenCategories = new boolean[]{true, true, true, true, true, true, true, true, true, true};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.inject(this);
        EventBus.getDefault().register(this);

        // Set up toolbar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Set up SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(getComponentName());

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
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
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

    public void onEvent(SearchFilterEvent event){
        if (event.getChosenCategories() == null) return;

        mChosenCategories = event.getChosenCategories();
        doSearch(lastQuery);
    }

    private void doSearch(String query){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
        Uri.Builder builder = Uri.parse(Constant.BASE_URL).buildUpon();

        builder.appendQueryParameter("f_search", query);

        for (int i = 0, len = mChosenCategories.length; i < len; i++){
            builder.appendQueryParameter(FILTER_PARAMS[i], mChosenCategories[i] ? "1" : "0");
        }

        suggestions.saveRecentQuery(query, null);
        ft.replace(R.id.frame, SearchFragment.newInstance(builder.toString()), SearchFragment.TAG);
        ft.commit();
    }

    private void openFilterDialog(){
        SearchFilterDialog dialog = SearchFilterDialog.newInstance(mChosenCategories);
        dialog.show(getSupportFragmentManager(), SearchFilterDialog.TAG);
    }

    private void searchByImage(){
        //
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if (!query.equals(lastQuery)){
            lastQuery = query;
            doSearch(query);
        }

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
