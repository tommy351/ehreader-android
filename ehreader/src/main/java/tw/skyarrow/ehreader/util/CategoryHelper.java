package tw.skyarrow.ehreader.util;

import tw.skyarrow.ehreader.R;

/**
 * Created by SkyArrow on 2014/1/27.
 */
public class CategoryHelper {
    public static int getResource(int id) {
        switch (id) {
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

    public static int toCategoryId(String category) {
        if (category.equals("Doujinshi")){
            return 1;
        } else if (category.equals("Manga")){
            return 2;
        } else if (category.equals("Artist CG Sets")){
            return 3;
        } else if (category.equals("Game CG Sets")){
            return 4;
        } else if (category.equals("Western")){
            return 5;
        } else if (category.equals("Non-H")){
            return 6;
        } else if (category.equals("Image Sets")){
            return 7;
        } else if (category.equals("Cosplay")){
            return 8;
        } else if (category.equals("Asian Porn")){
            return 9;
        } else {
            return 10;
        }
    }
}
