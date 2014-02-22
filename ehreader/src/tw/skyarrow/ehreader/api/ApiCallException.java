package tw.skyarrow.ehreader.api;

import org.apache.http.HttpResponse;

/**
 * Created by SkyArrow on 2014/2/19.
 */
public class ApiCallException extends RuntimeException {
    private int code = 0;
    private String url;
    private HttpResponse response;

    public ApiCallException(int code) {
        this.code = code;
    }

    public ApiCallException(int code, String url, HttpResponse response) {
        this.code = code;
        this.url = url;
        this.response = response;
    }

    public ApiCallException(int code, String detailMessage) {
        super(detailMessage);
        this.code = code;
    }

    public ApiCallException(int code, Throwable throwable) {
        super(throwable);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public String getUrl() {
        return url;
    }

    public HttpResponse getResponse() {
        return response;
    }
}
