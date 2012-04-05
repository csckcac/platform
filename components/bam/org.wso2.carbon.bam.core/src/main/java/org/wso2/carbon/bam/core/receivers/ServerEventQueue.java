/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bam.core.receivers;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.bam.common.dataobjects.common.EventingServerDO;
import org.wso2.carbon.bam.common.dataobjects.service.OperationStatisticsDO;
import org.wso2.carbon.bam.common.dataobjects.service.ServerDO;
import org.wso2.carbon.bam.common.dataobjects.service.ServerStatisticsDO;
import org.wso2.carbon.bam.common.dataobjects.service.ServiceDO;
import org.wso2.carbon.bam.common.dataobjects.service.ServiceStatisticsDO;
import org.wso2.carbon.bam.core.admin.BAMDataServiceAdmin;
import org.wso2.carbon.bam.core.cache.CacheConstant;
import org.wso2.carbon.bam.core.cache.CacheData;
import org.wso2.carbon.bam.core.cache.CacheImpl;
import org.wso2.carbon.bam.core.persistence.BAMPersistenceManager;
import org.wso2.carbon.bam.core.util.BAMUtil;
import org.wso2.carbon.bam.util.BAMConstants;
import org.wso2.carbon.bam.util.BAMException;

import javax.xml.namespace.QName;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerEventQueue extends AbstractQueue {

    private static final Log log = LogFactory.getLog(ServerEventQueue.class);

    private CacheImpl cacheImpl;

    public ServerEventQueue(int threadPoolSize) {
        super(threadPoolSize);
    }

    protected void processEvents(MessageContext[] messageContexts)  {

        //Get cacheImpl object for later use
        cacheImpl = new CacheImpl();

        for (int i = 0; i < messageContexts.length; i++) {
            BAMDataServiceAdmin dsAdmin = new BAMDataServiceAdmin();
            List<EventData> eventData;
            try {
                eventData = getEventDataArray(messageContexts[i].getEnvelope().getBody());
            } catch (BAMException e) {
                log.error("BAM Statistics MessageReceiver invokeBusinessLogic " +
                          e.getLocalizedMessage() + "\n" + "BAM MR invokeBusinessLogic SOAP Envelope causing the problem" +
                          messageContexts[i].getEnvelope().toString(), e);
                return;
            }
            for (EventData data : eventData) {
                storeData(data, dsAdmin);
            }

            if (log.isDebugEnabled()) {
                log.debug("BAM MR invokeBusinessLogic SOAP Envelope " + messageContexts[i].getEnvelope().toString());
            }
        }
////        for (int i = 0; i < messageContexts.length; i++) {
//        BAMDataServiceAdmin dsAdmin = new BAMDataServiceAdmin();
//        EventData[] eventData;
//        try {
//            eventData = getEventDataArray(messageContexts.getEnvelope().getBody());
//        } catch (BAMException e) {
//            log.error("BAM Statistics MessageReceiver invokeBusinessLogic " +
//                      e.getLocalizedMessage() + "\n" + "BAM MR invokeBusinessLogic SOAP Envelope causing the problem" +
//                      messageContexts.getEnvelope().toString(), e);
//            return;
//        }
//        for (EventData data : eventData) {
//            storeData(data, dsAdmin);
//        }
//
//        if (log.isDebugEnabled()) {
//            log.debug("BAM MR invokeBusinessLogic SOAP Envelope " + messageContexts.getEnvelope().toString());
//        }
////        }

    }

    public static final String STATISTICS_DATA_NS_URI = "http://wso2.org/ns/2009/09/bam/service/statistics/data";

    // OM element names
    public static final String STATISTICS_DATA_ELEMENT_NAME_EVENT = "Event";
    public static final String STATISTICS_DATA_ELEMENT_NAME_SERVICE_STATISTICS_DATA = "ServiceStatisticsData";
    public static final String STATISTICS_DATA_ELEMENT_NAME_TENANT_ID = "TenantID";
    public static final String STATISTICS_DATA_ELEMENT_NAME_SERVER_NAME = "ServerName";
    public static final String STATISTICS_DATA_ELEMENT_NAME_AVG_RESPONSE_TIME = "AverageResponseTime";
    public static final String STATISTICS_DATA_ELEMENT_NAME_MIN_RESPONSE_TIME = "MinimumResponseTime";
    public static final String STATISTICS_DATA_ELEMENT_NAME_MAX_RESPONSE_TIME = "MaximumResponseTime";
    public static final String STATISTICS_DATA_ELEMENT_NAME_REQUEST_COUNT = "RequestCount";
    public static final String STATISTICS_DATA_ELEMENT_NAME_RESPONSE_COUNT = "ResponseCount";
    public static final String STATISTICS_DATA_ELEMENT_NAME_FAULT_COUNT = "FaultCount";
    public static final String STATISTICS_DATA_ELEMENT_NAME_SERVICE_NAME = "ServiceName";
    public static final String STATISTICS_DATA_ELEMENT_NAME_OPERATION_NAME = "OperationName";
    public static final String STATISTICS_DATA_ELEMENT_NAME_TIMESTAMP = "Timestamp";




    private List<EventData> getEventDataArray(OMElement rootElement) throws BAMException {

        Iterator serviceStatisticsDataIterator;
        OMElement eventElement = rootElement.getFirstChildWithName(
                new QName(STATISTICS_DATA_NS_URI, STATISTICS_DATA_ELEMENT_NAME_EVENT));

        if (eventElement == null) {
            throw new BAMException("Event element not found in the message");
        }

        QName serviceStatisticsDataQName =
                new QName(STATISTICS_DATA_NS_URI, STATISTICS_DATA_ELEMENT_NAME_SERVICE_STATISTICS_DATA);
        serviceStatisticsDataIterator = eventElement.getChildrenWithName(serviceStatisticsDataQName);

        List<EventData> eventDataList = new ArrayList<EventData>();

        while (serviceStatisticsDataIterator.hasNext()) {
            EventData eventData = new EventData();
            OMElement serviceStatisticsDataElement;

            serviceStatisticsDataElement = (OMElement) serviceStatisticsDataIterator.next();

            if (serviceStatisticsDataElement == null) {
                throw new BAMException("ServiceStatisticsData element not found in the message");
            }

            int tenantID = getIntDataValueWithQName(
                    new QName(STATISTICS_DATA_NS_URI, STATISTICS_DATA_ELEMENT_NAME_TENANT_ID), serviceStatisticsDataElement);
            if (tenantID == -1) {
                tenantID = CarbonConstants.SUPER_TENANT_ID;
            }

            String serverName = getStringDataValueWithQName(
                    new QName(STATISTICS_DATA_NS_URI, STATISTICS_DATA_ELEMENT_NAME_SERVER_NAME), serviceStatisticsDataElement);
            if (serverName == null) {
                throw new BAMException("ServerName element not found in the message");
            }

            double avgResTime = getDoubleDataValueWithQName(
                    new QName(STATISTICS_DATA_NS_URI, STATISTICS_DATA_ELEMENT_NAME_AVG_RESPONSE_TIME), serviceStatisticsDataElement);
            long minResTime = getLongDataValueWithQName(
                    new QName(STATISTICS_DATA_NS_URI, STATISTICS_DATA_ELEMENT_NAME_MIN_RESPONSE_TIME), serviceStatisticsDataElement);
            long maxResTime = getLongDataValueWithQName(
                    new QName(STATISTICS_DATA_NS_URI, STATISTICS_DATA_ELEMENT_NAME_MAX_RESPONSE_TIME), serviceStatisticsDataElement);
            int requestCount = getIntDataValueWithQName(
                    new QName(STATISTICS_DATA_NS_URI, STATISTICS_DATA_ELEMENT_NAME_REQUEST_COUNT), serviceStatisticsDataElement);
            int responseCount = getIntDataValueWithQName(
                    new QName(STATISTICS_DATA_NS_URI, STATISTICS_DATA_ELEMENT_NAME_RESPONSE_COUNT), serviceStatisticsDataElement);
            int faultCount = getIntDataValueWithQName(
                    new QName(STATISTICS_DATA_NS_URI, STATISTICS_DATA_ELEMENT_NAME_FAULT_COUNT), serviceStatisticsDataElement);
            String serviceName = getStringDataValueWithQName(
                    new QName(STATISTICS_DATA_NS_URI, STATISTICS_DATA_ELEMENT_NAME_SERVICE_NAME), serviceStatisticsDataElement);
            String operationName = getStringDataValueWithQName(
                    new QName(STATISTICS_DATA_NS_URI, STATISTICS_DATA_ELEMENT_NAME_OPERATION_NAME), serviceStatisticsDataElement);
            Timestamp timestamp = getTimestampValueWithQname(
                    new QName(STATISTICS_DATA_NS_URI, STATISTICS_DATA_ELEMENT_NAME_TIMESTAMP), serviceStatisticsDataElement);

            eventData.setTenantID(tenantID);
            eventData.setServerName(serverName);
            eventData.setAvgResTime(avgResTime);
            eventData.setMinResTime(minResTime);
            eventData.setMaxResTime(maxResTime);
            eventData.setRequestCount(requestCount);
            eventData.setResponseCount(responseCount);
            eventData.setFaultCount(faultCount);
            eventData.setServiceName(serviceName);
            eventData.setOperationName(operationName);
            eventData.setTimestamp(timestamp);

            eventDataList.add(eventData);

        }
        return eventDataList;
    }

    private void storeData(EventData data, BAMDataServiceAdmin dsAdmin) {

        if (data.operationName != null) {
            storeOperationData(data, dsAdmin);
        } else if (data.serviceName != null) {
            storeServiceData(data, dsAdmin);
        } else {
            storeServerData(data, dsAdmin);
        }
    }

    private void storeOperationData(EventData data, BAMDataServiceAdmin dsAdmin) {

        if (data.operationName != null && !"".equals(data.operationName)) {

            Calendar calendar = Calendar.getInstance();
            if(data.getTimestamp() != null) calendar.setTimeInMillis(data.getTimestamp().getTime());

            OperationStatisticsDO operationStatisticsDO = new OperationStatisticsDO(
                    data.serverName, calendar, data.avgResTime, data.maxResTime,
                    data.minResTime, data.requestCount, data.responseCount, data.faultCount,
                    data.serviceName, data.operationName);
            try {
                ServerDO monitoringServer = cacheImpl.getServer(dsAdmin, data.serverName, data.tenantID, BAMConstants.SERVER_TYPE_EVENTING,
                                    BAMConstants.SERVICE_STAT_TYPE);

                //TODO:check whether this is needed
                if (monitoringServer == null) {
                    synchronized (this) {
                        if (monitoringServer == null) {
                            if (data.serverName != null) {
                                ServerDO server = new EventingServerDO();
                                server.setTenantID(data.tenantID);
                                server.setServerURL(data.serverName);
                                server.setServerType(BAMConstants.SERVER_TYPE_EVENTING);
                                server.setCategory(BAMConstants.SERVICE_STAT_TYPE);
                                BAMPersistenceManager.getPersistenceManager(BAMUtil.getRegistry())
                                        .addMonitoredServer(server);
                            }
                        }
                        monitoringServer = cacheImpl.getServer(dsAdmin, data.serverName, data.tenantID,
                                                               BAMConstants.SERVER_TYPE_EVENTING,
                                                               BAMConstants.SERVICE_STAT_TYPE);
                    }
                }
                operationStatisticsDO.setServerID(monitoringServer.getId());
                ServiceDO service = cacheImpl.getService(dsAdmin, data.serverName, data.tenantID,
                                                         operationStatisticsDO.getServerID(),
                                                         data.serviceName);
                if (service != null) {

                    /**
                     * NOTE: If service is null, it is the first time that the data for that service is
                     * being added. In that case, we will not have an ID for the service yet, and the
                     * BAM persistance manager will make sure to add the service to the DB.
                     * We come into this block, only if the service is already in the DB
                     */
                    operationStatisticsDO.setServiceID(service.getId());
                }
                org.wso2.carbon.bam.common.dataobjects.service.OperationDO operation =
                        cacheImpl.getOperation(dsAdmin, data.serverName, data.tenantID,
                                               operationStatisticsDO.getServiceID(),
                                                   data.operationName);
                if (operation != null) {

                    /**
                     * NOTE: If operation is null, it is the first time that the data for that operation is
                     * being added. In that case, we will not have an ID for the operation yet, and the
                     * BAM persistance manager will make sure to add the operation to the DB.
                     * We come into this block, only if the operation is already in the DB
                     */
                    operationStatisticsDO.setOperationID(operation.getOperationID());
                }
                // now add the operation stats
                dsAdmin.addOperationStatistics(operationStatisticsDO);
            } catch (Exception e) {
                log.error("Error updating operation statistics data for server " + data.serverName +
                          " service " + data.serviceName + " operation " + data.operationName +
                          " from eventing message receiver \n" + e.getLocalizedMessage(), e);
            }
        }
    }

    private void storeServiceData(EventData data, BAMDataServiceAdmin dsAdmin) {

        Calendar calendar=Calendar.getInstance();
        if(data.getTimestamp() != null) calendar.setTimeInMillis(data.getTimestamp().getTime());
        ServiceStatisticsDO serviceStatisticsDO =
                new ServiceStatisticsDO(data.serverName, calendar, data.avgResTime,
                                        data.maxResTime, data.minResTime, data.requestCount,
                                        data.responseCount, data.faultCount, data.serviceName);

        try {

            /**
             * set both the server ID and service ID for stats object, as in eventing case, we only
             * know the server name and service name check whether server is already in DB else
             * add it(dsAdmin side BAM)
             */
            ServerDO monitoringServer = cacheImpl.getServer(dsAdmin, data.serverName, data.tenantID,
                                                            BAMConstants.SERVER_TYPE_EVENTING,
                                                            BAMConstants.SERVICE_STAT_TYPE);
            if (monitoringServer == null) {
                synchronized (this) {
                    if (monitoringServer == null) {
                        if (data.serverName != null) {
                            ServerDO server = new ServerDO();
                            server.setTenantID(data.tenantID);
                            server.setServerURL(data.serverName);
                            server.setServerType(BAMConstants.SERVER_TYPE_EVENTING);
                            server.setCategory(BAMConstants.SERVICE_STAT_TYPE);
                            BAMPersistenceManager.getPersistenceManager(BAMUtil.getRegistry())
                                    .addMonitoredServer(server);
                        }
                    }
                    monitoringServer =cacheImpl.getServer(dsAdmin, data.serverName, data.tenantID,
                                                            BAMConstants.SERVER_TYPE_EVENTING,
                                                            BAMConstants.SERVICE_STAT_TYPE);
                }
            }
            serviceStatisticsDO.setServerID(monitoringServer.getId());
            ServiceDO service = cacheImpl.getService(dsAdmin, data.serverName, data.tenantID,
                                                     serviceStatisticsDO.getServerID(),
                                                     data.serviceName);
            if (service != null) {

                /**
                 * NOTE: If service is null, it is the first time that the data for that service is
                 * being added. In that case, we will not have an ID for the service yet, and the
                 * BAM persistence manager will make sure to add the service to the DB.
                 * We come into this block, only if the service is already in the DB
                 */
                serviceStatisticsDO.setServiceID(service.getId());
            }
            // now add the service stats
            dsAdmin.addServiceStatistics(serviceStatisticsDO);
        } catch (Exception e) {
            log.error("Error updating service statistics data for server " + data.serverName + " service "
                      + data.serviceName + " from eventing message receiver " + e.getLocalizedMessage(), e);
        }
    }

    private void storeServerData(EventData data, BAMDataServiceAdmin dsAdmin) {

        Calendar calendar = Calendar.getInstance();
        if(data.getTimestamp() != null) calendar.setTimeInMillis(data.getTimestamp().getTime());

        ServerStatisticsDO serverStatisticsDO =
                new ServerStatisticsDO(data.serverName, calendar, data.avgResTime,
                                       data.maxResTime, data.minResTime, data.requestCount,
                                       data.responseCount, data.faultCount);

        try {

            /**
             * set the ID for stats object, as in eventing case, we oly know the server name, hence
             * we need to pick up the corresponding ID from data service
             */
            ServerDO monitoringServer = cacheImpl.getServer(dsAdmin, data.serverName, data.tenantID,
                                                            BAMConstants.SERVER_TYPE_EVENTING,
                                                            BAMConstants.SERVICE_STAT_TYPE);
            // server is not present, let's add a generic server
            if (monitoringServer == null) {
                synchronized (this) {
                    if (monitoringServer == null) {
                        if (data.serverName != null) {
                            ServerDO server = new ServerDO();
                            server.setTenantID(data.tenantID);
                            server.setServerURL(data.serverName);
                            server.setServerType(BAMConstants.SERVER_TYPE_EVENTING);
                            server.setCategory(BAMConstants.SERVICE_STAT_TYPE);
                            BAMPersistenceManager.getPersistenceManager(BAMUtil.getRegistry())
                                    .addMonitoredServer(server);
                        }
                    }
                    monitoringServer = dsAdmin.getServer(data.serverName, data.tenantID,
                                                         BAMConstants.SERVER_TYPE_EVENTING, BAMConstants.SERVICE_STAT_TYPE);
                }
            }
            int id = monitoringServer.getId();
            serverStatisticsDO.setServerID(id);
            // now, add the stats with that ID
            dsAdmin.addServerData(serverStatisticsDO);
        } catch (Exception e) {
            log.error("Error updating system level service statistics data for server " +
                      data.serverName + " from eventing message receiver " + e.getMessage(), e);
        }
    }


    /**
     * Encapsulates the data extraction logic from a received event
     */

    class EventData {

        // Event attributes
        String serverName;
        int tenantID;
        long minResTime;
        double avgResTime;
        long maxResTime;
        int requestCount;
        int responseCount;
        int faultCount;
        String serviceName;
        String operationName;
        Timestamp timestamp;

        public Timestamp getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Timestamp timestamp) {
            this.timestamp = timestamp;
        }

        public String getOperationName() {
            return operationName;
        }

        public void setOperationName(String operationName) {
            this.operationName = operationName;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public int getFaultCount() {
            return faultCount;
        }

        public void setFaultCount(int faultCount) {
            this.faultCount = faultCount;
        }

        public int getResponseCount() {
            return responseCount;
        }

        public void setResponseCount(int responseCount) {
            this.responseCount = responseCount;
        }

        public int getRequestCount() {
            return requestCount;
        }

        public void setRequestCount(int requestCount) {
            this.requestCount = requestCount;
        }

        public long getMaxResTime() {
            return maxResTime;
        }

        public void setMaxResTime(long maxResTime) {
            this.maxResTime = maxResTime;
        }

        public double getAvgResTime() {
            return avgResTime;
        }

        public void setAvgResTime(double avgResTime) {
            this.avgResTime = avgResTime;
        }

        public long getMinResTime() {
            return minResTime;
        }

        public void setMinResTime(long minResTime) {
            this.minResTime = minResTime;
        }

        public int getTenantID() {
            return tenantID;
        }

        public void setTenantID(int tenantID) {
            this.tenantID = tenantID;
        }

        public String getServerName() {
            return serverName;
        }

        public void setServerName(String serverName) {
            this.serverName = serverName;
        }
    }

    private int getIntDataValueWithQName(QName qName, OMElement serviceStatisticsDataElement) {
        OMElement element = serviceStatisticsDataElement.getFirstChildWithName(qName);
        int value = -1;

        if (element != null) {
            try {
                value = Integer.parseInt(element.getText().trim());
            } catch (Exception ignore) {
                log.info("Data not in required format (Integer) for data field: " +
                         qName.getLocalPart(), ignore);
            }
        }

        return value;
    }

    private long getLongDataValueWithQName(QName qName, OMElement serviceStatisticsDataElement) {
        OMElement element = serviceStatisticsDataElement.getFirstChildWithName(qName);
        long value = -1;

        if (element != null) {
            try {
                value = Long.parseLong(element.getText().trim());
            } catch (Exception ignore) {
                log.info("Data not in required format (Long) for data field: " +
                         qName.getLocalPart(), ignore);
            }
        }

        return value;
    }

    private Double getDoubleDataValueWithQName(QName qName, OMElement serviceStatisticsDataElement) {
        OMElement element = serviceStatisticsDataElement.getFirstChildWithName(qName);
        double value = -1;

        if (element != null) {
            try {
                value = Double.parseDouble(element.getText().trim());
            } catch (Exception ignore) {
                log.info("Data not in required format (Double) for data field: " +
                         qName.getLocalPart(), ignore);
            }
        }

        return value;
    }

    private String getStringDataValueWithQName(QName qName, OMElement serviceStatisticsDataElement) {
        OMElement element = serviceStatisticsDataElement.getFirstChildWithName(qName);
        String value = null;

        if (element != null) {
            value = element.getText().trim();
        }

        return value;
    }


    private Timestamp getTimestampValueWithQname(QName qName,
                                                 OMElement serviceStatisticsDataElement) {
        OMElement element = serviceStatisticsDataElement.getFirstChildWithName(qName);
        Timestamp timestamp = null;
        if (element != null) {
            timestamp = Timestamp.valueOf(element.getText());
        }
        return timestamp;
    }
}
