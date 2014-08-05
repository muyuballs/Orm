package info.breezes.orm.translator;

import android.database.Cursor;
import android.text.TextUtils;
import info.breezes.orm.annotation.Column;

import java.lang.reflect.Field;
import java.util.Date;

/**
 * Created by Qiao on 2014/5/19.
 */
public class DefaultColumnTranslator implements IColumnTranslator {

    @Override
    public String getColumnType(Field field, Column column) {
        Class<?> type = field.getType();
        if (byte[].class.isAssignableFrom(type)) {
            return "BLOB";
        }
        if (Date.class.isAssignableFrom(type)) {
            return "NVARCHAR";
        }
        if (column.autoincrement() && column.primaryKey()) {
            return "INTEGER";
        }
        if (String.class.isAssignableFrom(type)) {
            return "NVARCHAR";
        }
        if (boolean.class.isAssignableFrom(type)) {
            return "INTEGER";
        }
        if (float.class.isAssignableFrom(type) || double.class.isAssignableFrom(type) || long.class.isAssignableFrom(type)) {
            return "REAL";
        }
        if(int.class.isAssignableFrom(type)){
            return "INTEGER";
        }
        return TextUtils.isEmpty(column.type()) ? "TEXT" : column.type();
    }

    @Override
    public Object getColumnValue(Field field, Object obj) {
        try {
            if (Date.class.isAssignableFrom(field.getType())) {
                Date date = (Date) field.get(obj);
                return ""+date.getTime();
            }
            if (boolean.class.isAssignableFrom(field.getType())) {
                boolean bool = (Boolean) field.get(obj);
                return bool ? 1 : 0;
            }
            return field.get(obj);
        } catch (IllegalAccessException exp) {
            throw new RuntimeException(exp);
        }
    }

    @Override
    public Object readColumnValue(Cursor cursor, int index, Field field) {
        Class<?> type = field.getType();
        if (int.class.isAssignableFrom(type)) {
            return cursor.getInt(index);
        } else if (long.class.isAssignableFrom(type)) {
            return cursor.getLong(index);
        } else if (float.class.isAssignableFrom(type)) {
            return cursor.getFloat(index);
        } else if (double.class.isAssignableFrom(type)) {
            return cursor.getDouble(index);
        } else if (String.class.isAssignableFrom(type)) {
            return cursor.getString(index);
        } else if (byte[].class.isAssignableFrom(type)) {
            return cursor.getBlob(index);
        } else if (Date.class.isAssignableFrom(type)) {
            return parseToDate(cursor.getString(index));
        } else if (boolean.class.isAssignableFrom(type)) {
            return cursor.getInt(index) == 1;
        } else {
            return null;
        }
    }

    private Date parseToDate(String string) {
        if (string == null) {
            return null;
        } else {
            long time = Long.parseLong(string);
            return new Date(time);
        }
    }
}
