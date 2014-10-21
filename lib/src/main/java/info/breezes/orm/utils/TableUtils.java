package info.breezes.orm.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import info.breezes.orm.OrmConfig;
import info.breezes.orm.annotation.Column;
import info.breezes.orm.annotation.Table;
import info.breezes.orm.translator.IColumnTranslator;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by jianxingqiao on 5/4/2014.
 */
public class TableUtils {

    static class Col {
        String name;
        Column column;
        Field field;
    }

    public static boolean createTable(SQLiteDatabase database, Class<?> tableClass) {
        Table table = (Table) tableClass.getAnnotation(Table.class);
        if (table != null) {
            ArrayList<String> indexes = new ArrayList<String>();
            StringBuilder indexSql = new StringBuilder();
            StringBuilder createSql = new StringBuilder();
            createSql.append("CREATE TABLE IF NOT EXISTS ");
            createSql.append(getTableName(tableClass, table));
            createSql.append("(");
            ArrayList<Col> cols = new ArrayList<Col>();
            Field fields[] = tableClass.getFields();
            for (Field field : fields) {
                indexSql.setLength(0);
                Column column = (Column) field.getAnnotation(Column.class);
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
                if (!col.column.autoincrement() && !TextUtils.isEmpty(col.column.defaultValue())) {
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
            database.execSQL(createSql.toString());
            for (String str : indexes) {
                if (OrmConfig.Debug) {
                    Log.i("ORM Create Index ", str);
                }
                database.execSQL(str);
            }
            return true;
        } else {
            throw new RuntimeException(tableClass.getName() + " is not an orm table.");
        }
    }

    private static SQLStruct buildInsertSQL(SQLiteDatabase database, String table, Class<?> tableClass) {
        SQLStruct statementStruct = new SQLStruct();
        statementStruct.table = table;
        ArrayList<Field> params = new ArrayList<Field>();
        StringBuilder values = new StringBuilder(" VALUES(");
        StringBuilder insertSql = new StringBuilder();
        insertSql.append("INSERT INTO ");
        insertSql.append(table);
        insertSql.append("(");
        Field fields[] = tableClass.getFields();
        for (Field field : fields) {
            Column column = (Column) field.getAnnotation(Column.class);
            if (column != null && !column.autoincrement()) {
                IColumnTranslator translator = OrmConfig.getTranslator(field.getType());
                insertSql.append(getColumnName(field, column));
                insertSql.append(",");
                values.append("?,");
                params.add(field);
            }
        }
        insertSql.replace(insertSql.length() - 1, insertSql.length(), ")");
        values.replace(values.length() - 1, values.length(), ")");
        insertSql.append(values);
        statementStruct.params = params;
        statementStruct.sql = insertSql.toString();
        return statementStruct;
    }

    private static long insertInternal(SQLiteDatabase database, Object object, Context context) {
        Table table = (Table) object.getClass().getAnnotation(Table.class);
        String tableName = getTableName(object.getClass(), table);
        SQLStruct struct = SQLiteSQLCache.getStatement(tableName);
        if (struct == null) {
            struct = buildInsertSQL(database, tableName, object.getClass());
            SQLiteSQLCache.putStatement(struct);
        }

        ArrayList<Object> params = new ArrayList<Object>();

        for (Field field : struct.params) {
            params.add(OrmConfig.getTranslator(field.getType()).getColumnValue(field, object));
        }
        if (OrmConfig.Debug) {
            Log.i("ORM Insert Into Table[" + database.getPath() + "]", struct.sql);
        }
        database.execSQL(struct.sql, params.toArray());
        long rowId = getLastInsertRowId(database);
        if (rowId > 0) {
            notifyChange(tableName, context);
        }
        return rowId;

    }

    private static long getLastInsertRowId(SQLiteDatabase database) {
        Cursor cursor = database.rawQuery("select last_insert_rowid()", null);
        if (cursor.moveToNext()) {
            long rowId = cursor.getLong(0);
            cursor.close();
            return rowId;
        }
        return -1;
    }

    private static int updateInternal(SQLiteDatabase database, Object object, String baseColumn, Context context) {
        Table table = (Table) object.getClass().getAnnotation(Table.class);
        String tableName = getTableName(object.getClass(), table);
        ArrayList<Object> params = new ArrayList<Object>();
        StringBuilder whereCondition = new StringBuilder(" WHERE ");
        Object pkValue = null;
        StringBuilder updateSql = new StringBuilder();
        updateSql.append("UPDATE ");
        updateSql.append(tableName);
        updateSql.append(" SET ");
        Field fields[] = object.getClass().getFields();
        for (Field field : fields) {
            Column column = (Column) field.getAnnotation(Column.class);
            if (column != null) {
                IColumnTranslator translator = OrmConfig.getTranslator(field.getType());
                String columnName = getColumnName(field, column);
                if (baseColumn != null) {
                    if (columnName.equals(baseColumn)) {
                        whereCondition.append(columnName);
                        whereCondition.append("=?");
                        pkValue = translator.getColumnValue(field, object);
                    } else if (!column.autoincrement()) {
                        updateSql.append(columnName);
                        updateSql.append("=?,");
                        params.add(translator.getColumnValue(field, object));
                    }
                } else if (!column.autoincrement() && !column.primaryKey()) {
                    updateSql.append(columnName);
                    updateSql.append("=?,");
                    params.add(translator.getColumnValue(field, object));
                } else if (column.primaryKey()) {
                    whereCondition.append(columnName);
                    whereCondition.append("=?");
                    pkValue = translator.getColumnValue(field, object);
                }
            }
        }
        params.add(pkValue);
        updateSql.replace(updateSql.length() - 1, updateSql.length(), " ");
        updateSql.append(whereCondition);
        if (OrmConfig.Debug) {
            Log.i("ORM Update Table[" + database.getPath() + "]", updateSql.toString());
        }
        database.execSQL(updateSql.toString(), params.toArray());
        int changes = getLastChanges(database);
        if (changes > 0) {
            notifyChange(tableName, context);
        }
        return changes;
    }

    private static int getLastChanges(SQLiteDatabase database) {
        Cursor cursor = database.rawQuery("select changes()", null);
        if (cursor.moveToNext()) {
            int changes = cursor.getInt(0);
            cursor.close();
            return changes;
        }
        return -1;
    }

    private static int deleteInternal(SQLiteDatabase database, Object object, String baseColumn, Context context) {
        Table table = (Table) object.getClass().getAnnotation(Table.class);
        ArrayList<Object> params = new ArrayList<Object>();
        StringBuilder whereCondition = new StringBuilder(" WHERE ");
        Object pkValue = null;
        StringBuilder deleteSql = new StringBuilder();
        deleteSql.append("DELETE FROM ");
        String tableName = getTableName(object.getClass(), table);
        deleteSql.append(tableName);
        Field fields[] = object.getClass().getFields();
        for (Field field : fields) {
            Column column = (Column) field.getAnnotation(Column.class);
            if (column != null) {
                IColumnTranslator translator = OrmConfig.getTranslator(field.getType());
                String columnName = getColumnName(field, column);
                if (baseColumn != null && baseColumn.equals(columnName)) {
                    whereCondition.append(getColumnName(field, column));
                    whereCondition.append("=?");
                    pkValue = translator.getColumnValue(field, object);
                    break;//单字段主键,找到后立即结束查找
                } else if (column.primaryKey()) {
                    whereCondition.append(getColumnName(field, column));
                    whereCondition.append("=?");
                    pkValue = translator.getColumnValue(field, object);
                    break;//单字段主键,找到后立即结束查找
                }
            }
        }
        params.add(pkValue);
        deleteSql.replace(deleteSql.length() - 1, deleteSql.length(), " ");
        deleteSql.append(whereCondition);
        if (OrmConfig.Debug) {
            Log.i("ORM Delete From Table[" + database.getPath() + "]", deleteSql.toString());
        }
        database.execSQL(deleteSql.toString(), params.toArray());
        int changes = getLastChanges(database);
        if (changes > 0) {
            notifyChange(tableName, context);
        }
        return changes;
    }


    private static int deleteAllInternal(SQLiteDatabase database, String tableName, Context context) {
        if (OrmConfig.Debug) {
            Log.i("ORM Clear Table [" + database.getPath() + "]", "delete from " + tableName + " where 1=1");
        }
        database.execSQL("delete from " + tableName + " where 1=1");
        int changes = getLastChanges(database);
        if (changes > 0) {
            notifyChange(tableName, context);
        }
        return changes;
    }

    public static long insert(SQLiteDatabase database, Object object, Context context) {
        checkOrmTableInstance(object);
        boolean innerOpenTransaction = false;
        try {
            if (!database.inTransaction()) {
                database.beginTransaction();
                innerOpenTransaction = true;
            }
            long rowId = insertInternal(database, object, context);
            if (innerOpenTransaction) {
                database.setTransactionSuccessful();
            }
            return rowId;
        } finally {
            if (innerOpenTransaction) {
                database.endTransaction();
            }
        }
    }

    public static int update(SQLiteDatabase database, Object object, Context context) {
        checkOrmTableInstance(object);
        boolean innerOpenTransaction = false;
        try {
            if (!database.inTransaction()) {
                database.beginTransaction();
                innerOpenTransaction = true;
            }
            int rowCount = updateInternal(database, object, null, context);
            if (innerOpenTransaction) {
                database.setTransactionSuccessful();
            }
            return rowCount;
        } finally {
            if (innerOpenTransaction) {
                database.endTransaction();
            }
        }
    }

    public static long insertOrUpdate(SQLiteDatabase database, Object object, Context context) {
        checkOrmTableInstance(object);
        boolean innerOpenTransaction = false;
        try {
            if (!database.inTransaction()) {
                database.beginTransaction();
                innerOpenTransaction = true;
            }
            long count = updateInternal(database, object, null, context);
            if (count < 1) {
                count = insertInternal(database, object, context);
            }
            if (innerOpenTransaction) {
                database.setTransactionSuccessful();
            }
            return count;
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
            int count = deleteInternal(database, object, null, context);
            if (innerOpenTransaction) {
                database.setTransactionSuccessful();
            }
            return count;
        } finally {
            if (innerOpenTransaction) {
                database.endTransaction();
            }
        }
    }

    public static int updateBy(SQLiteDatabase database, Object object, String column, Context context) {
        checkOrmTableInstance(object);
        boolean innerOpenTransaction = false;
        try {
            if (!database.inTransaction()) {
                database.beginTransaction();
                innerOpenTransaction = true;
            }
            int rowCount = updateInternal(database, object, column, context);
            if (innerOpenTransaction) {
                database.setTransactionSuccessful();
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
            int count = deleteInternal(database, object, column, context);
            if (innerOpenTransaction) {
                database.setTransactionSuccessful();
            }
            return count;
        } finally {
            if (innerOpenTransaction) {
                database.endTransaction();
            }
        }
    }

    public static long[] insertAll(SQLiteDatabase database, Object[] objects, Context context) {
        if (objects != null) {
            boolean innerOpenTransaction = false;
            try {
                if (!database.inTransaction()) {
                    database.beginTransaction();
                    innerOpenTransaction = true;
                }
                long[] rowIds = new long[objects.length];
                for (int i = 0; i < objects.length; i++) {
                    Object object = objects[i];
                    checkOrmTableInstance(object);
                    rowIds[i] = insertInternal(database, object, null);
                }
                if (innerOpenTransaction) {
                    database.setTransactionSuccessful();
                }
                String tableName = getTableName(objects[0].getClass());
                notifyChange(tableName, context);
                return rowIds;
            } finally {
                if (innerOpenTransaction) {
                    database.endTransaction();
                }
            }
        }
        throw new NullPointerException("objects is null");
    }

    public static long[] insertOrUpdateAll(SQLiteDatabase database, Object[] objects, Context context) {
        if (objects != null) {
            boolean innerOpenTransaction = false;
            try {
                if (!database.inTransaction()) {
                    database.beginTransaction();
                    innerOpenTransaction = true;
                }
                long[] rowIds = new long[objects.length];
                for (int i = 0; i < objects.length; i++) {
                    Object object = objects[i];
                    checkOrmTableInstance(object);
                    long count = updateInternal(database, object, null, null);
                    if (count < 1) {
                        count = insertInternal(database, object, null);
                        rowIds[i] = getLastInsertRowId(database);
                    } else {
                        rowIds[i] = getLastChanges(database);
                    }
                }
                if (innerOpenTransaction) {
                    database.setTransactionSuccessful();
                }
                String tableName = getTableName(objects[0].getClass());
                notifyChange(tableName, context);
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
        if ((Table) tClass.getAnnotation(Table.class) == null) {
            throw new RuntimeException(tClass.getName() + " is not an orm table instance.");
        }
        boolean innerOpenTransaction = false;
        try {
            if (!database.inTransaction()) {
                database.beginTransaction();
                innerOpenTransaction = true;
            }
            int count = deleteAllInternal(database, getTableName(tClass), context);
            if (innerOpenTransaction) {
                database.setTransactionSuccessful();
            }
            return count;
        } finally {
            if (innerOpenTransaction) {
                database.endTransaction();
            }
        }
    }


    public static void checkOrmTableInstance(final Object object) {
        if ((Table) object.getClass().getAnnotation(Table.class) == null) {
            throw new RuntimeException(object.getClass().getName() + " is not an orm table instance.");
        }
    }

    public static String getColumnName(final Field field) {
        return getColumnName(field, (Column) field.getAnnotation(Column.class));
    }

    public static String getColumnName(final Field field, final Column column) {
        if (column != null) {
            return TextUtils.isEmpty(column.name()) ? field.getName() : column.name();
        } else {
            throw new RuntimeException(field.getName() + " is not an orm column.");
        }
    }

    public static String getTableName(final Class<?> tableClass) {
        return getTableName(tableClass, (Table) tableClass.getAnnotation(Table.class));
    }

    public static String getTableName(final Class<?> tableClass, final Table table) {
        if (table != null) {
            return TextUtils.isEmpty(table.name()) ? tableClass.getSimpleName() : table.name();
        } else {
            throw new RuntimeException(tableClass.getName() + " is not an orm table.");
        }
    }

    private static void notifyChange(final String s, final Context context) {
        if (context != null && !TextUtils.isEmpty(s)) {
            context.getContentResolver().notifyChange(Uri.parse("content://orm/" + s), null, false);
        }
    }
}
