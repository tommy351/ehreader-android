package tw.skyarrow.ehreader.service;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by SkyArrow on 2014/1/29.
 */
public class PhotoDownloadService extends IntentService {
    public PhotoDownloadService() {
        super("PhotoDownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //
    }
}
