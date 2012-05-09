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
package org.wso2.carbon.cassandra.mgt.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.cassandra.mgt.stub.explorer.CassandraExplorerAdminCassandraServerManagementException;
import org.wso2.carbon.cassandra.mgt.stub.explorer.CassandraExplorerAdminStub;
import org.wso2.carbon.cassandra.mgt.stub.explorer.xsd.Column;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

import java.rmi.RemoteException;

public class CassandraExplorerAdminClient {

    CassandraExplorerAdminStub explorerAdminStub;

    public CassandraExplorerAdminClient(ConfigurationContext ctx, String serverURL, String cookie)
            throws AxisFault {
        init(ctx, serverURL, cookie);
    }

    public CassandraExplorerAdminClient(javax.servlet.ServletContext servletContext,
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
        String serviceURL = serverURL + "CassandraExplorerAdmin";
        explorerAdminStub = new CassandraExplorerAdminStub(ctx, serviceURL);
        ServiceClient client = explorerAdminStub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
        options.setTimeOutInMilliSeconds(10000);
    }

    /**
     * Get All the rows for a given Column family
     *
     * @param keyspaceName
     * @param columnFamily
     * @param startKey
     * @param endKey
     * @param limit
     * @return
     * @throws java.rmi.RemoteException
     * @throws org.wso2.carbon.cassandra.mgt.stub.explorer.CassandraExplorerAdminCassandraServerManagementException
     *
     */
    public String[] getRows(String keyspaceName, String columnFamily, String startKey,
                            String endKey, int limit)
            throws RemoteException, CassandraExplorerAdminCassandraServerManagementException {
        return explorerAdminStub.getRowNamesForColumnFamily(keyspaceName, columnFamily, startKey,
                endKey, limit);

    }

    /**
     * Get columns for a given row key
     *
     * @param keyspaceName
     * @param columnFamily
     * @param rowName
     * @param startColumn
     * @param lastCoulmn
     * @param limit
     * @param isReversed
     * @return
     * @throws java.rmi.RemoteException
     * @throws org.wso2.carbon.cassandra.mgt.stub.explorer.CassandraExplorerAdminCassandraServerManagementException
     *
     */
    public Column[] getColumnsForRowName(String keyspaceName, String columnFamily, String rowName,
                                         String startColumn,
                                         String lastCoulmn, int limit, boolean isReversed)
            throws RemoteException, CassandraExplorerAdminCassandraServerManagementException {
        long startTime = System.currentTimeMillis();
        Column[] columns = explorerAdminStub.getColumnsForRow(keyspaceName, columnFamily,
                rowName, startColumn,
                lastCoulmn, limit, isReversed);
        long endTime = System.currentTimeMillis();
        System.out.println("Total elapsed time in execution for webservice  " +
                (endTime - startTime));
        return columns;
    }

    /**
     * Get columns sorted on last updated first order
     *
     * @param keyspaceName
     * @param columnFamily
     * @param rowName
     * @param startColumn
     * @param lastColumn
     * @param limit
     * @param isReversed
     * @throws java.rmi.RemoteException
     * @ * @throws org.wso2.carbon.cassandra.mgt.stub.explorer.CassandraExplorerAdminCassandraServerManagementException
     */
    public Column[] getColumnsInUpdateOrder(String keyspaceName, String columnFamily, String rowName,
                                            String startColumn, String lastColumn, int limit,
                                            boolean isReversed)
            throws CassandraExplorerAdminCassandraServerManagementException, RemoteException {
        return explorerAdminStub.getColumnsInUpdateOrder(keyspaceName, columnFamily, rowName,
                startColumn, lastColumn, limit, isReversed);
    }

    /**
     * Get a column for its key
     *
     * @param keyspace
     * @param columnFamily
     * @param rowName
     * @param columnName
     * @return
     * @throws org.wso2.carbon.cassandra.mgt.stub.explorer.CassandraExplorerAdminCassandraServerManagementException
     *
     * @throws java.rmi.RemoteException
     */
    public Column getColumn(String keyspace, String columnFamily, String rowName, String columnName)
            throws CassandraExplorerAdminCassandraServerManagementException, RemoteException {
        return explorerAdminStub.getColumn(keyspace, columnFamily, rowName, columnName);
    }

    public Column[] paginate(String keyspace, String columnFamily, String rowName,
                             int startingNo, int limit)
            throws CassandraExplorerAdminCassandraServerManagementException, RemoteException {
        return explorerAdminStub.paginate(keyspace, columnFamily, rowName, startingNo, limit);
    }

    public int getNoOfColumns(String keyspace, String columnFamily, String rowName)
            throws CassandraExplorerAdminCassandraServerManagementException, RemoteException {
        return explorerAdminStub.getNoOfColumns(keyspace, columnFamily, rowName);
    }

    public Column[] search(String keyspace, String columnFamily, String rowName, String searchKey,
                           int startingNo, int limit)
            throws CassandraExplorerAdminCassandraServerManagementException, RemoteException {
        return explorerAdminStub.searchColumns(keyspace, columnFamily, rowName, searchKey,
                startingNo, limit);
    }

    public int getNoOfFilteredResults(String keyspace, String columnFamily, String rowName,
                                      String searchKey)
            throws CassandraExplorerAdminCassandraServerManagementException, RemoteException {
        return explorerAdminStub.getNoSearchResults(keyspace, columnFamily, rowName, searchKey);
    }

}
