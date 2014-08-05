package info.breezes.orm.translator;

import android.database.Cursor;
import info.breezes.orm.annotation.Column;

import java.lang.reflect.Field;

/**
 * Created by Qiao on 2014/5/19.
 */
public interface IColumnTranslator {
    public String getColumnType(Field field, Column column);
    public Object getColumnValue(Field field, Object obj);
    public Object readColumnValue(Cursor cursor, int index, Field field);
}
