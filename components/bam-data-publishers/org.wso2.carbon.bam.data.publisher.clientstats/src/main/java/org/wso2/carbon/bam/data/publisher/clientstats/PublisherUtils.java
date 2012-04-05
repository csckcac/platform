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
package org.wso2.carbon.bam.data.publisher.clientstats;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.event.EventBrokerService;
import org.wso2.carbon.bam.data.publisher.clientstats.services.ClientStatisticsPublisherAdmin;

public class PublisherUtils {
	private static Log log = LogFactory.getLog(PublisherUtils.class);

	
	private static ClientStatisticsPublisherAdmin clientStatisticsPublisherAdmin = null;
	private static ConfigurationContext configurationContext;
	private static EventBrokerService eventBrokerService;
	private static ServerConfiguration serverConfiguration;
	public static final String TRANSPORT = "https"; // TODO: it is not ideal to
	// assume https is always
	// availabe

	public static final String SERVER_USER_DEFINED_DATA_NS_URI = "http://wso2.org/ns/2009/09/bam/server/user-defined/data";
    public static final String OPERATION_USER_DEFINED_DATA_NS_URI = "http://wso2.org/ns/2009/09/bam/operation/user-defined/data";

    public static final String SERVER_USER_DEFINED_DATA_NS_PREFIX = "svrusrdata";
    public static final String OPERATION_USER_DEFINED_DATA_NS_PREFIX = "oprusrdata";
    // OM element names
    public static final String ELEMENT_NAME_EVENT = "Event";
    public static final String ELEMENT_NAME_SERVER_USER_DEFINED_DATA = "ServerUserDefinedData";
    public static final String ELEMENT_NAME_OPERATION_USER_DEFINED_DATA = "OperationUserDefinedData";
    public static final String ELEMENT_NAME_SERVER_NAME = "ServerName";
    public static final String ELEMENT_NAME_OPERATION_NAME = "OperationName";
    public static final String ELEMENT_NAME_SERVICE_NAME = "ServiceName";
    public static final String ELEMENT_NAME_DATA = "Data";
    public static final String ELEMENT_NAME_KEY = "Key";
    public static final String ELEMENT_NAME_VALUE = "Value";
    
    // for server userdefined data
    public static OMElement getEventPayload(String epr, String userParam, String uuid,
                                            String service, String operation) {

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace statNamespace = factory.createOMNamespace(SERVER_USER_DEFINED_DATA_NS_URI,
                                                              SERVER_USER_DEFINED_DATA_NS_PREFIX);
        OMElement eventElement = factory.createOMElement(ELEMENT_NAME_EVENT, statNamespace);

        OMElement serverUserDefinedDataElement = factory
                .createOMElement(ELEMENT_NAME_SERVER_USER_DEFINED_DATA, statNamespace);
        eventElement.addChild(serverUserDefinedDataElement);

        OMElement serverNameElement = factory.createOMElement(ELEMENT_NAME_SERVER_NAME,
                                                              statNamespace);
        factory.createOMText(serverNameElement, epr);
        serverUserDefinedDataElement.addChild(serverNameElement);

        String clientID = userParam + "_" + uuid + "_" + epr;
        String clientValue = service + "_" + operation;
        addServerKeyValueElements(serverUserDefinedDataElement, clientID, clientValue);

        if (log.isDebugEnabled()) {
            log.debug("Event payload " + eventElement.toString());
        }
        return eventElement;
    }

    private static void addServerKeyValueElements(OMElement parent, String clientID,
                                                  String clientValue) {

        createServerDataElements(parent, clientID, clientValue);
    }

    private static void createServerDataElements(OMElement parent, String key, String value) {
        OMFactory factory = OMAbstractFactory.getOMFactory();
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

    // for operation userdefined data
    public static OMElement getEventPayload(String userParam, String uuid, double avgResponseTime,
                                            long minResponseTime, long maxResponseTime,
                                            int requestCount, int responseCount, int faultCount,
                                            String serviceName, String operationName,
                                            String remoteIPAddress, String wsas_server) {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace statNamespace = factory
                .createOMNamespace(OPERATION_USER_DEFINED_DATA_NS_URI,
                                   OPERATION_USER_DEFINED_DATA_NS_PREFIX);
        OMElement eventElement = factory.createOMElement(ELEMENT_NAME_EVENT, statNamespace);

        OMElement opeartionUserDefinedDataElement = factory
                .createOMElement(ELEMENT_NAME_OPERATION_USER_DEFINED_DATA, statNamespace);
        eventElement.addChild(opeartionUserDefinedDataElement);
        OMElement opeartionNameElement = factory.createOMElement(ELEMENT_NAME_OPERATION_NAME,
                                                                 statNamespace);
        factory.createOMText(opeartionNameElement, operationName);
        OMElement serviceNameElement = factory.createOMElement(ELEMENT_NAME_SERVICE_NAME,
                                                               statNamespace);
        factory.createOMText(serviceNameElement, serviceName);
        OMElement serverNameElement = factory.createOMElement(ELEMENT_NAME_SERVER_NAME,
                                                              statNamespace);
        factory.createOMText(serverNameElement, wsas_server);

        opeartionUserDefinedDataElement.addChild(opeartionNameElement);
        opeartionUserDefinedDataElement.addChild(serviceNameElement);
        opeartionUserDefinedDataElement.addChild(serverNameElement);
        addServiceKeyValueElements(opeartionUserDefinedDataElement, userParam, uuid,
                                   avgResponseTime, minResponseTime, maxResponseTime, requestCount,
                                   responseCount, faultCount, remoteIPAddress);
        if (log.isDebugEnabled()) {
            log.debug("Event payload " + eventElement.toString());
        }
        return eventElement;
    }

    private static void addServiceKeyValueElements(OMElement parent, String userParam, String uuid,
                                                   double avgResponseTime, long minResponseTime,
                                                   long maxResponseTime, int requestCount,
                                                   int responseCount, int faultCount,
                                                   String remoteIPAddress) {

        createOperarionDataElements(parent, userParam + "_" + uuid + "_" + "MaxProcessingTime",
                                    Long.toString(maxResponseTime));
        createOperarionDataElements(parent, userParam + "_" + uuid + "_" + "AverageProcessingTime",
                                    Double.toString(avgResponseTime));
        createOperarionDataElements(parent, userParam + "_" + uuid + "_" + "MinProcessingTime",
                                    Long.toString(minResponseTime));
        createOperarionDataElements(parent, userParam + "_" + uuid + "_" + "RequestCount", Integer
                .toString(requestCount));
        createOperarionDataElements(parent, userParam + "_" + uuid + "_" + "ResponseCount", Integer
                .toString(responseCount));
        createOperarionDataElements(parent, userParam + "_" + uuid + "_" + "FaultCount", Integer
                .toString(faultCount));
        createOperarionDataElements(parent, userParam + "_" + uuid + "_" + "IPAddress",
                                    remoteIPAddress);
    }

    private static void createOperarionDataElements(OMElement parent, String key, String value) {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace statNamespace = factory
                .createOMNamespace(OPERATION_USER_DEFINED_DATA_NS_URI,
                                   OPERATION_USER_DEFINED_DATA_NS_PREFIX);

        OMElement dataElement = factory.createOMElement(ELEMENT_NAME_DATA, statNamespace);
        parent.addChild(dataElement);

        OMElement keyElement = factory.createOMElement(ELEMENT_NAME_KEY, statNamespace);
        factory.createOMText(keyElement, key);
        dataElement.addChild(keyElement);

        OMElement valueElement = factory.createOMElement(ELEMENT_NAME_VALUE, statNamespace);
        factory.createOMText(valueElement, value);
        dataElement.addChild(valueElement);

    }
	public static EventBrokerService getEventBrokerService() {
		return eventBrokerService;
	}

	public static void setEventBrokerService(EventBrokerService eventBrokerService) {
		PublisherUtils.eventBrokerService = eventBrokerService;
	}

	public static ConfigurationContext getConfigurationContext() {
		return configurationContext;
	}

	public static void setConfigurationContext(ConfigurationContext configurationContext) {
		PublisherUtils.configurationContext = configurationContext;
	}

	public static ServerConfiguration getServerConfiguration() {
		return serverConfiguration;
	}

	public static void setServerConfiguration(ServerConfiguration serverConfiguration) {
		PublisherUtils.serverConfiguration = serverConfiguration;
	}
    public static ClientStatisticsPublisherAdmin getClientStatisticsPublisherAdmin() {
        return clientStatisticsPublisherAdmin;
    }

    public static void setClientStatisticsPublisherAdmin(
                                                         ClientStatisticsPublisherAdmin clientStatisticsPublisherAdmin) {
        PublisherUtils.clientStatisticsPublisherAdmin = clientStatisticsPublisherAdmin;
    }
}
