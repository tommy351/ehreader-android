package tw.skyarrow.ehreader.app.search;

import android.app.ActionBar;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;

import tw.skyarrow.ehreader.BaseApplication;
import tw.skyarrow.ehreader.Constant;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.app.main.AdActivity;
import tw.skyarrow.ehreader.app.main.MainFragmentWeb;
import tw.skyarrow.ehreader.util.ActionBarHelper;
import tw.skyarrow.ehreader.util.LoginHelper;

/**
 * Created by SkyArrow on 2014/2/2.
 */
public class FilterActivity extends AdActivity {
    public static final String TAG = "FilterActivity";

    public static final String EXTRA_FILTER = "filter";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupDrawer();
        setDrawerIndicatorEnabled(false);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        boolean isLoggedIn = LoginHelper.getInstance(this).isLoggedIn();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment fragment = new MainFragmentWeb();
        Bundle args = getIntent().getExtras();
        Uri.Builder builder = Uri.parse(isLoggedIn ? Constant.BASE_URL_EX : Constant.BASE_URL).buildUpon();
        boolean[] filter = args.getBooleanArray(EXTRA_FILTER);

        for (int i = 0; i < filter.length; i++) {
            String param = filter[i] ? "1" : "0";

            switch (i) {
                case 0:
                    builder.appendQueryParameter("f_doujinshi", param);
                    break;

                case 1:
                    builder.appendQueryParameter("f_manga", param);
                    break;

                case 2:
                    builder.appendQueryParameter("f_artistcg", param);
                    break;

                case 3:
                    builder.appendQueryParameter("f_gamecg", param);
                    break;

                case 4:
                    builder.appendQueryParameter("f_western", param);
                    break;

                case 5:
                    builder.appendQueryParameter("f_non-h", param);
                    break;

                case 6:
                    builder.appendQueryParameter("f_imageset", param);
                    break;

                case 7:
                    builder.appendQueryParameter("f_cosplay", param);
                    break;

                case 8:
                    builder.appendQueryParameter("f_asianporn", param);
                    break;

                case 9:
                    builder.appendQueryParameter("f_misc", param);
                    break;
            }
        }

        builder.appendQueryParameter("f_apply", "Apply Filter");

        args.putString(MainFragmentWeb.EXTRA_BASE, builder.build().toString());
        fragment.setArguments(args);

        ft.replace(R.id.container, fragment);
        ft.commit();
        setupAd();
    }

    @Override
    protected void onStart() {
        super.onStart();

        MapBuilder builder = MapBuilder.createAppView();
        builder.set(Fields.SCREEN_NAME, TAG);

        BaseApplication.getTracker().send(builder.build());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ActionBarHelper.upNavigation(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
