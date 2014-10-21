package info.breezes.orm.utils;

import android.database.sqlite.SQLiteStatement;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by Qiao on 2014/10/22.
 */
final class SQLStruct {
    public String table;
    public String sql;
    public ArrayList<Field> params;
}
