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
package org.wso2.carbon.bam.presentation;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMText;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.core.configurations.IndexConfiguration;
import org.wso2.carbon.bam.core.configurations.TableConfiguration;
import org.wso2.carbon.bam.core.dataobjects.Record;
import org.wso2.carbon.bam.core.persistence.MetaDataManager;
import org.wso2.carbon.bam.core.persistence.PersistencyConstants;
import org.wso2.carbon.bam.core.persistence.QueryIndex;
import org.wso2.carbon.bam.core.persistence.QueryManager;
import org.wso2.carbon.bam.core.persistence.exceptions.ConfigurationException;
import org.wso2.carbon.bam.core.persistence.exceptions.StoreException;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryService {

    private static final Log log = LogFactory.getLog(QueryService.class);

    private static final String ROWS = "rows";
    private static final String ROW = "row";

    public OMElement queryColumnFamily(String table, String indexName, CompositeIndex[] indexes)
            throws StoreException {

        QueryIndex index = null;

        if (indexName != null && !indexName.trim().equals("")) {
            index = new QueryIndex(indexName);
            if (indexes != null) {
                for (CompositeIndex compositeIndex : indexes) {
                    index.addCompositeRange(compositeIndex.getIndexName(),
                                            compositeIndex.getRangeFirst(),
                                            compositeIndex.getRangeLast());
                }
            }
        }

        QueryManager queryManager = new QueryManager();
        List<Record> records = null;
        try {
            records = queryManager.getRecords(getCredentials(), table, index, null);
        } catch (ConfigurationException e) {
            throw new StoreException("Unable to fetch credentials..", e);
        }

        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMElement rowsEl = fac.createOMElement(new QName(ROWS));

        if (records != null) {
            for (Record record : records) {
                OMElement keyEl = fac.createOMElement(new QName(ROW));

                Map<String, String> columns = record.getColumns();
                if (columns != null) {
                    for (Map.Entry<String, String> entry : columns.entrySet()) {
                        OMElement key = fac.createOMElement(new QName(entry.getKey()));
                        OMText value = fac.createOMText(entry.getValue());

                        key.addChild(value);
                        keyEl.addChild(key);
                    }
                }

                // Add indexes and their values
//                if (index != null) {
//                    Map<String, String> indexValues;
//                    try {
//                        indexValues = IndexManager.getInstance().getIndexesOfRecord(
//                                record, indexName);
//                    } catch (IndexingException e) {
//                        throw new StoreException("Unable to fetch index values..", e);
//                    }
//
//                    for (Map.Entry<String, String> entry : indexValues.entrySet()) {
//                        OMElement key = fac.createOMElement(new QName(entry.getKey()));
//                        OMText value = fac.createOMText(entry.getValue());
//
//                        key.addChild(value);
//                        keyEl.addChild(key);
//                    }
//                }

                // Add indexes and values
/*                if (!cfConfig.isPrimaryCF()) {
                    String rowKey = record.getRowKey();
                    String[] keyPartValues = rowKey.split("---");
                    List<KeyPart> keyParts = cfConfig.getRowKeyParts();

                    for (int i = 0; i < keyPartValues.length; i++) {
                        OMElement key = fac.createOMElement(new QName(keyParts.get(i).getName()));
                        OMText value = fac.createOMText(keyPartValues[i]);

                        key.addChild(value);
                        keyEl.addChild(key);
                    }
                }*/

                rowsEl.addChild(keyEl);
            }
        }

        return rowsEl;
    }

    public String[] getAllColumnFamilies() throws ConfigurationException {

        int tenantId = CarbonContext.getCurrentContext().getTenantId();

        MetaDataManager manager = MetaDataManager.getInstance();
        List<TableConfiguration> tableConfigurations = manager.getAllTableMetaData(
                org.wso2.carbon.bam.core.utils.Utils.getConnectionParameters(tenantId));

        List<String> tables = new ArrayList<String>();
        for (TableConfiguration tableConfiguration : tableConfigurations) {
            tables.add(tableConfiguration.getTableName());
        }

        return tables.toArray(new String[]{});

    }

    public String[] getIndexes(String cfName) throws ConfigurationException {

        int tenantId = CarbonContext.getCurrentContext().getTenantId();

        MetaDataManager manager = MetaDataManager.getInstance();
        List<IndexConfiguration> configurations = manager.getIndexMetaDataOfTable(getCredentials(),
                                                                                  cfName);

        List<String> indexes = new ArrayList<String>();
        for (IndexConfiguration configuration : configurations) {
            indexes.add(configuration.getIndexName());
        }

        return indexes.toArray(new String[]{});

    }

    public String[] getAllIndexValues(String cfName, String indexName) throws QueryException {
/*        List<String> indexValues = getQueryManager().queryIndex(cfName, indexName);
        return indexValues.toArray(new String[]{});*/

        return null;

    }
    
    private Map<String, String> getCredentials()
            throws ConfigurationException {

        return org.wso2.carbon.bam.core.utils.Utils.getConnectionParameters();

    }

}
