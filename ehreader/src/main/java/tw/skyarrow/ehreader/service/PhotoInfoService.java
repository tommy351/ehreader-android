package tw.skyarrow.ehreader.service;

import android.app.IntentService;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONException;

import java.io.IOException;

import de.greenrobot.event.EventBus;
import tw.skyarrow.ehreader.Constant;
import tw.skyarrow.ehreader.db.DaoMaster;
import tw.skyarrow.ehreader.db.DaoSession;
import tw.skyarrow.ehreader.db.Gallery;
import tw.skyarrow.ehreader.db.GalleryDao;
import tw.skyarrow.ehreader.db.Photo;
import tw.skyarrow.ehreader.event.PhotoInfoEvent;
import tw.skyarrow.ehreader.util.L;
import tw.skyarrow.ehreader.util.PhotoInfoHelper;

/**
 * Created by SkyArrow on 2014/1/29.
 */
public class PhotoInfoService extends IntentService {
    private static final String CLASS_NAME = "PhotoInfoService";

    public static final String GALLERY_ID = "galleryId";
    public static final String PHOTO_PAGE = "photoPage";

    private SQLiteDatabase db;
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private GalleryDao galleryDao;

    private PhotoInfoHelper infoHelper;
    private EventBus bus;

    public PhotoInfoService() {
        super(CLASS_NAME);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, Constant.DB_NAME, null);
        db = helper.getWritableDatabase();
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
        galleryDao = daoSession.getGalleryDao();

        infoHelper = new PhotoInfoHelper(this);
        bus = EventBus.getDefault();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        long galleryId = intent.getLongExtra(GALLERY_ID, 0);
        int page = intent.getIntExtra(PHOTO_PAGE, 0);

        if (galleryId <= 0) {
            L.e("Gallery ID must be a positive number.");
            return;
        }

        if (page <= 0) {
            L.e("Photo page must be a positive number.");
            return;
        }

        Gallery gallery = galleryDao.load(galleryId);

        if (gallery == null) {
            L.e("Gallery %d not found.", galleryId);
            return;
        }

        try {
            Photo photo = infoHelper.getPhotoInfo(gallery, page);

            bus.post(new PhotoInfoEvent(photo));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
