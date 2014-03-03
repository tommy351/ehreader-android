package tw.skyarrow.ehreader.service;

import android.app.IntentService;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

import de.greenrobot.event.EventBus;
import tw.skyarrow.ehreader.api.ApiCallException;
import tw.skyarrow.ehreader.api.DataLoader;
import tw.skyarrow.ehreader.db.DaoMaster;
import tw.skyarrow.ehreader.db.DaoSession;
import tw.skyarrow.ehreader.db.Gallery;
import tw.skyarrow.ehreader.db.GalleryDao;
import tw.skyarrow.ehreader.db.Photo;
import tw.skyarrow.ehreader.event.PhotoInfoEvent;
import tw.skyarrow.ehreader.util.DatabaseHelper;
import tw.skyarrow.ehreader.util.L;

/**
 * Created by SkyArrow on 2014/1/29.
 */
public class PhotoInfoService extends IntentService {
    private static final String CLASS_NAME = "PhotoInfoService";

    public static final String EXTRA_GALLERY = "galleryId";
    public static final String EXTRA_PAGE = "photoPage";

    private SQLiteDatabase db;
    private GalleryDao galleryDao;

    private DataLoader dataLoader;
    private EventBus bus;

    public PhotoInfoService() {
        super(CLASS_NAME);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        DatabaseHelper helper = DatabaseHelper.getInstance(this);
        db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();
        galleryDao = daoSession.getGalleryDao();

        dataLoader = DataLoader.getInstance(this);
        bus = EventBus.getDefault();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        long galleryId = intent.getLongExtra(EXTRA_GALLERY, 0);
        int page = intent.getIntExtra(EXTRA_PAGE, 0);

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
            Photo photo = dataLoader.getPhotoInfo(gallery, page);

            bus.post(new PhotoInfoEvent(galleryId, page, photo));
        } catch (ApiCallException e) {
            e.printStackTrace();

            bus.post(new PhotoInfoEvent(galleryId, page, e));
        }
    }
}
