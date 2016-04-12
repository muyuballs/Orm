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


import info.breezes.orm.translator.DefaultColumnTranslator;
import info.breezes.orm.translator.IColumnTranslator;

import java.util.HashMap;

public final class OrmConfig {
    private static HashMap<Class<?>, IColumnTranslator> columnTranslatorHashMap = new HashMap<Class<?>, IColumnTranslator>();
    private static IColumnTranslator defaultTranslator = new DefaultColumnTranslator();
    public static boolean Debug = false;
    public static boolean Emulate = false;
    public static boolean Response = true;
    public static boolean Notify = false;

    public static void register(Class<?> type, IColumnTranslator translator) {
        columnTranslatorHashMap.put(type, translator);
    }

    public static IColumnTranslator getTranslator(Class<?> type) {
        IColumnTranslator iColumnTranslator = columnTranslatorHashMap.get(type);
        if (iColumnTranslator == null) {
            iColumnTranslator = columnTranslatorHashMap.get(Object.class);
        }
        return iColumnTranslator == null ? defaultTranslator : iColumnTranslator;
    }
}
