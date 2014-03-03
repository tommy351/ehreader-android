package tw.skyarrow.ehreader.app.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import de.greenrobot.event.EventBus;
import tw.skyarrow.ehreader.Constant;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.app.pref.PrefActivity;
import tw.skyarrow.ehreader.app.search.FilterDialog;
import tw.skyarrow.ehreader.app.search.ImageSearchActivity;
import tw.skyarrow.ehreader.event.ListUpdateEvent;
import tw.skyarrow.ehreader.util.ActionBarHelper;
import tw.skyarrow.ehreader.util.LoginHelper;

public class MainActivity extends AdActivity {
    public static final String TAG = "MainActivity";

    public static final String EXTRA_TAB = "tab";
    public static final int TAB_GALLERY = 0;
    public static final int TAB_STARRED = 1;
    public static final int TAB_HISTORY = 2;
    public static final int TAB_DOWNLOAD = 3;

    public static final int REQUEST_PREFERENCE = 1;

    private EventBus bus;
    private int currentTab = 0;
    private String actionBarTitle = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bus = EventBus.getDefault();
        setupDrawer();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String launchPagePref = preferences.getString(getString(R.string.pref_launch_page), getString(R.string.pref_launch_page_default));
        int tab = getIntent().getIntExtra(EXTRA_TAB, Integer.parseInt(launchPagePref));

        if (savedInstanceState != null) {
            tab = savedInstanceState.getInt(EXTRA_TAB);
        }

        changeTab(tab);
        setupAd();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(EXTRA_TAB, currentTab);
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
    public void onItemClick(int i) {
        changeTab(i);
        closeDrawer();
    }

    private void changeTab(int i) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment fragment;
        Bundle args = new Bundle();
        String tag;
        boolean loggedIn = LoginHelper.getInstance(this).isLoggedIn();
        String[] tabs = getResources().getStringArray(R.array.main_tabs);
        actionBarTitle = tabs[i];

        DrawerHeaderAdapter adapter = getDrawerAdapter();

        adapter.getItem(currentTab).setSelected(false);
        adapter.getItem(i).setSelected(true);
        adapter.notifyDataSetChanged();

        currentTab = i;

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
                args.putString(MainFragmentWeb.EXTRA_BASE, loggedIn ? Constant.BASE_URL_EX : Constant.BASE_URL);
        }

        getSupportActionBar().setTitle(actionBarTitle);
        fragment.setArguments(args);
        ft.replace(R.id.container, fragment, tag);
        ft.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_PREFERENCE:
                bus.post(new ListUpdateEvent());
                break;
        }
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

        startActivityForResult(intent, REQUEST_PREFERENCE);
    }

    @Override
    public void onDrawerOpened(View view) {
        getSupportActionBar().setTitle(R.string.app_name);
    }

    @Override
    public void onDrawerClosed(View view) {
        getSupportActionBar().setTitle(actionBarTitle);
    }
}
