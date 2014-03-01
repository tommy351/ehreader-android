package tw.skyarrow.ehreader.app.main;

import android.content.res.Configuration;
import android.view.ViewGroup;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

import tw.skyarrow.ehreader.BuildConfig;
import tw.skyarrow.ehreader.R;

/**
 * Created by SkyArrow on 2014/2/28.
 */
public class AdActivity extends MainDrawerActivity {
    private AdView adView;
    private ViewGroup adContainer;

    protected void setupAd() {
        adContainer = (ViewGroup) findViewById(R.id.ad_container);
        String unitId = getString(R.string.admob_unit_id);
        boolean hideInDebugMode = getResources().getBoolean(R.bool.admob_hide_in_debug_mode);

        if (BuildConfig.DEBUG && hideInDebugMode) return;

        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        AdRequest adRequest = new AdRequest();
        adRequest.addTestDevice(AdRequest.TEST_EMULATOR);

        adView = new AdView(this, AdSize.SMART_BANNER, unitId);
        adContainer.addView(adView, lp);
        adView.loadAd(adRequest);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        adContainer.removeView(adView);
        if (adView != null) setupAd();
    }
}
