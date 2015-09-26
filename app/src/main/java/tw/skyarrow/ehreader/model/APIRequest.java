package tw.skyarrow.ehreader.model;

/**
 * Created by SkyArrow on 2015/9/26.
 */
public abstract class APIRequest {
    private String method;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
