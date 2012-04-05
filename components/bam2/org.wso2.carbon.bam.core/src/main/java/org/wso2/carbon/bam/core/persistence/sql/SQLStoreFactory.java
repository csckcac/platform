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

import org.wso2.carbon.bam.core.persistence.DataStore;
import org.wso2.carbon.bam.core.persistence.StoreFactory;
import org.wso2.carbon.bam.core.persistence.StoreFetcher;
import org.wso2.carbon.bam.core.persistence.exceptions.StoreException;

import java.util.Map;

public class SQLStoreFactory implements StoreFactory {

    private static SQLStoreFactory instance = new SQLStoreFactory();

    public static SQLStoreFactory getInstance() {
        return instance;
    }

    @Override
    public DataStore getDataStore(Map<String, String> credentials) throws StoreException {
        return new SQLDataStore();  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StoreFetcher getStoreFetcher(Map<String, String> credentials) throws StoreException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
