package tw.skyarrow.ehreader.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import tw.skyarrow.ehreader.migration.BaseMigration;
import tw.skyarrow.ehreader.migration.MigrationV2;
import tw.skyarrow.ehreader.model.DaoMaster;
import tw.skyarrow.ehreader.model.DaoSession;

/**
 * Created by SkyArrow on 2015/9/24.
 */
public class DatabaseHelper extends DaoMaster.OpenHelper {
    public static final String TAG = DatabaseHelper.class.getSimpleName();

    private static final String DB_NAME = "ehreader.db";

    private static DatabaseHelper instance;
    private static Map<Integer, BaseMigration> migrations = new HashMap<>();
    private SQLiteDatabase database;
    private DaoSession session;
    private final AtomicInteger openCounter = new AtomicInteger();

    static {
        migrations.put(2, new MigrationV2());
    }

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    public DatabaseHelper(Context context) {
        this(context, DB_NAME, null);
    }

    public static synchronized DatabaseHelper get(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }

        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        DaoMaster.createAllTables(db, false);
    }

    public synchronized DaoSession open() {
        if (openCounter.getAndIncrement() == 0) {
            database = getWritableDatabase();
            DaoMaster daoMaster = new DaoMaster(database);
            session = daoMaster.newSession();
        }

        return session;
    }

    public synchronized void close() {
        if (openCounter.decrementAndGet() == 0) {
            database.close();
            session = null;
            database = null;
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer) {
        Log.d(TAG, String.format("Upgrading tables from schema version %d to %d", oldVer, newVer));

        for (int i = oldVer; i <= newVer; i++) {
            if (migrations.containsKey(i)) {
                migrations.get(i).up(db);
            }
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVer, int newVer) {
        Log.d(TAG, String.format("Downgrading tables from schema version %d to %d", oldVer, newVer));

        for (int i = oldVer + 1; i > newVer; i--) {
            if (migrations.containsKey(i)) {
                migrations.get(i).down(db);
            }
        }
    }
}
