package tw.skyarrow.ehreader.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import com.google.analytics.tracking.android.EasyTracker;

import tw.skyarrow.ehreader.Constant;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.util.SearchHelper;

public class MainActivity extends ActionBarActivity implements ActionBar.OnNavigationListener {
    public static final int TAB_GALLERY = 0;
    public static final int TAB_STARRED = 1;
    public static final int TAB_HISTORY = 2;
    public static final int TAB_DOWNLOAD = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SpinnerAdapter spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.main_tabs,
                android.R.layout.simple_spinner_dropdown_item);
        ActionBar actionBar = getSupportActionBar();

        actionBar.setListNavigationCallbacks(spinnerAdapter, this);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setDisplayShowTitleEnabled(false);

        Bundle args;

        if (savedInstanceState != null) {
            args = savedInstanceState;
        } else {
            args = getIntent().getExtras();
        }

        if (args != null) {
            actionBar.setSelectedNavigationItem(args.getInt("tab"));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
    }

    @Override
    public void onStart() {
        super.onStart();

        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        EasyTracker.getInstance(this).activityStop(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        SearchHelper.createSearchMenu(this, menu.findItem(R.id.menu_search));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                return true;

            case R.id.menu_file_search:
                fileSearch();
                return true;

            case R.id.menu_filter:
                openFilterDialog();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(int i, long l) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment fragment;
        Bundle args = new Bundle();
        String tag;

        switch (i) {
            case TAB_STARRED:
                fragment = new MainFragmentStar();
                tag = MainFragmentStar.TAG;
                break;

            case TAB_HISTORY:
                fragment = new MainFragmentHistory();
                tag = MainFragmentHistory.TAG;
                break;

            case TAB_DOWNLOAD:
                fragment = new MainFragmentDownload();
                tag = MainFragmentDownload.TAG;
                break;

            default:
                fragment = new MainFragmentWeb();
                tag = MainFragmentWeb.TAG;
                args.putString("base", Constant.BASE_URL);
        }

        fragment.setArguments(args);
        ft.replace(R.id.container, fragment, tag);
        ft.commit();

        return true;
    }

    private void fileSearch() {
        Intent intent = new Intent(MainActivity.this, ImageSearchActivity.class);

        startActivity(intent);
    }

    private void openFilterDialog() {
        DialogFragment dialog = new FilterDialog();

        dialog.show(getSupportFragmentManager(), "filter");
    }
}
