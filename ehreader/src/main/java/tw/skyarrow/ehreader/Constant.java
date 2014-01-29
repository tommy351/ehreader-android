package tw.skyarrow.ehreader;

import java.util.regex.Pattern;

/**
 * Created by SkyArrow on 2014/1/25.
 */
public class Constant {
    public static final String DB_NAME = "ehreader.db";
    public static final String API_URL = "http://g.e-hentai.org/api.php";
    public static final String BASE_URL = "http://g.e-hentai.org";
    public static final String BASE_URL_LOFI = "http://lofi.e-hentai.org";
    public static final String GALLERY_URL = "http://g.e-hentai.org/g/%d/%s/?p=%d";
    public static final String GALLERY_URL_LOFI = "http://lofi.e-hentai.org/g/%d/%s/%d";
    public static final String PHOTO_URL = "http://g.e-hentai.org/s/%s/%d-%d";
    public static final String PHOTO_URL_LOFI = "http://lofi.e-hentai.org/s/%s/%d-%d";
    public static final String THUMBNAIL_URL = "http://ehgt.org/t/%s/%s/%s_l.jpg";
    public static final String IMAGE_SEARCH_URL = "http://ul.e-hentai.org/image_lookup.php";
    public static final Pattern GALLERY_URL_PATTERN = Pattern.compile("<a href=\"http://g.e-hentai.org/g/(\\d+)/(\\w+)/\" onmouseover");
}
