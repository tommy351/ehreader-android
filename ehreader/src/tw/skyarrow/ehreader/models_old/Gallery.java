package tw.skyarrow.ehreader.models_old;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class Gallery extends RealmObject {
    @Ignore public static final int CATEGORY_DOUJINSHI = 1;
    @Ignore public static final int CATEGORY_MANGA = 2;
    @Ignore public static final int CATEGORY_ARTISTCG = 3;
    @Ignore public static final int CATEGORY_GAMECG = 4;
    @Ignore public static final int CATEGORY_WESTERN = 5;
    @Ignore public static final int CATEGORY_NON_H = 6;
    @Ignore public static final int CATEGORY_IMAGESET = 7;
    @Ignore public static final int CATEGORY_COSPLAY = 8;
    @Ignore public static final int CATEGORY_ASIANPORN = 9;
    @Ignore public static final int CATEGORY_MISC = 10;

    @PrimaryKey
    private int id;
    private String title;
    private String subtitle;
    private String token;
    private int count;
    private String thumbnail;
    private boolean starred;
    private float rating;
    private Date created;
    private Date lastread;
    private RealmList<Tag> tags;
    private int progress;
    private String showkey;
    private long size;
    private RealmList<Photo> photos;
    private int category;
    private String uploader;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public boolean isStarred() {
        return starred;
    }

    public void setStarred(boolean starred) {
        this.starred = starred;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getLastread() {
        return lastread;
    }

    public void setLastread(Date lastread) {
        this.lastread = lastread;
    }

    public RealmList<Tag> getTags() {
        return tags;
    }

    public void setTags(RealmList<Tag> tags) {
        this.tags = tags;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getShowkey() {
        return showkey;
    }

    public void setShowkey(String showkey) {
        this.showkey = showkey;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public RealmList<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(RealmList<Photo> photos) {
        this.photos = photos;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public String getUploader() {
        return uploader;
    }

    public void setUploader(String uploader) {
        this.uploader = uploader;
    }

    public void setCategory(String category){
        if (category.equals("Doujinshi")){
            setCategory(CATEGORY_DOUJINSHI);
        } else if (category.equals("Manga")){
            setCategory(CATEGORY_MANGA);
        } else if (category.equals("Artist CG Sets")){
            setCategory(CATEGORY_ARTISTCG);
        } else if (category.equals("Game CG Sets")){
            setCategory(CATEGORY_GAMECG);
        } else if (category.equals("Western")){
            setCategory(CATEGORY_WESTERN);
        } else if (category.equals("Non-H")){
            setCategory(CATEGORY_NON_H);
        } else if (category.equals("Image Sets")){
            setCategory(CATEGORY_IMAGESET);
        } else if (category.equals("Cosplay")){
            setCategory(CATEGORY_COSPLAY);
        } else if (category.equals("Asian Porn")){
            setCategory(CATEGORY_ASIANPORN);
        } else {
            setCategory(CATEGORY_MISC);
        }
    }
}
