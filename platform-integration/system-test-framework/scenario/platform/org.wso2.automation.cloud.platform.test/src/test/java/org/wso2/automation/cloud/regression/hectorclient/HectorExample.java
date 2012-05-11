/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.automation.cloud.regression.hectorclient;

import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.ThriftCfDef;
import me.prettyprint.cassandra.service.ThriftKsDef;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.ColumnQuery;
import me.prettyprint.hector.api.query.QueryResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.platform.test.core.utils.UserInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Simple sample using hector API to connect to Cassandra
 */
public class HectorExample {
    private static final Log log = LogFactory.getLog(HectorExample.class);
    private static Cluster cluster;

    public static void main(String arg[]) throws InterruptedException {
        //Read User Inputs
        log.info("Tenant Id: ");
        String tenantId = "admin123@wso2manualQA0006.org";
        log.info("Tenant Password: ");
        String tenantPasswd = "admin123";
        log.info("Keyspace Name: ");
        String keyspaceName = "TestKeySpace";
        log.info("Column Family: ");
        String ColumnFamilyName = "TestColumnFamily";
        log.info("Column Name List ( separated by colon) : ");
        String columnNameList = "AAA:BBB:CCC:DDD";
        log.info("Number of Row you need: ");
        String rowCount = "1";

        cluster = ExampleHelper.createCluster(tenantId, tenantPasswd);
        createKeyspace(keyspaceName, ColumnFamilyName, columnNameList, rowCount);
    }

    public static Boolean executeKeySpaceSample(UserInfo user) throws InterruptedException {

        String tenantId = user.getUserName();
        log.info("Tenant Id: " + tenantId);

        String tenantPasswd = user.getPassword();
        log.info("Tenant Password: ");

        String keyspaceName = "TestKeySpace";
        log.info("Keyspace Name: " + keyspaceName);

        String columnFamilyName = "TestColumnFamily";
        log.info("Column Family: " + columnFamilyName);

        String columnNameList = "AAA:BBB:CCC:DDD";
        log.info("Column Name List ( separated by colon) : " + columnNameList);

        String rowCount = "1";
        log.info("Number of Row you need: " + rowCount);


        cluster = ExampleHelper.createCluster(tenantId, tenantPasswd);
        return (createKeyspace(keyspaceName, columnFamilyName, columnNameList, rowCount));

    }


    /**
     * Create a keyspace, add a column family and read a column's value
     *
     * @param keyspaceName
     * @param columnFamily
     * @param columnList
     * @param rowCount
     */
    private static Boolean createKeyspace(String keyspaceName, String columnFamily,
                                          String columnList,
                                          String rowCount) throws InterruptedException {
        Boolean cassendraSampleStatus = false;
        String addKeySpace;
        Keyspace keyspace;
        //Create Keyspace
        KeyspaceDefinition definition = new ThriftKsDef(keyspaceName);
        cluster.dropKeyspace(keyspaceName);
        Thread.sleep(3000);

        addKeySpace = cluster.addKeyspace(definition);

        //add columnt family
        ColumnFamilyDefinition familyDefinition = new ThriftCfDef(keyspaceName, columnFamily);
        cluster.addColumnFamily(familyDefinition);
        //Add data to a column

        keyspace = HFactory.createKeyspace(keyspaceName, cluster);


        Mutator<String> mutator = HFactory.createMutator(keyspace, new StringSerializer());
        String rowKey = null;
        List<String> keyList = new ArrayList<String>();
        for (int i = 0; i < Integer.parseInt(rowCount); i++) {
            rowKey = UUID.randomUUID().toString();
            keyList.add(rowKey);
            log.info("\nInserting Key " + rowKey + "To Column Family " + columnFamily + "\n");
            for (String columnName : columnList.split(":")) {
                String columnValue = "testvalue";
                mutator.insert(rowKey, columnFamily, HFactory.createStringColumn(columnName, columnValue));
                log.info("Column Name: " + columnName + " Value: " + columnValue + "\n");
            }
        }
        //Read Data
        ColumnQuery<String, String, String> columnQuery =
                HFactory.createStringColumnQuery(keyspace);
        for (String key : keyList) {
            log.info("\nretrieving Key " + rowKey + "From Column Family " + columnFamily + "\n");
            for (String columnName : columnList.split(":")) {
                columnQuery.setColumnFamily(columnFamily).setKey(key).setName(columnName);
                QueryResult<HColumn<String, String>> result = columnQuery.execute();
                HColumn<String, String> hColumn = result.get();
                //sout data
                log.info("Column: " + hColumn.getName() + " Value : " + hColumn.getValue() + "\n");
                if (hColumn.getValue().contains("testvalue")) {
                    log.info("Pass");
                    cassendraSampleStatus = true;
                    break;
                }
            }
        }
        return cassendraSampleStatus;
    }
}
