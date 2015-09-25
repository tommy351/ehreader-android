package tw.skyarrow.ehreader.model;

/**
 * Created by SkyArrow on 2015/9/24.
 */
public class GalleryId {
    private long id;
    private String token;

    public GalleryId(long id, String token) {
        this.id = id;
        this.token = token;
    }

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
