package tw.skyarrow.ehreader.app;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.os.Build;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.MenuItem;
import android.view.View;

/**
 * Created by SkyArrow on 2014/2/27.
 */
public abstract class SearchBarActivity extends DrawerActivity {
    private boolean isSearchViewExpanded = false;

    protected void setupSearchBar(MenuItem item) {
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(getComponentName());

        searchView.setSearchableInfo(searchableInfo);
        searchView.setIconifiedByDefault(true);
        searchView.setOnQueryTextFocusChangeListener(new FocusChangeListener(item));
    }

    private class FocusChangeListener implements View.OnFocusChangeListener {
        private MenuItem menuItem;

        private FocusChangeListener(MenuItem menuItem) {
            this.menuItem = menuItem;
        }

        @Override
        public void onFocusChange(View view, boolean focus) {
            isSearchViewExpanded = focus;

            if (!focus) {
                if (Build.VERSION.SDK_INT >= 14) {
                    menuItem.collapseActionView();
                }
            }
        }
    }

    public boolean isSearchViewExpanded() {
        return isSearchViewExpanded;
    }
}
