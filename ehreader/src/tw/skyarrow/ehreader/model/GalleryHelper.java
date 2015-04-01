package tw.skyarrow.ehreader.model;

import android.net.Uri;

import org.json.JSONArray;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GalleryHelper {
    public static final Pattern pGalleryURL = Pattern.compile("<a href=\"http://(g\\.e-|ex)hentai\\.org/g/(\\d+)/(\\w+)/\" onmouseover");

    public static String getIndexUrl(String base, int page){
        Uri.Builder b = Uri.parse(base).buildUpon();
        b.appendQueryParameter("page", Integer.toString(page));
        return b.build().toString();
    }

    public static JSONArray findIdInIndex(String html){
        JSONArray list = new JSONArray();
        Matcher matcher = pGalleryURL.matcher(html);

        while (matcher.find()){
            JSONArray arr = new JSONArray();
            int id = Integer.parseInt(matcher.group(2), 10);
            String token = matcher.group(3);

            arr.put(id);
            arr.put(token);
            list.put(arr);
        }

        return list;
    }
}
