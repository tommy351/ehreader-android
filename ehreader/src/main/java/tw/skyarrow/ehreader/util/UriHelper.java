package tw.skyarrow.ehreader.util;

import android.net.Uri;
import android.os.Environment;

import java.io.File;

import tw.skyarrow.ehreader.Constant;
import tw.skyarrow.ehreader.db.Gallery;
import tw.skyarrow.ehreader.db.Photo;

/**
 * Created by SkyArrow on 2014/1/29.
 */
public class UriHelper {
    public static String getGalleryUrlString(Gallery gallery) {
        return String.format(Constant.GALLERY_URL, gallery.getId(), gallery.getToken());
    }

    public static String getGalleryUrlString(Gallery gallery, int page) {
        Uri.Builder builder = Uri.parse(getGalleryUrlString(gallery)).buildUpon();

        builder.appendQueryParameter("p", Integer.toString(page));

        return builder.build().toString();
    }

    public static Uri getGalleryUri(Gallery gallery) {
        return Uri.parse(getGalleryUrlString(gallery));
    }

    public static Uri getGalleryUri(Gallery gallery, int page) {
        return Uri.parse(getGalleryUrlString(gallery, page));
    }

    public static String getPhotoUrlString(Photo photo) {
        return String.format(Constant.PHOTO_URL, photo.getToken(), photo.getGalleryId(), photo.getPage());
    }

    public static Uri getPhotoUri(Photo photo) {
        return Uri.parse(getPhotoUrlString(photo));
    }

    public static File getPicFolder() {
        return new File(Environment.getExternalStorageDirectory(), Constant.FOLDER_NAME);
    }

    public static File getGalleryFolder(Gallery gallery) {
        return getGalleryFolder(gallery.getId());
    }

    public static File getGalleryFolder(long id) {
        return new File(getPicFolder(), Long.toString(id));
    }

    public static File getPhotoFile(Photo photo) {
        if (photo.getFilename() == null) return null;

        return new File(getGalleryFolder(photo.getGalleryId()), photo.getFilename());
    }
}
