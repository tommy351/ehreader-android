package tw.skyarrow.ehreader.event;

/**
 * Created by SkyArrow on 2014/2/7.
 */
public class LoginEvent {
    public static final int LOGIN = 0;
    public static final int LOGOUT = 1;

    private int code;

    public LoginEvent(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
