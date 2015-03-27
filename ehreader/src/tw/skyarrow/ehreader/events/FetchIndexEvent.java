package tw.skyarrow.ehreader.events;

import java.util.List;

import tw.skyarrow.ehreader.models_old.Gallery;

public class FetchIndexEvent {
    public static final int EVENT_SUCCESS = 0;
    public static final int EVENT_FAILED = 1;

    private int event;
    private String url;
    private List<Gallery> galleryList;

    public FetchIndexEvent(int event, String url) {
        this.event = event;
        this.url = url;
    }

    public FetchIndexEvent(int event, String url, List<Gallery> galleryList) {
        this.event = event;
        this.url = url;
        this.galleryList = galleryList;
    }

    public int getEvent() {
        return event;
    }

    public void setEvent(int event) {
        this.event = event;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<Gallery> getGalleryList() {
        return galleryList;
    }

    public void setGalleryList(List<Gallery> galleryList) {
        this.galleryList = galleryList;
    }
}
