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
package org.wso2.carbon.bam.core.configurations;

import org.wso2.carbon.bam.core.dataobjects.Cursor;
import org.wso2.carbon.bam.core.persistence.exceptions.ConfigurationException;

import java.util.List;
import java.util.Map;

public interface MetaDataPersistor {
    
/*    public String getIndexRegistryPath();
    
    public String getTableRegistryPath();

    public String getCursorRegistryPath();*/

/*    @Deprecated
    public void persistIndexMetaData(int tenantId, IndexConfiguration configuration)
            throws ConfigurationException;*/
    
    public void persistIndexMetaData(
            Map<String, String> credentials, IndexConfiguration configuration)
            throws ConfigurationException;

/*    @Deprecated
    public void deleteIndexMetaData(int tenantId, String indexName) throws ConfigurationException;*/
    
    public void deleteIndexMetaData(Map<String, String> credentials, String indexName)
            throws ConfigurationException;

/*    @Deprecated
    public void persistTableMetaData(int tenantId, TableConfiguration configuration)
            throws ConfigurationException;*/
    
    public void persistTableMetaData(
            Map<String, String> credentials,TableConfiguration configuration)
            throws ConfigurationException;

/*    @Deprecated
    public void deleteTableMetaData(int tenantId, String tableName) throws ConfigurationException;*/
    
    public void deleteTableMetaData(Map<String, String> credentials, String tableName)
            throws ConfigurationException;
    
/*    @Deprecated
    public void persistCursorMetaData(int tenantId, Cursor cursor) throws ConfigurationException;*/
    
    public void persistCursorMetaData(Map<String, String> credentials, Cursor cursor) 
            throws ConfigurationException;

    public int[] getAllTenantsWithDefinedIndexes() throws ConfigurationException;

/*    @Deprecated
    public List<IndexConfiguration> getAllIndexMetaData(int tenantId)
            throws ConfigurationException;*/
    
    public List<IndexConfiguration> getAllIndexMetaData(Map<String, String> credentials)
            throws ConfigurationException;
    
/*    public IndexConfiguration getIndexMetaData(int tenantId, String indexName)
            throws ConfigurationException;*/
    
    public IndexConfiguration getIndexMetaData(Map<String, String> credentials, String indexName)
            throws ConfigurationException;

/*    @Deprecated
    public List<TableConfiguration> getAllTableMetaData(int tenantId)
            throws ConfigurationException;*/
    
    public List<TableConfiguration> getAllTableMetaData(Map<String, String> credentials)
        throws ConfigurationException;

/*    @Deprecated
    public TableConfiguration getTableMetaData(int tenantId, String table)
            throws ConfigurationException;*/

    public TableConfiguration getTableMetaData(Map<String, String> credentials, String table)
            throws ConfigurationException;
    
/*    @Deprecated
    public List<Cursor> getAllCursorMetaData(int tenantId) throws ConfigurationException;*/
    
    public List<Cursor> getAllCursorMetaData(Map<String, String> credentials) 
            throws ConfigurationException;
    
/*    @Deprecated
    public Cursor getCursorMetaData(int tenantId, String table, String cursorName)
            throws ConfigurationException;*/
    
    public Cursor getCursorMetaData(Map<String, String> credentials, String table,
                                    String cursorName) throws ConfigurationException;
    
    public void deleteCursorMetaData(Map<String, String> credentials, Cursor cursor)
            throws ConfigurationException;
    
    
}
