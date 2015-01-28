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

package info.breezes.orm.expressions;

public class Where {
    public String condition;
    public String column;
    public Object value;
    public String operation;

    public Where(String column, Object value, String operation) {
        this.column = column;
        this.value = value;
        this.operation = operation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Where where = (Where) o;

        if (!column.equals(where.column)) return false;
        if (!operation.equals(where.operation)) return false;
        if (!value.equals(where.value)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = column.hashCode();
        result = 31 * result + value.hashCode();
        result = 31 * result + operation.hashCode();
        return result;
    }
}