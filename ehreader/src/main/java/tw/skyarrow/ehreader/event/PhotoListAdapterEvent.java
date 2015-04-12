package tw.skyarrow.ehreader.event;

public class PhotoListAdapterEvent {
    public static final int ACTION_NEED_SRC = 1;

    private int position;
    private int action;

    public PhotoListAdapterEvent(int action, int position) {
        this.action = action;
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }
}
