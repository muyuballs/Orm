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

import info.breezes.orm.FCMap;
import info.breezes.orm.Index;

import java.util.ArrayList;

public final class TableStruct {
    public String table;
    public String insertSql;
    public String updateSql;
    public ArrayList<FCMap> fcmaps = new ArrayList<>();
    public ArrayList<Index> indexes = new ArrayList<>();

}
