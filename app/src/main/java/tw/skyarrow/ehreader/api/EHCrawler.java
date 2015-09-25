package tw.skyarrow.ehreader.api;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tw.skyarrow.ehreader.model.Gallery;
import tw.skyarrow.ehreader.model.GalleryDataResponse;
import tw.skyarrow.ehreader.model.GalleryId;
import tw.skyarrow.ehreader.model.GalleryMetaData;

/**
 * Created by SkyArrow on 2015/9/25.
 */
public class EHCrawler {
    private static final Pattern pGalleryURL = Pattern.compile("<a href=\"http://(?:g\\.e-|ex)hentai\\.org/g/(\\d+)/(\\w+)/\" onmouseover");

    public static GalleryId[] parseGalleryIndex(String html){
        List<GalleryId> list = new ArrayList<>();
        Matcher matcher = pGalleryURL.matcher(html);

        while (matcher.find()){
            long id = Long.parseLong(matcher.group(1), 10);
            GalleryId galleryId = new GalleryId(id, matcher.group(2));
            list.add(galleryId);
        }

        return list.toArray(new GalleryId[list.size()]);
    }

    public static List<Gallery> parseGalleryDataResponse(GalleryDataResponse res){
        List<Gallery> list = new ArrayList<>();

        for (GalleryMetaData metaData : res.getData()){
            Gallery gallery = new Gallery();
            gallery.setId(metaData.getId());
            gallery.setToken(metaData.getToken());
            gallery.setTitle(metaData.getTitle());
            gallery.setSubtitle(metaData.getTitleJpn());
            gallery.setCategory(metaData.getCategory());
            gallery.setThumbnail(metaData.getThumb());
            gallery.setCount(metaData.getFileCount());
            gallery.setRating(metaData.getRating());
            gallery.setUploader(metaData.getUploader());
            gallery.setCreated(metaData.getPosted());
            gallery.setSize(metaData.getFileSize());
            gallery.setTags(metaData.getTags());
            gallery.setStarred(false);
            gallery.setProgress(0);
            list.add(gallery);
        }

        return list;
    }
}
