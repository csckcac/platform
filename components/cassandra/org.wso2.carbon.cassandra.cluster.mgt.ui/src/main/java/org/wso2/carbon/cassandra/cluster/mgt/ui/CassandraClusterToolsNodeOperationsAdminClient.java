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
package org.wso2.carbon.cassandra.cluster.mgt.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.cassandra.cluster.mgt.stub.tools.CassandraClusterToolsAdminStub;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

import java.rmi.RemoteException;

/**
 * Class for performing node level operations
 */
public class CassandraClusterToolsNodeOperationsAdminClient {
    private static final Log log = LogFactory.getLog(CassandraClusterToolsNodeOperationsAdminClient.class);
    private CassandraClusterToolsAdminStub cassandraClusterToolsAdminStub;

    public CassandraClusterToolsNodeOperationsAdminClient(javax.servlet.ServletContext servletContext,
                                                          javax.servlet.http.HttpSession httpSession) throws Exception {
        ConfigurationContext ctx =
                (ConfigurationContext) servletContext.getAttribute(
                        CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) httpSession.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String serverURL = CarbonUIUtil.getServerURL(servletContext, httpSession);
        init(ctx, serverURL, cookie);

    }
    private void init(ConfigurationContext ctx,
                      String serverURL,
                      String cookie) throws AxisFault {
        String serviceURL = serverURL + "CassandraClusterToolsAdmin";
        cassandraClusterToolsAdminStub = new CassandraClusterToolsAdminStub(ctx, serviceURL);

        ServiceClient client = cassandraClusterToolsAdminStub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
        options.setTimeOutInMilliSeconds(10000);
    }

    /**
     * Drain the node
     * @return Return true if the operation successfully perform and else false
     * @throws CassandraClusterToolsAdminClientException for unable to perform operation due to exception
     */
    public boolean drainNode() throws CassandraClusterToolsAdminClientException {
        try {
            return cassandraClusterToolsAdminStub.drainNode();
        }catch (Exception e)
        {
            throw new CassandraClusterToolsAdminClientException("Error while draining the node",e,log);
        }
    }

    /**
     * Decommission the node
     * @return Return true if the operation successfully perform and else false
     * @throws CassandraClusterToolsAdminClientException for unable to perform operation due to exception
     */
    public boolean decommissionNode() throws CassandraClusterToolsAdminClientException {

        try {
            return cassandraClusterToolsAdminStub.decommissionNode();
        } catch (Exception e) {
            throw new CassandraClusterToolsAdminClientException("Error while decommision the node",e,log);
        }
    }

    /**
     * Perform garbage collector
     * @throws CassandraClusterToolsAdminClientException for unable to perform operation due to exception
     */
    public void performGC() throws CassandraClusterToolsAdminClientException{
        try {
            cassandraClusterToolsAdminStub.performGC();
        } catch (Exception e) {
            throw new CassandraClusterToolsAdminClientException("Error while perform GC",e,log);
        }
    }

    /**
     * Move node to new token
     * @param newToken  Name of the token
     * @return Return true if the operation successfully perform and else false
     * @throws CassandraClusterToolsAdminClientException for unable to perform operation due to exception
     */
    public boolean moveNode(String newToken) throws CassandraClusterToolsAdminClientException{
        try {
            return cassandraClusterToolsAdminStub.moveNode(newToken);
        } catch (Exception e) {
            throw new CassandraClusterToolsAdminClientException("Error while moving node",e,log);
        }
    }

    /**
     * Take a backup of entire node
     * @param tag Name of the backuo
     * @return Return true if the operation successfully perform and else false
     * @throws CassandraClusterToolsAdminClientException for unable to perform operation due to exception
     */
    public boolean takeNodeSnapShot(String tag) throws CassandraClusterToolsAdminClientException{
        try {
            return cassandraClusterToolsAdminStub.takeSnapshotOfNode(tag);
        } catch (Exception e) {
            throw new CassandraClusterToolsAdminClientException("Error while taking snapshot of node",e,log);
        }
    }

    /**
     * Clear a snapshot of node
     * @param tag Name of the snapshot which need to be clear
     * @return Return true if the operation successfully perform and else false
     * @throws CassandraClusterToolsAdminClientException for unable to perform operation due to exception
     */
    public boolean clearNodeSnapShot(String tag) throws CassandraClusterToolsAdminClientException{
        try {
            return cassandraClusterToolsAdminStub.clearSnapshotOfNode(tag);
        } catch (Exception e) {
            throw new CassandraClusterToolsAdminClientException("Error while clearing snapshot of node",e,log);
        }
    }

    /**
     * Start RPC server
     * @throws CassandraClusterToolsAdminClientException for unable to perform operation due to exception
     */
    public void startRPCServer() throws CassandraClusterToolsAdminClientException{
        try {
            cassandraClusterToolsAdminStub.startRPCServer();
        } catch (Exception e) {
            throw new CassandraClusterToolsAdminClientException("Error while starting RPC server",e,log);
        }
    }

    /**
     * Stop RPC server
     * @throws CassandraClusterToolsAdminClientException for unable to perform operation due to exception
     */
    public void stopRPCServer()  throws CassandraClusterToolsAdminClientException{
        try {
            cassandraClusterToolsAdminStub.stopRPCServer();
        } catch (Exception e) {
            throw new CassandraClusterToolsAdminClientException("Error while stopping RPC server",e,log);
        }
    }

    /**
     * Start Gossip server
     * @throws CassandraClusterToolsAdminClientException for unable to perform operation due to exception
     */
    public void startGossipServer() throws CassandraClusterToolsAdminClientException{
        try {
            cassandraClusterToolsAdminStub.startGossipServer();
        } catch (Exception e) {
            throw new CassandraClusterToolsAdminClientException("Error while starting Gossip server",e,log);
        }
    }

    /**
     * Stop Gossip server
     * @throws CassandraClusterToolsAdminClientException for unable to perform operation due to exception
     */
    public void stopGossipServer()  throws CassandraClusterToolsAdminClientException{
        try {
            cassandraClusterToolsAdminStub.stopGossipServer();
        } catch (Exception e) {
            throw new CassandraClusterToolsAdminClientException("Error while stopping Gossip server",e,log);
        }
    }

    /**
     * Enable or Disable incremental backup of cassandra node
     * @param status boolean value to set backup status
     * @throws CassandraClusterToolsAdminClientException  for unable to perform operation due to exception
     */
    public void setIncrementalBackUpStatus(boolean status)  throws CassandraClusterToolsAdminClientException{
        try {
            cassandraClusterToolsAdminStub.setIncrementalBackUpStatus(status);
        } catch (Exception e) {
            throw new CassandraClusterToolsAdminClientException("Error setting incremental backup status",e,log);
        }
    }

    /**
     * Join to cassandra ring
     * @throws CassandraClusterToolsAdminClientException for unable to perform operation due to exception
     */
    public boolean joinRing()  throws CassandraClusterToolsAdminClientException{
        try {
            return cassandraClusterToolsAdminStub.joinCluster();
        } catch (Exception e) {
            throw new CassandraClusterToolsAdminClientException("Error joining to the cluster",e,log);
        }
    }

    /**
     * Get Gossip server status
     * @return boolean
     */
    public boolean getGossipServerStatus(){
        try {
            return cassandraClusterToolsAdminStub.isGossipServerEnable();
        } catch (Exception e) {
            throw new CassandraClusterToolsAdminClientException("Error getting gossip server status",e,log);
        }
    }

    /**
     * Get RPC server status
     * @return boolean
     */
    public boolean getRPCServerStatus(){
        try {
            return cassandraClusterToolsAdminStub.isRPCRunning();
        } catch (Exception e) {
            throw new CassandraClusterToolsAdminClientException("Error getting RPC server status",e,log);
        }
    }

    /**
     * Get Incremental Backup status
     * @return boolean
     */
    public boolean getIncrementalBackUpStatus(){
        try {
            return cassandraClusterToolsAdminStub.getIncrementalBackUpStatus();
        } catch (Exception e) {
            throw new CassandraClusterToolsAdminClientException("Error getting RPC server status",e,log);
        }
    }

    /**
     * Check whether node is join the ring
     * @return boolean
     */
    public boolean isJoinedRing(){
        try {
            return cassandraClusterToolsAdminStub.isJoined();
        } catch (Exception e) {
            throw new CassandraClusterToolsAdminClientException("Error getting node join status",e,log);
        }
    }
}
