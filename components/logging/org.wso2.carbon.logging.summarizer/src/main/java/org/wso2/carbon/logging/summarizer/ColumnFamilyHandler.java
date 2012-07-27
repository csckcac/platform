package org.wso2.carbon.logging.summarizer;

/*
 * Copyright 2005,2006 WSO2, Inc. http://www.wso2.org
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

import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.factory.HFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class ColumnFamilyHandler {

    private Cluster cluster;
    String keyspaceName = "EVENT_KS";

    public Cluster newConnection() {
        if (cluster == null) {
            Map<String, String> credentials = new HashMap<String, String>();

            credentials.put("username", "admin");
            credentials.put("password", "admin");

            cluster = HFactory.createCluster("cluster",
                    new CassandraHostConfigurator("localhost:9160"),
                    credentials);
        }
        return cluster;
    }

    public List<ColumnFamilyDefinition> getColumnFamilies(String keyspaceName) {
        KeyspaceDefinition keyspaceDefinition = newConnection().describeKeyspace(keyspaceName);
        return keyspaceDefinition.getCfDefs();
    }


    public List<String> filterColumnFamilies(String keyspaceName) {
        ColumnFamilyHandler columnFamilyHandler = new ColumnFamilyHandler();
        List<ColumnFamilyDefinition> columnFamilyDefinitionList = columnFamilyHandler.getColumnFamilies(keyspaceName);
        List<String> selectedColumnFamilies = new ArrayList<String>(columnFamilyDefinitionList.size());

        //Retrieve Column Families that has previous date
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        String prevDate = dateFormat.format(date.getTime() - 1000 * 60 * 60 * 24);

        for (ColumnFamilyDefinition aColumnFamilyDefinitionList : columnFamilyDefinitionList) {

            if (aColumnFamilyDefinitionList.getName().contains("Application_Server")) {
                selectedColumnFamilies.add(aColumnFamilyDefinitionList.getName());
            }
        }
        return selectedColumnFamilies;
    }


    public void deleteColumnFamilies() {
        System.out.println("delete called");
        List<String> filteredColFamilies = filterColumnFamilies(keyspaceName);
        for (String filteredColFamily : filteredColFamilies) {
            newConnection().dropColumnFamily(keyspaceName, filteredColFamily);
        }

    }


}

