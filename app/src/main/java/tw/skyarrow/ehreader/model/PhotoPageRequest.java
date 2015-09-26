package tw.skyarrow.ehreader.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by SkyArrow on 2015/9/26.
 */
public class PhotoPageRequest extends APIRequest {
    @SerializedName("gid")
    private long galleryId;

    private int page;

    @SerializedName("imgkey")
    private String photoToken;

    @SerializedName("showkey")
    private String showKey;

    public PhotoPageRequest(){
        setMethod("showpage");
    }

    public long getGalleryId() {
        return galleryId;
    }

    public void setGalleryId(long galleryId) {
        this.galleryId = galleryId;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public String getPhotoToken() {
        return photoToken;
    }

    public void setPhotoToken(String photoToken) {
        this.photoToken = photoToken;
    }

    public String getShowKey() {
        return showKey;
    }

    public void setShowKey(String showKey) {
        this.showKey = showKey;
    }
}
