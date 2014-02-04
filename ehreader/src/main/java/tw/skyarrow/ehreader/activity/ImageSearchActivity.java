package tw.skyarrow.ehreader.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import tw.skyarrow.ehreader.R;

/**
 * Created by SkyArrow on 2014/1/29.
 */
public class ImageSearchActivity extends ActionBarActivity {
    private boolean isSelected = false;
    private static final int CONTAINER = R.id.container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Bundle args = getIntent().getExtras();
        Fragment fragment;

        if (args == null) {
            fragment = new ImageSearchSelectFragment();
        } else {
            if (args.getLong("photo") > 0) {
                fragment = new ImageSearchPhotoFragment();
            } else if (args.getString("base") != null) {
                fragment = new MainFragmentWeb();
            } else {
                fragment = new ImageSearchSelectFragment();
            }
        }

        fragment.setArguments(args);
        ft.replace(CONTAINER, fragment);
        ft.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                backState();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void backState() {
        if (isSelected) {
            FragmentManager fm = getSupportFragmentManager();
            fm.popBackStack();
            isSelected = false;
        } else {
            finish();
        }
    }

    public void displaySelectResult(String base) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment fragment = new MainFragmentWeb();
        Bundle args = new Bundle();
        isSelected = true;

        args.putString("base", base);
        fragment.setArguments(args);

        ft.replace(CONTAINER, fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(null);
        ft.commit();
    }

    public void displayPhotoResult(String base) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment fragment = new MainFragmentWeb();
        Bundle args = new Bundle();

        args.putString("base", base);
        fragment.setArguments(args);

        ft.replace(CONTAINER, fragment);
        ft.commit();
    }
}
