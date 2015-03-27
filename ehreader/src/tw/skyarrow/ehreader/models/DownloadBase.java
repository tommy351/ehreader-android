package tw.skyarrow.ehreader.models;

import io.realm.annotations.Ignore;

public class DownloadBase {
    public static final int STATUS_DOWNLOADING = 0;
    public static final int STATUS_PENDING = 1;
    public static final int STATUS_PAUSED = 2;
    public static final int STATUS_SUCCESS = 3;
    public static final int STATUS_ERROR = 4;
}
