package tw.skyarrow.ehreader.util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;

public class ActionBarHelper {
    public static void upNavigation(Activity activity) {
        upNavigation(activity, null);
    }

    public static void upNavigation(Activity activity, Bundle args) {
        Intent upIntent = NavUtils.getParentActivityIntent(activity);
        if (args != null) upIntent.putExtras(args);

        if (NavUtils.shouldUpRecreateTask(activity, upIntent)) {
            TaskStackBuilder.create(activity)
                    .addNextIntentWithParentStack(upIntent)
                    .startActivities();
        } else {
            activity.finish();
        }
    }
}
