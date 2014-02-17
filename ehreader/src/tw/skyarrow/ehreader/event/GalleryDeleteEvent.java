package tw.skyarrow.ehreader.event;

/**
 * Created by SkyArrow on 2014/2/3.
 */
public class GalleryDeleteEvent {
    private long id;

    public GalleryDeleteEvent(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
