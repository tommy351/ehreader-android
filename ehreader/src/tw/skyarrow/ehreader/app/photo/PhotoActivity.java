package tw.skyarrow.ehreader.app.photo;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;

import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import tw.skyarrow.ehreader.BaseApplication;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.api.ApiCallException;
import tw.skyarrow.ehreader.api.ApiErrorCode;
import tw.skyarrow.ehreader.db.DaoMaster;
import tw.skyarrow.ehreader.db.DaoSession;
import tw.skyarrow.ehreader.db.Gallery;
import tw.skyarrow.ehreader.db.GalleryDao;
import tw.skyarrow.ehreader.event.PhotoBookmarkDialogEvent;
import tw.skyarrow.ehreader.event.PhotoInfoEvent;
import tw.skyarrow.ehreader.util.ActionBarHelper;
import tw.skyarrow.ehreader.util.DatabaseHelper;

/**
 * Created by SkyArrow on 2014/1/31.
 */
public class PhotoActivity extends FragmentActivity implements View.OnSystemUiVisibilityChangeListener {
    @InjectView(R.id.pager)
    ViewPager pager;

    @InjectView(R.id.header)
    View headerView;

    @InjectView(R.id.seekbar)
    SeekBar seekBar;

    @InjectView(R.id.seekbar_area)
    View seekBarArea;

    @InjectView(R.id.hint)
    TextView hintText;

    public static final String TAG = "PhotoActivity";

    public static final String EXTRA_GALLERY = "id";
    public static final String EXTRA_PAGE = "page";

    private static final int UI_HIDE_DELAY = 3000;
    private static final int HINT_HIDE_DELAY = 500;

    private GalleryDao galleryDao;
    private SharedPreferences preferences;
    private EventBus bus;

    private Gallery gallery;

    private PagerAdapter pagerAdapter;
    private View decorView;
    private boolean isKitkat = Build.VERSION.SDK_INT >= 19;
    private boolean isVolumeNavEnabled = false;
    private int tmpPage = -1;
    private boolean isErrorDialogShow = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        ButterKnife.inject(this);
        bus = EventBus.getDefault();
        bus.register(this);

        decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(this);

        DatabaseHelper helper = DatabaseHelper.getInstance(this);
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();
        galleryDao = daoSession.getGalleryDao();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        Intent intent = getIntent();
        long galleryId = intent.getLongExtra(EXTRA_GALLERY, 0);
        gallery = galleryDao.load(galleryId);

        if (gallery == null) {
            // TODO error handling
            return;
        }

        final ActionBar actionBar = getActionBar();
        int page;
        final int total = gallery.getCount();

        pagerAdapter = new PhotoPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        actionBar.setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState != null) {
            page = savedInstanceState.getInt(EXTRA_PAGE);
        } else {
            page = intent.getIntExtra(EXTRA_PAGE, gallery.getProgress());
        }

        if (page < 0 || page >= total) page = 0;

        actionBar.setTitle(String.format("%d / %d", page + 1, total));
        setCurrent(page, false);
        seekBar.setMax(total - 1);
        seekBar.setProgress(page);
        seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        if (isKitkat) headerView.setVisibility(View.VISIBLE);

        boolean keepScreenOn = preferences.getBoolean(getString(R.string.pref_keep_screen_on),
                getBoolean(R.bool.pref_keep_screen_on_default));
        isVolumeNavEnabled = preferences.getBoolean(getString(R.string.pref_volume_key_navigation),
                getBoolean(R.bool.pref_volume_key_navigation_default));
        pager.setKeepScreenOn(keepScreenOn);

        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
                //
            }

            @Override
            public void onPageSelected(int i) {
                seekBar.setProgress(i);
                actionBar.setTitle(String.format("%s / %s", i + 1, total));
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                //
            }
        });

        pager.setOffscreenPageLimit(3);

        String orientation = preferences.getString(getString(R.string.pref_screen_orientation),
                getString(R.string.pref_screen_orientation_default));

        if (orientation.equals("landscape")) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else if (orientation.equals("portrait")) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        MapBuilder builder = MapBuilder.createAppView();
        builder.set(Fields.SCREEN_NAME, TAG);

        BaseApplication.getTracker().send(builder.build());
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        hideSystemUI();
        setSeekBarMargin();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bus.unregister(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setSeekBarMargin();
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
        outState.putInt(EXTRA_PAGE, getCurrent());
    }

    @Override
    protected void onPause() {
        super.onPause();

        gallery.setProgress(getCurrent());
        gallery.setLastread(new Date(System.currentTimeMillis()));
        galleryDao.update(gallery);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            delayedHideSystemUI(UI_HIDE_DELAY);
        } else {
            systemUIHandler.removeMessages(0);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (!isVolumeNavEnabled) return super.onKeyDown(keyCode, event);

        switch (keyCode) {
            // Previous page
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (tmpPage < 0) tmpPage = getCurrent();
                if (tmpPage > 0) tmpPage--;

                showHintText(tmpPage);
                return true;

            // Next page
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (tmpPage < 0) tmpPage = getCurrent();
                if (tmpPage < gallery.getCount() - 1) tmpPage++;

                showHintText(tmpPage);
                return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (!isVolumeNavEnabled) return super.onKeyUp(keyCode, event);

        switch (keyCode) {
            // Commit the page transition
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                setCurrent(tmpPage, false);
                tmpPage = -1;
                hideHintText();
                return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    public void onEventMainThread(PhotoInfoEvent event) {
        if (event.getGalleryId() != gallery.getId() || isErrorDialogShow) return;

        ApiCallException exception = event.getException();
        if (exception == null) return;

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle(R.string.photo_error_title)
                .setPositiveButton(R.string.ok, onDialogSubmitClick);

        switch (exception.getCode()) {
            case ApiErrorCode.GALLERY_PINNED:
                isErrorDialogShow = true;
                dialog.setMessage(R.string.photo_error_pinned);
                break;

            case ApiErrorCode.IO_ERROR:
                isErrorDialogShow = true;
                dialog.setMessage(R.string.photo_error_network)
                        .setPositiveButton(R.string.network_config, onDialogNetworkClick)
                        .setNegativeButton(R.string.cancel, null);
                break;

            case ApiErrorCode.TOKEN_OR_PAGE_INVALID:
                isErrorDialogShow = true;
                dialog.setMessage(R.string.photo_error_not_found);
                break;
        }

        if (isErrorDialogShow) {
            dialog.create().show();
        }
    }

    public void onEvent(PhotoBookmarkDialogEvent event) {
        if (event.getGalleryId() != gallery.getId()) return;

        setCurrent(event.getPage() - 1);
    }

    private DialogInterface.OnClickListener onDialogSubmitClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            isErrorDialogShow = false;
        }
    };

    private DialogInterface.OnClickListener onDialogNetworkClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            isErrorDialogShow = false;
            startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
        }
    };

    private boolean getBoolean(int res) {
        return getResources().getBoolean(res);
    }

    private void onBackClick() {
        Bundle args = new Bundle();

        args.putLong("id", gallery.getId());
        ActionBarHelper.upNavigation(this, args);
    }

    public int getCurrent() {
        return pager.getCurrentItem();
    }

    public void setCurrent(int i) {
        setCurrent(i, true);
    }

    public void setCurrent(int i, boolean anim) {
        pager.setCurrentItem(i, anim);
    }

    @Override
    public void onSystemUiVisibilityChange(int i) {
        if (isUiVisible()) {
            showSeekBar();
        } else {
            hideSeekBar();
        }
    }

    private class PhotoPagerAdapter extends FragmentStatePagerAdapter {
        public PhotoPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new PhotoFragment();
            Bundle args = new Bundle();

            args.putLong(PhotoFragment.EXTRA_GALLERY, gallery.getId());
            args.putInt(PhotoFragment.EXTRA_PAGE, i + 1);
            args.putString(PhotoFragment.EXTRA_TITLE, gallery.getTitle());
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

        args.putLong(PhotoBookmarkDialog.EXTRA_GALLERY, gallery.getId());

        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), PhotoBookmarkDialog.TAG);
    }

    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        private boolean isTracking = false;

        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            if (!isTracking) return;

            showHintText(i);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            isTracking = true;

            systemUIHandler.removeMessages(0);
            showHintText(getCurrent());
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            isTracking = false;

            setCurrent(seekBar.getProgress(), false);
            hideHintText();
            delayedHideSystemUI(UI_HIDE_DELAY);
        }
    };

    private void showHintText(int i) {
        int page = i + 1;
        SpannableString sp = new SpannableString(page + " / " + gallery.getCount());
        int pageLength = Integer.toString(page).length();

        sp.setSpan(new StyleSpan(Typeface.BOLD), 0, pageLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        hideHintHandler.removeMessages(0);
        hintText.setVisibility(View.VISIBLE);
        hintText.setText(sp);
    }

    private void hideHintText() {
        hideHintHandler.removeMessages(0);
        hideHintHandler.sendEmptyMessageDelayed(0, HINT_HIDE_DELAY);
    }

    private Handler hideHintHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Animation fadeOut = AnimationUtils.loadAnimation(PhotoActivity.this, R.anim.fade_out);

            hintText.setVisibility(View.GONE);
            hintText.startAnimation(fadeOut);
        }
    };

    public void toggleUIVisibility() {
        if (isUiVisible()) {
            hideSystemUI();
        } else {
            showSystemUI();
        }
    }

    private boolean isUiVisible() {
        if (Build.VERSION.SDK_INT >= 16) {
            return (decorView.getSystemUiVisibility() & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0;
        } else {
            return (getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) == 0;
        }
    }

    public void hideSystemUI() {
        systemUIHandler.removeMessages(0);

        int uiOptions = View.SYSTEM_UI_FLAG_LOW_PROFILE;

        if (Build.VERSION.SDK_INT >= 16) {
            uiOptions |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        if (isKitkat) {
            uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE;
        }

        decorView.setSystemUiVisibility(uiOptions);
        hideSeekBar();
    }
    public void showSystemUI() {
        int uiOptions = 0;

        if (Build.VERSION.SDK_INT >= 16) {
            uiOptions |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }

        if (isKitkat) {
            uiOptions |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        }

        decorView.setSystemUiVisibility(uiOptions);
        showSeekBar();
        delayedHideSystemUI(UI_HIDE_DELAY);
    }

    private void hideSeekBar() {
        Animation fadeOut = AnimationUtils.loadAnimation(PhotoActivity.this, R.anim.fade_out);

        seekBarArea.setVisibility(View.GONE);
        seekBarArea.startAnimation(fadeOut);
        if (isKitkat) headerView.setVisibility(View.GONE);
    }

    private void showSeekBar() {
        Animation fadeIn = AnimationUtils.loadAnimation(PhotoActivity.this, R.anim.fade_in);

        seekBarArea.setVisibility(View.VISIBLE);
        seekBarArea.startAnimation(fadeIn);
        if (isKitkat) headerView.setVisibility(View.VISIBLE);
    }

    private void setSeekBarMargin() {
        if (!isKitkat) return;

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(seekBarArea.getLayoutParams());

        // http://stackoverflow.com/a/20264361
        Resources resources = getResources();
        int navigationBarId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        int navigationBarSize = resources.getDimensionPixelSize(navigationBarId);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            layoutParams.setMargins(0, 0, navigationBarSize, 0);
        } else {
            layoutParams.setMargins(0, 0, 0, navigationBarSize);
        }

        seekBarArea.setLayoutParams(layoutParams);
    }

    private Handler systemUIHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            hideSystemUI();
        }
    };

    private void delayedHideSystemUI(int delay) {
        systemUIHandler.removeMessages(0);
        systemUIHandler.sendEmptyMessageDelayed(0, delay);
    }
}
