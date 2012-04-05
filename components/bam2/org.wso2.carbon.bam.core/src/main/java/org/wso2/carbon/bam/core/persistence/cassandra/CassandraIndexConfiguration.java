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
package org.wso2.carbon.bam.core.persistence.cassandra;

import org.wso2.carbon.bam.core.configurations.AbstractIndexConfiguration;
import org.wso2.carbon.bam.core.configurations.DataSourceType;
import org.wso2.carbon.bam.core.configurations.IndexConfiguration;


public class CassandraIndexConfiguration extends AbstractIndexConfiguration {

    private String indexingColumnFamily;

    private String cron;

    public CassandraIndexConfiguration(String indexName, String indexedTable,
                                       String[] indexedColumns, DataSourceType dataSourceType) {
        super(indexName, indexedTable, indexedColumns, dataSourceType);
    }
    
    // Copy constructor used to get a clone of a given index configuration.
    public CassandraIndexConfiguration(IndexConfiguration indexConfiguration) {
        super(indexConfiguration.getIndexName(), indexConfiguration.getIndexedTable(),
              indexConfiguration.getIndexedColumns(), indexConfiguration.getDataSourceType());

        if (indexConfiguration instanceof CassandraIndexConfiguration) {
            throw new RuntimeException("Expected a Cassandra index configuration..");
        }

        CassandraIndexConfiguration configuration = (CassandraIndexConfiguration)
                indexConfiguration;

        this.indexingColumnFamily = configuration.getIndexingColumnFamily();

    }
    
    public String getIndexingColumnFamily() {
        return indexingColumnFamily;
    }

    public void setIndexingColumnFamily(String indexingColumnFamily) {
        this.indexingColumnFamily = indexingColumnFamily;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

}
