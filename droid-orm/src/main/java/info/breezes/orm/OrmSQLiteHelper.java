/*
 * Copyright (c) 2014-2015, Qiao
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the LICENSE
 */

package info.breezes.orm;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import java.util.Collection;
import java.util.List;

import info.breezes.orm.utils.TableUtils;

public abstract class OrmSQLiteHelper extends SQLiteOpenHelper {

    private Context mContext;

    public OrmSQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
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

    public long[] insertAll(Collection collection) {
        return insertAll(collection.toArray());
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

    public long[] insertOrUpdateAll(Collection collection) {
        return insertOrUpdateAll(collection.toArray());
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
