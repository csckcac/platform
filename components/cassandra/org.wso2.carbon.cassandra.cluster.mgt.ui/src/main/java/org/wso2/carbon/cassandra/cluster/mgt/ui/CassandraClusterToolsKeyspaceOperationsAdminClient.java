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
import org.wso2.carbon.cassandra.mgt.stub.ks.CassandraKeyspaceAdminStub;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

import java.rmi.RemoteException;

/**
 * Class for performing the keyspace level operations
 */
public class CassandraClusterToolsKeyspaceOperationsAdminClient {

    private static final Log log = LogFactory.getLog(CassandraClusterToolsKeyspaceOperationsAdminClient.class);

    private CassandraClusterToolsAdminStub cassandraClusterToolsAdminStub;

    public CassandraClusterToolsKeyspaceOperationsAdminClient(ConfigurationContext ctx, String serverURL, String cookie)
            throws AxisFault {
        init(ctx, serverURL, cookie);
    }

    public CassandraClusterToolsKeyspaceOperationsAdminClient(javax.servlet.ServletContext servletContext,
                                                    javax.servlet.http.HttpSession httpSession)
            throws Exception {
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
     * Flush keyspace
     * @param keyspace Name of the keyspace
     * @return Return true if the operation successfully perform and else false
     * @throws CassandraClusterToolsAdminClientException for unable to perform operation due to exception
     */
    public boolean flushKeyspace(String keyspace) throws CassandraClusterToolsAdminClientException
    {
        validateKeyspace(keyspace);
        try {
            return cassandraClusterToolsAdminStub.flushKeyspace(keyspace);
        } catch (Exception e) {
            throw new CassandraClusterToolsAdminClientException("Error while flushing keyspace",e,log);
        }
    }

    /**
     * Compact keyspace
     * @param keyspace Name of the keyspace
     * @return Return true if the operation successfully perform and else false
     * @throws CassandraClusterToolsAdminClientException for unable to perform operation due to exception
     */
    public boolean compactKeyspace(String keyspace) throws CassandraClusterToolsAdminClientException
    {
        validateKeyspace(keyspace);
        try {
           return  cassandraClusterToolsAdminStub.compactKeyspace(keyspace);
        } catch (Exception e) {
            throw new CassandraClusterToolsAdminClientException("Error while compacting keyspace",e,log);
        }
    }

    /**
     * Cleanup keyspace
     * @param keyspace Name of the keyspace
     * @return Return true if the operation successfully perform and else false
     * @throws CassandraClusterToolsAdminClientException for unable to perform operation due to exception
     */
    public boolean cleanUpKeyspace(String keyspace) throws CassandraClusterToolsAdminClientException
    {
        validateKeyspace(keyspace);
        try {
            return cassandraClusterToolsAdminStub.cleanUpKeyspace(keyspace);
        } catch (Exception e) {
            throw new CassandraClusterToolsAdminClientException("Error while cleanUp keyspace",e,log);
        }
    }

    /**
     * Repair keyspace
     * @param keyspace Name of the keyspace
     * @return Return true if the operation successfully perform and else false
     * @throws CassandraClusterToolsAdminClientException for unable to perform operation due to exception
     */
    public boolean repairKeyspace(String keyspace) throws CassandraClusterToolsAdminClientException
    {
        validateKeyspace(keyspace);
        try {
            return cassandraClusterToolsAdminStub.repairKeyspace(keyspace);
        } catch (Exception e) {
            throw new CassandraClusterToolsAdminClientException("Error while repairing keyspace",e,log);
        }
    }

    /**
     * Scrub keyspace
     * @param keyspace Name of the keyspace
     * @return Return true if the operation successfully perform and else false
     * @throws CassandraClusterToolsAdminClientException for unable to perform operation due to exception
     */
    public boolean scrubKeyspace(String keyspace) throws CassandraClusterToolsAdminClientException
    {
        validateKeyspace(keyspace);
        try {
            return cassandraClusterToolsAdminStub.scrubKeyspace(keyspace);
        } catch (Exception e) {
            throw new CassandraClusterToolsAdminClientException("Error while scrubing keyspace",e,log);
        }
    }

    /**
     * UpgradeSSTables keyspace
     * @param keyspace Name of the keyspace
     * @return Return true if the operation successfully perform and else false
     * @throws CassandraClusterToolsAdminClientException for unable to perform operation due to exception
     */
    public boolean upgradeSSTablesKeyspace(String keyspace) throws CassandraClusterToolsAdminClientException
    {
        validateKeyspace(keyspace);
        try {
            return cassandraClusterToolsAdminStub.upgradeSSTablesInKeyspace(keyspace);
        } catch (Exception e) {
            throw new CassandraClusterToolsAdminClientException("Error while upgradeSSTables keyspace",e,log);
        }
    }

    /**
     * Backup data in a keyspace
     * @param tag  Name of the snapshot
     * @param keyspace Name of the keyspace which need to taken a snapshot
     * @return Return true if the operation successfully perform and else false
     * @throws CassandraClusterToolsAdminClientException for unable to perform operation due to exception
     */
    public boolean takeSnapshotKeyspace(String tag,String keyspace) throws CassandraClusterToolsAdminClientException
    {
        validateKeyspace(keyspace);
        try {
            return cassandraClusterToolsAdminStub.takeSnapshotOfKeyspace(tag,keyspace);
        } catch (Exception e) {
            throw new CassandraClusterToolsAdminClientException("Error while taking snapshot of keyspace",e,log);
        }
    }

    /**
     * Clear backup data in a keyspace
     * @param tag  Name of the snapshot need to be clear
     * @param keyspace Name of the keyspace which need to cleat the snapshot
     * @return Return true if the operation successfully perform and else false
     * @throws CassandraClusterToolsAdminClientException for unable to perform operation due to exception
     */
    public boolean clearSnapshotKeyspace(String tag,String keyspace) throws CassandraClusterToolsAdminClientException
    {
        validateKeyspace(keyspace);
        try {
            return cassandraClusterToolsAdminStub.clearSnapshotOfKeyspace(tag,keyspace);
        } catch (Exception e) {
            throw new CassandraClusterToolsAdminClientException("Error while clearing snapshot of keyspace",e,log);
        }
    }

    /**
     * validate Keyspace Name
     * @param keyspaceName
     * @throws CassandraClusterToolsAdminClientException
     */
    private void validateKeyspace(String keyspaceName) throws CassandraClusterToolsAdminClientException {
        if (keyspaceName == null || "".equals(keyspaceName.trim())) {
            throw new CassandraClusterToolsAdminClientException("The keyspace name is empty or null", log);
        }
    }
}
