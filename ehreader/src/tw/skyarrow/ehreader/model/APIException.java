package tw.skyarrow.ehreader.model;

public class APIException extends RuntimeException {
    public static final int GALLERY_NOT_FOUND = 1;
    public static final int TOKEN_NOT_FOUND = 2;
    public static final int PHOTO_NOT_FOUND = 3;

    private int code;

    public APIException(int code, String message) {
        super(message);
        this.code = code;
    }

    public APIException(int code){
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
