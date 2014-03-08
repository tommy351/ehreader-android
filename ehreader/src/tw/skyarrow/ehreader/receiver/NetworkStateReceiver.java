package tw.skyarrow.ehreader.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.util.DownloadHelper;
import tw.skyarrow.ehreader.util.NetworkHelper;
import tw.skyarrow.ehreader.util.UpdateHelper;

/**
 * Created by SkyArrow on 2014/2/4.
 */
public class NetworkStateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NetworkHelper network = NetworkHelper.getInstance(context);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        DownloadHelper downloadHelper = DownloadHelper.getInstance(context);
        UpdateHelper updateHelper = new UpdateHelper(context);

        boolean autoDownload = preferences.getBoolean(context.getString(R.string.pref_auto_download),
                context.getResources().getBoolean(R.bool.pref_auto_download_default));
        boolean downloadOverWifi = preferences.getBoolean(context.getString(R.string.pref_download_over_wifi),
                context.getResources().getBoolean(R.bool.pref_download_over_wifi_default));
        boolean autoUpdateInterrupted = preferences.getBoolean(context.getString(R.string.pref_update_is_interrupted), false);
        boolean networkAvailable = false;

        if (downloadOverWifi && network.isWifiAvailable()) {
            networkAvailable = true;
        } else if (network.isAvailable()) {
            networkAvailable = true;
        }

        if (networkAvailable) {
            if (autoDownload) downloadHelper.startAll();
            if (autoUpdateInterrupted) updateHelper.setupAlarm();
        } else {
            downloadHelper.pauseAll();
        }
    }
}
