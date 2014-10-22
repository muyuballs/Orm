package info.breezes.orm.utils;

import java.util.HashMap;

/**
 * Created by Qiao on 2014/10/21.
 */
final class TableStructCache {
    private static HashMap<String, TableStruct> innerCache;

    static {
        innerCache = new HashMap<String, TableStruct>();
    }

    public static void put(TableStruct statementStruct){
        innerCache.put(statementStruct.table,statementStruct);
    }
    public static TableStruct get(String table) {
       return innerCache.get(table);
    }
}
