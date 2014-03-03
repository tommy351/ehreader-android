package tw.skyarrow.ehreader.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.DiscCacheUtil;

import java.io.File;
import java.io.FileNotFoundException;

import tw.skyarrow.ehreader.db.DaoMaster;
import tw.skyarrow.ehreader.db.DaoSession;
import tw.skyarrow.ehreader.db.Photo;
import tw.skyarrow.ehreader.db.PhotoDao;
import tw.skyarrow.ehreader.util.DatabaseHelper;
import tw.skyarrow.ehreader.util.FileInfoHelper;

/**
 * Created by SkyArrow on 2014/2/11.
 */
public class PhotoProvider extends ContentProvider {
    public static final String AUTHORITY = "tw.skyarrow.ehreader.provider.PhotoProvider";

    private static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int PHOTO_ID = 1;

    public static final Uri PHOTO_URI = Uri.parse("content://" + AUTHORITY + "/photos");

    private SQLiteDatabase db;
    private PhotoDao photoDao;
    private ImageLoader imageLoader;

    static {
        uriMatcher.addURI(AUTHORITY, "photos/#", PHOTO_ID);
    }

    @Override
    public boolean onCreate() {
        DatabaseHelper helper = DatabaseHelper.getInstance(getContext());
        db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();
        photoDao = daoSession.getPhotoDao();
        imageLoader = ImageLoader.getInstance();

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Photo photo = getPhoto(uri);
        File file = getPhotoFile(photo);

        if (file == null) return null;

        MatrixCursor result = new MatrixCursor(projection);
        Object[] row = new Object[projection.length];

        for (int i = 0; i < projection.length; i++) {
            if (projection[i].compareToIgnoreCase(MediaStore.MediaColumns.DISPLAY_NAME) == 0) {
                row[i] = photo.getFilename();
            } else if (projection[i].compareToIgnoreCase(MediaStore.MediaColumns.SIZE) == 0) {
                row[i] = file.length();
            } else if (projection[i].compareToIgnoreCase(MediaStore.MediaColumns.DATA) == 0) {
                row[i] = file;
            } else if (projection[i].compareToIgnoreCase(MediaStore.MediaColumns.MIME_TYPE) == 0) {
                row[i] = FileInfoHelper.getMimeType(file);
            }
        }

        result.addRow(row);

        return result;
    }

    @Override
    public String getType(Uri uri) {
        Photo photo = null;

        switch (uriMatcher.match(uri)) {
            case PHOTO_ID:
                long photoId = ContentUris.parseId(uri);
                photo = photoDao.load(photoId);
                break;
        }

        if (photo == null) return "";

        return FileInfoHelper.getMimeType(photo.getFilename());
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        Photo photo = getPhoto(uri);
        File file = getPhotoFile(photo);

        if (file == null) {
            throw new FileNotFoundException(uri.getPath());
        } else {
            return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        }
    }

    private Photo getPhoto(Uri uri) {
        int match = uriMatcher.match(uri);

        if (match != PHOTO_ID) return null;

        long photoId = ContentUris.parseId(uri);
        Photo photo = photoDao.load(photoId);

        return photo;
    }

    private File getPhotoFile(Photo photo) {
        if (photo == null) return null;

        File file = photo.getFile();

        if (!file.exists()) {
            file = DiscCacheUtil.findInCache(photo.getSrc(), imageLoader.getDiscCache());
        }

        if (file.exists()) {
            return file;
        } else {
            return null;
        }
    }
}
