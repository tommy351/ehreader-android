package tw.skyarrow.ehreader.activity;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import com.google.analytics.tracking.android.EasyTracker;

import tw.skyarrow.ehreader.Constant;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.util.SearchHelper;

public class MainActivity extends FragmentActivity implements ActionBar.OnNavigationListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SpinnerAdapter spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.main_tabs,
                android.R.layout.simple_spinner_dropdown_item);
        ActionBar actionBar = getActionBar();

        actionBar.setListNavigationCallbacks(spinnerAdapter, this);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setDisplayShowTitleEnabled(false);
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
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(int i, long l) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment fragment;
        Bundle args = new Bundle();

        switch (i) {
            case 1:
                fragment = new MainFragmentStar();
                break;

            case 2:
                fragment = new MainFragmentDownload();
                break;

            default:
                fragment = new MainFragmentWeb();
                args.putString("base", Constant.BASE_URL);
        }

        fragment.setArguments(args);
        ft.replace(R.id.container, fragment);
        ft.commit();

        return true;
    }

    private void fileSearch() {
        Intent intent = new Intent(MainActivity.this, ImageSearchActivity.class);

        startActivity(intent);
    }
}
