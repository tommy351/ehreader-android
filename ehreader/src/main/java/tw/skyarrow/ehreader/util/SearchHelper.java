package tw.skyarrow.ehreader.util;

import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.MenuItem;

import tw.skyarrow.ehreader.Constant;

/**
 * Created by SkyArrow on 2014/1/28.
 */
public class SearchHelper {
    public static void createSearchMenu(Activity activity, MenuItem item) {
        SearchManager searchManager = (SearchManager) activity.getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(activity.getComponentName());

        searchView.setOnQueryTextListener(new QueryTextListener(item));
        searchView.setOnSuggestionListener(new SuggestionListener(item));
        searchView.setSearchableInfo(searchableInfo);
    }

    public static class QueryTextListener implements SearchView.OnQueryTextListener {
        private MenuItem menuItem;

        public QueryTextListener(MenuItem menuItem) {
            this.menuItem = menuItem;
        }

        @Override
        public boolean onQueryTextSubmit(String s) {
            if (Build.VERSION.SDK_INT >= 14) {
                menuItem.collapseActionView();
            }

            return false;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            return false;
        }
    }

    public static class SuggestionListener implements SearchView.OnSuggestionListener {
        private MenuItem menuItem;

        public SuggestionListener(MenuItem menuItem) {
            this.menuItem = menuItem;
        }

        @Override
        public boolean onSuggestionSelect(int i) {
            return false;
        }

        @Override
        public boolean onSuggestionClick(int i) {
            if (Build.VERSION.SDK_INT >= 14) {
                menuItem.collapseActionView();
            }

            return false;
        }
    }

    public static String buildUrl(String query) {
        Uri.Builder builder = Uri.parse(Constant.BASE_URL).buildUpon();

        builder.appendQueryParameter("f_search", query);

        return builder.build().toString();
    }
}
