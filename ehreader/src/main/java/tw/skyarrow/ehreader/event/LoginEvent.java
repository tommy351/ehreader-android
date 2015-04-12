package tw.skyarrow.ehreader.event;

public class LoginEvent {
    public static final int EVENT_SUCCESS = 1;
    public static final int EVENT_FAILED = 2;
    public static final int EVENT_LOGOUT = 3;

    private int event;

    public LoginEvent(int event) {
        this.event = event;
    }

    public int getEvent() {
        return event;
    }

    public void setEvent(int event) {
        this.event = event;
    }
}
