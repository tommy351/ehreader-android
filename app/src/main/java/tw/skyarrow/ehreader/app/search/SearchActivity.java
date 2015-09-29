package tw.skyarrow.ehreader.app.search;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.facebook.drawee.backends.pipeline.Fresco;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Subscription;
import rx.subjects.BehaviorSubject;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.app.entry.WebFragment;
import tw.skyarrow.ehreader.provider.SearchSuggestionProvider;
import tw.skyarrow.ehreader.util.FabricHelper;
import tw.skyarrow.ehreader.util.L;
import tw.skyarrow.ehreader.util.ToolbarHelper;

/**
 * Created by SkyArrow on 2015/9/28.
 */
public class SearchActivity extends AppCompatActivity {
    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @InjectView(R.id.search_view)
    SearchView searchView;

    private static final String[] FILTER_PARAMS = new String[]{
            "f_doujinshi", "f_manga", "f_artistcg", "f_gamecg", "f_western",
            "f_non-h", "f_imageset", "f_cosplay", "f_asianporn", "f_misc"
    };

    private boolean[] chosenCategories;
    private BehaviorSubject<String> querySubject;
    private Subscription subscription;

    public static Intent intent(Context context, String query){
        Intent intent = new Intent(context, SearchActivity.class);

        intent.setAction(Intent.ACTION_SEARCH);
        intent.putExtra(SearchManager.QUERY, query);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FabricHelper.setupFabric(this);
        Fresco.initialize(this);
        setContentView(R.layout.activity_search);
        ButterKnife.inject(this);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        chosenCategories = new boolean[]{true, true, true, true, true, true, true, true, true, true};
        querySubject = BehaviorSubject.create();
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                querySubject.onNext(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        setupSubscription();

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.frame);
        Intent intent = getIntent();
        String query = intent.getStringExtra(SearchManager.QUERY);

        if (fragment != null){
            FragmentTransaction ft = fm.beginTransaction();
            ft.attach(fragment);
            ft.commit();
        } else if (Intent.ACTION_SEARCH.equals(intent.getAction()) && !TextUtils.isEmpty(query)){
            searchView.setQuery(query, true);
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
                ToolbarHelper.upNavigation(this);
                return true;

            case R.id.action_filter:
                showFilterDialog();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        subscription.unsubscribe();
        super.onDestroy();
    }

    private void setupSubscription(){
        subscription = querySubject
                .filter(query -> !TextUtils.isEmpty(query))
                .subscribe(query -> {
                    searchView.clearFocus();
                    String baseUrl;

                    try {
                        baseUrl = "/?f_search=" + URLEncoder.encode(query, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        L.e(e);
                        return;
                    }

                    for (int i = 0, len = chosenCategories.length; i < len; i++) {
                        baseUrl += "&" + FILTER_PARAMS[i] + "=" + (chosenCategories[i] ? "1" : "0");
                    }

                    FragmentManager fm = getSupportFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                            SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);

                    suggestions.saveRecentQuery(query, null);
                    ft.replace(R.id.frame, WebFragment.create(baseUrl));
                    ft.commit();
                });
    }

    private void showFilterDialog(){
        boolean[] chooses = chosenCategories.clone();
        int[] categoryResources = {
                R.string.category_doujinshi,
                R.string.category_manga,
                R.string.category_artistcg,
                R.string.category_gamecg,
                R.string.category_western,
                R.string.category_non_h,
                R.string.category_imageset,
                R.string.category_cosplay,
                R.string.category_asianporn,
                R.string.category_misc
        };

        new AlertDialog.Builder(this)
                .setMultiChoiceItems(buildResourceArray(categoryResources), chooses, (dialog, i, b) -> {
                    chooses[i] = b;
                })
                .setPositiveButton(R.string.ok, (dialog, i) -> {
                    chosenCategories = chooses;
                    querySubject.onNext(querySubject.getValue());
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private String[] buildResourceArray(int[] arr){
        String[] result = new String[arr.length];

        for (int i = 0; i < arr.length; i++) {
            result[i] = getString(arr[i]);
        }

        return result;
    }
}
