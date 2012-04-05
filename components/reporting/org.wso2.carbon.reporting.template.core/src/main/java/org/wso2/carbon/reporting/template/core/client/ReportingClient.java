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

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.reporting.api.ReportingException;
import org.wso2.carbon.reporting.stub.*;
import org.wso2.carbon.reporting.stub.core.services.*;
import org.wso2.carbon.reporting.template.core.handler.database.BAMDBHandler;
import org.wso2.carbon.reporting.template.core.handler.database.DataSourceHandler;
import org.wso2.carbon.reporting.template.core.handler.metadata.ChartMetaDataHandler;
import org.wso2.carbon.reporting.template.core.handler.metadata.CompositeReportMetaDataHandler;
import org.wso2.carbon.reporting.template.core.handler.metadata.MetadataFinder;
import org.wso2.carbon.reporting.template.core.handler.metadata.TableReportMetaDataHandler;
import org.wso2.carbon.reporting.template.core.internal.ReportingTemplateComponent;
import org.wso2.carbon.reporting.template.core.util.chart.ChartReportDTO;
import org.wso2.carbon.reporting.template.core.util.common.ReportConstants;
import org.wso2.carbon.reporting.template.core.util.table.TableReportDTO;

import javax.activation.DataHandler;
import java.io.*;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/*
  This class communicates with reporting core back-end
*/
public class ReportingClient extends AbstractClient {
    private JrxmlFileUploaderStub fileUploaderStub;
    private DBReportingServiceStub dbReportingServiceStub;
    private ReportingResourcesSupplierStub resourceSupplierStub;

    private static Log log = LogFactory.getLog(ReportingClient.class);

    public ReportingClient() {
        try {
            String serverFileUploaderURL = getBackendServerURLHTTPS() + "services/JrxmlFileUploader";
            fileUploaderStub = new JrxmlFileUploaderStub(ReportingTemplateComponent.getConfigurationContextService().getServerConfigContext(), serverFileUploaderURL);

            String serverDBReportingURL = getBackendServerURLHTTPS() + "services/DBReportingService";
            dbReportingServiceStub = new DBReportingServiceStub(ReportingTemplateComponent.getConfigurationContextService().getServerConfigContext(), serverDBReportingURL);

            String serverReportingResourceURL = getBackendServerURLHTTPS() + "services/ReportingResourcesSupplier";
            resourceSupplierStub = new ReportingResourcesSupplierStub(ReportingTemplateComponent.getConfigurationContextService().getServerConfigContext(), serverReportingResourceURL);
        } catch (AxisFault axisFault) {
            log.error(axisFault);
        } catch (SocketException e) {
            log.error(e);
        }
    }


    public void uploadJrxmlFile(String filename, String fileContent) {
        try {
            fileUploaderStub.uploadJrxmlFile(filename, fileContent);
            fileUploaderStub.cleanup();
        } catch (RemoteException e) {
            log.error(e);
        } catch (JrxmlFileUploaderJRExceptionException e) {
            log.error(e);
        } catch (JrxmlFileUploaderReportingExceptionException e) {
            log.error(e);
        }
    }

    private DataHandler generateTableReport(String reportName, String type) throws ReportingException {
        TableReportDTO tableReport = new TableReportMetaDataHandler().getTableReportMetaData(reportName);
        Map[] data = null;
        if (tableReport.getDsName().equalsIgnoreCase(ReportConstants.BAMDATASOURCE)) {
            data = new BAMDBHandler().createMapDataSource(tableReport);
        } else {
            data = new DataSourceHandler().createMapDataSource(tableReport);
        }
        ReportDataSource dataSource = getReportDataSource(data);
        ReportParamMap[] maps = new ReportParamMap[1];
        ReportParamMap map = new ReportParamMap();
        map.setParamKey("TableDataSource");
        map.setDataSource(dataSource);
        maps[0] = map;
        DataHandler dataHandler = null;
        try {
            dataHandler = dbReportingServiceStub.getJRDataSourceReport(dataSource, reportName, maps, type);
            dbReportingServiceStub.cleanup();
            return dataHandler;
        } catch (RemoteException e) {
            log.error("Error while generating the report", e);
            throw new ReportingException(e.getMessage(), e);
        } catch (DBReportingServiceJRExceptionException e) {
            log.error("Error while generating the report", e);
            throw new ReportingException(e.getMessage(), e);
        } catch (DBReportingServiceReportingExceptionException e) {
            log.error("Error while generating the report", e);
            throw new ReportingException(e.getMessage(), e);
        }
    }


    public DataHandler generateReport(String reportName, String type) throws ReportingException {
        String reportType = MetadataFinder.findReportType(reportName);
        if (reportType != null) {
            if (reportType.equalsIgnoreCase(ReportConstants.TABLE_TYPE)) {
                return generateTableReport(reportName, type);
            }
            if (reportType.equalsIgnoreCase(ReportConstants.COMPOSITE_TYPE)) {
                return generateCompositeReport(reportName, type);
            } else {
                return generateChartReport(reportName, type);
            }
        } else {
            log.error("Couldn't find report type from the meta data for report -" + reportName);
            throw new ReportingException("Couldn't find report type from the meta data for report -" + reportName);
        }
    }


    private DataHandler generateCompositeReport(String reportName, String reportType) throws ReportingException {
        LinkedHashMap<String, String> report = new CompositeReportMetaDataHandler().getCompositeReport(reportName);
        ArrayList<ReportParamMap> mapList = new ArrayList<ReportParamMap>();

        int i = 0;
        for (String aReportName : report.keySet()) {
            String aReportType = MetadataFinder.findReportType(aReportName);
            Map[] data = null;
            if (aReportType.equalsIgnoreCase(ReportConstants.TABLE_TYPE)) {
                TableReportDTO tableReport = new TableReportMetaDataHandler().getTableReportMetaData(aReportName);
                if (tableReport.getDsName().equalsIgnoreCase(ReportConstants.BAMDATASOURCE)) {
                    data = new BAMDBHandler().createMapDataSource(tableReport);
                } else {
                    data = new DataSourceHandler().createMapDataSource(tableReport);
                }

            } else {
                ChartReportDTO chartReport = new ChartMetaDataHandler().getChartReportMetaData(aReportName);
                if (chartReport.getDsName().equalsIgnoreCase(ReportConstants.BAMDATASOURCE)) {
                    data = new BAMDBHandler().createMapDataSource(chartReport);
                } else {
                    data = new DataSourceHandler().createMapDataSource(chartReport);
                }
            }

            ReportDataSource dataSource = getReportDataSource(data);
            ReportParamMap map = new ReportParamMap();
            map.setParamKey(report.get(aReportName));
            map.setDataSource(dataSource);
            mapList.add(map);

            i++;
        }


        ReportParamMap[] maps = new ReportParamMap[mapList.size()];
        maps = mapList.toArray(maps);
        DataHandler dataHandler = null;
        try {
            dataHandler = dbReportingServiceStub.getJRDataSourceReport(null, reportName, maps, reportType);
            dbReportingServiceStub.cleanup();
            return dataHandler;
        } catch (RemoteException e) {
            log.error("Exception occurred while generating the composite report -" + reportName, e);
            throw new ReportingException(e.getMessage(), e);
        } catch (DBReportingServiceJRExceptionException e) {
            log.error("Exception occurred while generating the composite report -" + reportName, e);
            throw new ReportingException(e.getMessage(), e);
        } catch (DBReportingServiceReportingExceptionException e) {
            log.error("Exception occurred while generating the composite report -" + reportName, e);
            throw new ReportingException(e.getMessage(), e);
        }

    }

    private DataHandler generateChartReport(String reportName, String type) throws ReportingException {
        ChartReportDTO chartReport = new ChartMetaDataHandler().getChartReportMetaData(reportName);
        Map[] data = null;
        if (chartReport.getDsName().equalsIgnoreCase(ReportConstants.BAMDATASOURCE)) {
            data = new BAMDBHandler().createMapDataSource(chartReport);
        } else {
            data = new DataSourceHandler().createMapDataSource(chartReport);
        }
        ReportDataSource dataSource = getReportDataSource(data);
        ReportParamMap[] maps = new ReportParamMap[1];
        ReportParamMap map = new ReportParamMap();
        map.setParamKey("TableDataSource");
        map.setDataSource(dataSource);
        maps[0] = map;
        DataHandler dataHandler = null;
        try {
            dataHandler = dbReportingServiceStub.getJRDataSourceReport(dataSource, reportName, maps, type);
            dbReportingServiceStub.cleanup();
            return dataHandler;
        } catch (RemoteException e) {
            log.error("Exception occurred when generating chart report -"+reportName);
            throw new ReportingException(e.getMessage(), e);
        } catch (DBReportingServiceJRExceptionException e) {
             log.error("Exception occurred when generating chart report -"+reportName);
            throw new ReportingException(e.getMessage(), e);
        } catch (DBReportingServiceReportingExceptionException e) {
             log.error("Exception occurred when generating chart report -"+reportName);
            throw new ReportingException(e.getMessage(), e);
        }
    }



    private ReportDataSource getReportDataSource(Map[] mapDataSource) {
        ReportDataSource dataSource = new ReportDataSource();
        for (Map aMapData : mapDataSource) {
            Row row = new Row();

            Iterator it = aMapData.entrySet().iterator();
            while (it.hasNext()) {
                Column column = new Column();
                Map.Entry pairs = (Map.Entry) it.next();
                column.setKey(pairs.getKey().toString());
                Object valueObj = pairs.getValue();
                if (valueObj instanceof Integer) {
                    Integer number = (Integer) valueObj;
                    column.setValue(String.valueOf(number));
                    column.setType("java.lang.Integer");
                } else if (valueObj instanceof Double) {
                    Double number = (Double) valueObj;
                    column.setValue(String.valueOf(number));
                    column.setType("java.lang.Double");
                } else {
                    column.setValue(pairs.getValue().toString());
                    column.setType("java.lang.String");
                }
                row.addColumns(column);
            }
            dataSource.addRows(row);
        }
        return dataSource;
    }

    public InputStream getJrxmlResource(String filename) throws ReportingException {
        try {
            String content = resourceSupplierStub.getJRXMLFileContent(null, filename);
            InputStream is = new ByteArrayInputStream(content.getBytes());
            return is;
        } catch (RemoteException e) {
            log.error("Cannot access the service ReportingResourcesSupplier", e);
            throw new ReportingException("Cannot access the service ReportingResourcesSupplier");
        } catch (ReportingResourcesSupplierReportingExceptionException e) {
            log.error("Exception while retrieving the jrxml file", e);
            throw new ReportingException(e.getMessage(), e);
        }
    }

    public void uploadImage(String fileName, String reportName, DataHandler imageContent) throws ReportingException {
        try {
            fileUploaderStub.uploadLogo(fileName, reportName, imageContent);
        } catch (RemoteException e) {
            log.error("Cannot access file uploaded service for upload image", e);
            throw new ReportingException(e.getMessage(), e);
        } catch (JrxmlFileUploaderReportingExceptionException e) {
            log.error("Exception occurred while uploading the image", e);
            throw new ReportingException(e.getMessage(), e);
        }
    }


}
