package tw.skyarrow.ehreader.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import tw.skyarrow.ehreader.db.DaoMaster;

/**
 * Created by SkyArrow on 2014/3/3.
 */
// http://www.androiddesignpatterns.com/2012/05/correctly-managing-your-sqlite-database.html
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "ehreader.db";
    private static final int DB_VERSION = DaoMaster.SCHEMA_VERSION;

    private static DatabaseHelper instance;

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }

        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        L.i("Creating tables for schema version %d", DB_VERSION);
        DaoMaster.createAllTables(db, false);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer) {
        L.i("Upgrading tables from schema version %d to %d", oldVer, newVer);
        DaoMaster.dropAllTables(db, true);
        onCreate(db);
    }
}
