package tw.skyarrow.ehreader.app.gallery;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.InjectView;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.util.ActionBarHelper;

public class GalleryActivity extends ActionBarActivity {
    public static final String EXTRA_ID = "id";
    public static final String EXTRA_TOKEN = "token";

    private static final Pattern pGalleryURL = Pattern.compile("http://(?:g\\.e-|ex)hentai\\.org/g/(\\d+)/(\\w+)");

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        ButterKnife.inject(this);

        // Set up toolbar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

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
}
