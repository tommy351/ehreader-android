package tw.skyarrow.ehreader.app.gallery;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import butterknife.ButterKnife;
import butterknife.InjectView;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.util.ActionBarHelper;

public class GalleryActivity extends ActionBarActivity {
    public static final String EXTRA_ID = "id";
    public static final String EXTRA_TOKEN = "token";

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        ButterKnife.inject(this);

        Bundle args = getIntent().getExtras();
        long galleryId = args.getLong(EXTRA_ID);
        String galleryToken = args.getString(EXTRA_TOKEN);

        // Set up toolbar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Attach fragment
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment lastFragment = fm.findFragmentByTag(GalleryFragment.TAG);

        if (lastFragment == null){
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
}
