/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.bam.data.publisher.servicestats;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.data.publisher.servicestats.data.EventData;
import org.wso2.carbon.bam.data.publisher.servicestats.data.OperationStatisticData;
import org.wso2.carbon.bam.data.publisher.servicestats.data.ServiceStatisticData;
import org.wso2.carbon.bam.data.publisher.servicestats.data.StatisticData;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.statistics.services.util.OperationStatistics;
import org.wso2.carbon.statistics.services.util.ServiceStatistics;
import org.wso2.carbon.statistics.services.util.SystemStatistics;
import org.wso2.carbon.utils.NetworkUtils;

import java.net.SocketException;
import java.sql.Timestamp;
import java.util.List;

public class PublisherUtils {
    private static Log log = LogFactory.getLog(PublisherUtils.class);

    private static String serverName;   // Needed for integration test scenarios

    private static ServiceStatisticsQueue queue;

    public static void setServiceStatisticQueue(ServiceStatisticsQueue serviceStatisticQueue) {
        queue = serviceStatisticQueue;
    }

    public static ServiceStatisticsQueue getQueue() {
        return queue;
    }

    /**
     * Gets tenant based server name.
     */
    private static String getServerName(AxisConfiguration axisConfiguration) throws Exception {
        String serverName = null;
        String hostName;
        String baseServerUrl;
        try {
            hostName = NetworkUtils.getLocalHostname();
        } catch (SocketException e) {
            throw new Exception("Error getting host name for the BAM event payload", e);
        }
        String carbonHttpsPort = System.getProperty("carbon." +
                                                    ServiceStatisticsPublisherConstants.TRANSPORT +
                                                    ".port");
        if (carbonHttpsPort == null) {
            carbonHttpsPort = (String) axisConfiguration.getTransportIn(
                    ServiceStatisticsPublisherConstants.TRANSPORT).getParameter("port").getValue();
        }

        baseServerUrl = ServiceStatisticsPublisherConstants.TRANSPORT + "://" + hostName + ":" +
                        carbonHttpsPort;
        String context = ServerConfiguration.getInstance().getFirstProperty("WebContextRoot");
        String tenantDomain = SuperTenantCarbonContext.getCurrentContext(axisConfiguration).
                getTenantDomain();
        if (tenantDomain != null) {
            serverName = baseServerUrl + context + "t/" + tenantDomain;

        } else if (tenantDomain == null && context.equals("/")) {

            serverName = baseServerUrl + "";
        } else if (tenantDomain == null && !context.equals("/")) {
            serverName = baseServerUrl + context;

        }
        return serverName;
    }


    // Used in integration tests
    public static void setServerName(String name) {
        serverName = name;
    }

    /**
     * Returns an EventData bean populated from SystemStatistics.
     *
     *
     * @param statisticData
     * @param statistics
     * @return
     */
    public static EventData getSystemEventData(StatisticData statisticData, SystemStatistics statistics) {

        AxisConfiguration axisConfig = statisticData.getMsgCtxOfStatData().getConfigurationContext().getAxisConfiguration();
        int tenantID = SuperTenantCarbonContext.getCurrentContext(axisConfig).getTenantId();

        EventData data = new EventData();
        data.setTenantID(tenantID);
        data.setAvgResponseTime(statistics.getAvgResponseTime());
        data.setMaxResponseTime(statistics.getMaxResponseTime());
        data.setMinResponseTime(statistics.getMinResponseTime());
        data.setRequestCount(statistics.getTotalRequestCount());
        data.setResponseCount(statistics.getTotalResponseCount());
        data.setFaultCount(statistics.getTotalFaultCount());
        data.setTimestamp(statisticData.getTimestamp());

        return data;
    }

    /**
     * Returns an EventData bean populated from ServiceStatistics.
     *
     *
     * @param stats
     * @param timestamp
     * @return
     */
    public static EventData getServiceEventData(ServiceStatisticData stats, Timestamp timestamp) {

        EventData data = new EventData();
        data.setTenantID(stats.getTenantId());
        ServiceStatistics statistics = stats.getServiceStatistics();
        data.setAvgResponseTime(statistics.getAvgResponseTime());
        data.setMaxResponseTime(statistics.getMaxResponseTime());
        data.setMinResponseTime(statistics.getMinResponseTime());
        data.setRequestCount(statistics.getTotalRequestCount());
        data.setResponseCount(statistics.getTotalResponseCount());
        data.setFaultCount(statistics.getTotalFaultCount());
        data.setServiceName(stats.getServiceName());
        data.setTimestamp(timestamp);
        stats.setUpdateFlag(false);

        return data;
    }

    /**
     * Returns an EventData bean populated from OperationStatistics.
     *
     *
     * @param stats
     * @param timestamp
     * @return
     */
    public static EventData getOperationEventData(OperationStatisticData stats, Timestamp timestamp) {

        EventData data = new EventData();
        data.setTenantID(stats.getTenantId());
        OperationStatistics statistics = stats.getOperationStatistics();
        data.setAvgResponseTime(statistics.getAvgResponseTime());
        data.setMaxResponseTime(statistics.getMaxResponseTime());
        data.setMinResponseTime(statistics.getMinResponseTime());
        data.setRequestCount(statistics.getTotalRequestCount());
        data.setResponseCount(statistics.getTotalResponseCount());
        data.setFaultCount(statistics.getTotalFaultCount());
        data.setServiceName(stats.getServiceName());
        data.setOperationName(stats.getOperationName());
        data.setTimestamp(timestamp);
        stats.setUpdateFlag(false);

        return data;
    }

    /**
     * Message format for the event:
     * <p/>
     * (01) <statdata:Event xmlns:statdata="http://wso2.org/ns/2009/09/bam/service/statistics/data">
     * (02) <statdata:ServiceStatisticsData>
     * (03) <statdata:TenantID>23<statdata:TenantID>
     * (04) <statdata:ServerName>http://127.0.0.1:9763</statdata:ServerName>
     * (05) <statdata:AverageResponseTime>16.4</statdata:AverageResponseTime>
     * (06) <statdata:MinimumResponseTime>0</statdata:MinimumResponseTime>
     * (07) <statdata:MaximumResponseTime>109</statdata:MaximumResponseTime>
     * (08) <statdata:RequestCount>21</statdata:RequestCount>
     * (09) <statdata:ResponseCount>20</statdata:ResponseCount>
     * (10) <statdata:FaultCount>0</statdata:FaultCount>
     * (11) <statdata:ServiceName>HelloService</statdata:ServiceName>
     * (12) <statdata:OperationName>greet</statdata:OperationName>
     * (13) </statdata:ServiceStatisticsData>
     * (14) </statdata:Event>
     * <p/>
     * <p/>
     * Note that ServiceName element MUST be present if the message represents service specific
     * data. If the message is about operation specific data, then both ServiceName element and
     * OperationName element MUST be present. If it is system level data, neither the ServiceName nor
     * the OperationName are expected in the message.
     * <p/>
     * <p/>
     * Schema for message format:
     * <p/>
     * <?xml version="1.0" encoding="utf-8" ?> <xsd:schema
     * xmlns:xsd="http://www.w3.org/2001/XMLSchema" targetNamespace=
     * "http://wso2.org/ns/2009/09/bam/service/statistics/data"
     * tns="http://wso2.org/ns/2009/09/bam/service/statistics/data">
     * <p/>
     * <xsd:element name="Event">
     * <xsd:complexType>
     * <xsd:sequence>
     * <xsd:element name="ServiceStatisticsData">
     * <xsd:complexType>
     * <xsd:sequence>
     * <xsd:element name="TenantID" type="xsd:int"/>
     * <xsd:element name="ServerName" type="xsd:string"/>
     * <xsd:element name="AverageResponseTime" type="xsd:decimal"/>
     * <xsd:element name="MinimumResponseTime" type="xsd:long"/>
     * <xsd:element name="MaximumResponseTime" type="xsd:long"/>
     * <xsd:element name="RequestCount" type="xsd:integer"/>
     * <xsd:element name="ResponseCount" type="xsd:integer"/>
     * <xsd:element name="FaultCount" type="xsd:integer"/>
     * <xsd:element name="ServiceName" type="xsd:string" minOccurs="0"/>
     * <xsd:element name="OperationName" type="xsd:string" minOccurs="0"/>
     * </xsd:sequence>
     * </xsd:complexType>
     * </xsd:element>
     * </xsd:sequence>
     * </xsd:complexType>
     * </xsd:element>
     * </xsd:schema>
     */

/*    public static OMElement getEventPayload(MessageContext msgCtx, EventData data)
            throws Exception {

//        OMElement eventElement = getCommonPayload(msgCtx.getConfigurationContext().getAxisConfiguration(),
//                                                  data);

        // If this is a service or operation data related event. For operation data service name
        // also should be present.
        if (data.getServiceName() != null) {
            OMElement serviceInvocationDataElement = eventElement.getFirstElement();
            OMFactory factory = OMAbstractFactory.getOMFactory();
            OMNamespace statNamespace = factory.createOMNamespace(
                    ServiceStatisticsPublisherConstants.STATISTICS_DATA_NS_URI,
                    ServiceStatisticsPublisherConstants.STATISTICS_DATA_NS_PREFIX);
            OMElement serviceNameElement = factory.createOMElement(
                    ServiceStatisticsPublisherConstants.STATISTICS_DATA_ELEMENT_NAME_SERVICE_NAME,
                    statNamespace);
            factory.createOMText(serviceNameElement, data.getServiceName());
            serviceInvocationDataElement.addChild(serviceNameElement);

        }

        // If this is a operation data related event
        if (data.getOperationName() != null) {
            OMElement serviceInvocationDataElement = eventElement.getFirstElement();
            OMFactory factory = OMAbstractFactory.getOMFactory();
            OMNamespace statNamespace = factory.createOMNamespace(
                    ServiceStatisticsPublisherConstants.STATISTICS_DATA_NS_URI,
                    ServiceStatisticsPublisherConstants.STATISTICS_DATA_NS_PREFIX);
            OMElement operationNameElement = factory.createOMElement(
                    ServiceStatisticsPublisherConstants.STATISTICS_DATA_ELEMENT_NAME_OPERATION_NAME,
                    statNamespace);
            factory.createOMText(operationNameElement, data.getOperationName());
            serviceInvocationDataElement.addChild(operationNameElement);
        }

        if (log.isDebugEnabled()) {
            log.debug("Event payload " + eventElement.getText());
        }

        return eventElement;

    }*/
    public static OMElement getEventPayload(MessageContext msgCtx, EventData systemData,
                                            List<EventData> serviceDataList,
                                            List<EventData> operationDataList) {

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace statNamespace = factory.createOMNamespace(
                ServiceStatisticsPublisherConstants.STATISTICS_DATA_NS_URI,
                ServiceStatisticsPublisherConstants.STATISTICS_DATA_NS_PREFIX);
        OMElement eventElement = factory.createOMElement(
                ServiceStatisticsPublisherConstants.STATISTICS_DATA_ELEMENT_NAME_EVENT,
                statNamespace);
        OMElement systemStatisticDataElement = factory.createOMElement(
                ServiceStatisticsPublisherConstants.STATISTICS_DATA_ELEMENT_NAME_SERVICE_STATISTICS_DATA,
                statNamespace);
        try {
            getCommonPayload(msgCtx, factory, statNamespace, systemData, systemStatisticDataElement);
        } catch (Exception e) {
            log.error("Error occurred while creating system payload", e);
        }
        eventElement.addChild(systemStatisticDataElement);


        for (EventData serviceData : serviceDataList) {
            OMElement serviceStatisticDataElement = factory.createOMElement(
                    ServiceStatisticsPublisherConstants.STATISTICS_DATA_ELEMENT_NAME_SERVICE_STATISTICS_DATA,
                    statNamespace);
            try {
                getCommonPayload(msgCtx, factory, statNamespace, serviceData, serviceStatisticDataElement);
            } catch (Exception e) {
                log.error("Error occurred while creating service payload", e);
            }

            OMElement serviceNameElement = factory.createOMElement(
                    ServiceStatisticsPublisherConstants.STATISTICS_DATA_ELEMENT_NAME_SERVICE_NAME,
                    statNamespace);
            factory.createOMText(serviceNameElement, serviceData.getServiceName());
            serviceStatisticDataElement.addChild(serviceNameElement);
            eventElement.addChild(serviceStatisticDataElement);
        }


        for (EventData operationData : operationDataList) {
            OMElement operationStatisticDataElement = factory.createOMElement(
                    ServiceStatisticsPublisherConstants.STATISTICS_DATA_ELEMENT_NAME_SERVICE_STATISTICS_DATA,
                    statNamespace);
            try {
                getCommonPayload(msgCtx, factory, statNamespace, operationData, operationStatisticDataElement);
            } catch (Exception e) {
                log.error("Error occurred while creating operation payload", e);
            }
            OMElement operationNameElement = factory.createOMElement(
                    ServiceStatisticsPublisherConstants.STATISTICS_DATA_ELEMENT_NAME_OPERATION_NAME,
                    statNamespace);
            factory.createOMText(operationNameElement, operationData.getOperationName());
            OMElement operationServiceNameElement = factory.createOMElement(
                    ServiceStatisticsPublisherConstants.STATISTICS_DATA_ELEMENT_NAME_SERVICE_NAME,
                    statNamespace);
            factory.createOMText(operationServiceNameElement, operationData.getServiceName());
            operationStatisticDataElement.addChild(operationNameElement);
            operationStatisticDataElement.addChild(operationServiceNameElement);
            eventElement.addChild(operationStatisticDataElement);
        }

        return eventElement;
    }


    public static OMElement createServerRestartedEvent() {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace statNamespace = factory.createOMNamespace(
                ServiceStatisticsPublisherConstants.STATISTICS_DATA_NS_URI,
                ServiceStatisticsPublisherConstants.STATISTICS_DATA_NS_PREFIX);
        OMElement eventElement = factory.createOMElement(
                ServiceStatisticsPublisherConstants.STATISTICS_DATA_ELEMENT_NAME_EVENT,
                statNamespace);
        OMElement systemStatisticDataElement = factory.createOMElement(
                ServiceStatisticsPublisherConstants.STATISTICS_DATA_ELEMENT_NAME_SERVICE_STATISTICS_DATA,
                statNamespace);
        return null;
    }

    /**
     * publish event
     * @param statisticData
     * @throws Exception
     */
    public static void publishEvent(StatisticData statisticData) throws Exception {
        queue.enqueue(statisticData);
    }

    /**
     * Returns the common payload for System, Service and Operation statistics events.
     */
    private static void getCommonPayload(MessageContext msgCtx, OMFactory factory,
                                         OMNamespace statNamespace,
                                         EventData data, OMElement serviceInvocationDataElement)
            throws Exception {

        OMElement tenantElement = factory.createOMElement(
                ServiceStatisticsPublisherConstants.STATISTICS_DATA_ELEMENT_NAME_TENANT_ID,
                statNamespace);

        factory.createOMText(tenantElement, Integer.toString(data.getTenantID()));
        serviceInvocationDataElement.addChild(tenantElement);

        OMElement serverNameElement = factory.createOMElement(
                ServiceStatisticsPublisherConstants.STATISTICS_DATA_ELEMENT_NAME_SERVER_NAME,
                statNamespace);

        if (msgCtx == null) {    // For integration test scenarios
            factory.createOMText(serverNameElement, serverName);
        } else {
            factory.createOMText(serverNameElement, getServerName(msgCtx.getConfigurationContext().
                    getAxisConfiguration()));
        }
        serviceInvocationDataElement.addChild(serverNameElement);

        OMElement minResponseTimeElement = factory.createOMElement(
                ServiceStatisticsPublisherConstants.STATISTICS_DATA_ELEMENT_NAME_MIN_RESPONSE_TIME,
                statNamespace);
        factory.createOMText(minResponseTimeElement, Long.toString(data.getMinResponseTime()));
        serviceInvocationDataElement.addChild(minResponseTimeElement);

        OMElement maxResponseTimeElement = factory.createOMElement(
                ServiceStatisticsPublisherConstants.STATISTICS_DATA_ELEMENT_NAME_MAX_RESPONSE_TIME,
                statNamespace);
        factory.createOMText(maxResponseTimeElement, Long.toString(data.getMaxResponseTime()));
        serviceInvocationDataElement.addChild(maxResponseTimeElement);

        OMElement avgResponseTimeElement = factory.createOMElement(
                ServiceStatisticsPublisherConstants.STATISTICS_DATA_ELEMENT_NAME_AVG_RESPONSE_TIME,
                statNamespace);
        factory.createOMText(avgResponseTimeElement, Double.toString(data.getAvgResponseTime()));
        serviceInvocationDataElement.addChild(avgResponseTimeElement);

        OMElement requestCountElement = factory.createOMElement(
                ServiceStatisticsPublisherConstants.STATISTICS_DATA_ELEMENT_NAME_REQUEST_COUNT,
                statNamespace);
        factory.createOMText(requestCountElement, Integer.toString(data.getRequestCount()));
        serviceInvocationDataElement.addChild(requestCountElement);

        OMElement responseCountElement = factory.createOMElement(
                ServiceStatisticsPublisherConstants.STATISTICS_DATA_ELEMENT_NAME_RESPONSE_COUNT,
                statNamespace);
        factory.createOMText(responseCountElement, Integer.toString(data.getResponseCount()));
        serviceInvocationDataElement.addChild(responseCountElement);

        OMElement faultCountElement = factory.createOMElement(
                ServiceStatisticsPublisherConstants.STATISTICS_DATA_ELEMENT_NAME_FAULT_COUNT,
                statNamespace);
        factory.createOMText(faultCountElement, Integer.toString(data.getFaultCount()));
        serviceInvocationDataElement.addChild(faultCountElement);

        OMElement timestampElement = factory.createOMElement(
                ServiceStatisticsPublisherConstants.STATISTICS_DATA_ELEMENT_TIMESTAMP,
                statNamespace);
        factory.createOMText(timestampElement, data.getTimestamp().toString());
        serviceInvocationDataElement.addChild(timestampElement);

    }

}
