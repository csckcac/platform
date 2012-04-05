/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.reporting.template.core.client;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.index.stub.IndexAdminServiceConfigurationException;
import org.wso2.carbon.bam.index.stub.IndexAdminServiceStub;
import org.wso2.carbon.bam.index.stub.service.types.TableDTO;
import org.wso2.carbon.bam.presentation.stub.QueryServiceConfigurationException;
import org.wso2.carbon.bam.presentation.stub.QueryServiceStoreException;
import org.wso2.carbon.bam.presentation.stub.QueryServiceStub;
import org.wso2.carbon.reporting.api.ReportingException;
import org.wso2.carbon.reporting.template.core.internal.ReportingTemplateComponent;


import java.net.SocketException;
import java.rmi.RemoteException;
import java.util.ArrayList;

/*
This class is client for the cassendra data source which BAM uses.
 */
public class BAMDBClient extends AbstractClient {
    private QueryServiceStub queryServiceStub;
    private IndexAdminServiceStub indexAdminServiceStub;


    private static Log log = LogFactory.getLog(BAMDBClient.class);

    public BAMDBClient() throws ReportingException {
        try {
            if (ReportingTemplateComponent.isBAMServer()) {
                String queryServiceURL = getBackendServerURLHTTPS() + "services/QueryService";
                queryServiceStub = new QueryServiceStub(ReportingTemplateComponent.getConfigurationContextService().getServerConfigContext(), queryServiceURL);

                String indexAdminURL = getBackendServerURLHTTPS() + "services/IndexAdminService";
                indexAdminServiceStub = new IndexAdminServiceStub(ReportingTemplateComponent.getConfigurationContextService().getServerConfigContext(), indexAdminURL);
            }
        } catch (SocketException e) {
            log.error("Socket exception in BAM DB Service", e);
            throw new ReportingException("Socket exception in BAM DB Service", e);
        } catch (AxisFault axisFault) {
            log.error(axisFault);
            throw new ReportingException("Axis Fault occured", axisFault);
        }

    }


    public String[] getAllColumnFamilies() throws ReportingException {
        try {
            ArrayList<String> userDefinedFamily = new ArrayList<String>();
            String[] allFamily = queryServiceStub.getAllColumnFamilies();
            for (String aColumnFamily : allFamily) {
                if (isUserDefined(aColumnFamily) & !isIndex(aColumnFamily))
                    userDefinedFamily.add(aColumnFamily);
            }
            return userDefinedFamily.toArray(new String[userDefinedFamily.size()]);
        } catch (RemoteException e) {
            throw new ReportingException(e.getMessage(), e);
        } catch (QueryServiceConfigurationException e) {
            throw new ReportingException(e.getMessage(), e);
        }
    }


    private boolean isUserDefined(String columnFamilyName) {
        if (columnFamilyName.equalsIgnoreCase("CORRELATION"))
            return false;
        else if (columnFamilyName.equalsIgnoreCase("EVENT"))
            return false;
        else if (columnFamilyName.equalsIgnoreCase("META"))
            return false;
        else if (columnFamilyName.equalsIgnoreCase("META_INFO"))
            return false;
        else if (columnFamilyName.equalsIgnoreCase("BASE"))
            return false;
        else if (columnFamilyName.equalsIgnoreCase("CURSOR_INFO"))
            return false;
        else if (columnFamilyName.equalsIgnoreCase("INDEX_INFO"))
            return false;
        else return !columnFamilyName.equalsIgnoreCase("TABLE_INFO");
    }


    private boolean isIndex(String cfName) {
        return cfName.contains("_Index_");
    }

    public String[] getColumnNames(String columnFamily) {
        try {
            TableDTO[] allTables = indexAdminServiceStub.getAllTableMetaData(false);
            String[] colums = null;
            for (TableDTO tableDTO : allTables) {
                if (tableDTO.getTableName() != null && tableDTO.getTableName().equalsIgnoreCase(columnFamily)) {
                    colums = tableDTO.getColumns();
                    break;
                }
            }
            return colums;
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        } catch (IndexAdminServiceConfigurationException e) {
            e.printStackTrace();
            return null;
        }
    }

    public OMElement queryColumnFamily(String columnFamilyname, String indexName, String[] compositeIndexNames) throws ReportingException {

        QueryServiceStub.CompositeIndex[] compositeIndexes = null;
        if (compositeIndexNames != null)
            compositeIndexes = new QueryServiceStub.CompositeIndex[compositeIndexNames.length];
        else compositeIndexes = null;

        if (compositeIndexNames != null) {
            int i = 0;
            for (String compositeIndex : compositeIndexNames) {
                compositeIndexes[i] = new QueryServiceStub.CompositeIndex();
                compositeIndexes[i].setIndexName(compositeIndex);
                compositeIndexes[i].setRangeFirst("");
                compositeIndexes[i].setRangeLast("");
            }
        }


        try {
            return queryServiceStub.queryColumnFamily(columnFamilyname, indexName, compositeIndexes);
        } catch (RemoteException e) {
            throw new ReportingException(e.getMessage(), e);
        } catch (QueryServiceStoreException e) {
            throw new ReportingException(e.getMessage(), e);
        }
    }

    public String[] getIndexes(String columnFamilyName) throws ReportingException {
        try {
            return queryServiceStub.getIndexes(columnFamilyName);
        } catch (RemoteException e) {
            throw new ReportingException(e.getMessage(), e);
        } catch (QueryServiceConfigurationException e) {
            throw new ReportingException(e.getMessage(), e);
        }
    }

    public boolean isBAMDBFound() {
        String[] tables = null;
        try {
            tables = queryServiceStub.getAllColumnFamilies();
            return tables != null && tables.length != 0;
        } catch (RemoteException e) {
            return false;
        } catch (QueryServiceConfigurationException e) {
            return false;
        }

    }

}
