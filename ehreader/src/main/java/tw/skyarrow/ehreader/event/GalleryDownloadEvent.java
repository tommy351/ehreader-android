package tw.skyarrow.ehreader.event;

import android.os.Bundle;

import tw.skyarrow.ehreader.db.Download;
import tw.skyarrow.ehreader.db.Gallery;

/**
 * Created by SkyArrow on 2014/1/28.
 */
public class GalleryDownloadEvent {
    private int code;
    private Download download;

    public GalleryDownloadEvent(int code, Download download) {
        this.code = code;
        this.download = download;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Download getDownload() {
        return download;
    }

    public void setDownload(Download download) {
        this.download = download;
    }
}
