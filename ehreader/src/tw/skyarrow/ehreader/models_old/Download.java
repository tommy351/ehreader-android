package tw.skyarrow.ehreader.models_old;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class Download extends RealmObject {
    @Ignore public static final int STATUS_DOWNLOADING = 0;
    @Ignore public static final int STATUS_PENDING = 1;
    @Ignore public static final int STATUS_PAUSED = 2;
    @Ignore public static final int STATUS_SUCCESS = 3;
    @Ignore public static final int STATUS_ERROR = 4;

    @PrimaryKey
    private int id;
    private Date created;
    private int status;
    private int progress;
    private Gallery gallery;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public Gallery getGallery() {
        return gallery;
    }

    public void setGallery(Gallery gallery) {
        this.gallery = gallery;
    }
}
