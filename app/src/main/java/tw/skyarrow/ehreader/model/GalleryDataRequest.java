package tw.skyarrow.ehreader.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by SkyArrow on 2015/9/24.
 */
public class GalleryDataRequest {
    private final String method;
    @SerializedName("gidlist")
    private GalleryId[] list;

    public GalleryDataRequest(){
        this.method = "gdata";
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
