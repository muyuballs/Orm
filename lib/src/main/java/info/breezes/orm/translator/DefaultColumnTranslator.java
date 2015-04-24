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

package info.breezes.orm.translator;

import android.database.Cursor;
import android.text.TextUtils;

import info.breezes.orm.annotation.Column;

import java.lang.reflect.Field;
import java.util.Date;

public class DefaultColumnTranslator implements IColumnTranslator {

    @Override
    public String getColumnType(Field field, Column column) {
        Class<?> type = field.getType();
        if (String.class.isAssignableFrom(type)) {
            return "NVARCHAR";
        }
        if (int.class.isAssignableFrom(type)) {
            return "INTEGER";
        }
        if (float.class.isAssignableFrom(type) || double.class.isAssignableFrom(type) || long.class.isAssignableFrom(type)) {
            return "REAL";
        }
        if (byte[].class.isAssignableFrom(type)) {
            return "BLOB";
        }
        if (Date.class.isAssignableFrom(type)) {
            return "NVARCHAR";
        }
        if (column.autoincrement() && column.primaryKey()) {
            return "INTEGER";
        }
        if (boolean.class.isAssignableFrom(type)) {
            return "INTEGER";
        }
        return TextUtils.isEmpty(column.type()) ? "TEXT" : column.type();
    }

    @Override
    public Object getColumnValue(Field field, Object obj) {
        try {
            if (Date.class.isAssignableFrom(field.getType())) {
                Date date = (Date) field.get(obj);
                if (date == null) {
                    return 0;
                }
                return date.getTime();
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
        if (String.class.isAssignableFrom(type)) {
            return cursor.getString(index);
        } else if (int.class.isAssignableFrom(type)) {
            return cursor.getInt(index);
        } else if (long.class.isAssignableFrom(type)) {
            return cursor.getLong(index);
        } else if (float.class.isAssignableFrom(type)) {
            return cursor.getFloat(index);
        } else if (double.class.isAssignableFrom(type)) {
            return cursor.getDouble(index);
        } else if (byte[].class.isAssignableFrom(type)) {
            return cursor.getBlob(index);
        } else if (Date.class.isAssignableFrom(type)) {
            return parseToDate(cursor.getLong(index));
        } else if (boolean.class.isAssignableFrom(type)) {
            return cursor.getInt(index) == 1;
        } else {
            return null;
        }
    }

    private Date parseToDate(long time) {
        return new Date(time);
    }
}
