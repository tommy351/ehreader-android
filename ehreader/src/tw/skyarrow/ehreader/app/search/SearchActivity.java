package tw.skyarrow.ehreader.app.search;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import tw.skyarrow.ehreader.Constant;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.app.main.WebFragment;
import tw.skyarrow.ehreader.event.SearchFilterEvent;
import tw.skyarrow.ehreader.provider.SearchSuggestionProvider;
import tw.skyarrow.ehreader.util.ActionBarHelper;
import tw.skyarrow.ehreader.util.LoginHelper;
import tw.skyarrow.ehreader.view.RecyclerViewItemClickListener;

public class SearchActivity extends ActionBarActivity implements RecyclerViewItemClickListener.OnItemClickListener {

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    @InjectView(R.id.input)
    EditText mTextInput;

    @InjectView(R.id.suggestion_list)
    RecyclerView mSuggestionListView;

    private static final String[] FILTER_PARAMS = new String[]{
            "f_doujinshi", "f_manga", "f_artistcg", "f_gamecg", "f_western",
            "f_non-h", "f_imageset", "f_cosplay", "f_asianporn", "f_misc"
    };

    private static final int SUGGESTION_LIMIT = 3;

    private String lastQuery = "";
    private boolean[] mChosenCategories = new boolean[]{true, true, true, true, true, true, true, true, true, true};
    private SearchSuggestionListAdater mSuggestionAdapter;
    private SearchableInfo mSearchable;

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

        // Set up text input
        mTextInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                boolean shouldSubmit = false;

                if (actionId == EditorInfo.IME_ACTION_SEARCH){
                    shouldSubmit = true;
                } else if (event != null){
                    switch (event.getKeyCode()){
                        case KeyEvent.KEYCODE_ENTER:
                        case KeyEvent.KEYCODE_NUMPAD_ENTER:
                            shouldSubmit = true;
                            break;
                    }
                }

                if (!shouldSubmit) return false;

                String query = textView.getText().toString();
                doSearch(query);
                return true;
            }
        });

        mTextInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mSuggestionAdapter.swapCursor(getSuggestions(editable.toString()));
            }
        });

        mTextInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                if (focus){
                    showSuggestionList();
                } else {
                    hideSuggestionList();
                }
            }
        });

        // Set up SearchableInfo
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        mSearchable = searchManager.getSearchableInfo(getComponentName());
        mTextInput.setHint(mSearchable.getHintId());

        // Set up suggestion list
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mSuggestionAdapter = new SearchSuggestionListAdater(this, getSuggestions(""));
        mSuggestionListView.setLayoutManager(layoutManager);
        mSuggestionListView.setAdapter(mSuggestionAdapter);
        mSuggestionListView.addOnItemTouchListener(new RecyclerViewItemClickListener(this, this));

        // Find the last fragment
        FragmentManager fm = getSupportFragmentManager();
        Fragment lastFragment = fm.findFragmentByTag(WebFragment.TAG);
        Intent intent = getIntent();

        if (lastFragment != null){
            FragmentTransaction ft = fm.beginTransaction();
            ft.attach(lastFragment);
            ft.commit();
        } else if (Intent.ACTION_SEARCH.equals(intent.getAction())){
            String query = intent.getStringExtra(SearchManager.QUERY);
            mTextInput.setText(query);
            doSearch(query);
        } else {
            mTextInput.requestFocus();
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
                if (!lastQuery.isEmpty() && mTextInput.isFocused()){
                    restoreTextInput();
                } else {
                    ActionBarHelper.upNavigation(this);
                }

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

    @Override
    public void onBackPressed() {
        if (!lastQuery.isEmpty() && mTextInput.isFocused()){
            restoreTextInput();
        } else {
            super.onBackPressed();
        }
    }

    public void onEvent(SearchFilterEvent event){
        if (event.getChosenCategories() == null) return;

        mChosenCategories = event.getChosenCategories();
        doSearch(lastQuery);
    }

    private void doSearch(String query){
        clearFocus();

        if (query == null || query.equals(lastQuery)) return;

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
        boolean loggedIn = LoginHelper.getInstance(this).isLoggedIn();
        Uri.Builder builder = Uri.parse(loggedIn ? Constant.BASE_URL_EX : Constant.BASE_URL).buildUpon();
        lastQuery = query;

        builder.appendQueryParameter("f_search", query);

        for (int i = 0, len = mChosenCategories.length; i < len; i++){
            builder.appendQueryParameter(FILTER_PARAMS[i], mChosenCategories[i] ? "1" : "0");
        }

        suggestions.saveRecentQuery(query, null);
        ft.replace(R.id.frame, WebFragment.newInstance(builder.toString()), WebFragment.TAG);
        ft.commit();
    }

    private void openFilterDialog(){
        SearchFilterDialog dialog = SearchFilterDialog.newInstance(mChosenCategories);
        dialog.show(getSupportFragmentManager(), SearchFilterDialog.TAG);
    }

    private void searchByImage(){
        //
    }

    // Copied from: android.app.SearchManager:showSuggestions
    private Cursor getSuggestions(String query){
        String authority = mSearchable.getSuggestAuthority();

        Uri.Builder uriBuilder = new Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT)
                .authority(authority)
                .query("")  // TODO: Remove, workaround for a bug in Uri.writeToParcel()
                .fragment("");  // TODO: Remove, workaround for a bug in Uri.writeToParcel()

        // if content path provided, insert it now
        final String contentPath = mSearchable.getSuggestPath();
        if (contentPath != null) {
            uriBuilder.appendEncodedPath(contentPath);
        }

        // append standard suggestion query path
        uriBuilder.appendPath(SearchManager.SUGGEST_URI_PATH_QUERY);

        // get the query selection, may be null
        String selection = mSearchable.getSuggestSelection();
        // inject query, either as selection args or inline
        String[] selArgs = null;
        if (selection != null) {    // use selection if provided
            selArgs = new String[] { query };
        } else {                    // no selection, use REST pattern
            uriBuilder.appendPath(query);
        }

        uriBuilder.appendQueryParameter(SearchManager.SUGGEST_PARAMETER_LIMIT, String.valueOf(SUGGESTION_LIMIT));

        Uri uri = uriBuilder.build();

        // finally, make the query
        return getContentResolver().query(uri, null, selection, selArgs, null);
    }

    private void showSuggestionList(){
        mSuggestionListView.setVisibility(View.VISIBLE);
    }

    private void hideSuggestionList(){
        mSuggestionListView.setVisibility(View.GONE);
    }

    private void restoreTextInput(){
        mTextInput.setText(lastQuery);
        clearFocus();
    }

    private void clearFocus(){
        mTextInput.clearFocus();
    }

    @Override
    public void onItemClick(View childView, int position) {
        Cursor cursor = mSuggestionAdapter.getCursor();

        if (cursor != null && cursor.moveToPosition(position)){
            SearchSuggestionItem item = SearchSuggestionItem.fromCursor(cursor);
            String query = item.getTitle();

            mTextInput.setText(query);
            doSearch(query);
        }
    }

    @Override
    public void onItemLongPress(View childView, int position) {
        //
    }
}
