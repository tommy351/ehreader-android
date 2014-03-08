package tw.skyarrow.ehreader.event;

import tw.skyarrow.ehreader.api.ApiCallException;
import tw.skyarrow.ehreader.db.Photo;

/**
 * Created by SkyArrow on 2014/1/29.
 */
public class PhotoInfoEvent {
    private Photo photo;
    private long galleryId;
    private int page;
    private ApiCallException exception;

    public PhotoInfoEvent(long galleryId, int page, Photo photo) {
        this.galleryId = galleryId;
        this.page = page;
        this.photo = photo;
    }

    public PhotoInfoEvent(long galleryId, int page, ApiCallException exception) {
        this.galleryId = galleryId;
        this.page = page;
        this.exception = exception;
    }

    public Photo getPhoto() {
        return photo;
    }

    public void setPhoto(Photo photo) {
        this.photo = photo;
    }

    public long getGalleryId() {
        return galleryId;
    }

    public void setGalleryId(long galleryId) {
        this.galleryId = galleryId;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public ApiCallException getException() {
        return exception;
    }

    public void setException(ApiCallException exception) {
        this.exception = exception;
    }
}
