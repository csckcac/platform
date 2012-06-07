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
package org.wso2.carbon.usage.agent.util;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.event.core.EventBroker;
import org.wso2.carbon.event.core.Message;
import org.wso2.carbon.event.core.exception.EventBrokerException;
import org.wso2.carbon.usage.agent.beans.BandwidthUsage;
import org.wso2.carbon.usage.agent.exception.UsageException;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.NetworkUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.net.SocketException;

/**
 * this class provide utility methods to publish usage statistics
 */
public class PublisherUtils {
    private static Log log = LogFactory.getLog(PublisherUtils.class);
    private static final String TRANSPORT = "https";
    private static ConfigurationContextService configurationContextService;


    /**
     * method to update server name
     * @param tenantId tenant id
     * @return server name
     * @throws UsageException
     */

    public static String updateServerName(int tenantId) throws UsageException {

        String serverName;
        String hostName;

        try {
            hostName = NetworkUtils.getLocalHostname();
        } catch (SocketException e) {
            throw new UsageException("Error getting host name for the registry usage event payload",
                    e);
        }

        ConfigurationContextService configurationContextService = PublisherUtils.
                getConfigurationContextService();
        ConfigurationContext configurationContext;
        if (configurationContextService != null) {
            configurationContext = configurationContextService.getServerConfigContext();
        } else {
            throw new UsageException("ConfigurationContext is null");
        }
//        int port = CarbonUtils.getTransportPort(configurationContext, "https");

        String carbonHttpsPort = System.getProperty("carbon." + TRANSPORT + ".port");
        if (carbonHttpsPort == null) {
            carbonHttpsPort = Integer.toString(
                    CarbonUtils.getTransportPort(configurationContext, TRANSPORT));
        }
        String baseServerUrl = TRANSPORT + "://" + hostName + ":" + carbonHttpsPort;
        String context = configurationContext.getContextRoot();

        String tenantDomain = null;
        try {
            Tenant tenant = Util.getRealmService().getTenantManager().getTenant(tenantId);
            if(tenant!=null){
                tenantDomain = tenant.getDomain();
            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UsageException("Failed to get tenant domain", e);
        }

        if (tenantDomain != null) {

            serverName = baseServerUrl + context + "t/" + tenantDomain;

        } else if (context.equals("/")) {

            serverName = baseServerUrl + "";
        } else {
            serverName = baseServerUrl + context;

        }

        return serverName;
    }

    /**
     * this method generate the payload according to the following format.
     * <p/>
     * soapenv:Body xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
     * <svrusrdata:Event xmlns:svrusrdata="http://wso2.org/ns/2009/09/bam/server/user-defined/data">
     *  <svrusrdata:ServerUserDefinedData>
     *    <svrusrdata:TenantID>1</svrusrdata:TenantID>
     *    <svrusrdata:ServerName>localhost:port/</svrusrdata:ServerName>
     *      <svrusrdata:Data>
     *          <svrusrdata:Key>registryOutgoingBw</svrusrdata:Key>
     *          <svrusrdata:Value>0</svrusrdata:Value>
     *      </svrusrdata:Data>
     *    </svrusrdata:ServerUserDefinedData>
     * </svrusrdata:Event>
     * </soapenv:Body>
     *
     * @param usage  BandwidthUsage
     * @return eventElement
     * @throws Exception
     */
    //TODO Refactor: This should go inside BAM: common package
    public static OMElement getEventPayload(BandwidthUsage usage) throws Exception {

        String measurement = usage.getMeasurement();
        String value = Long.toString(usage.getValue());
        int tenantId = usage.getTenantId();

        OMFactory factory = OMAbstractFactory.getOMFactory();

        // add the xml namespace
        OMNamespace statNamespace = factory.createOMNamespace(
                UsageAgentConstants.STATISTICS_DATA_NS_URI,
                UsageAgentConstants.STATISTICS_DATA_NS_PREFIX);
        OMElement eventElement = factory.createOMElement(
                UsageAgentConstants.STATISTICS_DATA_ELEMENT_NAME_EVENT, statNamespace);
        OMElement serviceInvocationDataElement = factory.createOMElement(
                UsageAgentConstants.STATISTICS_DATA_ELEMENT_NAME_SERVICE_STATISTICS_DATA,
                statNamespace);
        eventElement.addChild(serviceInvocationDataElement);

        // add server name data element
        OMElement serverNameElement = factory.createOMElement(
                UsageAgentConstants.STATISTICS_DATA_ELEMENT_NAME_SERVER_NAME, statNamespace);
        String serverName = PublisherUtils.updateServerName(usage.getTenantId());
        factory.createOMText(serverNameElement, serverName);
        serviceInvocationDataElement.addChild(serverNameElement);

        // add tenant id data element
        OMElement tenantElement = factory.createOMElement(
                UsageAgentConstants.STATISTICS_DATA_ELEMENT_NAME_TENANT_ID, statNamespace);
        factory.createOMText(tenantElement, Integer.toString(tenantId));
        serviceInvocationDataElement.addChild(tenantElement);

        // add data element to carry key, value pair
        OMElement dataElement = factory.createOMElement(
                UsageAgentConstants.ELEMENT_NAME_DATA, statNamespace);
        serviceInvocationDataElement.addChild(dataElement);

        OMElement keyElement = factory.createOMElement(
                UsageAgentConstants.ELEMENT_NAME_KEY, statNamespace);
        factory.createOMText(keyElement, measurement);
        dataElement.addChild(keyElement);

        OMElement valueElement = factory.createOMElement(
                UsageAgentConstants.ELEMENT_NAME_VALUE, statNamespace);
        factory.createOMText(valueElement, value);
        dataElement.addChild(valueElement);

        return eventElement;
    }


    /**
     * this method get the event payload, construct the SOAP envelop and call the publish method in
     * EventBrokerService.
     *
     * @param usage BandwidthUsage
     * @throws UsageException
     */
    public static void publish(BandwidthUsage usage) throws UsageException {

        OMElement statMessage;
        Message message;

        // get the event payload
        try {
            statMessage = PublisherUtils.getEventPayload(usage);
            message = new Message();
            message.setMessage(statMessage);
        } catch (Exception e) {
            log.error("Failed to get usage event payload", e);
            return;
        }

        // get the topic of the event to be published
        String topic = UsageAgentConstants.BANDWIDTH_USAGE_TOPIC;
        EventBroker eventBrokerService = Util.getEventBrokerService();
        // publish the event
        try {
            SuperTenantCarbonContext.startTenantFlow();
            SuperTenantCarbonContext.getCurrentContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            SuperTenantCarbonContext.getCurrentContext().getTenantDomain(true);
            // use publishRobust since collect data  and send after summarize 
            eventBrokerService.publishRobust(message, topic);
        } catch (EventBrokerException e) {
            log.error("SystemStatisticsHandler - Unable to send notification for stat threshold", e);
        } finally {
            SuperTenantCarbonContext.endTenantFlow();
        }
    }

    /**
     * method to get configurationContextService
     * @return configurationContextService
     */

    public static ConfigurationContextService getConfigurationContextService() {
        return configurationContextService;
    }

    /**
     * method to setConfigurationContextService
     * @param configurationContextService
     */
    public static void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        PublisherUtils.configurationContextService = configurationContextService;
    }

}
