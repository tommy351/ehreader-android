package tw.skyarrow.ehreader.migration;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by SkyArrow on 2015/9/24.
 */
public interface BaseMigration {
    void up(SQLiteDatabase db);
    void down(SQLiteDatabase db);
}
