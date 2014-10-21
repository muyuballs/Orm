package info.breezes.orm;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import info.breezes.orm.utils.TableUtils;

/**
 * Created by Qiao on 2014/10/21.
 */
public class TestOrmSQLiteHelper extends SimpleOrmSQLiteHelper {

    public TestOrmSQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        TableUtils.createTable(db,Employee.class);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
