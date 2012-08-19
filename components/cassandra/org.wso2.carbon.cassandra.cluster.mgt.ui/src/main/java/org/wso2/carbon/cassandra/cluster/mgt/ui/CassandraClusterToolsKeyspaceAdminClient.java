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
import org.wso2.carbon.cassandra.mgt.stub.ks.CassandraKeyspaceAdminStub;
import org.wso2.carbon.cassandra.mgt.stub.ks.xsd.ColumnFamilyInformation;
import org.wso2.carbon.cassandra.mgt.stub.ks.xsd.KeyspaceInformation;
import org.wso2.carbon.cassandra.mgt.stub.ks.xsd.TokenRangeInformation;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

/**
 * Data access class for retrieving keyspaces and column families for the user
 */

public class CassandraClusterToolsKeyspaceAdminClient {

    private static final Log log = LogFactory.getLog(CassandraClusterToolsKeyspaceAdminClient.class);

    private CassandraKeyspaceAdminStub cassandraAdminStub;

    public CassandraClusterToolsKeyspaceAdminClient(ConfigurationContext ctx, String serverURL, String cookie)
            throws AxisFault {
        init(ctx, serverURL, cookie);
    }

    public CassandraClusterToolsKeyspaceAdminClient(javax.servlet.ServletContext servletContext,
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
        String serviceURL = serverURL + "CassandraKeyspaceAdmin";
        cassandraAdminStub = new CassandraKeyspaceAdminStub(ctx, serviceURL);
        ServiceClient client = cassandraAdminStub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
        options.setTimeOutInMilliSeconds(10000);
    }

    /**
     *    Returns Cluster Name
     * @return    cluster name
     * @throws CassandraClusterToolsAdminClientException
     */

    public String getClusterName() throws CassandraClusterToolsAdminClientException {
        try {
            return cassandraAdminStub.getClusterName();
        } catch (Exception e) {
            throw new CassandraClusterToolsAdminClientException("Error retrieving Cluster Name !", e, log);
        }
    }

    /**
     * Gets all keyspaces in a cluster
     *
     * @param clusterName The name of the cluster
     * @param username    The name of the current user
     * @param password    The password of the current user
     * @return A <code>String</code> array representing the names of keyspaces
     * @throws CassandraClusterToolsAdminClientException For errors during locating   kepspaces
     */

    public String[] lisKeyspaces(String clusterName, String username, String password)
            throws CassandraClusterToolsAdminClientException {
        try {
            return cassandraAdminStub.listKeyspaces(clusterName, username, password);
        } catch (Exception e) {
            throw new CassandraClusterToolsAdminClientException("Error retrieving keyspace names !", e, log);
        }
    }

    /**
     * Get all the keyspaces belong to the currently singed up user
     *
     * @return A <code>String</code> array representing the names of keyspaces
     * @throws CassandraClusterToolsAdminClientException For errors during locating  kepspaces
     */
    public String[] listKeyspacesOfCurrentUSer() throws CassandraClusterToolsAdminClientException {
        try {
            return cassandraAdminStub.listKeyspacesOfCurrentUser();
        } catch (Exception e) {
            throw new CassandraClusterToolsAdminClientException("Error retrieving keyspace names !", e, log);
        }
    }

    /**
     * Get all the CFs belong to the currently singed up user for a given keyspace
     *
     * @param keyspaceName the name of the keyspace
     * @return A <code>String</code> array representing the names of CFs
     * @throws CassandraClusterToolsAdminClientException For errors during locating CFs
     */
    public String[] listColumnFamiliesOfCurrentUser(String keyspaceName)
            throws CassandraClusterToolsAdminClientException {
        validateKeyspace(keyspaceName);
        try {
            return cassandraAdminStub.listColumnFamiliesOfCurrentUser(keyspaceName);
        } catch (Exception e) {
            throw new CassandraClusterToolsAdminClientException("Error retrieving CF names !", e, log);
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
