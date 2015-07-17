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

package info.breezes.orm.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteClosable;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import info.breezes.orm.FCMap;
import info.breezes.orm.OrmConfig;
import info.breezes.orm.annotation.Column;
import info.breezes.orm.annotation.Table;
import info.breezes.orm.model.Db;
import info.breezes.orm.translator.IColumnTranslator;

public class TableUtils {

    static class Col {
        String name;
        Column column;
        Field field;
    }

    public static void upgradeTable(SQLiteDatabase db, Class<?> tableClass) {
        if (OrmConfig.Emulate) {
            return;
        }
        Table table = tableClass.getAnnotation(Table.class);
        if (table != null) {
            String tableName = getTableName(tableClass);
            int oldVersion = 0;
            try {
                Cursor cursor1 = db.rawQuery("select version from __orm_db_version__ where table_name=?", new String[]{tableName});
                if (cursor1.moveToNext()) {
                    oldVersion = cursor1.getInt(0);
                }
                cursor1.close();
            } catch (Exception exp) {
                if (OrmConfig.Debug) {
                    Log.w("ORM", exp.getMessage(), exp);
                }
            }
            if (oldVersion == table.version()) {
                if (OrmConfig.Debug) {
                    Log.d("ORM", "not need upgrade or downgrade");
                }
                return;
            }
            ArrayList<String> oldColumns = new ArrayList<>();
            try {
                Cursor cursor = db.rawQuery("pragma table_info(" + tableName + ")", null);

                int nameIndex = cursor.getColumnIndex("name");
                while (cursor.moveToNext()) {
                    oldColumns.add(cursor.getString(nameIndex));
                }
                cursor.close();
            } catch (Exception exp) {
                if (OrmConfig.Debug) {
                    Log.w("ORM", exp.getMessage(), exp);
                }
            }
            String tmpTableName = "_tmp_" + tableName + System.nanoTime();
            if (createTable(db, tableClass, tmpTableName)) {
                if (oldColumns.size() > 0) {
                    String updateSql = "insert into " + tmpTableName + "(" + TextUtils.join(",", oldColumns) + ")" + " select * from " + tableName;
                    if (OrmConfig.Debug) {
                        Log.d("ORM insert into tmp", updateSql);
                    }
                    db.execSQL(updateSql);
                    db.execSQL("drop table " + tableName);
                    if (OrmConfig.Debug) {
                        Log.d("ORM delete old", "drop table " + tableName);
                    }
                }
                String renameSql = "alter table " + tmpTableName + " rename to " + tableName;
                if (OrmConfig.Debug) {
                    Log.d("ORM rename", renameSql);
                }
                db.execSQL(renameSql);
                try {
                    Db db1 = new Db();
                    db1.version = table.version();
                    db1.tableName = tableName;
                    insertOrUpdate(db, db1, null);
                }catch (Exception exp) {
                    if (OrmConfig.Debug) {
                        Log.w("ORM", exp.getMessage(), exp);
                    }
                }
            }
        } else {
            throw new RuntimeException(tableClass.getName() + " is not an orm table.");
        }
    }

    public static boolean createTable(SQLiteDatabase database, Class<?> tableClass, String tableName) {
        Table table = tableClass.getAnnotation(Table.class);
        if (table != null) {
            ArrayList<String> indexes = new ArrayList<>();
            StringBuilder indexSql = new StringBuilder();
            StringBuilder createSql = new StringBuilder();
            createSql.append("CREATE TABLE IF NOT EXISTS ");
            createSql.append(TextUtils.isEmpty(tableName) ? getTableName(tableClass, table) : tableName);
            createSql.append("(");
            ArrayList<Col> cols = new ArrayList<>();
            Field fields[] = tableClass.getFields();
            for (Field field : fields) {
                indexSql.setLength(0);
                Column column = field.getAnnotation(Column.class);
                if (column != null) {
                    Col col = new Col();
                    col.name = getColumnName(field, column);
                    col.column = column;
                    col.field = field;
                    cols.add(col);
                    if (column.uniqueIndex()) {
                        indexSql.append("CREATE UNIQUE INDEX ");
                        indexSql.append(getTableName(tableClass, table));
                        indexSql.append("_uq_");
                        indexSql.append(System.nanoTime());
                        indexSql.append(" ON ");
                        indexSql.append(getTableName(tableClass, table));
                        indexSql.append("(");
                        indexSql.append(getColumnName(field, column));
                        indexSql.append(" DESC ");
                        indexSql.append(")");
                        indexes.add(indexSql.toString());
                    }
                }
            }
            Collections.sort(cols, new Comparator<Col>() {
                @Override
                public int compare(Col lhs, Col rhs) {
                    return lhs.column.order() - rhs.column.order();
                }
            });

            for (Col col : cols) {
                createSql.append(" ");
                IColumnTranslator translator = OrmConfig.getTranslator(col.field.getType());
                String columnType = translator.getColumnType(col.field, col.column);
                createSql.append(col.name);
                createSql.append(" ");
                createSql.append(columnType);
                if (columnType.contains("CHAR")) {
                    createSql.append("(");
                    createSql.append(col.column.length());
                    createSql.append(")");
                }
                if (col.column.primaryKey()) {
                    createSql.append(" PRIMARY KEY");
                }
                if (col.column.autoincrement()) {
                    createSql.append(" AUTOINCREMENT");
                }
                if (!col.column.autoincrement()
                        && !TextUtils.isEmpty(col.column.defaultValue())) {
                    createSql.append(" DEFAULT ");
                    createSql.append(col.column.defaultValue());
                }
                if (col.column.notNull()) {
                    createSql.append(" NOT NULL");
                }
                createSql.append(",");
            }

            createSql.replace(createSql.length() - 1, createSql.length(), ")");
            if (OrmConfig.Debug) {
                Log.i("ORM Create Table[" + database.getPath() + "]", createSql.toString());
            }
            if (!OrmConfig.Emulate) {
                database.execSQL(createSql.toString());
                for (String str : indexes) {
                    if (OrmConfig.Debug) {
                        Log.i("ORM Create Index ", str);
                    }
                    database.execSQL(str);
                }
            }
            try {
                Db db1 = new Db();
                db1.version = table.version();
                db1.tableName = tableName;
                insertOrUpdate(database, db1, null);
            }catch (Exception exp) {
                if (OrmConfig.Debug) {
                    Log.w("ORM", exp.getMessage(), exp);
                }
            }
            return true;
        } else {
            throw new RuntimeException(tableClass.getName() + " is not an orm table.");
        }
    }

    public static boolean createTable(SQLiteDatabase database, Class<?> tableClass) {
        return createTable(database, tableClass, null);
    }

    private static <T> long insertInternal(SQLiteStatement statement, TableStruct struct, T entity) {
        int index = 0;
        for (FCMap fcmap : struct.fcmaps) {
            if (!fcmap.autoincrement) {
                bindArg(++index, statement, fcmap.dataType, fcmap.translator.getColumnValue(fcmap.field, entity));
            }
        }
        if (!OrmConfig.Emulate) {
            return statement.executeInsert();
        }
        return 0;
    }

    private static <T> int updateInternal(SQLiteDatabase database, SQLiteStatement statement, TableStruct struct, T entity) {
        Object pkValue = null;
        FCMap.DataType pkType = FCMap.DataType.String;
        int index = 0;
        for (FCMap fcmap : struct.fcmaps) {
            if (fcmap.column.primaryKey()) {
                pkType = fcmap.dataType;
                pkValue = fcmap.translator.getColumnValue(fcmap.field, entity);
            } else {
                bindArg(++index, statement, fcmap.dataType, fcmap.translator.getColumnValue(fcmap.field, entity));
            }
        }
        bindArg(++index, statement, pkType, pkValue);
        if (!OrmConfig.Emulate) {
            statement.execute();
            return getLastChanges(database);
        }
        return 0;
    }

    private static <T> int updateInternal(SQLiteDatabase database, TableStruct struct, T object, String baseColumn) {
        ArrayList<Object> params = new ArrayList<>();
        Object pkValue = null;
        String sql;
        if (TextUtils.isEmpty(baseColumn)) {
            for (FCMap fcmap : struct.fcmaps) {
                if (fcmap.column.primaryKey()) {
                    pkValue = fcmap.translator.getColumnValue(fcmap.field, object);
                } else {
                    params.add(fcmap.translator.getColumnValue(fcmap.field, object));
                }
            }
            params.add(pkValue);
            sql = struct.updateSql;
        } else {
            StringBuilder whereCondition = new StringBuilder(" WHERE ");
            StringBuilder updateSql = new StringBuilder();
            updateSql.append("UPDATE ");
            updateSql.append(struct.table);
            updateSql.append(" SET ");
            for (FCMap fcmap : struct.fcmaps) {
                String columnName = fcmap.columnName;
                if (columnName.equals(baseColumn)) {
                    whereCondition.append(columnName);
                    whereCondition.append("=?");
                    pkValue = fcmap.translator.getColumnValue(fcmap.field, object);
                } else if (!fcmap.column.autoincrement()) {
                    updateSql.append(columnName);
                    updateSql.append("=?,");
                    params.add(fcmap.translator.getColumnValue(fcmap.field, object));
                }
            }
            params.add(pkValue);
            updateSql.replace(updateSql.length() - 1, updateSql.length(), " ");
            updateSql.append(whereCondition);
            sql = updateSql.toString();
        }
        if (OrmConfig.Debug) {
            Log.i("ORM Update Table[" + database.getPath() + "]", sql);
        }
        if (!OrmConfig.Emulate) {
            database.execSQL(sql, params.toArray());
            return getLastChanges(database);
        }
        return 0;
    }

    private static int getLastChanges(SQLiteDatabase database) {
        if (!OrmConfig.Response) {
            return -1;
        }
        Cursor cursor = database.rawQuery("select changes()", null);
        if (cursor.moveToNext()) {
            int changes = cursor.getInt(0);
            cursor.close();
            return changes;
        }
        return -1;
    }

    private static int deleteInternal(SQLiteDatabase database, Object object, String baseColumn) {
        Table table = object.getClass().getAnnotation(Table.class);
        ArrayList<Object> params = new ArrayList<>();
        StringBuilder whereCondition = new StringBuilder(" WHERE ");
        Object pkValue = null;
        StringBuilder deleteSql = new StringBuilder();
        deleteSql.append("DELETE FROM ");
        String tableName = getTableName(object.getClass(), table);
        deleteSql.append(tableName);
        Field fields[] = object.getClass().getFields();
        for (Field field : fields) {
            Column column = field.getAnnotation(Column.class);
            if (column != null) {
                IColumnTranslator translator = OrmConfig.getTranslator(field.getType());
                String columnName = getColumnName(field, column);
                if (baseColumn != null && baseColumn.equals(columnName)) {
                    whereCondition.append(getColumnName(field, column));
                    whereCondition.append("=?");
                    pkValue = translator.getColumnValue(field, object);
                    break;// 单字段主键,找到后立即结束查找
                } else if (column.primaryKey()) {
                    whereCondition.append(getColumnName(field, column));
                    whereCondition.append("=?");
                    pkValue = translator.getColumnValue(field, object);
                    break;// 单字段主键,找到后立即结束查找
                }
            }
        }
        params.add(pkValue);
        deleteSql.append(whereCondition);
        if (OrmConfig.Debug) {
            Log.i("ORM Delete From Table[" + database.getPath() + "]", deleteSql.toString());
        }
        if (!OrmConfig.Emulate) {
            database.execSQL(deleteSql.toString(), params.toArray());
            return getLastChanges(database);
        }
        return 0;
    }

    private static int deleteAllInternal(SQLiteDatabase database, String tableName) {
        if (!OrmConfig.Emulate) {
            database.execSQL("delete from " + tableName + " where 1=1");
            return getLastChanges(database);
        }
        return 0;
    }

    public static <T> long insert(SQLiteDatabase database, T entity, Context context) {
        checkOrmTableInstance(entity);
        boolean innerOpenTransaction = false;
        try {
            if (!database.inTransaction()) {
                database.beginTransaction();
                innerOpenTransaction = true;
            }
            TableStruct struct = buildTableStruct(entity.getClass());
            SQLiteStatement statement = null;
            long rowId = -1;
            try {
                statement = database.compileStatement(struct.insertSql);
                rowId = insertInternal(statement, struct, entity);
            } finally {
                safeClose(statement);
            }
            if (innerOpenTransaction) {
                database.setTransactionSuccessful();
            }
            if (OrmConfig.Notify && rowId != -1) {
                notifyChange(struct.table, context);
            }
            return rowId;
        } finally {
            if (innerOpenTransaction) {
                database.endTransaction();
            }
        }
    }

    public static <T> int update(SQLiteDatabase database, T entity, Context context) {
        checkOrmTableInstance(entity);
        boolean innerOpenTransaction = false;
        try {
            if (!database.inTransaction()) {
                database.beginTransaction();
                innerOpenTransaction = true;
            }
            TableStruct struct = buildTableStruct(entity.getClass());
            int rowCount = updateInternal(database, struct, entity, null);
            if (innerOpenTransaction) {
                database.setTransactionSuccessful();
            }
            if (OrmConfig.Notify && rowCount > 0) {
                notifyChange(struct.table, context);
            }
            return rowCount;
        } finally {
            if (innerOpenTransaction) {
                database.endTransaction();
            }
        }
    }

    public static <T> long insertOrUpdate(SQLiteDatabase database, T entity, Context context) {
        checkOrmTableInstance(entity);
        boolean innerOpenTransaction = false;
        try {
            if (!database.inTransaction()) {
                database.beginTransaction();
                innerOpenTransaction = true;
            }
            TableStruct struct = buildTableStruct(entity.getClass());
            SQLiteStatement statement = null;
            SQLiteStatement statementUpdate = null;
            long result = -1;
            try {
                statement = database.compileStatement(struct.insertSql);
                statementUpdate = database.compileStatement(struct.updateSql);
                result = updateInternal(database, statementUpdate, struct, entity);
                if (result < 1) {
                    result = insertInternal(statement, struct, entity);
                }
                if (innerOpenTransaction) {
                    database.setTransactionSuccessful();
                }
                if (OrmConfig.Notify && result != 0) {
                    notifyChange(struct.table, context);
                }
            } finally {
                safeClose(statement);
                safeClose(statementUpdate);
            }
            return result;
        } finally {
            if (innerOpenTransaction) {
                database.endTransaction();
            }
        }
    }

    public static int delete(SQLiteDatabase database, Object object, Context context) {
        checkOrmTableInstance(object);
        boolean innerOpenTransaction = false;
        try {
            if (!database.inTransaction()) {
                database.beginTransaction();
                innerOpenTransaction = true;
            }
            int count = deleteInternal(database, object, null);
            if (innerOpenTransaction) {
                database.setTransactionSuccessful();
            }
            if (OrmConfig.Notify && count > 0) {
                notifyChange(getTableName(object.getClass()), context);
            }
            return count;
        } finally {
            if (innerOpenTransaction) {
                database.endTransaction();
            }
        }
    }

    public static <T> int updateBy(SQLiteDatabase database, T entity, String column, Context context) {
        checkOrmTableInstance(entity);
        boolean innerOpenTransaction = false;
        try {
            if (!database.inTransaction()) {
                database.beginTransaction();
                innerOpenTransaction = true;
            }
            TableStruct struct = buildTableStruct(entity.getClass());
            int rowCount = updateInternal(database, struct, entity, column);
            if (innerOpenTransaction) {
                database.setTransactionSuccessful();
            }
            if (OrmConfig.Notify && rowCount > 0) {
                notifyChange(struct.table, context);
            }
            return rowCount;
        } finally {
            if (innerOpenTransaction) {
                database.endTransaction();
            }
        }
    }

    public static int deleteBy(SQLiteDatabase database, Object object, String column, Context context) {
        checkOrmTableInstance(object);
        boolean innerOpenTransaction = false;
        try {
            if (!database.inTransaction()) {
                database.beginTransaction();
                innerOpenTransaction = true;
            }
            int count = deleteInternal(database, object, column);
            if (innerOpenTransaction) {
                database.setTransactionSuccessful();
            }
            if (OrmConfig.Notify && count > 0) {
                notifyChange(getTableName(object.getClass()), context);
            }
            return count;
        } finally {
            if (innerOpenTransaction) {
                database.endTransaction();
            }
        }
    }

    public static <T> long[] insertAll(SQLiteDatabase database, T[] objects, Context context) {
        if (objects != null) {
            boolean innerOpenTransaction = false;
            try {
                if (!OrmConfig.Emulate && !database.inTransaction()) {
                    database.beginTransaction();
                    innerOpenTransaction = true;
                }
                long[] rowIds = new long[objects.length];
                if (objects.length > 0) {
                    checkOrmTableInstance(objects[0]);
                    TableStruct struct = buildTableStruct(objects[0].getClass());
                    SQLiteStatement statement = null;
                    try {
                        statement = database.compileStatement(struct.insertSql);
                        for (int i = 0; i < objects.length; i++) {
                            rowIds[i] = insertInternal(statement, struct, objects[i]);
                        }
                    } finally {

                        safeClose(statement);
                    }
                    if (!OrmConfig.Emulate) {
                        if (innerOpenTransaction) {
                            database.setTransactionSuccessful();
                        }
                        String tableName = getTableName(objects[0].getClass());
                        notifyChange(tableName, context);
                    }
                }
                return rowIds;
            } finally {
                if (!OrmConfig.Emulate && innerOpenTransaction) {
                    database.endTransaction();
                }
            }
        }
        throw new NullPointerException("objects is null");
    }

    public static <T> long[] insertOrUpdateAll(SQLiteDatabase database, T[] objects, Context context) {
        if (objects != null) {
            boolean innerOpenTransaction = false;
            try {
                if (!database.inTransaction()) {
                    database.beginTransaction();
                    innerOpenTransaction = true;
                }
                long[] rowIds = new long[objects.length];
                if (objects.length > 0) {
                    checkOrmTableInstance(objects[0]);
                    TableStruct struct = buildTableStruct(objects[0].getClass());
                    SQLiteStatement statement = null;
                    SQLiteStatement statementUpdate = null;
                    try {
                        statement = database.compileStatement(struct.insertSql);
                        statementUpdate = database.compileStatement(struct.updateSql);
                        for (int i = 0; i < objects.length; i++) {
                            long count = updateInternal(database, statementUpdate, struct, objects[i]);
                            if (count < 1) {
                                rowIds[i] = insertInternal(statement, struct, objects[i]);
                            } else {
                                rowIds[i] = getLastChanges(database);
                            }
                        }
                        if (innerOpenTransaction) {
                            database.setTransactionSuccessful();
                        }
                    } finally {
                        safeClose(statement);
                        safeClose(statementUpdate);
                    }
                    String tableName = getTableName(objects[0].getClass());
                    notifyChange(tableName, context);
                }
                return rowIds;
            } finally {
                if (innerOpenTransaction) {
                    database.endTransaction();
                }
            }
        }
        throw new NullPointerException("objects is null");
    }

    public static int clear(SQLiteDatabase database, Class<?> tClass, Context context) {
        if (tClass.getAnnotation(Table.class) == null) {
            throw new RuntimeException(tClass.getName() + " is not an orm table instance.");
        }
        boolean innerOpenTransaction = false;
        try {
            if (!database.inTransaction()) {
                database.beginTransaction();
                innerOpenTransaction = true;
            }
            int count = deleteAllInternal(database, getTableName(tClass));
            if (innerOpenTransaction) {
                database.setTransactionSuccessful();
            }
            if (OrmConfig.Notify && count > 0) {
                notifyChange(getTableName(tClass), context);
            }
            return count;
        } finally {
            if (innerOpenTransaction) {
                database.endTransaction();
            }
        }
    }

    public static void checkOrmTableInstance(final Object object) {
        if (object.getClass().getAnnotation(Table.class) == null) {
            throw new RuntimeException(object.getClass().getName() + " is not an orm table instance.");
        }
    }

    public static String getColumnName(final Field field) {
        return getColumnName(field, field.getAnnotation(Column.class));
    }

    public static String getColumnName(final Field field, final Column column) {
        if (column != null) {
            return TextUtils.isEmpty(column.name()) ? field.getName() : column.name();
        } else {
            throw new RuntimeException(field.getName() + " is not an orm column.");
        }
    }

    public static String getTableName(final Class<?> tableClass) {
        return getTableName(tableClass, tableClass.getAnnotation(Table.class));
    }

    public static String getTableName(final Class<?> tableClass, final Table table) {
        if (table != null) {
            return TextUtils.isEmpty(table.name()) ? tableClass.getSimpleName() : table.name();
        } else {
            throw new RuntimeException(tableClass.getName() + " is not an orm table.");
        }
    }

    public static TableStruct buildTableStruct(Class<?> tableClass) {
        Table table = tableClass.getAnnotation(Table.class);
        String tableName = getTableName(tableClass, table);
        TableStruct tableStruct = TableStructCache.get(tableName);
        if (tableStruct != null) {
            return tableStruct;
        }
        tableStruct = new TableStruct();
        tableStruct.table = tableName;
        ArrayList<FCMap> fcmaps = new ArrayList<>();
        StringBuilder values = new StringBuilder(" VALUES(");
        StringBuilder insertSql = new StringBuilder();
        insertSql.append("INSERT INTO ");
        insertSql.append(tableName);
        insertSql.append("(");
        Field fields[] = tableClass.getFields();
        int i = 0;
        for (Field field : fields) {
            Column column = field.getAnnotation(Column.class);
            if (column != null) {
                FCMap fcmap = new FCMap();
                fcmap.field = field;
                fcmap.dataType = getDataType(field);
                fcmap.autoincrement = column.autoincrement();
                fcmap.columnName = getColumnName(field, column);
                fcmap.translator = OrmConfig.getTranslator(field.getType());
                fcmap.index = i++;
                fcmap.column = column;
                if (!column.autoincrement()) {
                    insertSql.append(fcmap.columnName);
                    insertSql.append(",");
                    values.append("?,");
                }
                fcmaps.add(fcmap);
            }
        }
        insertSql.replace(insertSql.length() - 1, insertSql.length(), ")");
        values.replace(values.length() - 1, values.length(), ")");
        insertSql.append(values);
        tableStruct.fcmaps = fcmaps;
        tableStruct.insertSql = insertSql.toString();
        buildUpdateByPrimaryKeySql(tableStruct);
        TableStructCache.put(tableStruct);
        return tableStruct;
    }

    private static void buildUpdateByPrimaryKeySql(TableStruct tableStruct) {
        StringBuilder whereCondition = new StringBuilder(" WHERE ");
        StringBuilder updateSql = new StringBuilder();
        updateSql.append("UPDATE ");
        updateSql.append(tableStruct.table);
        updateSql.append(" SET ");
        for (FCMap fcmap : tableStruct.fcmaps) {
            String columnName = fcmap.columnName;
            if (!fcmap.column.autoincrement() && !fcmap.column.primaryKey()) {
                updateSql.append(columnName);
                updateSql.append("=?,");
            } else if (fcmap.column.primaryKey()) {
                whereCondition.append(columnName);
                whereCondition.append("=?");
            }
        }
        updateSql.replace(updateSql.length() - 1, updateSql.length(), " ");
        updateSql.append(whereCondition);
        tableStruct.updateSql = updateSql.toString();
    }

    private static FCMap.DataType getDataType(Field field) {
        Class<?> type = field.getType();
        if (int.class.isAssignableFrom(type)) {
            return FCMap.DataType.Int;
        } else if (long.class.isAssignableFrom(type)) {
            return FCMap.DataType.Long;
        } else if (float.class.isAssignableFrom(type)) {
            return FCMap.DataType.Float;
        } else if (double.class.isAssignableFrom(type)) {
            return FCMap.DataType.Double;
        } else if (byte[].class.isAssignableFrom(type)) {
            return FCMap.DataType.Blob;
        } else if (Date.class.isAssignableFrom(type)) {
            return FCMap.DataType.Date;
        } else if (boolean.class.isAssignableFrom(type)) {
            return FCMap.DataType.Boolean;
        } else {
            return FCMap.DataType.String;
        }
    }

    private static void bindArg(int index, SQLiteStatement statement, FCMap.DataType dataType, Object value) {
        if (value == null) {
            statement.bindNull(index);
        } else {
            switch (dataType) {
                case Int:
                    statement.bindLong(index, (int) value);
                    break;
                case Long:
                    statement.bindLong(index, (long) value);
                    break;
                case Float:
                    statement.bindDouble(index, (float) value);
                    break;
                case Double:
                    statement.bindDouble(index, (double) value);
                    break;
                case Date:
                    statement.bindLong(index, ((Date) value).getTime());
                    break;
                case Boolean:
                    statement.bindLong(index, ((boolean) value) ? 1 : 0);
                    break;
                case Blob:
                    statement.bindBlob(index, (byte[]) value);
                    break;
                default:
                    statement.bindString(index, String.valueOf(value));
                    break;
            }
        }
    }

    private static void safeClose(SQLiteClosable closable) {
        if (closable != null) {
            try {
                closable.releaseReference();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void notifyChange(final String s, final Context context) {
        if (OrmConfig.Notify && context != null && !TextUtils.isEmpty(s)) {
            context.getContentResolver().notifyChange(Uri.parse("content://orm/" + s), null, false);
        }
    }
}
