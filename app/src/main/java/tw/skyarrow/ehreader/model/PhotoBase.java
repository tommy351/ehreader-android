package tw.skyarrow.ehreader.model;

import android.content.Context;
import android.text.TextUtils;

import java.io.File;
import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;
import tw.skyarrow.ehreader.util.StorageHelper;

/**
 * Created by SkyArrow on 2015/9/24.
 */
public abstract class PhotoBase {
    public abstract Long getId();
    public abstract void setId(Long id);

    public abstract int getPage();
    public abstract void setPage(int page);

    public abstract String getToken();
    public abstract void setToken(String token);

    public abstract Boolean getBookmarked();
    public abstract void setBookmarked(Boolean bookmarked);

    public abstract String getFilename();
    public abstract void setFilename(String filename);

    public abstract Integer getWidth();
    public abstract void setWidth(Integer width);

    public abstract Integer getHeight();
    public abstract void setHeight(Integer height);

    public abstract String getSrc();
    public abstract void setSrc(String src);

    public abstract Boolean getDownloaded();
    public abstract void setDownloaded(Boolean downloaded);

    public abstract long getGalleryId();
    public abstract void setGalleryId(long galleryId);

    public abstract Boolean getInvalid();
    public abstract void setInvalid(Boolean invalid);

    public abstract String getRetryId();
    public abstract void setRetryId(String retryId);

    public void setDefaultFields(){
        setBookmarked(false);
        setDownloaded(false);
        setInvalid(false);
    }

    public static Photo findPhoto(PhotoDao photoDao, long galleryId, int photoPage){
        QueryBuilder<Photo> qb = photoDao.queryBuilder();
        qb.where(qb.and(
                PhotoDao.Properties.GalleryId.eq(galleryId),
                PhotoDao.Properties.Page.eq(photoPage)
        ));
        qb.orderDesc(PhotoDao.Properties.Id);
        qb.limit(1);

        List<Photo> list = qb.list();

        if (list != null && list.size() > 0){
            return list.get(0);
        }

        return null;
    }

    public boolean shouldReload() {
        return TextUtils.isEmpty(getSrc()) || getInvalid();
    }

    public File getFile(Context context){
        String src = getSrc();

        if (src == null) return null;

        String filename = src.substring(src.lastIndexOf("/") + 1, src.length());
        File downloadDir = StorageHelper.getDownloadDir(context);

        return new File(downloadDir, getGalleryId() + File.separator + filename);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o instanceof PhotoBase){
            PhotoBase photo = (PhotoBase) o;
            return getId().equals(photo.getId());
        }

        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return getId().intValue();
    }
}
