package tw.skyarrow.ehreader.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.webkit.MimeTypeMap;

import com.android.volley.toolbox.ImageLoader;

import java.io.FileNotFoundException;

import tw.skyarrow.ehreader.model.DaoMaster;
import tw.skyarrow.ehreader.model.DaoSession;
import tw.skyarrow.ehreader.model.Photo;
import tw.skyarrow.ehreader.model.PhotoDao;
import tw.skyarrow.ehreader.util.DatabaseHelper;

/**
 * Created by SkyArrow on 15/4/5.
 */
public class PhotoProvider extends ContentProvider {
    public static final String AUTHORITY = "tw.skyarrow.ehreader.provider.PhotoProvider";

    private static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int PHOTO_ID = 1;

    public static final Uri PHOTO_URI = Uri.parse("content://" + AUTHORITY + "/photos");

    private PhotoDao photoDao;

    static {
        uriMatcher.addURI(AUTHORITY, "photos/#", PHOTO_ID);
    }

    @Override
    public boolean onCreate() {
        SQLiteDatabase db = DatabaseHelper.getReadableDatabase(getContext());
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();
        photoDao = daoSession.getPhotoDao();

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        Photo photo = null;

        switch (uriMatcher.match(uri)){
            case PHOTO_ID:
                long photoId = ContentUris.parseId(uri);
                photo = photoDao.load(photoId);
                break;
        }

        if (photo == null || photo.getSrc() == null) return "";

        return MimeTypeMap.getFileExtensionFromUrl(photo.getSrc());
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {


        return super.openFile(uri, mode);
    }
}
