package tw.skyarrow.ehreader.event;

import android.net.Uri;

/**
 * Created by SkyArrow on 2014/1/29.
 */
public class ImageSearchUploadedEvent {
    private String url;
    private boolean similar;
    private boolean onlyCover;

    public ImageSearchUploadedEvent(String url, boolean similar, boolean onlyCover) {
        this.url = url;
        this.similar = similar;
        this.onlyCover = onlyCover;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isSimilar() {
        return similar;
    }

    public void setSimilar(boolean similar) {
        this.similar = similar;
    }

    public boolean isOnlyCover() {
        return onlyCover;
    }

    public void setOnlyCover(boolean onlyCover) {
        this.onlyCover = onlyCover;
    }

    public String buildUrl() {
        Uri uri = Uri.parse(url);
        String hash = uri.getQueryParameter("f_shash");
        String from = uri.getQueryParameter("fs_from");

        Uri.Builder builder = Uri.parse(url).buildUpon();

        builder.clearQuery();
        builder.appendQueryParameter("f_shash", hash);
        builder.appendQueryParameter("fs_from", from);

        if (similar) {
            builder.appendQueryParameter("fs_similar", "1");
        }

        if (onlyCover) {
            builder.appendQueryParameter("fs_covers", "1");
        }

        return builder.build().toString();
    }
}
