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
package org.wso2.carbon.bam.data.publisher.mediationstats;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.aspects.ComponentType;
import org.apache.synapse.aspects.statistics.ErrorLog;
import org.apache.synapse.aspects.statistics.view.InOutStatisticsView;
import org.apache.synapse.aspects.statistics.view.Statistics;
import org.wso2.carbon.bam.data.publisher.mediationstats.services.BAMMediationStatsPublisherAdmin;
import org.wso2.carbon.bam.lwevent.core.LightWeightEventBrokerInterface;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.bam.lwevent.core.LightWeightEventBroker;
import org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService;
import org.wso2.carbon.mediation.statistics.MediationStatisticsSnapshot;
import org.wso2.carbon.mediation.statistics.StatisticsRecord;
import org.wso2.carbon.statistics.services.SystemStatisticsUtil;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.NetworkUtils;

import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Util class for formatting event payload
 */
public class PublisherUtils {

    private static Log log = LogFactory.getLog(PublisherUtils.class);

    private static final String TRANSPORT = "https"; // TODO: it is not ideal to assume https is always available
    private static final String SERVER_USER_DEFINED_DATA_NS_URI = "http://wso2.org/ns/2009/09/bam/server/user-defined/data";
    private static final String SERVER_USER_DEFINED_DATA_NS_PREFIX = "svrusrdata";
    private static final String ELEMENT_NAME_EVENT = "Event";
    private static final String ELEMENT_NAME_SERVER_USER_DEFINED_DATA = "ServerUserDefinedData";
    private static final String ELEMENT_NAME_SERVER_NAME = "ServerName";
    private static final String ELEMENT_NAME_TENANT_ID = "TenantID";
    private static final String ELEMENT_NAME_DATA = "Data";
    private static final String ELEMENT_NAME_KEY = "Key";
    private static final String ELEMENT_NAME_VALUE = "Value";
    private static final String ELEMENT_NAME_TIMESTAMP = "Timestamp";

    private static BAMMediationStatsPublisherAdmin bamMediationStatsPublisherAdmin;
    private static SystemStatisticsUtil sysStatUtil;
    private static SynapseEnvironmentService synapseEnvironmentService;
    private static LightWeightEventBrokerInterface lightWeightEventBroker;
    private static ConfigurationContextService configurationContextService;
    private static ConfigurationContext axisConfigurationContext;
    private static String serverName;

    private static OMFactory factory = OMAbstractFactory.getOMFactory();

    public static String updateServerName(AxisConfiguration axisConfiguration)
            throws MediationPublisherException {

        // Used in integration tests
        if (axisConfiguration == null) {
            return serverName;
        }

        String serverName = null;
        String hostName;

        try {
            hostName = NetworkUtils.getLocalHostname();
        } catch (SocketException e) {
            throw new MediationPublisherException("Error getting host name for the BAM event payload", e);
        }

        ConfigurationContextService confContextService = PublisherUtils.getConfigurationContextService();
        int port = CarbonUtils.getTransportPort(confContextService.getServerConfigContext(), "https");
        String baseServerUrl = TRANSPORT + "://" + hostName + ":" + port;
        ConfigurationContext configurationContext = confContextService.getServerConfigContext();
        String context = configurationContext.getContextRoot();
        SuperTenantCarbonContext tenantCarbonContext = SuperTenantCarbonContext.getCurrentContext(axisConfiguration);
        String tenantDomain = null;
        if (tenantCarbonContext != null) {
            tenantDomain = tenantCarbonContext.getTenantDomain();
        }

        if (tenantDomain != null) {
            serverName = baseServerUrl + context + "t/" + tenantDomain;
        } else if (tenantDomain == null && context.equals("/")) {
            serverName = baseServerUrl + "";
        } else if (tenantDomain == null && !context.equals("/")) {
            serverName = baseServerUrl + context;
        }

        return serverName;
    }

    /**
     * Generating Data Event model as key value pair.
     * Message format:
     * <p/>
     * (01) <svrusrdata:Event xmlns:svrusrdata="http://wso2.org/ns/2009/09/bam/server/user-defined/data">
     * (02)    <svrusrdata:ServerUserDefinedData>
     * (03)        <svrusrdata:ServerName>http://127.0.0.1:8280</svrusrdata:ServerName>
     * (04)        <svrusrdata:Data>
     * (05)            <svrusrdata:Key>EndpointInMaxProcessingTime-simple</svrusrdata:Key>
     * (06)            <svrusrdata:Value>15</svrusrdata:Value>
     * (07)        </svrusrdata:Data>
     * (08)        <svrusrdata:Data>
     * (09)            <svrusrdata:Key>EndpointInAvgProcessingTime-simple</svrusrdata:Key>
     * (10)            <svrusrdata:Value>15.0</svrusrdata:Value>
     * (11)        </svrusrdata:Data>
     * (12)        <svrusrdata:Data>
     * (13)            <svrusrdata:Key>EndpointInMinProcessingTime-simple</svrusrdata:Key>
     * (14)            <svrusrdata:Value>15</svrusrdata:Value>
     * (15)        </svrusrdata:Data>
     * (16)        <svrusrdata:Data>
     * (17)            <svrusrdata:Key>EndpointInCount-simple</svrusrdata:Key>
     * (18)            <svrusrdata:Value>1</svrusrdata:Value>
     * (19)        </svrusrdata:Data>
     * (20)        <svrusrdata:Data>
     * (21)            <svrusrdata:Key>EndpointInFaultCount-simple</svrusrdata:Key>
     * (22)            <svrusrdata:Value>0</svrusrdata:Value>
     * (23)        </svrusrdata:Data>
     * (24)        <svrusrdata:Data>
     * (25)            <svrusrdata:Key>EndpointInID</svrusrdata:Key>
     * (26)            <svrusrdata:Value>simple</svrusrdata:Value>
     * (27)        </svrusrdata:Data>
     * (28)        <svrusrdata:Data>
     * (29)            <svrusrdata:Key>EndpointInCumulativeCount-simple</svrusrdata:Key>
     * (30)            <svrusrdata:Value>3</svrusrdata:Value>
     * (31)        </svrusrdata:Data>
     * (32)        <svrusrdata:Data>
     * (33)            <svrusrdata:Key>EndpointOutCumulativeCount-simple</svrusrdata:Key>
     * (34)            <svrusrdata:Value>0</svrusrdata:Value>
     * (35)        </svrusrdata:Data>
     * (36)    </svrusrdata:ServerUserDefinedData>
     * (37) </svrusrdata:Event>
     * <p/>
     * Schema for message format:
     * <p/>
     * <?xml version="1.0" encoding="utf-8" ?>
     * <xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema"
     * targetNamespace="http://wso2.org/ns/2009/09/bam/server/user-defined/data"
     * tns="http://wso2.org/ns/2009/09/bam/service/statistics/data">
     * <p/>
     * <xsd:element name="Event">
     * <xsd:complexType>
     * <xsd:sequence>
     * <xsd:element name="ServerUserDefinedData">
     * <xsd:complexType>
     * <xsd:sequence>
     * <xsd:element name="ServerName" type="xsd:string"/>
     * <xsd:element name="TenantID" type="xsd:string"/>
     * <xsd:element name="Data" minOccurs="0" maxOccurs="unbounded">
     * <xsd:complexType>
     * <xsd:sequence>
     * <xsd:element name="Key" type="xsd:string"/>
     * <xsd:element name="Value" type="xsd:string"/>
     * </xsd:sequence>
     * </xsd:complexType>
     * </xsd:element>
     * </xsd:sequence>
     * </xsd:complexType>
     * </xsd:element>
     * </xsd:sequence>
     * </xsd:complexType>
     * </xsd:element>
     * </xsd:schema>
     *
     * @throws MediationPublisherException
     */

    public static OMElement getEventPayload(AxisConfiguration axisConfiguration,
                                            Map<String, Map<String, InOutStatisticsView>> statsMap,
                                            ComponentType componentType,
                                            int totalInCount, int totalOutCount)
            throws MediationPublisherException {


        OMNamespace statNamespace = factory.createOMNamespace(SERVER_USER_DEFINED_DATA_NS_URI,
                                                              SERVER_USER_DEFINED_DATA_NS_PREFIX);
        OMElement eventElement = factory.createOMElement(ELEMENT_NAME_EVENT, statNamespace);

        OMElement serverUserDefinedDataElement = factory.createOMElement(
                ELEMENT_NAME_SERVER_USER_DEFINED_DATA, statNamespace);
        eventElement.addChild(serverUserDefinedDataElement);

        OMElement serverNameElement = factory.createOMElement(ELEMENT_NAME_SERVER_NAME, statNamespace);
        factory.createOMText(serverNameElement, updateServerName(axisConfiguration));
        serverUserDefinedDataElement.addChild(serverNameElement);

        String statTypePrefix = "Any";
        if (componentType == ComponentType.PROXYSERVICE) {
            statTypePrefix = "Proxy";
        } else if (componentType == ComponentType.ENDPOINT) {
            statTypePrefix = "Endpoint";
        } else if (componentType == ComponentType.SEQUENCE) {
            statTypePrefix = "Sequence";
        }

        for (Map<String, InOutStatisticsView> viewMap : statsMap.values()) {
            for (InOutStatisticsView view : viewMap.values()) {
                if (view != null) {
                    // in stat count always > 0
                    addKeyValueElements(serverUserDefinedDataElement, statTypePrefix, "In", view.getInStatistics());
                    if (view.getOutStatistics() != null && view.getOutStatistics().getCount() > 0) {
                        // out stat count can be zero
                        addKeyValueElements(serverUserDefinedDataElement, statTypePrefix, "Out", view.getOutStatistics());
                    }

                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Event payload " + eventElement.toString());
        }
        return eventElement;
    }

    private static void addKeyValueElements(OMElement parent, String statType, String direction,
                                            Statistics statistics) {

        if (statistics != null) {
            // suffix data fields with the stat ID, which represent endpoint, proxy or sequence name
            String id = "-" + statistics.getId();
            // Maximum processing time
            createDataElements(parent, statType + direction + MDPublisherConstants.BAM_MAX_PROCESS_TIME + id, Long
                    .toString(statistics.getMaxProcessingTime()));

            // Average processing time
            createDataElements(parent, statType + direction + MDPublisherConstants.BAM_AVG_PROCESS_TIME + id, Double
                    .toString(statistics.getAvgProcessingTime()));

            // Minimum processing time
            createDataElements(parent, statType + direction + MDPublisherConstants.BAM_MIN_PROCESS_TIME + id, Long
                    .toString(statistics.getMinProcessingTime()));

            // Count
            createDataElements(parent, statType + direction + MDPublisherConstants.BAM_COUNT + id, Integer.toString(statistics
                    .getCount()));
            // cumulative is equal to count as we are prcd .not clearing the Synapse stat data
            createDataElements(parent, statType + direction + MDPublisherConstants.BAM_CUMULATIVE_COUNT + id, Integer
                    .toString(statistics.getCount()));

            // Fault Count
            createDataElements(parent, statType + direction + MDPublisherConstants.BAM_FAULT_COUNT + id, Integer.toString(statistics
                    .getFaultCount()));

            // ID
            createDataElements(parent, statType + direction + MDPublisherConstants.BAM_ID, statistics.getId());
        }
    }

    /**
     * Generate Event payload from the statistics record as a key value pair
     *
     * @param record
     * @param axisConfiguration
     * @param tenantId
     * @return
     * @throws Exception
     */
    public static OMElement getEventPayload(StatisticsRecord record,
                                            AxisConfiguration axisConfiguration, int tenantId)
            throws Exception {


        OMNamespace statNamespace = factory.createOMNamespace(SERVER_USER_DEFINED_DATA_NS_URI,
                                                              SERVER_USER_DEFINED_DATA_NS_PREFIX);

        OMElement eventElement = factory.createOMElement(ELEMENT_NAME_EVENT, statNamespace);
        OMElement serverUserDefinedDataElement = factory.createOMElement(
                ELEMENT_NAME_SERVER_USER_DEFINED_DATA, statNamespace);
        eventElement.addChild(serverUserDefinedDataElement);

        OMElement serverNameElement = factory.createOMElement(ELEMENT_NAME_SERVER_NAME, statNamespace);
        factory.createOMText(serverNameElement, updateServerName(axisConfiguration));
        serverUserDefinedDataElement.addChild(serverNameElement);
        OMElement tenantOMOmElement = factory.createOMElement(ELEMENT_NAME_TENANT_ID, statNamespace);
        factory.createOMText(tenantOMOmElement, String.valueOf(tenantId));
        serverUserDefinedDataElement.addChild(tenantOMOmElement);

        java.util.Date currentDate = new java.util.Date();
        java.sql.Timestamp timestamp = new java.sql.Timestamp(currentDate.getTime());
        OMElement timestampOMElement = factory.createOMElement(ELEMENT_NAME_TIMESTAMP, statNamespace);
        factory.createOMText(timestampOMElement, timestamp.toString());
        serverUserDefinedDataElement.addChild(timestampOMElement);


        String statTypePrefix;
        switch (record.getType()) {
            case PROXYSERVICE:
                statTypePrefix = "Proxy";
                break;
            case SEQUENCE:
                statTypePrefix = "Sequence";
                break;
            case ENDPOINT:
                statTypePrefix = "Endpoint";
                break;
            default:
                statTypePrefix = "Any";
        }

        String direction = record.isInStatistic() ? "In" : "Out";
        addKeyValueElements(serverUserDefinedDataElement, statTypePrefix, direction, record);

        if (log.isDebugEnabled()) {
            log.debug("Event payload " + eventElement.toString());
        }
        return eventElement;
    }

    /**
     * Get and put the error logs which are retrived from synapse
     *
     * @param mediationStatisticsSnapshot
     * @param errorMap
     */
    public static void addErrorCategories(MediationStatisticsSnapshot mediationStatisticsSnapshot,
                                          Map<String, Object> errorMap) {
        List<ErrorLog> errorLogs = mediationStatisticsSnapshot.getErrorLogs();

        MediationSnapshotWrapper mediationSnapshotWrapper = new MediationSnapshotWrapper(mediationStatisticsSnapshot);

        if (errorLogs != null) {
            for (ErrorLog errorLog : errorLogs) {

                // Add errorID
                String idKey = mediationSnapshotWrapper.getStatTypePrefix() + mediationSnapshotWrapper.getDirection() + "ErrorID-"
                               + mediationSnapshotWrapper.getResId();
                errorMap.put(idKey, errorLog.getErrorCode());

                // Add errorName (TODO: we use the same value for key and name at the moment)
                String nameKey = mediationSnapshotWrapper.getStatTypePrefix()
                                 + mediationSnapshotWrapper.getDirection() + "ErrorName-Category-"
                                 + errorLog.getErrorCode() + "-ResourceID-" + mediationSnapshotWrapper.getResId();
                errorMap.put(nameKey, errorLog.getErrorCode());
            }
        }
    }

    /**
     * Get error counts
     *
     * @param mediationStatisticsSnapshot
     * @return
     */
    public static Map<String, Object> calculateErrorCounts(
            MediationStatisticsSnapshot mediationStatisticsSnapshot) {
        List<ErrorLog> errorLogs = mediationStatisticsSnapshot.getErrorLogs();
        Map<String, Object> errorMap = new HashMap<String, Object>();

        MediationSnapshotWrapper mediationSnapshotWrapper = new MediationSnapshotWrapper(
                mediationStatisticsSnapshot);

        // Iterate over error logs and create the errorMap with key-value pairs
        // as required by BAM
        if (errorLogs != null) {
            for (ErrorLog errorLog : errorLogs) {
                String key = mediationSnapshotWrapper.getStatTypePrefix()
                             + mediationSnapshotWrapper.getDirection() + "ErrorCount-Category-"
                             + errorLog.getErrorCode() + "-ResourceID-" + mediationSnapshotWrapper.getResId();

                Integer count = (Integer) errorMap.get(errorLog.getErrorCode());
                if (count == null) {
                    errorMap.put(key, 1);
                } else {
                    errorMap.put(key, count + 1);
                }
            }
        }

        return errorMap;
    }

    private static void addKeyValueElements(OMElement parent, String statType, String direction,
                                            StatisticsRecord entityRecord) {
        if (entityRecord == null) {
            return;
        }

        String id = "-" + entityRecord.getResourceId();
        createDataElements(parent, statType + direction + MDPublisherConstants.BAM_MAX_PROCESS_TIME + id, String
                .valueOf(entityRecord.getMaxTime()));
        createDataElements(parent, statType + direction + MDPublisherConstants.BAM_AVG_PROCESS_TIME + id, String
                .valueOf(entityRecord.getAvgTime()));
        createDataElements(parent, statType + direction + MDPublisherConstants.BAM_MIN_PROCESS_TIME + id, String
                .valueOf(entityRecord.getMinTime()));
        createDataElements(parent, statType + direction + MDPublisherConstants.BAM_COUNT + id, String.valueOf(entityRecord
                .getTotalCount()));
        createDataElements(parent, statType + direction + MDPublisherConstants.BAM_CUMULATIVE_COUNT + id, String.valueOf(entityRecord
                .getTotalCount()));
        createDataElements(parent, statType + direction + MDPublisherConstants.BAM_FAULT_COUNT + id, String.valueOf(entityRecord
                .getFaultCount()));
        createDataElements(parent, statType + direction + MDPublisherConstants.BAM_ID, entityRecord.getResourceId());
    }

    private static void createDataElements(OMElement parent, String key, String value) {

        OMNamespace statNamespace = factory.createOMNamespace(SERVER_USER_DEFINED_DATA_NS_URI,
                                                              SERVER_USER_DEFINED_DATA_NS_PREFIX);

        OMElement dataElement = factory.createOMElement(ELEMENT_NAME_DATA, statNamespace);
        parent.addChild(dataElement);

        OMElement keyElement = factory.createOMElement(ELEMENT_NAME_KEY, statNamespace);
        factory.createOMText(keyElement, key);
        dataElement.addChild(keyElement);

        OMElement valueElement = factory.createOMElement(ELEMENT_NAME_VALUE, statNamespace);
        factory.createOMText(valueElement, value);
        dataElement.addChild(valueElement);

    }

    public static void setMediationStatPublisherAdmin(
            BAMMediationStatsPublisherAdmin bamMediationStatsPublisherAdmin) {
        PublisherUtils.bamMediationStatsPublisherAdmin = bamMediationStatsPublisherAdmin;
    }

    public static BAMMediationStatsPublisherAdmin getMediationStatPublisherAdmin() {
        return bamMediationStatsPublisherAdmin;
    }

    public static void setSystemStatististicsUtil(SystemStatisticsUtil systemStatisticsUtil) {
        PublisherUtils.sysStatUtil = systemStatisticsUtil;
    }

    public static SystemStatisticsUtil getSystemStatisticsUtil() {
        return sysStatUtil;
    }

    public static void setSynapseEnvironmentService(
            SynapseEnvironmentService synapseEnvironmentService) {
        PublisherUtils.synapseEnvironmentService = synapseEnvironmentService;
    }

    public static SynapseEnvironmentService getSynapseEnvironmentService() {
        return synapseEnvironmentService;
    }

    public static void setEventBroker(LightWeightEventBrokerInterface broker) {
        PublisherUtils.lightWeightEventBroker = broker;
    }

    public static LightWeightEventBrokerInterface getEventBroker() {
        return lightWeightEventBroker;
    }

    /**
     * Generate Event payload from the statistics record as a key value pair
     *
     * @param errorMap
     * @param axisConfiguration
     * @param tenantId
     * @return
     * @throws Exception
     */
    public static OMElement getEventPayload(Map<String, Object> errorMap,
                                            AxisConfiguration axisConfiguration, int tenantId)
            throws Exception {

        OMNamespace statNamespace = factory.createOMNamespace(SERVER_USER_DEFINED_DATA_NS_URI,
                                                              SERVER_USER_DEFINED_DATA_NS_PREFIX);

        OMElement eventElement = factory.createOMElement(ELEMENT_NAME_EVENT, statNamespace);
        OMElement serverUserDefinedDataElement = factory.createOMElement(
                ELEMENT_NAME_SERVER_USER_DEFINED_DATA, statNamespace);
        eventElement.addChild(serverUserDefinedDataElement);

        OMElement serverNameElement = factory.createOMElement(ELEMENT_NAME_SERVER_NAME, statNamespace);
        factory.createOMText(serverNameElement, updateServerName(axisConfiguration));
        serverUserDefinedDataElement.addChild(serverNameElement);
        OMElement tenantOMOmElement = factory.createOMElement(ELEMENT_NAME_TENANT_ID, statNamespace);
        factory.createOMText(tenantOMOmElement, String.valueOf(tenantId));
        serverUserDefinedDataElement.addChild(tenantOMOmElement);

        java.util.Date currentDate = new java.util.Date();
        java.sql.Timestamp timestamp = new java.sql.Timestamp(currentDate.getTime());
        OMElement timestampOMElement = factory.createOMElement(ELEMENT_NAME_TIMESTAMP, statNamespace);
        factory.createOMText(timestampOMElement, timestamp.toString());
        serverUserDefinedDataElement.addChild(timestampOMElement);


        for (Map.Entry<String, Object> errorEntry : errorMap.entrySet()) {
            createDataElements(serverUserDefinedDataElement, errorEntry.getKey(), String.valueOf(errorEntry.getValue()));
        }

        if (log.isDebugEnabled()) {
            log.debug("Error event payload " + eventElement.toString());
        }

        return eventElement;
    }

    public static void setConfigurationContextService(ConfigurationContextService contextService) {
        PublisherUtils.configurationContextService = contextService;
    }

    public static ConfigurationContextService getConfigurationContextService() {
        return configurationContextService;
    }

    public static void setServerName(String serverName) {
        PublisherUtils.serverName = serverName;
    }

    public static String getServerName() {
        return serverName;
    }

    private static class MediationSnapshotWrapper {
        private MediationStatisticsSnapshot mediationStatisticsSnapshot;
        private String statTypePrefix = "Any";
        private String direction;
        private String resId;

        public MediationSnapshotWrapper(MediationStatisticsSnapshot mediationStatisticsSnapshot) {
            this.mediationStatisticsSnapshot = mediationStatisticsSnapshot;
            updateFields();
        }

        public String getStatTypePrefix() {
            return statTypePrefix;
        }

        public String getDirection() {
            return direction;
        }

        public String getResId() {
            return resId;
        }

        private MediationSnapshotWrapper updateFields() {
            switch (mediationStatisticsSnapshot.getUpdate().getType()) {
                case PROXYSERVICE:
                    statTypePrefix = "Proxy";
                    break;
                case SEQUENCE:
                    statTypePrefix = "Sequence";
                    break;
                case ENDPOINT:
                    statTypePrefix = "Endpoint";
                    break;
                default:
                    statTypePrefix = "Any";
            }

            direction = mediationStatisticsSnapshot.getUpdate().isInStatistic() ? "In" : "Out";
            resId = mediationStatisticsSnapshot.getUpdate().getResourceId();
            return this;
        }

        public static ConfigurationContext getConfigurationContext() {
            return axisConfigurationContext;
        }

        public static void setConfigurationContext(ConfigurationContext axisConfigurationContext) {
            PublisherUtils.axisConfigurationContext = axisConfigurationContext;
        }
    }
}

