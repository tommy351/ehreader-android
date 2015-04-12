package tw.skyarrow.ehreader.event;

import tw.skyarrow.ehreader.model.Download;

public class GalleryDownloadEvent {
    private Download download;

    public GalleryDownloadEvent(Download download) {
        this.download = download;
    }

    public Download getDownload() {
        return download;
    }

    public void setDownload(Download download) {
        this.download = download;
    }
}
