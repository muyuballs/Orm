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
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;

import info.breezes.orm.utils.TableUtils;

public class SimpleOrmSQLiteHelper extends OrmSQLiteHelper {

    public static final String TAG = "SimpleOrmSQLiteHelper";
    private SQLiteDatabase database;
    private Class<?>[] tables;

    public SimpleOrmSQLiteHelper(Context context, String name, int version, Class<?>... tables) {
        super(context, name, null, version);
        this.tables = tables;
    }

    public SimpleOrmSQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, Class<?>... tables) {
        super(context, name, factory, version);
        this.tables = tables;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public SimpleOrmSQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if (tables != null && tables.length > 0) {
            for (Class<?> table : tables) {
                if (table != null) {
                    TableUtils.createTable(db, table);
                }
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "upgrade:" + oldVersion + "->" + newVersion);
        for (Class<?> table : tables) {
            if (table != null) {
                TableUtils.upgradeTable(db, table);
            }
        }
    }

    @Override
    public SQLiteDatabase getCurrentDatabase(boolean writable) {
        if (database == null || !database.isOpen()) {
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
    }
}
