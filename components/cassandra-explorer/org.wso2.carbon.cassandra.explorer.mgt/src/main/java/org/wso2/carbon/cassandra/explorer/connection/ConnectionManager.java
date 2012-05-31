/*
*  Licensed to the Apache Software Foundation (ASF) under one
*  or more contributor license agreements.  See the NOTICE file
*  distributed with this work for additional information
*  regarding copyright ownership.  The ASF licenses this file
*  to you under the Apache License, Version 2.0 (the
*  "License"); you may not use this file except in compliance
*  with the License.  You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

package org.wso2.carbon.cassandra.explorer.connection;

import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.factory.HFactory;
import org.wso2.carbon.cassandra.explorer.exception.CassandraExplorerException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConnectionManager {

    private static Cluster cluster = null;

    public ConnectionManager(String clusterName, CassandraHostConfigurator hostConfigurator,
                             Map<String, String> credentials) throws CassandraExplorerException {
        this.setCassandraCluster(clusterName,hostConfigurator,credentials);
    }

    private void setCassandraCluster(String clusterName, CassandraHostConfigurator hostConfigurator,
                                       Map<String, String> credentials) throws CassandraExplorerException {
        this.cluster = HFactory.getOrCreateCluster(clusterName, hostConfigurator, credentials);
        if(cluster == null){
            throw new CassandraExplorerException("Cannot connect to cluster");
        }
    }

    public List<Keyspace> getCassandraKeySpacesList(Cluster cluster) {
        List<KeyspaceDefinition> KeyspaceDefsList = cluster.describeKeyspaces();
        List<Keyspace> keyspaceList = new ArrayList<Keyspace>();
        for (KeyspaceDefinition keyspaceDefinition : KeyspaceDefsList) {
            keyspaceList.add(HFactory.createKeyspace(keyspaceDefinition.getName(), cluster));
        }
        return keyspaceList;
    }

    public static Keyspace getKeyspace(Cluster cluster, String keyspaceName) {
        return HFactory.createKeyspace(keyspaceName, cluster);
    }

    public static Cluster getCluster() throws CassandraExplorerException {
      if(cluster!= null){
          return cluster;
      } else{
          throw new CassandraExplorerException("Cannot find a cluster, Please connect");
      }
    }

    public boolean isConnected(){
         return (cluster!=null);
    }

}
