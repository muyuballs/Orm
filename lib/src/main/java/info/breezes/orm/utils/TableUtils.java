package info.breezes.orm.utils;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;
import info.breezes.orm.OrmConfig;
import info.breezes.orm.annotation.Column;
import info.breezes.orm.annotation.Table;
import info.breezes.orm.tranlator.IColumnTranslator;


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by jianxingqiao on 5/4/2014.
 */
public class TableUtils {
   static class Col{
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
            ArrayList<Col> cols=new ArrayList<Col>();
            Field fields[] = tableClass.getFields();
            for (Field field : fields) {
                indexSql.setLength(0);
                Column column = (Column) field.getAnnotation(Column.class);
                if (column != null) {
                    Col col=new Col();
                    col.name=getColumnName(field, column);
                    col.column=column;
                    col.field=field;
                    cols.add(col);
                    if (column.uniqueIndex()) {
                        indexSql.append("CREATE UNIQUE INDEX ");
                        indexSql.append(getTableName(tableClass, table));
                        indexSql.append(".uq.");
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
            Collections.sort(cols,new Comparator<Col>() {
                @Override
                public int compare(Col lhs, Col rhs) {
                    return lhs.column.number() - rhs.column.number();
                }
            });

            for (Col col:cols){
                createSql.append(" ");
                IColumnTranslator translator = OrmConfig.getTranslator(col.field.getType());
                String columnType = translator.getColumnType(col.field,col.column);
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
            Log.i("ORM Create Table ", createSql.toString());
            database.execSQL(createSql.toString());
            for (String str : indexes) {
                Log.i("ORM Create Index ", str);
                database.execSQL(str);
            }
            return true;
        } else {
            throw new RuntimeException(tableClass.getName() + " is not an orm table.");
        }
    }

    public static long insert(SQLiteDatabase database, Object object) {
        checkOrmTableInstance(object);
        Table table = (Table) object.getClass().getAnnotation(Table.class);
        ArrayList<Object> params = new ArrayList<Object>();
        StringBuilder values = new StringBuilder(" VALUES(");
        StringBuilder insertSql = new StringBuilder();
        insertSql.append("INSERT INTO ");
        insertSql.append(getTableName(object.getClass(), table));
        insertSql.append("(");
        Field fields[] = object.getClass().getFields();
        for (Field field : fields) {
            Column column = (Column) field.getAnnotation(Column.class);
            if (column != null && !column.autoincrement()) {
                IColumnTranslator translator = OrmConfig.getTranslator(field.getType());
                insertSql.append(getColumnName(field, column));
                insertSql.append(",");
                values.append("?,");
                params.add(translator.getColumnValue(field, object));
            }
        }
        insertSql.replace(insertSql.length() - 1, insertSql.length(), ")");
        values.replace(values.length() - 1, values.length(), ")");
        insertSql.append(values);
        Log.i("ORM Insert Into Table ", insertSql.toString());
        try {
            database.beginTransaction();
            database.execSQL(insertSql.toString(), params.toArray());
            long rowId = getLastInsertRowId(database);
            database.setTransactionSuccessful();
            return rowId;
        } finally {
            database.endTransaction();
        }
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

    public static int update(SQLiteDatabase database, Object object) {
        checkOrmTableInstance(object);
        Table table = (Table) object.getClass().getAnnotation(Table.class);
        ArrayList<Object> params = new ArrayList<Object>();
        StringBuilder whereCondition = new StringBuilder(" WHERE ");
        Object pkValue = null;
        StringBuilder updateSql = new StringBuilder();
        updateSql.append("UPDATE ");
        updateSql.append(getTableName(object.getClass(), table));
        updateSql.append(" SET ");
        Field fields[] = object.getClass().getFields();
        for (Field field : fields) {
            IColumnTranslator translator = OrmConfig.getTranslator(field.getType());
            Column column = (Column) field.getAnnotation(Column.class);
            if (column != null && !column.autoincrement() && !column.primaryKey()) {
                updateSql.append(getColumnName(field, column));
                updateSql.append("=?,");
                params.add(translator.getColumnValue(field, object));
            } else if (column != null && column.primaryKey()) {
                whereCondition.append(getColumnName(field, column));
                whereCondition.append("=?");
                pkValue = translator.getColumnValue(field, object);
            }
        }
        params.add(pkValue);
        updateSql.replace(updateSql.length() - 1, updateSql.length(), " ");
        updateSql.append(whereCondition);
        Log.i("ORM Update Table ", updateSql.toString());
        try {
            database.beginTransaction();
            database.execSQL(updateSql.toString(), params.toArray());
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
        return 1;
    }

    public static void checkOrmTableInstance(Object object) {
        if ((Table) object.getClass().getAnnotation(Table.class) == null) {
            throw new RuntimeException(object.getClass().getName() + " is not an orm table instance.");
        }
    }

    public static String getColumnName(Field field) {
        return getColumnName(field, (Column) field.getAnnotation(Column.class));
    }

    public static String getColumnName(Field field, Column column) {
        if (column != null) {
            return TextUtils.isEmpty(column.name()) ? field.getName() : column.name();
        } else {
            throw new RuntimeException(field.getName() + " is not an orm column.");
        }
    }

    public static String getTableName(Class<?> tableClass) {
        return getTableName(tableClass, (Table) tableClass.getAnnotation(Table.class));
    }

    public static String getTableName(Class<?> tableClass, Table table) {
        if (table != null) {
            return TextUtils.isEmpty(table.name()) ? tableClass.getSimpleName() : table.name();
        } else {
            throw new RuntimeException(tableClass.getName() + " is not an orm table.");
        }
    }
}
