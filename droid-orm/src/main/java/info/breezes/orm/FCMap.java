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

package info.breezes.orm;

import info.breezes.orm.annotation.Column;
import info.breezes.orm.translator.IColumnTranslator;

import java.lang.reflect.Field;

public class FCMap {
    public enum DataType{
        String,
        Int,
        Long,
        Float,
        Double,
        Date,
        Boolean,
        Blob
    }
    public Field field;
    public int index;
    public String columnName;
    public Column column;
    public IColumnTranslator translator;
    public boolean blob;
    public boolean autoincrement;
    public DataType dataType=DataType.String;
}