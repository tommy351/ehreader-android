package tw.skyarrow.ehreader.util;

import android.webkit.MimeTypeMap;

import java.io.File;

/**
 * Created by SkyArrow on 2014/1/31.
 */
public class FileInfoHelper {
    private static final String[] UNITS = {"K", "M", "G", "T", "P", "E", "Z", "Y"};

    public static String toBits(long i) {
        double n = i * 8f;
        int unit = -1;

        while (n > 1024) {
            n /= 1024;
            unit++;
        }

        return buildString("b", n, unit);
    }

    public static String toBytes(long i) {
        double n = (double) i;
        int unit = -1;

        while (n > 1024) {
            n /= 1024;
            unit++;
        }

        return buildString("B", n, unit);
    }

    private static String buildString(String suffix, double i, int unit) {
        return String.format("%.2f %s" + suffix, i, unit < 0 ? "" : UNITS[unit]);
    }

    public static String getExtension(File file) {
        return getExtension(file.getAbsolutePath());
    }

    public static String getExtension(String path) {
        return MimeTypeMap.getFileExtensionFromUrl(path);
    }

    public static String getMimeType(File file) {
        return getMimeType(file.getAbsolutePath());
    }

    public static String getMimeType(String path) {
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String extension = getExtension(path);

        return mime.getMimeTypeFromExtension(extension);
    }
}
