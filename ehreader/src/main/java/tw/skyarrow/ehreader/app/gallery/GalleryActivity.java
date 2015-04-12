package tw.skyarrow.ehreader.app.gallery;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.event.GalleryScrollEvent;
import tw.skyarrow.ehreader.util.ActionBarHelper;

public class GalleryActivity extends ActionBarActivity {
    public static final String EXTRA_ID = "id";
    public static final String EXTRA_TOKEN = "token";

    private static final Pattern pGalleryURL = Pattern.compile("http://(?:g\\.e-|ex)hentai\\.org/g/(\\d+)/(\\w+)");

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    @InjectView(R.id.toolbar_container)
    View mToolbarContainer;

    private Drawable mToolbarBackground;
    private int mBakcgroundHeight;

    public static Intent newIntent(Context context, long id, String token){
        Intent intent = new Intent(context, GalleryActivity.class);

        intent.putExtra(EXTRA_ID, id);
        intent.putExtra(EXTRA_TOKEN, token);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        ButterKnife.inject(this);

        // Set up toolbar
        mToolbarBackground = mToolbarContainer.getBackground().mutate();
        mBakcgroundHeight = getResources().getDimensionPixelSize(R.dimen.gallery_background_height);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        updateToolbarBackground(0);

        // Attach fragment
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment lastFragment = fm.findFragmentByTag(GalleryFragment.TAG);

        if (lastFragment == null){
            Intent intent = getIntent();
            long galleryId;
            String galleryToken;

            if (Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null){
                String url = intent.getData().toString();
                Matcher matcher = pGalleryURL.matcher(url);

                if (matcher.find()){
                    galleryId = Long.parseLong(matcher.group(1), 10);
                    galleryToken = matcher.group(2);
                } else {
                    // TODO: error handling
                    return;
                }
            } else {
                Bundle args = intent.getExtras();
                galleryId = args.getLong(EXTRA_ID);
                galleryToken = args.getString(EXTRA_TOKEN);
            }

            ft.replace(R.id.frame, GalleryFragment.newInstance(galleryId, galleryToken), GalleryFragment.TAG);
        } else {
            ft.attach(lastFragment);
        }

        ft.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                ActionBarHelper.upNavigation(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    public void onEvent(GalleryScrollEvent event){
        updateToolbarBackground(event.getPosition());
    }

    @SuppressWarnings("deprecation")
    private void updateToolbarBackground(int scrollPosition){
        float ratio = 0;
        int headerHeight = mBakcgroundHeight - mToolbar.getHeight();

        if (scrollPosition > 0 && headerHeight > 0){
            ratio = (float) Math.min(Math.max(scrollPosition, 0), headerHeight) / headerHeight;
        }

        int newAlpha = (int) (ratio * 255);
        mToolbarBackground.setAlpha(newAlpha);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
            mToolbarContainer.setBackground(mToolbarBackground);
        } else {
            mToolbarContainer.setBackgroundDrawable(mToolbarBackground);
        }
    }
}
