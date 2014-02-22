package tw.skyarrow.ehreader.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import tw.skyarrow.ehreader.service.UpdateCheckService;

/**
 * Created by SkyArrow on 2014/2/20.
 */
public class UpdateAlarmReceiver extends BroadcastReceiver {
    public static final String TAG = "UpdateAlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(getIntent(context));
    }

    private Intent getIntent(Context context) {
        Intent intent = new Intent(context, UpdateCheckService.class);

        intent.setAction(UpdateCheckService.ACTION_CHECK_UPDATE);
        intent.putExtra(UpdateCheckService.EXTRA_NOTIFICATION, true);

        return intent;
    }
}
