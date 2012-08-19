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

/**
 * Class for performing the column family operations
 */
public class CassandraClusterToolsColumnFamilyOperationsAdminClient {
    private static final Log log = LogFactory.getLog(CassandraClusterToolsColumnFamilyOperationsAdminClient.class);

    private CassandraClusterToolsAdminStub cassandraClusterToolsAdminStub;

    public CassandraClusterToolsColumnFamilyOperationsAdminClient(ConfigurationContext ctx, String serverURL, String cookie)
            throws AxisFault {
        init(ctx, serverURL, cookie);
    }

    public CassandraClusterToolsColumnFamilyOperationsAdminClient(javax.servlet.ServletContext servletContext,
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
     * Flush column family
     * @param keyspace Name of the keyspace which is the target column family located
     * @param columnFamily Name of the column family
     * @return Return true if the operation is success and else false
     * @throws CassandraClusterToolsAdminClientException due to exception in performing the operation
     */
    public boolean flushColumnFamily(String keyspace,String columnFamily)throws CassandraClusterToolsAdminClientException
    {
        validateKeyspace(keyspace);
        validateColumnFamily(columnFamily);
        try {
            return cassandraClusterToolsAdminStub.flushColumnFamily(keyspace,columnFamily);
        } catch (Exception e) {
            throw new CassandraClusterToolsAdminClientException("Error while flushing column family",e,log);
        }
    }

    /**
     * Repair column family
     * @param keyspace Name of the keyspace which is the target column family located
     * @param columnFamily Name of the column family
     * @return Return true if the operation is success and else false
     * @throws CassandraClusterToolsAdminClientException due to exception in performing the operation
     */
    public boolean repairColumnFamily(String keyspace,String columnFamily) throws CassandraClusterToolsAdminClientException
    {
        validateKeyspace(keyspace);
        validateColumnFamily(columnFamily);
        try {
            return cassandraClusterToolsAdminStub.repairColumnFamily(keyspace,columnFamily);
        } catch (Exception e) {
            throw new CassandraClusterToolsAdminClientException("Error while repairing column family",e,log);
        }
    }

    /**
     * Cleanup column family
     * @param keyspace Name of the keyspace which is the target column family located
     * @param columnFamily Name of the column family
     * @return Return true if the operation is success and else false
     * @throws CassandraClusterToolsAdminClientException due to exception in performing the operation
     */
    public boolean cleanUpColumnFamily(String keyspace,String columnFamily)
    {
        validateKeyspace(keyspace);
        validateColumnFamily(columnFamily);
        try {
            return cassandraClusterToolsAdminStub.cleanUpColumnFamily(keyspace,columnFamily);
        } catch (Exception e) {
            throw new CassandraClusterToolsAdminClientException("Error while cleanUp column family",e,log);
        }
    }

    /**
     * Compact column family
     * @param keyspace Name of the keyspace which is the target column family located
     * @param columnFamily Name of the column family
     * @return Return true if the operation is success and else false
     * @throws CassandraClusterToolsAdminClientException due to exception in performing the operation
     */
    public boolean compactColumnFamily(String keyspace,String columnFamily)
    {
        validateKeyspace(keyspace);
        validateColumnFamily(columnFamily);
        try {
            return cassandraClusterToolsAdminStub.compactColumnFamily(keyspace,columnFamily);
        } catch (Exception e) {
            throw new CassandraClusterToolsAdminClientException("Error while compacting column family",e,log);
        }
    }

    /**
     * Scrub column family
     * @param keyspace Name of the keyspace which is the target column family located
     * @param columnFamily Name of the column family
     * @return Return true if the operation is success and else false
     * @throws CassandraClusterToolsAdminClientException due to exception in performing the operation
     */
    public boolean scrubColumnFamily(String keyspace,String columnFamily)
    {
        validateKeyspace(keyspace);
        validateColumnFamily(columnFamily);
        try {
            return cassandraClusterToolsAdminStub.scrubColumnFamily(keyspace,columnFamily);
        } catch (Exception e) {
            throw new CassandraClusterToolsAdminClientException("Error while scrub column family",e,log);
        }
    }

    /**
     * UpgradeSSTables column family
     * @param keyspace Name of the keyspace which is the target column family located
     * @param columnFamily Name of the column family
     * @return Return true if the operation is success and else false
     * @throws CassandraClusterToolsAdminClientException due to exception in performing the operation
     */
    public boolean upgradeSSTablesColumnFamily(String keyspace,String columnFamily)
    {
        validateKeyspace(keyspace);
        validateColumnFamily(columnFamily);
        try {
            return cassandraClusterToolsAdminStub.upgradeSSTablesColumnFamily(keyspace,columnFamily);
        } catch (Exception e) {
            throw new CassandraClusterToolsAdminClientException("Error while upgradeSSTables column family",e,log);
        }
    }

    /**
     * validate Keyspace Name
     * @param keyspaceName  Name of the keyspace
     * @throws CassandraClusterToolsAdminClientException
     */
    private void validateKeyspace(String keyspaceName) throws CassandraClusterToolsAdminClientException {
        if (keyspaceName == null || "".equals(keyspaceName.trim())) {
            throw new CassandraClusterToolsAdminClientException("The keyspace name is empty or null", log);
        }
    }

    /**
     * validate column family Name
     * @param columnFamily Name of the column family
     * @throws CassandraClusterToolsAdminClientException
     */
    private void validateColumnFamily(String columnFamily) throws CassandraClusterToolsAdminClientException {
        if (columnFamily == null || "".equals(columnFamily.trim())) {
            throw new CassandraClusterToolsAdminClientException("The column family name is empty or null", log);
        }
    }
}
