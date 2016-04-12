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

import android.database.Cursor;
import android.util.Log;

import info.breezes.orm.FCMap;
import info.breezes.orm.OrmConfig;
import info.breezes.orm.annotation.Column;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CursorUtils {

    public static <T> T readCurrentEntity(Class<T> type, Cursor cursor, TableStruct tableStruct) {
        long st = System.currentTimeMillis();
        try {
            T entity = type.newInstance();
            int index = 0;
            for (FCMap fcMap : tableStruct.fcmaps) {
                Object value = fcMap.translator.readColumnValue(cursor, index++, fcMap.field);
                if (value != null) {
                    fcMap.field.set(entity, value);
                }
            }
            if (OrmConfig.Debug) {
                Log.d("CursorUtils", "readCurrentEntity With ColumnIndexMap cost:" + (System.currentTimeMillis() - st));
            }
            return entity;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

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
            if (OrmConfig.Debug) {
                Log.d("CursorUtils", "readCurrentEntity With ColumnIndexMap cost:" + (System.currentTimeMillis() - st));
            }
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
            if (OrmConfig.Debug) {
                Log.d("CursorUtils", "readCurrentEntity cost:" + (System.currentTimeMillis() - st));
            }
            return entity;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
