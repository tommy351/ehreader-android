package tw.skyarrow.ehreader.event;

import android.os.Bundle;

import tw.skyarrow.ehreader.db.Gallery;

/**
 * Created by SkyArrow on 2014/1/28.
 */
public class GalleryDownloadEvent {
    private int code;
    private Gallery gallery;
    private Object extra;

    public GalleryDownloadEvent(int code, Gallery gallery) {
        this(code, gallery, null);
    }

    public GalleryDownloadEvent(int code, Gallery gallery, Object extra) {
        this.code = code;
        this.gallery = gallery;
        this.extra = extra;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Gallery getGallery() {
        return gallery;
    }

    public void setGallery(Gallery gallery) {
        this.gallery = gallery;
    }

    public Object getExtra() {
        return extra;
    }

    public void setExtra(Object extra) {
        this.extra = extra;
    }
}
