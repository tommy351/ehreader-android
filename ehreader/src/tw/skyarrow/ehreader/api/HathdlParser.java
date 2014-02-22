package tw.skyarrow.ehreader.api;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tw.skyarrow.ehreader.db.Photo;
import tw.skyarrow.ehreader.util.L;

/**
 * Created by SkyArrow on 2014/2/8.
 */
public class HathdlParser {
    private long id;
    private int count;
    private String title;
    private List<Photo> photos;

    private static final Pattern pFileList = Pattern.compile("(\\d+) ([a-z0-9-]+) (.+)");

    public HathdlParser(String str) {
        String[] lines = str.split("\\n");
        boolean filelist = false;
        photos = new ArrayList<Photo>();

        for (String line : lines) {
            L.d("Read line: %s", line);

            if (filelist) {
                if (line.isEmpty()) {
                    break;
                }

                Matcher matcher = pFileList.matcher(line);
                int page = 0;
                String token = "";
                String filename = "";

                while (matcher.find()) {
                    page = Integer.parseInt(matcher.group(1));
                    token = matcher.group(2);
                    filename = matcher.group(3);
                }

                L.v("Photo: {page: %d, token: %s, filename: %s}", page, token, filename);

                Photo photo = new Photo();
                photo.setPage(page);
                photo.setToken(token);
                photo.setFilename(filename);
                photo.setGalleryId(id);

                photos.add(photo);
            } else if (line.startsWith("GID")) {
                id = Long.parseLong(line.substring(4));
            } else if (line.startsWith("FILES")) {
                count = Integer.parseInt(line.substring(6));
            } else if (line.startsWith("TITLE")) {
                title = line.substring(6);
            } else if (line.startsWith("FILELIST")) {
                filelist = true;
            }
        }
    }

    public long getId() {
        return id;
    }

    public int getCount() {
        return count;
    }

    public String getTitle() {
        return title;
    }

    public List<Photo> getPhotos() {
        return photos;
    }
}
