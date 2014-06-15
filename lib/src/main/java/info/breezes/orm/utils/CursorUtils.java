package info.breezes.orm.utils;

import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import info.breezes.orm.OrmConfig;
import info.breezes.orm.annotation.Column;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Qiao on 2014/5/27.
 */
public class CursorUtils {
    public static <T> T readCurrentEntity(Class<T> type, Cursor cursor, Map<String, Integer> columnIndex) {
        long st = System.currentTimeMillis();
        try {
            T entity = type.newInstance();
            Field fields[] = type.getFields();
            for (Field field : fields) {
                Column column = field.getAnnotation(Column.class);
                if (column != null) {
                    String columnName = TableUtils.getColumnName(field, column);
                    Class<?> fieldType = field.getType();
                    Object value = OrmConfig.getTranslator(fieldType).readColumnValue(cursor, columnIndex.get(columnName), field);
                    if (value != null) {
                        field.set(entity, value);
                    }
                }
            }
            Log.d("CursorUtils", "readCurrentEntity With ColumnIndexMap cost:" + (System.currentTimeMillis() - st));
            return entity;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> readEntities(Class<T> type, Cursor cursor, Map<String, Integer> columnIndex) {
        ArrayList<T> arrayList = new ArrayList<T>();
        while (cursor.moveToNext()) {
            arrayList.add(readCurrentEntity(type, cursor, columnIndex));
        }
        return arrayList;
    }

    public static <T> T readCurrentEntity(Class<T> type, Cursor cursor) {
        long st = System.currentTimeMillis();
        try {
            T entity = type.newInstance();
            Field fields[] = type.getFields();
            for (Field field : fields) {
                Column column = field.getAnnotation(Column.class);
                if (column != null) {
                    String columnName = TableUtils.getColumnName(field);
                    Class<?> fieldType = field.getType();
                    Object value = OrmConfig.getTranslator(fieldType).readColumnValue(cursor, cursor.getColumnIndex(columnName), field);
                    if (value != null) {
                        field.set(entity, value);
                    }
                }
            }
            Log.d("CursorUtils", "readCurrentEntity cost:" + (System.currentTimeMillis() - st));
            return entity;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
