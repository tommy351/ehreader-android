package tw.skyarrow.ehreader.model;

import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;
import tw.skyarrow.ehreader.Constant;

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

    public static Photo findPhoto(PhotoDao photoDao, long galleryId, int page){
        QueryBuilder<Photo> qb = photoDao.queryBuilder();
        qb.where(qb.and(
                PhotoDao.Properties.GalleryId.eq(galleryId),
                PhotoDao.Properties.Page.eq(page),
                PhotoDao.Properties.Invalid.notEq(true)
        ));
        qb.orderDesc(PhotoDao.Properties.Id);
        qb.limit(1);

        List<Photo> list = qb.list();

        if (list.size() > 0){
            return list.get(0);
        } else {
            return null;
        }
    }

    public String getURL(){
        return String.format(Constant.PHOTO_URL, getToken(), getGalleryId(), getPage());
    }
}
