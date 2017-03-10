package info.breezes.orm.utils;


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import info.breezes.orm.FCMap;
import info.breezes.orm.Index;
import info.breezes.orm.OrmConfig;
import info.breezes.orm.annotation.Column;
import info.breezes.orm.annotation.Table;

public final class TableStructManager {
    private static HashMap<String, TableStruct> innerCache;

    static {
        innerCache = new HashMap<>();
    }

    public static void put(TableStruct statementStruct) {
        innerCache.put(statementStruct.table, statementStruct);
    }

    public static TableStruct get(String table) {
        return innerCache.get(table);
    }


    public static TableStruct buildTableStruct(Class<?> tableClass) {
        Table table = tableClass.getAnnotation(Table.class);
        String tableName = TableUtils.getTableName(tableClass, table);
        TableStruct tableStruct = get(tableName);
        if (tableStruct != null) {
            return tableStruct;
        }
        tableStruct = new TableStruct();
        tableStruct.table = tableName;
        parseTableClass(0, tableClass, tableStruct.fcmaps);
        buildIndexes(tableStruct);
        buildInsertSql(tableStruct);
        buildUpdateByPrimaryKeySql(tableStruct);
        put(tableStruct);
        return tableStruct;
    }

    public List<FCMap> buildFcMap(Class<?> tableClass) {
        ArrayList<FCMap> fcmap = new ArrayList<>();
        parseTableClass(0, tableClass, fcmap);
        return fcmap;
    }

    private static void buildIndexes(TableStruct tableStruct) {
        for (FCMap f : tableStruct.fcmaps) {
            if (f.column.uniqueIndex()) {
                Index index = new Index();
                index.type = Index.IndexType.UNIQUE;
                index.fcMap = f;
                tableStruct.indexes.add(index);
            }
        }
    }

    private static void parseTableClass(int i, Class<?> tableClass, ArrayList<FCMap> fcmaps) {
        for (Field field : tableClass.getDeclaredFields()) {
            if (contains(fcmaps, field)) {
                continue;
            }
            Column column = field.getAnnotation(Column.class);
            if (column != null) {
                field.setAccessible(true);
                FCMap fcmap = new FCMap();
                fcmap.field = field;
                fcmap.dataType = getDataType(field);
                fcmap.autoincrement = column.autoincrement();
                fcmap.columnName = TableUtils.getColumnName(field, column);
                fcmap.translator = OrmConfig.getTranslator(field.getType());
                fcmap.index = i++;
                fcmap.column = column;
                fcmaps.add(fcmap);
            }
        }
        tableClass = tableClass.getSuperclass();
        if (tableClass != null && !Object.class.equals(tableClass)) {
            parseTableClass(i, tableClass, fcmaps);
        }
    }

    private static boolean contains(ArrayList<FCMap> fcmaps, Field field) {
        for (FCMap fcMap : fcmaps) {
            if (fcMap.field.getName().equals(field.getName())) {
                return true;
            }
        }
        return false;
    }

    private static void buildInsertSql(TableStruct tableStruct) {
        StringBuilder stringBuilder = new StringBuilder("INSERT INTO ");
        StringBuilder values = new StringBuilder(" ) VALUES (");
        stringBuilder.append(tableStruct.table);
        stringBuilder.append(" ( ");
        boolean first = true;
        for (FCMap f : tableStruct.fcmaps) {
            if (!f.autoincrement) {
                if (first) {
                    first = false;
                    stringBuilder.append(f.columnName);
                    values.append("?");
                } else {
                    stringBuilder.append(",");
                    stringBuilder.append(f.columnName);
                    values.append(",?");
                }
            }
        }
        stringBuilder.append(values).append(" )");
        tableStruct.insertSql = stringBuilder.toString();
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
}
