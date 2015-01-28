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

package info.breezes.orm.utils;

import java.util.HashMap;

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
