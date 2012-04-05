package org.wso2.carbon.bam.clustermonitor.ui;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.clustermonitor.ui.data.ConfigReader;
import org.wso2.carbon.bam.clustermonitor.ui.data.GraphData;
import org.wso2.carbon.bam.clustermonitor.ui.data.OperationData;
import org.wso2.carbon.bam.clustermonitor.ui.data.ProxyServiceData;
import org.wso2.carbon.bam.clustermonitor.ui.data.ProxyServiceTableData;
import org.wso2.carbon.bam.clustermonitor.ui.data.ServiceTableData;
import org.wso2.carbon.bam.index.stub.IndexAdminServiceStub;
import org.wso2.carbon.bam.index.stub.service.types.IndexDTO;
import org.wso2.carbon.bam.presentation.stub.QueryServiceStub;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class ClusterAdminClient {

    public static final String RESPONSE_TIME = "response_time";
    public static final String RESPONSE_COUNT = "response_count";
    public static final String FAULT_COUNT = "fault_count";
    public static final String REQUEST_COUNT = "request_count";
    public static final String OPERATION = "operation";
    public static final String SERVICE = "service";
    public static final String CLUSTER = "cluster";
    public static final String OPERATION_NAME = "operation_name";
    public static final String SERVICE_NAME = "service_name";
    public static final String ROWS = "rows";
    public static final String ROW = "row";
    private IndexAdminServiceStub indexAdminStub;

    private QueryServiceStub queryServiceStub;

    private String INDEX_ADMIN_SERVICE_URL = "IndexAdminService";

    private String QUERY_ADMIN_SERVICE_URL = "QueryService";

    private IndexDTO clusterIndexDTO = null;
    private IndexDTO serviceIndexDTO = null;
    private IndexDTO operationIndexDTO = null;
    private IndexDTO proxyServiceIndexDTO = null;

    private static final Log log = LogFactory.getLog(ClusterAdminClient.class);


    private Map<String, Object> resultMap = new HashMap<String, Object>();

    public ClusterAdminClient(String cookie, String backendServerURL,
                              ConfigurationContext configCtx) throws AxisFault {
        String indexServiceURL = backendServerURL + INDEX_ADMIN_SERVICE_URL;
        indexAdminStub = new IndexAdminServiceStub(configCtx, indexServiceURL);
        ServiceClient indexServiceClient = indexAdminStub._getServiceClient();
        Options indexServiceClientOptions = indexServiceClient.getOptions();
        indexServiceClientOptions.setManageSession(true);
        indexServiceClientOptions.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);


        String queryServiceURL = backendServerURL + QUERY_ADMIN_SERVICE_URL;
        queryServiceStub = new QueryServiceStub(configCtx, queryServiceURL);
        ServiceClient queryServiceClient = queryServiceStub._getServiceClient();
        Options queryServiceClientOptions = queryServiceClient.getOptions();
        queryServiceClientOptions.setManageSession(true);
        queryServiceClientOptions.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        ConfigReader.getClusterMonitorConfig();

        String clusterIndexName = ConfigReader.getClusterIndexName();
        String serviceIndexName = ConfigReader.getServiceIndexName();
        String operationIndexName = ConfigReader.getOperationIndexName();
        String proxyServiceIndexName = ConfigReader.getProxyServiceIndexName();

        try {
            proxyServiceIndexDTO = indexAdminStub.getIndex(proxyServiceIndexName);
            clusterIndexDTO = indexAdminStub.getIndex(clusterIndexName);
            serviceIndexDTO = indexAdminStub.getIndex(serviceIndexName);
            operationIndexDTO = indexAdminStub.getIndex(operationIndexName);
        } catch (Exception e) {
            log.error("Error occurred while getting indexing data, check the index name", e);
        }
    }

    public String[] getDataCenters() {
        String[] dataCenters = null;
        try {
            dataCenters = indexAdminStub.getIndexValues(ConfigReader.getClusterIndexName(),
                                                        clusterIndexDTO.getIndexedColumns()[0]);
        } catch (Exception e) {
            log.error("Error while retrieving data centers", e);
        }
        return dataCenters;
    }

    public String[] getClusters(String dataCenter) {
        String[] clusters = null;
        try {
            clusters = indexAdminStub.getSubIndexValues(ConfigReader.getClusterIndexName(),
                                                        clusterIndexDTO.getIndexedColumns()[0],
                                                        dataCenter);
        } catch (Exception e) {
            log.error("Error while retrieving clusters", e);
        }

        return clusters;
    }

    public String[] getProxyServices(String cluster) {
        String[] proxyServices = null;

        try {
            if (proxyServiceIndexDTO != null) {
                proxyServices = indexAdminStub.getSubIndexValues(ConfigReader.getProxyServiceIndexName(),
                                                                 proxyServiceIndexDTO.getIndexedColumns()[1],
                                                                 cluster);
            }
        } catch (Exception e) {
            log.error("Error while retrieving services", e);
        }
        return proxyServices;
    }

    public String[] getServices(String cluster) {
        String[] services = null;

        try {
            services = indexAdminStub.getSubIndexValues(ConfigReader.getServiceIndexName(),
                                                        serviceIndexDTO.getIndexedColumns()[1],
                                                        cluster);
        } catch (Exception e) {
            log.error("Error while retrieving services", e);
        }
        return services;
    }

    public String[] getOperations(String serviceName) {
        String[] operations = null;

        try {
            operations = indexAdminStub.getSubIndexValues(ConfigReader.getOperationIndexName(),
                                                          operationIndexDTO.getIndexedColumns()[2],
                                                          serviceName);
        } catch (Exception e) {
            log.error("Error while retrieving operations", e);
        }
        return operations;
    }

    public ServiceTableData getClusterStatistics(String dataCenter, String cluster) {
        ServiceTableData tableData = null;

        QueryServiceStub.CompositeIndex[] compositeIndexes = new QueryServiceStub.CompositeIndex[2];
        compositeIndexes[0] = new QueryServiceStub.CompositeIndex();
        compositeIndexes[0].setIndexName(operationIndexDTO.getIndexedColumns()[0]);
        compositeIndexes[0].setRangeFirst(dataCenter);
        compositeIndexes[0].setRangeLast(dataCenter);

        compositeIndexes[1] = new QueryServiceStub.CompositeIndex();
        compositeIndexes[1].setIndexName(operationIndexDTO.getIndexedColumns()[1]);
        compositeIndexes[1].setRangeFirst(cluster);
        String changedClusterName = getNextStringInLexicalOrder(cluster);
        compositeIndexes[1].setRangeLast(changedClusterName);

        try {
            tableData = new ServiceTableData();

            OMElement queryResult = queryServiceStub.queryColumnFamily(operationIndexDTO.getIndexedTable(),
                                                                       ConfigReader.getOperationIndexName(),
                                                                       compositeIndexes);
            OMElement rowsElement = queryResult.getFirstChildWithName(new QName(ROWS));
            Iterator oMElementIterator = rowsElement.getChildrenWithName(new QName(ROW));
            while (oMElementIterator.hasNext()) {

                OMElement element = (OMElement) oMElementIterator.next();

                String responseTimeStr = element.getFirstChildWithName(new QName(RESPONSE_TIME)).getText();
                if (responseTimeStr != null) {
                    responseTimeStr = "" + round(Float.parseFloat(responseTimeStr), 4);
                }
                String responseCountStr = element.getFirstChildWithName(new QName(RESPONSE_COUNT)).getText();
                String faultCountStr = element.getFirstChildWithName(new QName(FAULT_COUNT)).getText();
                String operationNameStr = element.getFirstChildWithName(new QName(OPERATION_NAME)).getText();
                String serviceNameStr = element.getFirstChildWithName(new QName(SERVICE_NAME)).getText();
                String requestCountStr = element.getFirstChildWithName(new QName(REQUEST_COUNT)).getText();

                OperationData operationData = new OperationData();
                operationData.setOperationName(operationNameStr);
                operationData.setRequestCount(requestCountStr);
                operationData.setFaultCount(faultCountStr);
                operationData.setResponseCount(responseCountStr);
                operationData.setResponseTime(responseTimeStr);

                tableData.setServiceData(serviceNameStr, operationData);

            }
        } catch (Exception e) {
            log.error("Error while retrieving cluster statistics", e);
        }
        return tableData;
    }

    public ProxyServiceTableData getProxyServiceTableData(String dataCenter, String cluster) {
        ProxyServiceTableData tableData = null;

        if (proxyServiceIndexDTO != null) {
            QueryServiceStub.CompositeIndex[] compositeIndexes = new QueryServiceStub.CompositeIndex[2];
            compositeIndexes[0] = new QueryServiceStub.CompositeIndex();
            compositeIndexes[0].setIndexName(proxyServiceIndexDTO.getIndexedColumns()[0]);
            compositeIndexes[0].setRangeFirst(dataCenter);
            compositeIndexes[0].setRangeLast(dataCenter);

            compositeIndexes[1] = new QueryServiceStub.CompositeIndex();
            compositeIndexes[1].setIndexName(proxyServiceIndexDTO.getIndexedColumns()[1]);
            compositeIndexes[1].setRangeFirst(cluster);
            String changedClusterName = getNextStringInLexicalOrder(cluster);
            compositeIndexes[1].setRangeLast(changedClusterName);

            try {
                tableData = new ProxyServiceTableData();

                OMElement queryResult = queryServiceStub.queryColumnFamily(proxyServiceIndexDTO.getIndexedTable(),
                                                                           ConfigReader.getProxyServiceIndexName(),
                                                                           compositeIndexes);
                OMElement rowsElement = queryResult.getFirstChildWithName(new QName(ROWS));
                Iterator oMElementIterator = rowsElement.getChildrenWithName(new QName(ROW));
                while (oMElementIterator.hasNext()) {

                    OMElement element = (OMElement) oMElementIterator.next();

                    String responseTimeStr = element.getFirstChildWithName(new QName("avg_processing_time")).getText();
                    if (responseTimeStr != null) {
                        responseTimeStr = "" + round(Float.parseFloat(responseTimeStr), 4);
                    }
                    String faultCountStr = element.getFirstChildWithName(new QName(FAULT_COUNT)).getText();
                    String direction = element.getFirstChildWithName(new QName("direction")).getText();
                    String proxyNameStr = element.getFirstChildWithName(new QName("resource_id")).getText();
                    String countStr = element.getFirstChildWithName(new QName("count")).getText();

                    ProxyServiceData proxyData = new ProxyServiceData();
                    proxyData.setCount(countStr);
                    proxyData.setFaultCount(faultCountStr);
                    proxyData.setResponseTime(responseTimeStr);
                    proxyData.setDirection(direction);

                    tableData.setServiceData(proxyNameStr, proxyData);

                }
            } catch (Exception e) {
                log.error("Error while retrieving cluster statistics", e);
            }
        }
        return tableData;
    }


    private void getDataFromCassandra(String[] value) {
        if (value.length == 2) {
            getClusterSummaryData(value);
        } else if (value.length == 3) {
            getServiceSummaryData(value);
        } else if (value.length == 4) {
            getOperationSummaryData(value);
        }
    }

    private void getOperationSummaryData(String[] value) {
        QueryServiceStub.CompositeIndex[] compositeIndex = new QueryServiceStub.CompositeIndex[4];

        compositeIndex[0] = new QueryServiceStub.CompositeIndex();
        compositeIndex[0].setIndexName(operationIndexDTO.getIndexedColumns()[0]);
        compositeIndex[0].setRangeFirst(value[0]);
        compositeIndex[0].setRangeLast(value[0]);

        compositeIndex[1] = new QueryServiceStub.CompositeIndex();
        compositeIndex[1].setIndexName(operationIndexDTO.getIndexedColumns()[1]);
        compositeIndex[1].setRangeFirst(value[1]);
        compositeIndex[1].setRangeLast(value[1]);

        compositeIndex[2] = new QueryServiceStub.CompositeIndex();
        compositeIndex[2].setIndexName(operationIndexDTO.getIndexedColumns()[2]);
        compositeIndex[2].setRangeFirst(value[2]);
        compositeIndex[2].setRangeLast(value[2]);

        compositeIndex[3] = new QueryServiceStub.CompositeIndex();
        compositeIndex[3].setIndexName(operationIndexDTO.getIndexedColumns()[3]);
        compositeIndex[3].setRangeFirst(value[3]);
        String changedOperationName = getNextStringInLexicalOrder(value[3]);
        compositeIndex[3].setRangeLast(changedOperationName);
        try {
            OMElement result = queryServiceStub.queryColumnFamily(operationIndexDTO.getIndexedTable(),
                                                                  ConfigReader.getOperationIndexName(),
                                                                  compositeIndex);

            OMElement rowElement = result.getFirstChildWithName(new QName(ROWS)).getFirstChildWithName(new QName(ROW));
            String responseTimeStr = rowElement.getFirstChildWithName(new QName(RESPONSE_TIME)).getText();
            float responseTime = Float.parseFloat(responseTimeStr);

            String requestCountStr = rowElement.getFirstChildWithName(new QName(REQUEST_COUNT)).getText();
            float requestCount = Float.parseFloat(requestCountStr);

            String responseCountStr = rowElement.getFirstChildWithName(new QName(RESPONSE_COUNT)).getText();
            float responseCount = Float.parseFloat(responseCountStr);

            String faultCountStr = rowElement.getFirstChildWithName(new QName(FAULT_COUNT)).getText();
            float faultCount = Float.parseFloat(faultCountStr);

            resultMap.put(RESPONSE_TIME, responseTime);
            resultMap.put(RESPONSE_COUNT, responseCount);
            resultMap.put(FAULT_COUNT, faultCount);
            resultMap.put(REQUEST_COUNT, requestCount);

        } catch (Exception e) {
            log.error("Error while retrieving operations summary data", e);
        }
    }

    private void getServiceSummaryData(String[] value) {
        QueryServiceStub.CompositeIndex[] compositeIndex = new QueryServiceStub.CompositeIndex[3];

        compositeIndex[0] = new QueryServiceStub.CompositeIndex();
        compositeIndex[0].setIndexName(serviceIndexDTO.getIndexedColumns()[0]);
        compositeIndex[0].setRangeFirst(value[0]);
        compositeIndex[0].setRangeLast(value[0]);

        compositeIndex[1] = new QueryServiceStub.CompositeIndex();
        compositeIndex[1].setIndexName(serviceIndexDTO.getIndexedColumns()[1]);
        compositeIndex[1].setRangeFirst(value[1]);
        compositeIndex[1].setRangeLast(value[1]);

        compositeIndex[2] = new QueryServiceStub.CompositeIndex();
        compositeIndex[2].setIndexName(serviceIndexDTO.getIndexedColumns()[2]);
        compositeIndex[2].setRangeFirst(value[2]);
        String newServiceVal = getNextStringInLexicalOrder(value[2]);
        compositeIndex[2].setRangeLast(newServiceVal);
        try {
            OMElement result = queryServiceStub.queryColumnFamily(serviceIndexDTO.getIndexedTable(),
                                                                  ConfigReader.getServiceIndexName(),
                                                                  compositeIndex);

            OMElement rowElement = result.getFirstChildWithName(new QName(ROWS)).getFirstChildWithName(new QName(ROW));
            String responseTimeStr = rowElement.getFirstChildWithName(new QName(RESPONSE_TIME)).getText();
            float responseTime = Float.parseFloat(responseTimeStr);

            String requestCountStr = rowElement.getFirstChildWithName(new QName(REQUEST_COUNT)).getText();
            float requestCount = Float.parseFloat(requestCountStr);

            String responseCountStr = rowElement.getFirstChildWithName(new QName(RESPONSE_COUNT)).getText();
            float responseCount = Float.parseFloat(responseCountStr);

            String faultCountStr = rowElement.getFirstChildWithName(new QName(FAULT_COUNT)).getText();
            float faultCount = Float.parseFloat(faultCountStr);

            resultMap.put(RESPONSE_TIME, responseTime);
            resultMap.put(RESPONSE_COUNT, responseCount);
            resultMap.put(FAULT_COUNT, faultCount);
            resultMap.put(REQUEST_COUNT, requestCount);

        } catch (Exception e) {
            log.error("Error while retrieving service summary data", e);
        }
    }

    private void getClusterSummaryData(String[] value) {

        QueryServiceStub.CompositeIndex[] compositeIndex = new QueryServiceStub.CompositeIndex[2];
        compositeIndex[0] = new QueryServiceStub.CompositeIndex();
        compositeIndex[0].setIndexName(clusterIndexDTO.getIndexedColumns()[0]);
        compositeIndex[0].setRangeFirst(value[0]);
        compositeIndex[0].setRangeLast(value[0]);

        compositeIndex[1] = new QueryServiceStub.CompositeIndex();
        compositeIndex[1].setIndexName(clusterIndexDTO.getIndexedColumns()[1]);
        compositeIndex[1].setRangeFirst(value[1]);
        String newClusterVal = getNextStringInLexicalOrder(value[1]);
        compositeIndex[1].setRangeLast(newClusterVal);
        try {
            OMElement result = queryServiceStub.queryColumnFamily(clusterIndexDTO.getIndexedTable(),
                                                                  ConfigReader.getClusterIndexName(),
                                                                  compositeIndex);

            OMElement rowElement = result.getFirstChildWithName(new QName(ROWS)).getFirstChildWithName(new QName(ROW));
            String responseTimeStr = rowElement.getFirstChildWithName(new QName(RESPONSE_TIME)).getText();
            float responseTime = Float.parseFloat(responseTimeStr);

            String requestCountStr = rowElement.getFirstChildWithName(new QName(REQUEST_COUNT)).getText();
            float requestCount = Float.parseFloat(requestCountStr);

            String responseCountStr = rowElement.getFirstChildWithName(new QName(RESPONSE_COUNT)).getText();
            float responseCount = Float.parseFloat(responseCountStr);

            String faultCountStr = rowElement.getFirstChildWithName(new QName(FAULT_COUNT)).getText();
            float faultCount = Float.parseFloat(faultCountStr);

            resultMap.put(RESPONSE_TIME, responseTime);
            resultMap.put(RESPONSE_COUNT, responseCount);
            resultMap.put(FAULT_COUNT, faultCount);
            resultMap.put(REQUEST_COUNT, requestCount);

        } catch (Exception e) {
            log.error("Error while retrieving cluster summary data", e);
        }
    }

    public Point[] getRequestCount(String[] value) {
        //update resultMap
        getDataFromCassandra(value);

        Queue<Point> requestCountQueue = getRequestCountQueue(value);

        float requestCount = (Float) resultMap.get(REQUEST_COUNT);
        if (requestCountQueue.size() < 10) {
            requestCountQueue.add(new Point(0, requestCount));
        } else {
            requestCountQueue.poll();
            requestCountQueue.add(new Point(0, requestCount));
        }
        String key = null;
        if (value.length == 4) {
            key = constructKey(value, OPERATION);
            GraphData.requestCountQueue.put(key, requestCountQueue);
        } else if (value.length == 3) {
            key = constructKey(value, SERVICE);
            GraphData.requestCountQueue.put(key, requestCountQueue);
        } else if (value.length == 2) {
            key = constructKey(value, CLUSTER);
            GraphData.requestCountQueue.put(key, requestCountQueue);
        }
        return requestCountQueue.toArray(new Point[requestCountQueue.size()]);
    }

    private Queue<Point> getRequestCountQueue(String[] value) {
        Queue<Point> requestCountQueue = null;
        String key = null;
        if (value.length == 4) {
            key = constructKey(value, OPERATION);
            requestCountQueue = GraphData.requestCountQueue.get(key);
        } else if (value.length == 3) {
            key = constructKey(value, SERVICE);
            requestCountQueue = GraphData.requestCountQueue.get(key);
        } else if (value.length == 2) {
            key = constructKey(value, CLUSTER);
            requestCountQueue = GraphData.requestCountQueue.get(key);
        }
        if (requestCountQueue == null) {
            requestCountQueue = new ConcurrentLinkedQueue<Point>();
        }
        return requestCountQueue;
    }

    public Point[] getResponseCount(String[] value) {

        Queue<Point> responseCountQueue = getResponseCountQueue(value);

        float responseCount = (Float) resultMap.get(RESPONSE_COUNT);
        if (responseCountQueue.size() < 10) {
            responseCountQueue.add(new Point(0, responseCount));
        } else {
            responseCountQueue.poll();
            responseCountQueue.add(new Point(0, responseCount));
        }
        String key = null;
        if (value.length == 4) {
            key = constructKey(value, OPERATION);
            GraphData.responseCountQueue.put(key, responseCountQueue);
        } else if (value.length == 3) {
            key = constructKey(value, SERVICE);
            GraphData.responseCountQueue.put(key, responseCountQueue);
        } else if (value.length == 2) {
            key = constructKey(value, CLUSTER);
            GraphData.responseCountQueue.put(key, responseCountQueue);
        }
        return responseCountQueue.toArray(new Point[responseCountQueue.size()]);
    }

    private Queue<Point> getResponseCountQueue(String[] value) {
        Queue<Point> responseCountQueue = null;
        String key = null;
        if (value.length == 4) {
            key = constructKey(value, OPERATION);
            responseCountQueue = GraphData.responseCountQueue.get(key);
        } else if (value.length == 3) {
            key = constructKey(value, SERVICE);
            responseCountQueue = GraphData.responseCountQueue.get(key);
        } else if (value.length == 2) {
            key = constructKey(value, CLUSTER);
            responseCountQueue = GraphData.responseCountQueue.get(key);
        }
        if (responseCountQueue == null) {
            responseCountQueue = new ConcurrentLinkedQueue<Point>();
        }
        return responseCountQueue;
    }

    public Point[] getFaultCount(String[] value) {

        Queue<Point> faultCountQueue = getFaultCountQueue(value);

        float faultCount = (Float) resultMap.get(FAULT_COUNT);
        if (faultCountQueue.size() < 10) {
            faultCountQueue.add(new Point(0, faultCount));
        } else {
            faultCountQueue.poll();
            faultCountQueue.add(new Point(0, faultCount));
        }
        String key = null;
        if (value.length == 4) {
            key = constructKey(value, OPERATION);
            GraphData.faultCountQueue.put(key, faultCountQueue);
        } else if (value.length == 3) {
            key = constructKey(value, SERVICE);
            GraphData.faultCountQueue.put(key, faultCountQueue);
        } else if (value.length == 2) {
            key = constructKey(value, CLUSTER);
            GraphData.faultCountQueue.put(key, faultCountQueue);
        }
        return faultCountQueue.toArray(new Point[faultCountQueue.size()]);
    }

    private Queue<Point> getFaultCountQueue(String[] value) {
        Queue<Point> faultCountQueue = null;
        String key = null;
        if (value.length == 4) {
            key = constructKey(value, OPERATION);
            faultCountQueue = GraphData.faultCountQueue.get(key);
        } else if (value.length == 3) {
            key = constructKey(value, SERVICE);
            faultCountQueue = GraphData.faultCountQueue.get(key);
        } else if (value.length == 2) {
            key = constructKey(value, CLUSTER);
            faultCountQueue = GraphData.faultCountQueue.get(key);
        }
        if (faultCountQueue == null) {
            faultCountQueue = new ConcurrentLinkedQueue<Point>();
        }
        return faultCountQueue;
    }

    public Point[] getResponseTime(String[] value) {

        Queue<Point> responseTimeQueue = getResponseTimeQueue(value);

        float responseTime = (Float) resultMap.get(RESPONSE_TIME);


        if (responseTimeQueue.size() < 10) {
            responseTimeQueue.add(new Point(0, responseTime));
        } else {
            responseTimeQueue.poll();
            responseTimeQueue.add(new Point(0, responseTime));
        }
        String key = null;
        if (value.length == 4) {
            key = constructKey(value, OPERATION);
            GraphData.responseTimeQueue.put(key, responseTimeQueue);
        } else if (value.length == 3) {
            key = constructKey(value, SERVICE);
            GraphData.responseTimeQueue.put(key, responseTimeQueue);
        } else if (value.length == 2) {
            key = constructKey(value, CLUSTER);
            GraphData.responseTimeQueue.put(key, responseTimeQueue);
        }
        return responseTimeQueue.toArray(new Point[responseTimeQueue.size()]);
    }

    private Queue<Point> getResponseTimeQueue(String[] value) {
        Queue<Point> responseTimeQueue = null;
        String key = null;
        if (value.length == 4) {
            key = constructKey(value, OPERATION);
            responseTimeQueue = GraphData.responseTimeQueue.get(key);
        } else if (value.length == 3) {
            key = constructKey(value, SERVICE);
            responseTimeQueue = GraphData.responseTimeQueue.get(key);
        } else if (value.length == 2) {
            key = constructKey(value, CLUSTER);
            responseTimeQueue = GraphData.responseTimeQueue.get(key);
        }
        if (responseTimeQueue == null) {
            responseTimeQueue = new ConcurrentLinkedQueue<Point>();
        }
        return responseTimeQueue;
    }


    private String constructKey(String[] value, String type) {
        String key = null;
        for (String val : value) {
            key = key + "_" + val;
        }
        return key + "_" + type;
    }

    private String getNextStringInLexicalOrder(String str) {

        if ((str == null) || (str.equals(""))) {
            return str;
        }

        byte[] bytes = str.getBytes();

        byte last = bytes[bytes.length - 1];
        last = (byte) (last + 1);        // Not very accurate. Need to improve this more to handle
        //  overflows.

        bytes[bytes.length - 1] = last;

        return new String(bytes);
    }

    private float round(double valueToRound, int numberOfDecimalPlaces) {
        double multipicationFactor = Math.pow(10, numberOfDecimalPlaces);
        double interestedInZeroDPs = valueToRound * multipicationFactor;
        return new Double(Math.round(interestedInZeroDPs) / multipicationFactor).floatValue();
    }

}

