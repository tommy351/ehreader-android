package tw.skyarrow.ehreader.activity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import tw.skyarrow.ehreader.Constant;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.db.DaoMaster;
import tw.skyarrow.ehreader.db.DaoSession;
import tw.skyarrow.ehreader.db.Gallery;
import tw.skyarrow.ehreader.db.GalleryDao;
import tw.skyarrow.ehreader.db.PhotoDao;
import tw.skyarrow.ehreader.event.PhotoDialogEvent;

/**
 * Created by SkyArrow on 2014/1/31.
 */
public class PhotoActivity extends ActionBarActivity {
    @InjectView(R.id.pager)
    ViewPager pager;

    private SQLiteDatabase db;
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private GalleryDao galleryDao;

    private Gallery gallery;

    private PagerAdapter pagerAdapter;
    private EventBus bus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_photo);
        ButterKnife.inject(this);

        bus = EventBus.getDefault();
        bus.register(this);

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, Constant.DB_NAME, null);
        db = helper.getWritableDatabase();
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
        galleryDao = daoSession.getGalleryDao();

        Bundle args = getIntent().getExtras();
        long galleryId = args.getLong("id");
        gallery = galleryDao.load(galleryId);
        final ActionBar actionBar = getSupportActionBar();
        int page;

        pagerAdapter = new PhotoPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        actionBar.setDisplayHomeAsUpEnabled(true);
        //toggleUIVisibility();

        if (savedInstanceState != null) {
            page = savedInstanceState.getInt("page");
        } else if (args.containsKey("page")) {
            page = args.getInt("page");
        } else {
            page = gallery.getProgress();
        }

        if (page < 0) page = 0;

        actionBar.setTitle(String.format("%d / %d", page + 1, gallery.getCount()));
        pager.setCurrentItem(page, false);

        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
                //
            }

            @Override
            public void onPageSelected(int i) {
                actionBar.setTitle(String.format("%s / %s", i + 1, gallery.getCount()));
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                //
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.photo, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackClick();
                return true;

            case R.id.menu_bookmark_list:
                listBookmarks();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("page", pager.getCurrentItem());
    }

    @Override
    protected void onPause() {
        super.onPause();

        gallery.setProgress(pager.getCurrentItem());
        gallery.setLastread(new Date(System.currentTimeMillis()));
        galleryDao.update(gallery);
    }

    public void onEvent(PhotoDialogEvent event) {
        if (event.getId() == gallery.getId()) {
            pager.setCurrentItem(event.getPage(), false);
        }
    }

    private void onBackClick() {
        Intent intent = new Intent(this, GalleryActivity.class);
        Bundle args = new Bundle();

        args.putLong("id", gallery.getId());
        intent.putExtras(args);
        NavUtils.navigateUpTo(this, intent);
    }

    private class PhotoPagerAdapter extends FragmentStatePagerAdapter {
        public PhotoPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new PhotoFragment();
            Bundle args = new Bundle();

            args.putLong("id", gallery.getId());
            args.putInt("page", i + 1);
            args.putString("title", gallery.getTitle());
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public int getCount() {
            return gallery.getCount();
        }
    }

    private void listBookmarks() {
        DialogFragment dialog = new PhotoBookmarkDialog();
        Bundle args = new Bundle();

        args.putLong("id", gallery.getId());

        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), "bookmark");
    }
/*
    private void toggleUIVisibility() {
        View decorView = getWindow().getDecorView();
        int uiOptions = decorView.getSystemUiVisibility();
        int newUiOptions = uiOptions;

        if (Build.VERSION.SDK_INT >= 14) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        if (Build.VERSION.SDK_INT >= 16) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }

        if (Build.VERSION.SDK_INT >= 18) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        decorView.setSystemUiVisibility(newUiOptions);
    }*/
}
