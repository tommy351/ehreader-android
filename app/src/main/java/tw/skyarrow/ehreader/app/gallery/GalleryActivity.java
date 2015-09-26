package tw.skyarrow.ehreader.app.gallery;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.model.DaoSession;
import tw.skyarrow.ehreader.model.Gallery;
import tw.skyarrow.ehreader.model.GalleryDao;
import tw.skyarrow.ehreader.util.DatabaseHelper;
import tw.skyarrow.ehreader.util.FabricHelper;
import tw.skyarrow.ehreader.util.ToolbarHelper;

/**
 * Created by SkyArrow on 2015/9/26.
 */
public class GalleryActivity extends AppCompatActivity {
    public static final String GALLERY_ID = "GALLERY_ID";

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @InjectView(R.id.tab_layout)
    TabLayout tabLayout;

    @InjectView(R.id.view_pager)
    ViewPager viewPager;

    @InjectView(R.id.cover)
    SimpleDraweeView cover;

    @InjectView(R.id.coordinator)
    CoordinatorLayout coordinatorLayout;

    private long galleryId;
    private Gallery gallery;
    private DatabaseHelper dbHelper;
    private GalleryDao galleryDao;

    public static Intent intent(Context context, long galleryId) {
        Intent intent = new Intent(context, GalleryActivity.class);

        intent.putExtra(GALLERY_ID, galleryId);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FabricHelper.setupFabric(this);
        Fresco.initialize(this);
        setContentView(R.layout.activity_gallery);
        ButterKnife.inject(this);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        Intent intent = getIntent();
        galleryId = intent.getLongExtra(GALLERY_ID, 0);
        dbHelper = DatabaseHelper.get(this);
        DaoSession daoSession = dbHelper.open();
        galleryDao = daoSession.getGalleryDao();
        gallery = galleryDao.load(galleryId);

        viewPager.setAdapter(new GalleryPagerAdapter(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);
        cover.setImageURI(Uri.parse(gallery.getThumbnail()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (gallery == null) return false;

        getMenuInflater().inflate(R.menu.gallery, menu);

        MenuItem addFavorite = menu.findItem(R.id.action_add_to_favorites);
        MenuItem removeFavorite = menu.findItem(R.id.action_remove_from_favorites);

        if (gallery.getStarred() != null && gallery.getStarred()){
            addFavorite.setVisible(false);
        } else {
            removeFavorite.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ToolbarHelper.upNavigation(this);
                return true;

            case R.id.action_add_to_favorites:
                setGalleryStarred(true);
                return true;

            case R.id.action_remove_from_favorites:
                setGalleryStarred(false);
                return true;

            case R.id.action_share:
                shareGallery();
                return true;

            case R.id.action_download:
                downloadGallery();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }

    private class GalleryPagerAdapter extends FragmentPagerAdapter {
        private final String tabTitles[] = new String[]{"About", "Comments"};

        public GalleryPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return GalleryAboutFragment.create(galleryId);

                case 1:
                    return GalleryCommentFragment.create(galleryId);
            }

            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }
    }

    private void setGalleryStarred(boolean starred){
        int message = starred ? R.string.favorites_added : R.string.favorites_removed;

        gallery.setStarred(starred);
        galleryDao.updateInTx(gallery);
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG).show();
        invalidateOptionsMenu();
    }

    private void shareGallery(){
        Intent intent = new Intent(Intent.ACTION_SEND);

        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, gallery.getTitle() + " " + gallery.getUrl());

        startActivity(Intent.createChooser(intent, getString(R.string.action_share)));
    }

    private void downloadGallery(){
        //
    }
}
