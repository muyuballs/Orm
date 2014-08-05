package info.breezes.orm;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import info.breezes.orm.utils.TableUtils;

/**
 * Created by jianxingqiao on 5/4/2014.
 */

public abstract class OrmSQLiteHelper extends SQLiteOpenHelper {

    private Context mContext;

    public OrmSQLiteHelper(Context context, String name, android.database.sqlite.SQLiteDatabase.CursorFactory factory, int version) {
        this(context, name, factory, version, null);
    }

    public OrmSQLiteHelper(Context context, String name, android.database.sqlite.SQLiteDatabase.CursorFactory factory, int version, android.database.DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
        this.mContext = context;
    }

    public synchronized void close() {
        super.close();
    }

    public <T> QueryAble<T> query(Class<T> tableClass) {
        return new QueryAble<T>(tableClass, getReadableDatabase(),mContext);
    }

    public long insert(Object object) {
        return TableUtils.insert(getWritableDatabase(), object, mContext);
    }

    public int update(Object object) {
        return TableUtils.update(getWritableDatabase(), object, mContext);
    }

    public long insertOrUpdate(Object object) {
        return TableUtils.insertOrUpdate(getWritableDatabase(), object, mContext);
    }

    public <T> int updateBy(Object object, String column) {
        return TableUtils.updateBy(getWritableDatabase(), object, column, mContext);
    }

    public int delete(Object object) {
        return TableUtils.delete(getWritableDatabase(), object, mContext);
    }

    public <T> int deleteBy(Object object, String column) {
        return TableUtils.deleteBy(getWritableDatabase(), object, column, mContext);
    }

    public long[] insertAll(Object[] objects) {
        return TableUtils.insertAll(getWritableDatabase(), objects, mContext);
    }
}
