package tw.skyarrow.ehreader.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkStateHelper {
    public static ConnectivityManager getConnectivityManager(Context context){
        return (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public static NetworkInfo getActiveNetworkInfo(Context context){
        return getConnectivityManager(context).getActiveNetworkInfo();
    }

    public static boolean isAvailable(Context context){
        NetworkInfo info = getActiveNetworkInfo(context);
        return info != null && info.isConnectedOrConnecting();
    }

    public static boolean isWifiAvailable(Context context){
        NetworkInfo info = getActiveNetworkInfo(context);
        if (info == null || info.isConnectedOrConnecting()) return false;

        switch (info.getType()){
            case ConnectivityManager.TYPE_ETHERNET:
            case ConnectivityManager.TYPE_WIFI:
                return true;

            default:
                return false;
        }
    }
}
