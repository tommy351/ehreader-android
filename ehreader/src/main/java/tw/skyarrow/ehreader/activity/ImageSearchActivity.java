package tw.skyarrow.ehreader.activity;

import android.app.ActionBar;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MenuItem;

import de.greenrobot.event.EventBus;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.event.ImageSearchUploadedEvent;

/**
 * Created by SkyArrow on 2014/1/29.
 */
public class ImageSearchActivity extends FragmentActivity {
    private static final int CONTAINER = R.id.container;

    private boolean isSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(CONTAINER, new ImageSearchSelectFragment());
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

    public void onEvent(ImageSearchUploadedEvent event) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment fragment = new MainFragmentWeb();
        Bundle args = new Bundle();
        isSelected = true;

        args.putString("base", event.buildUrl());
        fragment.setArguments(args);

        ft.replace(CONTAINER, fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }
}
