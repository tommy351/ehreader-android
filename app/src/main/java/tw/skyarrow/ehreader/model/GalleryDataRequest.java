package tw.skyarrow.ehreader.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by SkyArrow on 2015/9/24.
 */
public class GalleryDataRequest extends APIRequest {
    @SerializedName("gidlist")
    private GalleryId[] list;

    public GalleryDataRequest() {
        setMethod("gdata");
    }

    public GalleryDataRequest(GalleryId[] list) {
        this();
        this.list = list;
    }

    public GalleryId[] getList() {
        return list;
    }

    public void setList(GalleryId[] list) {
        this.list = list;
    }
}
