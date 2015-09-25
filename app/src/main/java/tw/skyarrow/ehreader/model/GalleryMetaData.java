package tw.skyarrow.ehreader.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by SkyArrow on 2015/9/24.
 */
public class GalleryMetaData {
    @SerializedName("gid")
    private long id;
    private String token;
    private String archiverKey;
    private String title;
    private String titleJpn;
    private String category;
    private String thumb;
    private String uploader;
    private String posted;
    private String filecount;
    private long filesize;
    private boolean expunged;
    private String rating;
    private String torrentcount;
    private String[] tags;

    public String getArchiverKey() {
        return archiverKey;
    }

    public String getCategory() {
        return category;
    }

    public boolean isExpunged() {
        return expunged;
    }

    public int getFileCount() {
        return Integer.valueOf(filecount);
    }

    public long getFileSize() {
        return filesize;
    }

    public long getId() {
        return id;
    }

    public Date getPosted() {
        return new Date(Integer.valueOf(posted) * 1000);
    }

    public float getRating() {
        return Float.valueOf(rating);
    }

    public String[] getTags() {
        return tags;
    }

    public String getThumb() {
        return thumb;
    }

    public String getTitle() {
        return title;
    }

    public String getTitleJpn() {
        return titleJpn;
    }

    public String getToken() {
        return token;
    }

    public int getTorrentCount() {
        return Integer.valueOf(torrentcount);
    }

    public String getUploader() {
        return uploader;
    }
}
