package info.breezes.orm;

import info.breezes.orm.translator.IColumnTranslator;

import java.lang.reflect.Field;

/**
 * Created by Qiao on 2014/10/21.
 */
public class FCMap {
    public Field field;
    public IColumnTranslator translator;
    public int index;
    public boolean blob;
}