package tw.skyarrow.ehreader.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;
import java.util.Map;

import tw.skyarrow.ehreader.migration.BaseMigration;
import tw.skyarrow.ehreader.migration.MigrationV2;
import tw.skyarrow.ehreader.model.DaoMaster;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "ehreader.db";
    private static final int DB_VERSION = DaoMaster.SCHEMA_VERSION;

    private static DatabaseHelper mInstance;
    private static Map<Integer, BaseMigration> migrations = new HashMap<>();

    static {
        migrations.put(2, new MigrationV2());
    }

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DatabaseHelper(Context context){
        this(context, DB_NAME, null, DB_VERSION);
    }

    public static synchronized DatabaseHelper getInstance(Context context){
        if (mInstance == null){
            mInstance = new DatabaseHelper(context.getApplicationContext());
        }

        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        DaoMaster.createAllTables(db, false);
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

        for (int i = oldVer; i <= newVer; i++){
            if (migrations.containsKey(i)){
                migrations.get(i).up(db);
            }
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVer, int newVer) {
        L.i("Downgrading tables from schema version %d to %d", oldVer, newVer);

        for (int i = oldVer + 1; i > newVer; i--){
            if (migrations.containsKey(i)){
                migrations.get(i).down(db);
            }
        }
    }
}
