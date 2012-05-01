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
package org.wso2.carbon.cassandra.mgt;

import org.apache.cassandra.db.ColumnFamilyStoreMBean;
import org.apache.cassandra.service.StorageServiceMBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;

import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Admin service for accessing a Cassandra Cluster
 */
public class CassandraClusterAdmin extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(CassandraClusterAdmin.class);

    private CassandraMBeanLocator cassandraMBeanLocator;

    public CassandraClusterAdmin() {
        cassandraMBeanLocator = new CassandraMBeanLocator(ManagementFactory.getPlatformMBeanServer()); //Todo if need to use a remote connector
    }

    /**
     * Get information about all the nodes in a cluster
     *
     * @return an array of <code>NodeInformation</code>
     * @throws CassandraServerManagementException
     *          for errors during accessing nodes
     */
    public NodeInformation[] getNodes() throws CassandraServerManagementException {

        StorageServiceMBean storageServiceMBean = cassandraMBeanLocator.locateStorageServiceMBean();
        Map<String, String> tokenToEndpoint = storageServiceMBean.getTokenToEndpointMap();
        List<String> sortedTokens = new ArrayList<String>(tokenToEndpoint.keySet());
        Collections.sort(sortedTokens);

        Collection<String> liveNodes = storageServiceMBean.getLiveNodes();
        Collection<String> deadNodes = storageServiceMBean.getUnreachableNodes();
        Collection<String> joiningNodes = storageServiceMBean.getJoiningNodes();
        Collection<String> leavingNodes = storageServiceMBean.getLeavingNodes();
        Map<String, String> loadMap = storageServiceMBean.getLoadMap();

        List<NodeInformation> nodeInformations = new ArrayList<NodeInformation>();

        // Calculate per-token ownership of the ring
        Map<String, Float> ownerships = storageServiceMBean.getOwnership();

        for (String token : sortedTokens) {
            String primaryEndpoint = tokenToEndpoint.get(token);
            String status = liveNodes.contains(primaryEndpoint)
                    ? "Up"
                    : deadNodes.contains(primaryEndpoint)
                    ? "Down"
                    : "?";
            String state = joiningNodes.contains(primaryEndpoint)
                    ? "Joining"
                    : leavingNodes.contains(primaryEndpoint)
                    ? "Leaving"
                    : "Normal";
            String load = loadMap.containsKey(primaryEndpoint)
                    ? loadMap.get(primaryEndpoint)
                    : "?";
            String owns = new DecimalFormat("##0.00%").format(ownerships.get(token));

            NodeInformation nodeInformation = new NodeInformation();
            nodeInformation.setAddress(primaryEndpoint);
            nodeInformation.setState(state);
            nodeInformation.setStatus(status);
            nodeInformation.setOwn(owns);
            nodeInformation.setLoad(load);
            nodeInformation.setToken(token.toString());
            nodeInformations.add(nodeInformation);
        }

        return nodeInformations.toArray(new NodeInformation[nodeInformations.size()]);
    }

    /**
     * Returns a <code>ColumnFamilyStats</code> representing the stats for the column family with the given name
     *
     * @param keyspace the name of the keyspace
     * @param cf       name of the column family
     * @return <code>ColumnFamilyStats</code> instance
     * @throws CassandraServerManagementException
     *          for errors during accessing CF stats
     */
    public ColumnFamilyStats getColumnFamilyStats(String keyspace, String cf) throws CassandraServerManagementException {
        validateKeyspace(keyspace);
        validateCF(cf);

        String keyspaceWithDomainName = keyspace;
        String domainName = CarbonContext.getCurrentContext().getTenantDomain();
        if (domainName != null && !"".equals(domainName)) {
            domainName = domainName.replace(".", "_");
            keyspaceWithDomainName = domainName + "_" + keyspace;
        }

        ColumnFamilyStoreMBean columnFamilyStoreMBean =
                cassandraMBeanLocator.locateColumnFamilyStoreMBean(keyspaceWithDomainName, cf);
        if (columnFamilyStoreMBean == null) {
            if (log.isDebugEnabled()) {
                log.debug("Cannot locate a ColumnFamilyStoreMBean for column family : " + cf);
            }
            return null;
        }

        ColumnFamilyStats columnFamilyStats = new ColumnFamilyStats();

        columnFamilyStats.setLiveSSTableCount(columnFamilyStoreMBean.getLiveSSTableCount());
        columnFamilyStats.setLiveDiskSpaceUsed(columnFamilyStoreMBean.getLiveDiskSpaceUsed());
        columnFamilyStats.setTotalDiskSpaceUsed(columnFamilyStoreMBean.getTotalDiskSpaceUsed());

        columnFamilyStats.setMemtableColumnsCount(columnFamilyStoreMBean.getMemtableColumnsCount());
        columnFamilyStats.setMemtableSwitchCount(columnFamilyStoreMBean.getMemtableSwitchCount());
        columnFamilyStats.setMemtableDataSize(columnFamilyStoreMBean.getMemtableDataSize());

        columnFamilyStats.setReadCount(columnFamilyStoreMBean.getReadCount());
        columnFamilyStats.setReadLatency(columnFamilyStoreMBean.getRecentReadLatencyMicros());
        columnFamilyStats.setWriteCount(columnFamilyStoreMBean.getWriteCount());
        columnFamilyStats.setWriteLatency(columnFamilyStoreMBean.getRecentWriteLatencyMicros());
        columnFamilyStats.setPendingTasks(columnFamilyStoreMBean.getPendingTasks());

        return columnFamilyStats;
    }

    //TODO remove

    private void validateKeyspace(String keyspaceName) throws CassandraServerManagementException {
        if (keyspaceName == null || "".equals(keyspaceName.trim())) {
            throw new CassandraServerManagementException("The keyspace name is empty or null", log);
        }
    }

    private void validateCF(String columnFamilyName) throws CassandraServerManagementException {
        if (columnFamilyName == null || "".equals(columnFamilyName.trim())) {
            throw new CassandraServerManagementException("The column family name is empty or null", log);
        }
    }

}
