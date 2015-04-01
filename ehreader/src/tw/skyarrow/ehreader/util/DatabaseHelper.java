package tw.skyarrow.ehreader.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import tw.skyarrow.ehreader.model.DaoMaster;

public class DatabaseHelper extends DaoMaster.OpenHelper {
    private static final String DB_NAME = "ehreader.db";

    private static DatabaseHelper mInstance;

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    public DatabaseHelper(Context context){
        this(context, DB_NAME, null);
    }

    public static synchronized DatabaseHelper getInstance(Context context){
        if (mInstance == null){
            mInstance = new DatabaseHelper(context.getApplicationContext());
        }

        return mInstance;
    }

    public static SQLiteDatabase getReadableDatabase(Context context){
        return getInstance(context).getReadableDatabase();
    }

    public static SQLiteDatabase getWritableDatabase(Context context){
        return getInstance(context).getWritableDatabase();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer) {
        L.i("Upgrading tables from schema version %d to %d", oldVer, newVer);
        DaoMaster.dropAllTables(db, true);
        onCreate(db);
    }
}
