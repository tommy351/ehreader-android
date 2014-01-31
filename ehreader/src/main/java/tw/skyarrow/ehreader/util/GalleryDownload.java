package tw.skyarrow.ehreader.util;

import tw.skyarrow.ehreader.db.Gallery;

/**
 * Created by SkyArrow on 2014/1/31.
 */
public class GalleryDownload {
    private int downloadProgress;
    private Gallery gallery;

    public GalleryDownload(Gallery gallery, int downloadProgress) {
        this.gallery = gallery;
        this.downloadProgress = downloadProgress;
    }

    public Gallery getGallery() {
        return gallery;
    }

    public void setGallery(Gallery gallery) {
        this.gallery = gallery;
    }

    public int getDownloadProgress() {
        return downloadProgress;
    }

    public void setDownloadProgress(int downloadProgress) {
        this.downloadProgress = downloadProgress;
    }
}
