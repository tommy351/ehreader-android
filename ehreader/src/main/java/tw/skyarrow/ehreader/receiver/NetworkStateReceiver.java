package tw.skyarrow.ehreader.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.util.DownloadHelper;
import tw.skyarrow.ehreader.util.NetworkHelper;

/**
 * Created by SkyArrow on 2014/2/4.
 */
public class NetworkStateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NetworkHelper network = new NetworkHelper(context);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        DownloadHelper downloadHelper = new DownloadHelper(context);
        boolean autoDownload = preferences.getBoolean(context.getString(R.string.pref_auto_download), true);
        boolean downloadOverWifi = preferences.getBoolean(context.getString(R.string.pref_download_over_wifi), true);
        boolean networkAvailable = false;

        if (downloadOverWifi && network.isWifiAvailable()) {
            networkAvailable = true;
        } else if (network.isAvailable()) {
            networkAvailable = true;
        }

        if (networkAvailable) {
            if (autoDownload) downloadHelper.startAllDownload();
        } else {
            downloadHelper.pauseAllDownload();
        }
    }
}
