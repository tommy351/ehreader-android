package tw.skyarrow.ehreader.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by SkyArrow on 2015/9/24.
 */
public class GalleryDataResponse {
    @SerializedName("gmetadata")
    private GalleryMetaData[] data;

    public GalleryMetaData[] getData() {
        return data;
    }
}
