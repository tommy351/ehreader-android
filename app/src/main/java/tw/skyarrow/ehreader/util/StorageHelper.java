package tw.skyarrow.ehreader.util;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by SkyArrow on 2015/9/28.
 */
public class StorageHelper {
    public static File getDownloadDir(Context context) {
        File dir = context.getFilesDir();
        if (dir == null) return null;

        return new File(dir, Environment.DIRECTORY_DOWNLOADS);
    }
}
