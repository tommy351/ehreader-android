package tw.skyarrow.ehreader.db;

/**
 * Created by SkyArrow on 2014/2/1.
 */
public abstract class DownloadBase {
    public static final int STATUS_DOWNLOADING = 0;
    public static final int STATUS_PENDING = 1;
    public static final int STATUS_PAUSED = 2;
    public static final int STATUS_SUCCESS = 3;
    public static final int STATUS_ERROR = 4;
}
