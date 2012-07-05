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

import me.prettyprint.cassandra.service.CassandraHost;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.cassandra.service.ThriftCluster;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.factory.HFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.wso2.carbon.cassandra.explorer.exception.CassandraExplorerException;
import org.wso2.carbon.cassandra.explorer.session.ExplorerSessionManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConnectionManager {

    private static final String EXPLORER_CLUSTER_CONNECTION = "connection";

    public ConnectionManager(String clusterName, String connectionUrl,
                             Map<String, String> credentials) throws CassandraExplorerException {
        this.setCassandraCluster(clusterName, connectionUrl, credentials);
    }

    private void setCassandraCluster(String clusterName, String connectionUrl,
                                     Map<String, String> credentials)
            throws CassandraExplorerException {
        try {
            CassandraHostConfigurator hostConfigurator = new CassandraHostConfigurator(connectionUrl);
            hostConfigurator.setRetryDownedHosts(false);
            // this.cluster = HFactory.getOrCreateCluster(clusterName, hostConfigurator, credentials);
            Cluster cluster = new ThriftCluster(clusterName, hostConfigurator, credentials);
            ExplorerSessionManager.setSessionObject(EXPLORER_CLUSTER_CONNECTION, cluster);
        } catch (Exception exception) {
            throw new CassandraExplorerException(exception.getMessage(), exception.getCause());
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
        Cluster cluster = (Cluster) ExplorerSessionManager.getSessionObject(EXPLORER_CLUSTER_CONNECTION);
        if (cluster != null) {
            return cluster;
        } else {
            throw new CassandraExplorerException("Cannot find a cluster, Please connect");
        }
    }

    public boolean isConnected() {
        return (ExplorerSessionManager.getSessionObject(EXPLORER_CLUSTER_CONNECTION)
                != null);
    }


}
