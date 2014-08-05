package info.breezes.orm;


import info.breezes.orm.translator.DefaultColumnTranslator;
import info.breezes.orm.translator.IColumnTranslator;

import java.util.HashMap;

/**
 * Created by Qiao on 2014/5/19.
 */
public final class OrmConfig {
    private static HashMap<Class<?>, IColumnTranslator> columnTranslatorHashMap = new HashMap<Class<?>, IColumnTranslator>();
    private static IColumnTranslator defaultTranslator = new DefaultColumnTranslator();

    public static void register(Class<?> type, IColumnTranslator translator) {
        columnTranslatorHashMap.put(type, translator);
    }

    public static IColumnTranslator getTranslator(Class<?> type) {
        IColumnTranslator iColumnTranslator=columnTranslatorHashMap.get(type);
        if( iColumnTranslator==null){
            iColumnTranslator=columnTranslatorHashMap.get(Object.class);
        }
        return iColumnTranslator==null?defaultTranslator:iColumnTranslator;
    }
}
