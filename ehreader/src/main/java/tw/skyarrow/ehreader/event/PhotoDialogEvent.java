package tw.skyarrow.ehreader.event;

/**
 * Created by SkyArrow on 2014/2/2.
 */
public class PhotoDialogEvent {
    private long id;
    private int page;

    public PhotoDialogEvent(long id, int page) {
        this.id = id;
        this.page = page;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }
}
