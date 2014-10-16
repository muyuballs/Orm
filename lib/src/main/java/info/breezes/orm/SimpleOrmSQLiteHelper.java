package info.breezes.orm;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by jianxingqiao on 14-6-15.
 */
public abstract class SimpleOrmSQLiteHelper extends OrmSQLiteHelper {

    private SQLiteDatabase database;

    public SimpleOrmSQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        this(context, name, factory, version, null);
    }

    public SimpleOrmSQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }


    @Override
    public SQLiteDatabase getCurrentDatabase(boolean writable) {
        if (database == null) {
            database = writable ? getWritableDatabase() : getReadableDatabase();
        }
        return database;
    }

}
