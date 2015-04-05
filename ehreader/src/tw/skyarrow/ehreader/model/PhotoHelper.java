package tw.skyarrow.ehreader.model;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.UnsupportedCharsetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhotoHelper {
    public static final Pattern pVariables = Pattern.compile("var +(\\w+) *= *(.+?);");
    public static final Pattern pImageSrc = Pattern.compile("<img id=\"img\" src=\"([^\"]+)\"");

    public static PhotoPageData readPhotoPage(String html) throws UnsupportedEncodingException {
        PhotoPageData data = new PhotoPageData();
        Matcher matcher = pVariables.matcher(html);

        while (matcher.find()){
            String key = matcher.group(1);
            String value = matcher.group(2);

            if (key.equals("showkey")){
                data.setShowkey(value.substring(1, value.length() - 1));
            } else if (key.equals("si")){
                data.setRetryId(value);
            } else if (key.equals("x")){
                data.setWidth(Integer.parseInt(value, 10));
            } else if (key.equals("y")){
                data.setHeight(Integer.parseInt(value, 10));
            }
        }

        matcher = pImageSrc.matcher(html);

        while (matcher.find()){
            data.setSrc(URLDecoder.decode(matcher.group(1), "UTF-8"));
        }


        return data;
    }
}
