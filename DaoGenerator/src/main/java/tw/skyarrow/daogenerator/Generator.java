package tw.skyarrow.daogenerator;

import java.io.File;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;
import de.greenrobot.daogenerator.ToMany;
import de.greenrobot.daogenerator.ToOne;

/**
 * Created by SkyArrow on 2014/1/26.
 */
public class Generator {
    private static final int dbVersion = 1;
    private static final String dbPackage = "tw.skyarrow.ehreader.db";
    private static final String dbPath = "../ehreader/src-gen";

    public static void main(String[] args) throws Exception {
        Schema schema = new Schema(dbVersion, dbPackage);

        // Photo
        Entity photo = schema.addEntity("Photo");

        photo.addIdProperty().autoincrement();
        Property photoPage = photo.addIntProperty("page").notNull().getProperty();
        photo.addStringProperty("token").notNull();
        photo.addBooleanProperty("bookmarked");
        photo.addStringProperty("filename");
        photo.addIntProperty("width");
        photo.addIntProperty("height");
        photo.addStringProperty("src");
        photo.addBooleanProperty("downloaded");
        photo.addBooleanProperty("invalid");

        // Recent Image Search
        Entity imageSearch = schema.addEntity("ImageSearch");

        imageSearch.addIdProperty().autoincrement();
        imageSearch.addStringProperty("path");

        // Gallery
        Entity gallery = schema.addEntity("Gallery");

        gallery.addIdProperty();
        gallery.addStringProperty("token").notNull();
        gallery.addStringProperty("title");
        gallery.addStringProperty("subtitle");
        gallery.addIntProperty("category");
        gallery.addIntProperty("count");
        gallery.addStringProperty("thumbnail");
        gallery.addBooleanProperty("starred");
        gallery.addFloatProperty("rating");
        gallery.addDateProperty("created");
        gallery.addDateProperty("lastread");
        gallery.addStringProperty("tags");
        gallery.addStringProperty("uploader");
        gallery.addIntProperty("progress");
        gallery.addStringProperty("showkey");
        gallery.addLongProperty("size");

        Property photoProperty = photo.addLongProperty("galleryId").notNull().getProperty();
        ToMany galleryToPhotos = gallery.addToMany(photo, photoProperty);
        galleryToPhotos.setName("photos");
        galleryToPhotos.orderAsc(photoPage);

        // Download
        Entity download = schema.addEntity("Download");
        download.setSuperclass("DownloadStatus");

        Property downloadId = download.addIdProperty().autoincrement().getProperty();
        download.addIntProperty("status");
        download.addIntProperty("progress");
        download.addDateProperty("created");

        download.addToOne(gallery, downloadId);

        // Generate DAO
        File file = new File(dbPath);
        if (!file.exists()) file.mkdirs();

        new DaoGenerator().generateAll(schema, dbPath);
    }
}
