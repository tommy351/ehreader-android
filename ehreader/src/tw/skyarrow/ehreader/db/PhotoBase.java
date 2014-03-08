package tw.skyarrow.ehreader.db;

import android.net.Uri;

import java.io.File;

import tw.skyarrow.ehreader.Constant;
import tw.skyarrow.ehreader.util.DownloadHelper;

/**
 * Created by SkyArrow on 2014/2/4.
 */
public abstract class PhotoBase {
    abstract String getToken();
    abstract long getGalleryId();
    abstract int getPage();
    abstract String getFilename();

    public String getUrl() {
        return getUrl(false);
    }

    public String getUrl(boolean ex) {
        String base = ex ? Constant.PHOTO_URL_EX : Constant.PHOTO_URL;

        return String.format(base, getToken(), getGalleryId(), getPage());
    }

    public Uri getUri() {
        return getUri(false);
    }

    public Uri getUri(boolean ex) {
        return Uri.parse(getUrl(ex));
    }

    public File getFile() {
        File galleryFolder = new File(DownloadHelper.getFolder(), Long.toString(getGalleryId()));

        return new File(galleryFolder, getFilename());
    }
}
