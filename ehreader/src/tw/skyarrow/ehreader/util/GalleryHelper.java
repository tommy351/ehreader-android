package tw.skyarrow.ehreader.util;

import android.net.Uri;

public class GalleryHelper {
    public static String getIndexUrl(String base, int page){
        Uri.Builder b = Uri.parse(base).buildUpon();
        b.appendQueryParameter("page", Integer.toString(page));
        return b.build().toString();
    }
}
