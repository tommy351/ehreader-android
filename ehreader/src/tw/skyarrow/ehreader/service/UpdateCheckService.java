package tw.skyarrow.ehreader.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.github.zafarkhaja.semver.Version;

import de.greenrobot.event.EventBus;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.event.UpdateCheckEvent;
import tw.skyarrow.ehreader.util.UpdateHelper;

/**
 * Created by SkyArrow on 2014/2/21.
 */
public class UpdateCheckService extends Service {
    public static final String TAG = "UpdateCheckService";

    public static final String ACTION_CHECK_UPDATE = "ACTION_CHECK_UPDATE";
    public static final String EXTRA_NOTIFICATION = "notification";

    public static final int EVENT_LATEST = 0;
    public static final int EVENT_AVAILABLE = 1;
    public static final int EVENT_ERROR = 2;

    private UpdateHelper updateHelper;
    private NotificationManager nm;
    private SharedPreferences preferences;
    private EventBus bus;

    @Override
    public void onCreate() {
        super.onCreate();

        updateHelper = new UpdateHelper(this);
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        bus = EventBus.getDefault();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();

        if (action != null && action.equals(ACTION_CHECK_UPDATE)) {
            boolean showNotification = intent.getBooleanExtra(EXTRA_NOTIFICATION, false);
            new Thread(new UpdateCheckRunnable(showNotification)).start();
        }

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final class UpdateCheckRunnable implements Runnable {
        private boolean showNotification;

        public UpdateCheckRunnable(boolean showNotification) {
            this.showNotification = showNotification;
        }

        @Override
        public void run() {
            SharedPreferences.Editor editor = preferences.edit();
            try {
                Version latestVer = updateHelper.getLatestVersion();

                editor.putLong(getString(R.string.pref_update_checked_at), System.currentTimeMillis());
                editor.putBoolean(getString(R.string.pref_update_is_interrupted), false);

                if (updateHelper.compare(latestVer)) {
                    if (showNotification) {
                        sendNotification(latestVer);
                    }

                    bus.post(new UpdateCheckEvent(EVENT_AVAILABLE, latestVer));
                } else {
                    bus.post(new UpdateCheckEvent(EVENT_LATEST, latestVer));
                }
            } catch (Exception e) {
                e.printStackTrace();
                editor.putBoolean(getString(R.string.pref_update_is_interrupted), true);
                bus.post(new UpdateCheckEvent(EVENT_ERROR, null));
            } finally {
                editor.commit();
                updateHelper.setupAlarm();
                stopSelf();
            }
        }
    }

    private void sendNotification(Version version) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        intent.setData(Uri.parse(UpdateHelper.getDownloadUrl(version)));

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentText(getString(R.string.notification_update_title))
                .setSmallIcon(R.drawable.ic_notification_info)
                .setContentText(String.format(getString(R.string.notification_update_msg), version.toString()))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        nm.notify(TAG, 0, builder.build());
    }
}
