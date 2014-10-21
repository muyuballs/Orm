package info.breezes.orm;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import info.breezes.orm.utils.TableUtils;

/**
 * Created by jianxingqiao on 5/4/2014.
 */

public abstract class OrmSQLiteHelper extends SQLiteOpenHelper {

    private Context mContext;

    public OrmSQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        this(context, name, factory, version, null);
    }

    public OrmSQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, android.database.DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
        this.mContext = context;
    }

    public abstract SQLiteDatabase getCurrentDatabase(boolean writable);

    public synchronized void close() {
        super.close();
    }

    public <T> QueryAble<T> query(Class<T> tableClass) {
        return new QueryAble<T>(tableClass, getCurrentDatabase(false), mContext);
    }

    public long insert(Object object) {
        return TableUtils.insert(getCurrentDatabase(true), object, mContext);
    }

    public int update(Object object) {
        return TableUtils.update(getCurrentDatabase(true), object, mContext);
    }

    public long insertOrUpdate(Object object) {
        return TableUtils.insertOrUpdate(getCurrentDatabase(true), object, mContext);
    }

    public <T> int updateBy(Object object, String column) {
        return TableUtils.updateBy(getCurrentDatabase(true), object, column, mContext);
    }

    public int delete(Object object) {
        return TableUtils.delete(getCurrentDatabase(true), object, mContext);
    }

    public <T> int deleteBy(Object object, String column) {
        return TableUtils.deleteBy(getCurrentDatabase(true), object, column, mContext);
    }

    public long[] insertAll(Object[] objects) {
        SQLiteDatabase database = getCurrentDatabase(true);
        boolean inTransaction = database.inTransaction();
        if (!inTransaction) {
            database.beginTransaction();
        }
        try {
            long[] result = TableUtils.insertAll(database, objects, mContext);
            if (!inTransaction) {
                database.setTransactionSuccessful();
            }
            return result;
        } finally {
            if (!inTransaction) {
                database.endTransaction();
            }
        }
    }

    public long[] insertOrUpdateAll(Object[] objects) {
        SQLiteDatabase database = getCurrentDatabase(true);
        boolean inTransaction = database.inTransaction();
        if (!inTransaction) {
            database.beginTransaction();
        }
        try {
            long[] result = TableUtils.insertOrUpdateAll(database, objects, mContext);
            if (!inTransaction) {
                database.setTransactionSuccessful();
            }
            return result;
        } finally {
            if (!inTransaction) {
                database.endTransaction();
            }
        }
    }

    public int clear(Class<?> tClass) {
        return TableUtils.clear(getCurrentDatabase(true), tClass, mContext);
    }
}
