package tw.skyarrow.ehreader.util;

import android.net.Uri;

import tw.skyarrow.ehreader.db.Gallery;

/**
 * Created by SkyArrow on 2014/1/29.
 */
public class GalleryHelper {
    private static final String BASE_URL = "http://g.e-hentai.org/g/%d/%s/";

    public static String getUrlString(Gallery gallery) {
        return String.format(BASE_URL, gallery.getId(), gallery.getToken());
    }

    public static Uri getUri(Gallery gallery) {
        return Uri.parse(getUrlString(gallery));
    }
}
