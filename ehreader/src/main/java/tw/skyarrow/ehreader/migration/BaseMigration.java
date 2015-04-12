package tw.skyarrow.ehreader.migration;

import android.database.sqlite.SQLiteDatabase;

public abstract class BaseMigration {
    public abstract void up(SQLiteDatabase db);
    public abstract void down(SQLiteDatabase db);
}
