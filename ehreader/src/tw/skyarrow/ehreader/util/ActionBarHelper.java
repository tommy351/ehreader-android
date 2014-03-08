package tw.skyarrow.ehreader.util;

import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.MenuItem;
import android.widget.SearchView;

import tw.skyarrow.ehreader.Constant;

/**
 * Created by SkyArrow on 2014/1/28.
 */
public class ActionBarHelper {
    public static void upNavigation(Activity activity) {
        upNavigation(activity, null);
    }

    public static void upNavigation(Activity activity, Bundle args) {
        Intent upIntent = NavUtils.getParentActivityIntent(activity);

        if (args != null) upIntent.putExtras(args);

        if (NavUtils.shouldUpRecreateTask(activity, upIntent)) {
            TaskStackBuilder.create(activity)
                    .addNextIntentWithParentStack(upIntent)
                    .startActivities();
        } else {
            activity.finish();
        }
    }

    public static void createSearchMenu(Activity activity, MenuItem item) {
        SearchManager searchManager = (SearchManager) activity.getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) item.getActionView();
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(activity.getComponentName());

        searchView.setOnQueryTextListener(new QueryTextListener(item));
        searchView.setOnSuggestionListener(new SuggestionListener(item));
        searchView.setSearchableInfo(searchableInfo);
        searchView.setIconifiedByDefault(true);
    }

    public static class QueryTextListener implements SearchView.OnQueryTextListener {
        private MenuItem menuItem;

        public QueryTextListener(MenuItem menuItem) {
            this.menuItem = menuItem;
        }

        @Override
        public boolean onQueryTextSubmit(String s) {
            menuItem.collapseActionView();
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
            menuItem.collapseActionView();
            return false;
        }
    }

    public static String buildSearchUrl(String query) {
        return buildSearchUrl(query, false);
    }

    public static String buildSearchUrl(String query, boolean ex) {
        Uri.Builder builder = Uri.parse(ex ? Constant.BASE_URL_EX : Constant.BASE_URL).buildUpon();

        builder.appendQueryParameter("f_search", query);

        return builder.build().toString();
    }
}
