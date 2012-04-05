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
package org.wso2.carbon.bam.core.persistence.sql;

import org.wso2.carbon.bam.core.configurations.DataSourceType;
import org.wso2.carbon.bam.core.dataobjects.Cursor;
import org.wso2.carbon.bam.core.dataobjects.Record;
import org.wso2.carbon.bam.core.persistence.QueryIndex;
import org.wso2.carbon.bam.core.persistence.QueryManager;
import org.wso2.carbon.bam.core.persistence.StoreFetcher;
import org.wso2.carbon.bam.core.persistence.exceptions.StoreException;

import java.util.List;
import java.util.Map;

public class SQLStoreFetcher implements StoreFetcher {

    @Override
    public void initialize(Map<String, String> credentials) throws StoreException {

    }

    @Override
    public List<String> fetchIndexValues(String indexName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> fetchTableColumns(String tableName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Record> fetchRecords(String table, String primaryKey, List<String> filterByColumns) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Record> fetchRecords(String table, QueryIndex index,
                                   List<String> filterByColumns) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Record> fetchRecords(String table, QueryIndex index, List<String> filterByColumns,
                                   int batchSize, Cursor cursor) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public DataSourceType getDataSourceType() {
        return DataSourceType.SQL;
    }

}
