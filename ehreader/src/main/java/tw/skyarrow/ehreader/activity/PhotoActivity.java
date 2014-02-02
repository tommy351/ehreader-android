package tw.skyarrow.ehreader.activity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.SeekBar;
import android.widget.TextView;

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
import tw.skyarrow.ehreader.event.PhotoDialogEvent;

/**
 * Created by SkyArrow on 2014/1/31.
 */
public class PhotoActivity extends ActionBarActivity {
    @InjectView(R.id.pager)
    ViewPager pager;

    @InjectView(R.id.seekbar)
    SeekBar seekBar;

    @InjectView(R.id.seekbar_area)
    View seekBarArea;

    @InjectView(R.id.hint)
    TextView hintText;

    private SQLiteDatabase db;
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private GalleryDao galleryDao;

    private Gallery gallery;

    private PagerAdapter pagerAdapter;
    private EventBus bus;
    private View decorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_photo);
        ButterKnife.inject(this);

        decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(onSystemUiVisibilityChangeListener);
        showSystemUI();

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
        seekBar.setMax(gallery.getCount() - 1);
        seekBar.setProgress(page);
        seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);

        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
                //
            }

            @Override
            public void onPageSelected(int i) {
                seekBar.setProgress(i);
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

    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            if (pager.getCurrentItem() != i) {
                showHintText(i);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            hintText.setVisibility(View.VISIBLE);
            showHintText(pager.getCurrentItem());
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            hintText.setVisibility(View.GONE);
            pager.setCurrentItem(seekBar.getProgress(), false);
        }
    };

    private void showHintText(int i) {
        int page = i + 1;
        SpannableString sp = new SpannableString(page + " / " + gallery.getCount());
        int pageLength = Integer.toString(page).length();

        sp.setSpan(new StyleSpan(Typeface.BOLD), 0, pageLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        hintText.setText(sp);
    }

    private View.OnSystemUiVisibilityChangeListener onSystemUiVisibilityChangeListener = new View.OnSystemUiVisibilityChangeListener() {
        @Override
        public void onSystemUiVisibilityChange(int visibility) {
            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                // visible
                Animation fadeIn = AnimationUtils.loadAnimation(PhotoActivity.this, R.anim.fade_in);

                seekBarArea.setVisibility(View.VISIBLE);
                seekBarArea.startAnimation(fadeIn);
            } else {
                // invisible
                Animation fadeOut = AnimationUtils.loadAnimation(PhotoActivity.this, R.anim.fade_out);

                seekBarArea.setVisibility(View.GONE);
                seekBarArea.startAnimation(fadeOut);
            }
        }
    };

    public void toggleUIVisibility() {
        if (isSystemUiVisible()) {
            hideSystemUI();
        } else {
            showSystemUI();
        }
    }

    public boolean isSystemUiVisible() {
        return (decorView.getSystemUiVisibility() & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0;
    }

    public void hideSystemUI() {
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LOW_PROFILE;

        decorView.setSystemUiVisibility(uiOptions);
    }

    public void showSystemUI() {
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;

        decorView.setSystemUiVisibility(uiOptions);
    }
}
