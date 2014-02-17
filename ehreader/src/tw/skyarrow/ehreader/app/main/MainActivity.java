package tw.skyarrow.ehreader.app.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import tw.skyarrow.ehreader.BaseApplication;
import tw.skyarrow.ehreader.Constant;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.app.filter.FilterDialog;
import tw.skyarrow.ehreader.app.pref.PrefActivity;
import tw.skyarrow.ehreader.app.search.ImageSearchActivity;
import tw.skyarrow.ehreader.util.ActionBarHelper;

public class MainActivity extends ActionBarActivity implements ActionBar.OnNavigationListener {
    private static final String TAG = "MainActivity";

    public static final int TAB_GALLERY = 0;
    public static final int TAB_STARRED = 1;
    public static final int TAB_HISTORY = 2;
    public static final int TAB_DOWNLOAD = 3;

    private static final int CONTAINER = R.id.container;

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

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String launchPagePref = preferences.getString(getString(R.string.pref_launch_page), "0");
        int tab = Integer.parseInt(launchPagePref);
        Bundle args;

        if (savedInstanceState != null) {
            args = savedInstanceState;
        } else {
            args = getIntent().getExtras();
        }

        if (args != null) {
            tab = args.getInt("tab");
        }

        actionBar.setSelectedNavigationItem(tab);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        ActionBarHelper.createSearchMenu(this, menu.findItem(R.id.menu_search));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                openSettings();
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
        boolean loggedIn = BaseApplication.isLoggedIn();

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
                args.putString("base", loggedIn ? Constant.BASE_URL_EX : Constant.BASE_URL);
        }

        fragment.setArguments(args);
        ft.replace(CONTAINER, fragment, tag);
        ft.commit();

        return true;
    }

    private void fileSearch() {
        Intent intent = new Intent(this, ImageSearchActivity.class);

        startActivity(intent);
    }

    private void openFilterDialog() {
        DialogFragment dialog = new FilterDialog();

        dialog.show(getSupportFragmentManager(), FilterDialog.TAG);
    }

    private void openSettings() {
        Intent intent = new Intent(this, PrefActivity.class);

        startActivity(intent);
    }
}
