package tw.skyarrow.ehreader.migration;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by SkyArrow on 2015/9/24.
 */
public class MigrationV2 implements BaseMigration {
    @Override
    public void up(SQLiteDatabase db) {
        // Drop ImageSearch table
        db.execSQL("DROP TABLE IF EXISTS 'IMAGE_SEARCH';");

        // Add photosPerPage field to Gallery table
        db.execSQL("ALTER TABLE 'GALLERY' ADD 'PHOTO_PER_PAGE' INTEGER;");

        // Add retryId field to Photo table
        db.execSQL("ALTER TABLE 'PHOTO' ADD 'RETRY_ID' TEXT;");
    }

    @Override
    public void down(SQLiteDatabase db) {

    }
}
