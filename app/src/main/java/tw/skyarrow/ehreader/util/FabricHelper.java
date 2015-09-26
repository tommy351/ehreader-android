package tw.skyarrow.ehreader.util;

import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.mopub.common.MoPub;

import io.fabric.sdk.android.Fabric;
import tw.skyarrow.ehreader.BuildConfig;

/**
 * Created by SkyArrow on 2015/9/25.
 */
public class FabricHelper {
    public static void setupFabric(Context context) {
        Crashlytics crashlytics = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();

        Fabric.with(context, crashlytics, new MoPub());
    }
}
