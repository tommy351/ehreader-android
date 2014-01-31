package tw.skyarrow.ehreader.event;

import tw.skyarrow.ehreader.db.Photo;

/**
 * Created by SkyArrow on 2014/1/29.
 */
public class PhotoInfoEvent {
    Photo photo;

    public PhotoInfoEvent(Photo photo) {
        this.photo = photo;
    }

    public Photo getPhoto() {
        return photo;
    }

    public void setPhoto(Photo photo) {
        this.photo = photo;
    }
}
