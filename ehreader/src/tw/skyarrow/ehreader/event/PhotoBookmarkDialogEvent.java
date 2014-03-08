package tw.skyarrow.ehreader.event;

/**
 * Created by SkyArrow on 2014/3/5.
 */
public class PhotoBookmarkDialogEvent {
    public long galleryId;
    private int page;

    public PhotoBookmarkDialogEvent(long galleryId, int page) {
        this.galleryId = galleryId;
        this.page = page;
    }

    public long getGalleryId() {
        return galleryId;
    }

    public void setGalleryId(int galleryId) {
        this.galleryId = galleryId;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }
}
