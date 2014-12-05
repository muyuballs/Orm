package info.breezes.orm;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import info.breezes.orm.utils.TableUtils;

/**
 * Created by Qiao on 14-6-15.
 */
public class SimpleOrmSQLiteHelper extends OrmSQLiteHelper {

    public static final String TAG = "SimpleOrmSQLiteHelper";
    private SQLiteDatabase database;
    private Class<?>[] tables;

    public SimpleOrmSQLiteHelper(Context context, String name, int version, Class<?>... tables) {
        this(context, name, null, version);
        this.tables = tables;
    }

    public SimpleOrmSQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        this(context, name, factory, version, null);
    }

    public SimpleOrmSQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if(tables!=null && tables.length>0) {
            for (Class<?> table: tables) {
                if(table!=null) {
                    TableUtils.createTable(db, table);
                }
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public SQLiteDatabase getCurrentDatabase(boolean writable) {
        if (database == null) {
            database = writable ? getWritableDatabase() : getReadableDatabase();
        } else if (database.isReadOnly() && writable) {
            database.close();
            database = getWritableDatabase();
        }
        return database;
    }

    public SQLiteDatabase beginTransaction() {
        getCurrentDatabase(true);
        if (!database.inTransaction()) {
            database.beginTransaction();
        } else {
            Log.w(TAG, "current database already in transaction.");
        }
        return database;
    }

    public void commit() {
        database.setTransactionSuccessful();
        database.endTransaction();
    }

    public void rollback() {
        database.endTransaction();
        ;
    }
}
