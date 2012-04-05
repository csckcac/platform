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
import org.wso2.carbon.bam.core.persistence.DataStore;
import org.wso2.carbon.bam.core.persistence.QueryManager;
import org.wso2.carbon.bam.core.persistence.exceptions.StoreException;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

public class SQLDataStore implements DataStore {

    @Override
    public void initialize(Map<String, String> credentials) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void persistData(String table, String key, Map<String, String> columns)
            throws StoreException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void deleteData(String table, String key) throws StoreException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void updateData(String table, String key, Map<String, String> columns)
            throws StoreException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void persistBinaryData(String table, String key, Map<String, ByteBuffer> columns)
            throws StoreException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void startBatchCommit() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void endBatchCommit() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isTableExists(String table) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean createTable(String table, List<String> columns) throws StoreException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean deleteTable(String table) throws StoreException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public DataSourceType getDataSourceType() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
