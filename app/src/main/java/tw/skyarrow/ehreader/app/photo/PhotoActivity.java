package tw.skyarrow.ehreader.app.photo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;

import com.facebook.drawee.backends.pipeline.Fresco;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.dao.query.QueryBuilder;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.app.gallery.GalleryActivity;
import tw.skyarrow.ehreader.model.DaoSession;
import tw.skyarrow.ehreader.model.Gallery;
import tw.skyarrow.ehreader.model.GalleryDao;
import tw.skyarrow.ehreader.model.Photo;
import tw.skyarrow.ehreader.model.PhotoDao;
import tw.skyarrow.ehreader.service.PhotoFetchService;
import tw.skyarrow.ehreader.util.DatabaseHelper;
import tw.skyarrow.ehreader.util.FabricHelper;
import tw.skyarrow.ehreader.util.L;
import tw.skyarrow.ehreader.util.ToolbarHelper;
import tw.skyarrow.ehreader.view.RecyclerViewItemClickListener;

/**
 * Created by SkyArrow on 2015/9/26.
 */
public class PhotoActivity extends AppCompatActivity implements View.OnSystemUiVisibilityChangeListener {
    public static final String GALLERY_ID = "GALLERY_ID";

    private static final boolean IS_JELLY_BEAN = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    private static final boolean IS_KITKAT = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    private static final int UI_HIDE_DELAY = 4000;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @InjectView(R.id.list)
    RecyclerView recyclerView;

    @InjectView(R.id.appbar)
    AppBarLayout appBar;

    @InjectView(R.id.seekbar)
    SeekBar seekBar;

    private long galleryId;
    private DatabaseHelper dbHelper;
    private GalleryDao galleryDao;
    private PhotoDao photoDao;
    private Gallery gallery;
    private PhotoListAdapter listAdapter;
    private Map<Integer, Photo> photoMap;
    private View decorView;
    private Handler systemUIHandler;
    private LinearLayoutManager layoutManager;
    private CompositeSubscription subscriptions;
    private BehaviorSubject<Integer> progressSubject;

    public static Intent intent(Context context, long galleryId){
        Intent intent = new Intent(context, PhotoActivity.class);
        Bundle args = bundle(galleryId);

        intent.putExtras(args);

        return intent;
    }

    public static Bundle bundle(long galleryId){
        Bundle bundle = new Bundle();

        bundle.putLong(GALLERY_ID, galleryId);

        return bundle;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FabricHelper.setupFabric(this);
        Fresco.initialize(this);
        setContentView(R.layout.activity_photo);
        ButterKnife.inject(this);

        Intent intent = getIntent();
        galleryId = intent.getLongExtra(GALLERY_ID, 0);
        subscriptions = new CompositeSubscription();

        dbHelper = DatabaseHelper.get(this);
        DaoSession daoSession = dbHelper.open();
        galleryDao = daoSession.getGalleryDao();
        photoDao = daoSession.getPhotoDao();
        progressSubject = BehaviorSubject.create();

        setupPhotoList();
        setupActionBar();
        setupSeekBar();
        subscribePhotoFetch();

        setListPosition(0);
        progressSubject.onNext(getListPosition());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                ToolbarHelper.upNavigation(this, GalleryActivity.bundle(galleryId));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        // Save progress to database
        gallery.setProgress(getListPosition() + 1);
        gallery.setLastread(new Date(System.currentTimeMillis()));
        galleryDao.updateInTx(gallery);

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        subscriptions.unsubscribe();
        dbHelper.close();
        super.onDestroy();
    }

    private void setupActionBar(){
        // Set up action bar
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Set system UI visibility listener
        decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(this);
        systemUIHandler = new SystemUIHandler(this);

        // Subscribe to progress subject
        Subscription subscription = progressSubject
                .distinctUntilChanged()
                .subscribe(progress -> {
                    updateActionBarTitle(progress);
                });

        subscriptions.add(subscription);
    }

    private void setupPhotoList(){
        // Load gallery
        gallery = galleryDao.load(galleryId);
        photoMap = new HashMap<>();

        // Load photos
        QueryBuilder<Photo> qb = photoDao.queryBuilder();
        qb.where(PhotoDao.Properties.GalleryId.eq(galleryId));
        List<Photo> photoList = qb.list();

        for (Photo photo : photoList){
            photoMap.put(photo.getPage(), photo);
        }

        // Set up recycler view
        layoutManager = new LinearLayoutManager(this);
        listAdapter = new PhotoListAdapter(this, gallery, photoMap);
        listAdapter.setHasStableIds(true);
        recyclerView.setAdapter(listAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        // Subscribe to list adapter
        Subscription subscription = listAdapter.getEventBus()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> {
                    Photo photo = event.photo;
                    photo.setInvalid(true);
                    photoDao.updateInTx(photo);
                    photoMap.put(photo.getPage(), photo);
                    listAdapter.notifyItemChanged(photo.getPage() - 1);
                }, L::e);

        subscriptions.add(subscription);

        // Toggle System UI on item click
        recyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(this, new RecyclerViewItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                toggleUIVisibility();
            }

            @Override
            public void onItemLongPress(View view, int position) {

            }
        }));

        // Listen to scroll event
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                progressSubject.onNext(getListPosition());
            }
        });
    }

    private void setupSeekBar(){
        seekBar.setMax(gallery.getCount() - 1);

        // Listen to change event
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressSubject.onNext(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setListPosition(seekBar.getProgress());
            }
        });

        Subscription subscription = progressSubject
                .distinctUntilChanged()
                .subscribe(seekBar::setProgress);

        subscriptions.add(subscription);
    }

    private void subscribePhotoFetch(){
        // Update photo after photo is fetched
        Subscription subscription = PhotoFetchService.getBus()
                .filter(photo -> photo.getGalleryId() == galleryId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(photo -> {
                    L.d("Received PhotoFetchService event: %d - %d", photo.getGalleryId(), photo.getPage());

                    photoMap.put(photo.getPage(), photo);
                    listAdapter.notifyDataSetChanged();
                }, L::e);

        subscriptions.add(subscription);
    }

    @Override
    public void onSystemUiVisibilityChange(int visibility) {
        appBar.setVisibility(visibility);
        seekBar.setVisibility(visibility);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus){
            delayedHideSystemUI();
        } else {
            cancelHideSystemUI();
        }
    }

    @SuppressLint("NewApi")
    private void hideSystemUI(){
        int uiOptions = View.SYSTEM_UI_FLAG_LOW_PROFILE;

        if (IS_JELLY_BEAN){
            uiOptions |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        if (IS_KITKAT){
            uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE;
        }

        decorView.setSystemUiVisibility(uiOptions);
    }

    @SuppressLint("NewApi")
    private void showSystemUI(){
        int uiOptions = 0;

        if (IS_JELLY_BEAN){
            uiOptions |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }

        if (IS_KITKAT){
            uiOptions |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        }

        decorView.setSystemUiVisibility(uiOptions);
    }

    private void delayedHideSystemUI(){
        cancelHideSystemUI();
        systemUIHandler.sendEmptyMessageDelayed(0, UI_HIDE_DELAY);
    }

    private void cancelHideSystemUI(){
        systemUIHandler.removeMessages(0);
    }

    @SuppressLint("NewApi")
    private boolean isUIVisible(){
        if (IS_JELLY_BEAN){
            return (decorView.getSystemUiVisibility() & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0;
        } else {
            return (getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) == 0;
        }
    }

    private void toggleUIVisibility(){
        if (isUIVisible()){
            hideSystemUI();
        } else {
            showSystemUI();
        }
    }

    private int getListPosition(){
        return layoutManager.findFirstVisibleItemPosition();
    }

    private void setListPosition(int position){
        layoutManager.scrollToPosition(position);
    }

    private void updateActionBarTitle(int position){
        getSupportActionBar().setTitle(String.format("%d / %d", position + 1, gallery.getCount()));
    }

    private static class SystemUIHandler extends Handler {
        private final WeakReference<PhotoActivity> activityRef;

        private SystemUIHandler(PhotoActivity activity){
            this.activityRef = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            PhotoActivity activity = activityRef.get();
            if (activity == null) return;

            activity.hideSystemUI();
        }
    }
}
