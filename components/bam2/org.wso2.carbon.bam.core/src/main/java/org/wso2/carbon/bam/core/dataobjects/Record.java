/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bam.core.dataobjects;

import java.util.HashMap;
import java.util.Map;

public class Record<V> {

    private final String key;

    private final Map<String, V> columns;

    public Record(String key, Map<String, V> columns) {
        this.key = key;

        if (key == null) {
            throw new RuntimeException("Record key should not be null..");
        }

        if (columns == null) {
            throw new RuntimeException("Record columns should not be null..");
        }

        this.columns = columns;

    }

    public String getKey() {
        return key;
    }

    public Map<String, V> getColumns() {
        return columns;
    }

    public void addColumn(String key, V value) {
        columns.put(key, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Record)) {
            return false;
        }

        Record record = (Record) o;

        if (key != null ? !key.equals(record.key) : record.key != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }

}
