package tw.skyarrow.ehreader_dao;

import java.io.File;

import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;
import de.greenrobot.daogenerator.ToMany;

public class Generator {
    private static final int DB_VERSION = 2;
    private static final String DB_PACKAGE = "tw.skyarrow.ehreader.model";
    private static final String DB_PATH = "./app/src-gen";

    public static void main(String[] args) throws Exception {
        Schema schema = new Schema(DB_VERSION, DB_PACKAGE);

        // Photo
        Entity photo = schema.addEntity("Photo");
        photo.setSuperclass("PhotoBase");

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
        photo.addStringProperty("retryId");

        // Gallery
        Entity gallery = schema.addEntity("Gallery");
        gallery.setSuperclass("GalleryBase");

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
        gallery.addIntProperty("photoPerPage");

        Property photoProperty = photo.addLongProperty("galleryId").notNull().getProperty();
        ToMany galleryToPhotos = gallery.addToMany(photo, photoProperty);
        galleryToPhotos.setName("photos");
        galleryToPhotos.orderAsc(photoPage);

        // Download
        Entity download = schema.addEntity("Download");
        download.setSuperclass("DownloadBase");

        Property downloadId = download.addIdProperty().autoincrement().getProperty();
        download.addIntProperty("status");
        download.addIntProperty("progress");
        download.addDateProperty("created");

        download.addToOne(gallery, downloadId);

        // Generate DAO
        File file = new File(DB_PATH);
        if (!file.exists()) file.mkdirs();

        new de.greenrobot.daogenerator.DaoGenerator().generateAll(schema, DB_PATH);
    }
}
