package tw.skyarrow.ehreader.event;

/**
 * Created by SkyArrow on 2014/2/28.
 */
public class PhotoDownloadEvent {
    private long id;
    private boolean downloaded;

    public PhotoDownloadEvent(long id, boolean downloaded) {
        this.id = id;
        this.downloaded = downloaded;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isDownloaded() {
        return downloaded;
    }

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }
}
