package tw.skyarrow.ehreader.model;

import java.util.Date;

public abstract class DownloadBase {
    public static final int STATUS_DOWNLOADING = 0;
    public static final int STATUS_PENDING = 1;
    public static final int STATUS_PAUSED = 2;
    public static final int STATUS_SUCCESS = 3;
    public static final int STATUS_ERROR = 4;

    public abstract Long getId();
    public abstract void setId(Long id);

    public abstract Integer getStatus();
    public abstract void setStatus(Integer status);

    public abstract Integer getProgress();
    public abstract void setProgress(Integer progress);

    public abstract Date getCreated();
    public abstract void setCreated(Date created);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o instanceof DownloadBase){
            DownloadBase download = (DownloadBase) o;
            return getId().equals(download.getId());
        }

        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return getId().intValue();
    }
}
