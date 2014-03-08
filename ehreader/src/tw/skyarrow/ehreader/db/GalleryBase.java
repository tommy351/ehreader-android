package tw.skyarrow.ehreader.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import java.io.File;

import tw.skyarrow.ehreader.Constant;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.util.DownloadHelper;

/**
 * Created by SkyArrow on 2014/2/4.
 */
public abstract class GalleryBase {
    abstract Long getId();
    abstract String getToken();
    abstract Integer getCategory();
    abstract void setCategory(Integer category);
    abstract String getTitle();
    abstract String getSubtitle();

    public int getCategoryResource() {
        switch (getCategory()) {
            case 1:
                return R.string.category_doujinshi;
            case 2:
                return R.string.category_manga;
            case 3:
                return R.string.category_artistcg;
            case 4:
                return R.string.category_gamecg;
            case 5:
                return R.string.category_western;
            case 6:
                return R.string.category_non_h;
            case 7:
                return R.string.category_imageset;
            case 8:
                return R.string.category_cosplay;
            case 9:
                return R.string.category_asianporn;
            default:
                return R.string.category_misc;
        }
    }

    public void setCategory(String category) {
        if (category.equals("Doujinshi")){
            setCategory(1);
        } else if (category.equals("Manga")){
            setCategory(2);
        } else if (category.equals("Artist CG Sets")){
            setCategory(3);
        } else if (category.equals("Game CG Sets")){
            setCategory(4);
        } else if (category.equals("Western")){
            setCategory(5);
        } else if (category.equals("Non-H")){
            setCategory(6);
        } else if (category.equals("Image Sets")){
            setCategory(7);
        } else if (category.equals("Cosplay")){
            setCategory(8);
        } else if (category.equals("Asian Porn")){
            setCategory(9);
        } else {
            setCategory(10);
        }
    }

    public Uri getUri() {
        return getUri(0, false);
    }

    public Uri getUri(int page) {
        return getUri(page, false);
    }

    public Uri getUri(boolean ex) {
        return getUri(0, ex);
    }

    public Uri getUri(int page, boolean ex) {
        String base = ex ? Constant.GALLERY_URL_EX : Constant.GALLERY_URL;
        String url = String.format(base, getId(), getToken());
        Uri.Builder builder = Uri.parse(url).buildUpon();

        builder.appendQueryParameter("p", Integer.toString(page));

        return builder.build();
    }

    public String getUrl() {
        return getUrl(0, false);
    }

    public String getUrl(int page) {
        return getUrl(page, false);
    }

    public String getUrl(boolean ex) {
        return getUrl(0, ex);
    }

    public String getUrl(int page, boolean ex) {
        return getUri(page, ex).toString();
    }

    public File getFolder() {
        return new File(DownloadHelper.getFolder(), Long.toString(getId()));
    }

    public String[] getTitles(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean displayJapaneseTitle = preferences.getBoolean(context.getString(R.string.pref_japanese_title),
                context.getResources().getBoolean(R.bool.pref_japanese_title_default));
        String title = getTitle();
        String subtitle = getSubtitle();

        if (displayJapaneseTitle && !subtitle.isEmpty()) {
            title = getSubtitle();
            subtitle = getTitle();
        }

        String[] arr = {title, subtitle};

        return arr;
    }
}
