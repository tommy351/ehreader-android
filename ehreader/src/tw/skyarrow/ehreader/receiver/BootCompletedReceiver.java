package tw.skyarrow.ehreader.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import tw.skyarrow.ehreader.util.UpdateHelper;

/**
 * Created by SkyArrow on 2014/2/28.
 */
public class BootCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        UpdateHelper updateHelper = new UpdateHelper(context);

        updateHelper.setupAlarm();
    }
}
