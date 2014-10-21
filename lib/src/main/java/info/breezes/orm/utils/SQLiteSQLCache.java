package info.breezes.orm.utils;

import java.util.HashMap;

/**
 * Created by Qiao on 2014/10/21.
 */
final class SQLiteSQLCache {
    private static HashMap<String, SQLStruct> innerCache;

    static {
        innerCache = new HashMap<String, SQLStruct>();
    }

    public static void putStatement(SQLStruct statementStruct){
        innerCache.put(statementStruct.table,statementStruct);
    }
    public static SQLStruct getStatement(String table) {
       return innerCache.get(table);
    }
}
