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
package org.wso2.carbon.bam.analyzer.analyzers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.analyzer.analyzers.configs.GetConfig;
import org.wso2.carbon.bam.analyzer.engine.DataContext;
import org.wso2.carbon.bam.core.configurations.IndexConfiguration;
import org.wso2.carbon.bam.core.configurations.TableConfiguration;
import org.wso2.carbon.bam.core.dataobjects.Cursor;
import org.wso2.carbon.bam.core.dataobjects.Record;
import org.wso2.carbon.bam.core.persistence.MetaDataManager;
import org.wso2.carbon.bam.core.persistence.QueryIndex;
import org.wso2.carbon.bam.core.persistence.QueryManager;
import org.wso2.carbon.bam.core.persistence.exceptions.ConfigurationException;
import org.wso2.carbon.bam.core.persistence.exceptions.StoreException;

import java.util.List;

/**
 * Analyzer syntax :
 * <p/>
 * <get name='' batchSize='integer'>
 * [0..1] <where index=''>
 * <range column='' start='' end=''/> [1..*]
 * </where>
 * </get>
 */
public class GetAnalyzer extends AbstractAnalyzer {

    private static final Log log = LogFactory.getLog(GetAnalyzer.class);

    public GetAnalyzer(AnalyzerConfig analyzerConfig) {
        super(analyzerConfig);
    }

    public void analyze(DataContext dataContext) {

        GetConfig getConfig = (GetConfig) getAnalyzerConfig();

        // Check pre-requisites. 
        //      1. Whether the table is present.
        //      2. Whether the index is present if an index is defined.
        MetaDataManager metaDataManager = MetaDataManager.getInstance();
        TableConfiguration tableConfiguration;
        IndexConfiguration indexConfiguration;
        try {
            tableConfiguration = metaDataManager.getTableMetaData(
                    getExecutingTenantId(), getConfig.getTable());

            QueryIndex index = getConfig.getIndex();

            String indexName = null;
            if (index != null) {
                indexName = getConfig.getIndex().getIndexName();
            }

            indexConfiguration = null;
            if (indexName != null) {
                indexConfiguration = metaDataManager.getIndexMetaData(dataContext.getCredentials(),
                                                                      indexName);
            }

            if (tableConfiguration == null || (indexName != null && indexConfiguration == null)) {
                if (log.isDebugEnabled()) {
                    log.debug("Returning from get analyzer since required table or index is not " +
                              "present yet..");
                }
                return;
            }
        } catch (ConfigurationException e) {
            log.error("Error while fetching table and index meta data. Returning from get " +
                      "analyzer..", e);
            return;
        }

        List<Record> records = null;
        QueryManager manager = new QueryManager();

        // Fetch the records
        if (isBatchSizeDefined(getConfig)) {
            try {
                Cursor cursor = new Cursor(getConfig.getTable(), getAnalyzerSequenceName(),
                                           getPositionInSequence());
                records = manager.getRecords(dataContext.getCredentials(), getConfig.getTable(),
                                             getConfig.getIndex(), null, getConfig.getBatchSize(),
                                             cursor);

                if (cursor.getResumePoint() != null) {
                    getAnalyzerSequence().setCursor(cursor);
                }
            } catch (StoreException e) {
                handleException(getConfig, e);
            }

        } else {
            try {
                records = manager.getRecords(dataContext.getCredentials(), getConfig.getTable(),
                                             getConfig.getIndex(), null);
            } catch (StoreException e) {
                handleException(getConfig, e);
            }
        }

/*        List<IndexConfiguration> indexConfigurations = new ArrayList<IndexConfiguration>();
        if (indexConfiguration == null) {
            try {
                indexConfigurations = metaDataManager.getIndexMetaDataOfTable(
                        getExecutingTenantId(), getConfig.getTable());
            } catch (ConfigurationException e) {
                handleException(getConfig, e);
            }
        }*/

        
        // Create temporary an index configuration for fetched data. Index defaults to a Cassandra
        // index. Will be re-assigned to the correct index configuration type at the first
        // subsequent put analyzer according to the data source defined there
/*        CassandraIndexConfiguration index = new CassandraIndexConfiguration(
                AnalyzerConfigConstants.TEMPORARY_INDEX, null,
                indexConfiguration.getIndexedColumns(), DataSourceType.CASSANDRA);
        indexConfiguration.setAutoGenerated(Boolean.TRUE);

        Map properties = dataContext.getSequenceProperties(getAnalyzerSequenceName());
        properties.put(AnalyzerConfigConstants.INDEX, index);*/

        setData(dataContext, records);

    }

    private void handleException(GetConfig getConfig, Exception e) {
        log.error("Error while fetching data from table " + getConfig.getTable() + "..", e);
        throw new RuntimeException(e);
    }

    private boolean isBatchSizeDefined(GetConfig config) {
        if (config.getBatchSize() < Integer.MAX_VALUE) {
            return true;
        }

        return false;
    }

}
