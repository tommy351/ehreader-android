package tw.skyarrow.ehreader.app.photo;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import butterknife.ButterKnife;
import butterknife.InjectView;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.util.ActionBarHelper;

public class PhotoActivity extends ActionBarActivity implements ViewPager.OnPageChangeListener {
    public static final String EXTRA_GALLERY_ID = "gallery_id";
    public static final String EXTRA_PAGE = "page";

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    @InjectView(R.id.pager)
    ViewPager mPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        ButterKnife.inject(this);

        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPager.setOnPageChangeListener(this);
        mPager.setOffscreenPageLimit(3);
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
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        //
    }

    @Override
    public void onPageSelected(int position) {
        //
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        //
    }
}
