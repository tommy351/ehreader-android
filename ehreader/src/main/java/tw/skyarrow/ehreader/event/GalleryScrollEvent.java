package tw.skyarrow.ehreader.event;

public class GalleryScrollEvent {
    private int position;

    public GalleryScrollEvent(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
