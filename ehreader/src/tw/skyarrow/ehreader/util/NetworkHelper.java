package tw.skyarrow.ehreader.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by SkyArrow on 2014/1/26.
 */
public class NetworkHelper {
    private Context context;
    private ConnectivityManager cm;

    private static NetworkHelper instance = null;

    private NetworkHelper(Context context) {
        this.context = context;
        this.cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public static NetworkHelper getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkHelper(context.getApplicationContext());
        }

        return instance;
    }

    public NetworkInfo getNetworkInfo() {
        return cm.getActiveNetworkInfo();
    }

    public boolean isAvailable() {
        NetworkInfo netInfo = getNetworkInfo();

        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public boolean isWifiAvailable() {
        NetworkInfo netInfo = getNetworkInfo();

        return netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }
}
