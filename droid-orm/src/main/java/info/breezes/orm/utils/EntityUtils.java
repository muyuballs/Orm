package info.breezes.orm.utils;


import android.content.ContentValues;

import java.lang.reflect.Field;

import info.breezes.orm.OrmConfig;
import info.breezes.orm.annotation.Column;

public class EntityUtils {
    public static ContentValues buildValues(Object entity) {
        ContentValues values = new ContentValues();
        Field fields[] = entity.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            Column column = field.getAnnotation(Column.class);
            if (column != null && !column.autoincrement()) {
                String columnName = TableUtils.getColumnName(field, column);
                Class<?> fieldType = field.getType();
                Object value = OrmConfig.getTranslator(fieldType).getColumnValue(field, entity);
                if (value != null) {
                    if (fieldType.equals(int.class) || fieldType.equals(Integer.class)) {
                        values.put(columnName, (int) value);
                    } else if (fieldType.equals(long.class) || fieldType.equals(Long.class)) {
                        values.put(columnName, (long) value);
                    } else if (fieldType.equals(float.class) || fieldType.equals(Float.class)) {
                        values.put(columnName, (float) value);
                    } else if (fieldType.equals(double.class) || fieldType.equals(Double.class)) {
                        values.put(columnName, (double) value);
                    } else if (fieldType.equals(byte.class) || fieldType.equals(Byte.class)) {
                        values.put(columnName, (byte) value);
                    } else if (fieldType.equals(boolean.class) || fieldType.equals(Boolean.class)) {
                        values.put(columnName, (boolean) value);
                    } else if (fieldType.equals(String.class)) {
                        values.put(columnName, (String) value);
                    } else if (fieldType.equals(byte[].class)) {
                        values.put(columnName, (byte[]) value);
                    }
                } else {
                    values.putNull(columnName);
                }
            }
        }
        return values;
    }
}
