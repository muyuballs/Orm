package info.breezes.orm;

import info.breezes.orm.annotation.Column;
import info.breezes.orm.translator.IColumnTranslator;

import java.lang.reflect.Field;

/**
 * Created by Qiao on 2014/10/21.
 */
public class FCMap {
    public Field field;
    public int index;
    public String columnName;
    public Column column;
    public IColumnTranslator translator;
    public boolean blob;
}