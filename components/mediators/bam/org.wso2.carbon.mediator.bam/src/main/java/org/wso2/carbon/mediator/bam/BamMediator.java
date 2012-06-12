/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.mediator.bam;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;
import java.util.List;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.agent.Agent;
import org.wso2.carbon.agent.DataPublisher;
import org.wso2.carbon.agent.commons.Event;
import org.wso2.carbon.agent.commons.exception.*;
import org.wso2.carbon.agent.conf.AgentConfiguration;
import org.wso2.carbon.agent.exception.AgentException;
import org.wso2.carbon.agent.exception.TransportException;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import java.net.MalformedURLException;
import org.wso2.carbon.bam.mediationstats.data.publisher.stub.conf.Property;

/**
 * Transforms the current message payload using the given BAM configuration.
 * The current message context is replaced with the result as XML.
 */
public class BamMediator extends AbstractMediator {

    private static final Log log = LogFactory.getLog(BamMediator.class);
    private static final String ADMIN_SERVICE_PARAMETER = "adminService";
    private static final String HIDDEN_SERVICE_PARAMETER = "hiddenService";

    /*private String serverProfile = "";
    private String streamName = "org.wso2.carbon.mediator.bam.BamMediator";
    private String streamVersion = "1.0.0";
    private String serverIp = "localhost";
    private String serverPort = "7611";
    private String userName = "admin";
    private String password = "admin";*/

    private String serverProfile = "";
    private String streamName = "";
    private String streamVersion = "";
    private String serverIp = "";
    private String serverPort = "";
    private String userName = "";
    private String password = "";



    private List<Property> properties = null;

    private String streamId = null;
    //private AgentConfiguration agentConfiguration = null;
    //private Agent agent = null;
    private DataPublisher dataPublisher = null;

    public BamMediator() {

    }

    public boolean mediate(MessageContext mc) {

        SynapseLog synLog = getLog(mc);

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Start : BAM mediator");

            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Message : " + mc.getEnvelope());
            }
        }

        // Do somthing useful..
        // Note the access to the Synapse Message context
        org.apache.axis2.context.MessageContext msgCtx = ((Axis2MessageContext) mc).getAxis2MessageContext();

        AxisService service = msgCtx.getAxisService();
        if (service == null ||
            service.getParameter(ADMIN_SERVICE_PARAMETER) != null ||
            service.getParameter(HIDDEN_SERVICE_PARAMETER) != null) {
            return true;
        }

        AxisConfiguration axisConfiguration = msgCtx.getConfigurationContext().getAxisConfiguration();
        int tenantId = SuperTenantCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
//        Map<Integer, ActivityConfigData> tenantSpecificActivity = TenantActivityConfigData.getTenantSpecificEventingConfigData();
//        ActivityConfigData activityConfigData = tenantSpecificActivity.get(tenantId);
        try {
            logMessage(tenantId, mc);
        } catch (AgentException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (MalformedStreamDefinitionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (StreamDefinitionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (WrongEventTypeException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (DifferentStreamDefinitionAlreadyDefinedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (MalformedURLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (AuthenticationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (TransportException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("End : BAM mediator");

            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Message : " + mc.getEnvelope());
            }
        }

        return true;
    }

    private void logMessage(int tenantId, MessageContext messageContext)
            throws AgentException, MalformedStreamDefinitionException, StreamDefinitionException,
                   WrongEventTypeException, DifferentStreamDefinitionAlreadyDefinedException,
                   MalformedURLException, AuthenticationException, TransportException {

        if (streamId == null) {
            AgentConfiguration agentConfiguration = new AgentConfiguration();
            //agentConfiguration.setTrustStore("/works/platform_trunk/graphite/components/agent/org.wso2.carbon.agent.server/src/test/resources/client-truststore.jks");
            /*String keyStorePath = CarbonUtils.getCarbonHome() + File.separator + "repository" +
                                  File.separator + "resources" + File.separator + "security" +
                                  File.separator + "client-truststore.jks";
            String keyStorePassword = "wso2carbon";
            agentConfiguration.setTrustStore(keyStorePath);
            agentConfiguration.setTrustStorePassword(keyStorePassword);
            System.setProperty("javax.net.ssl.trustStore", keyStorePath);
            System.setProperty("javax.net.ssl.trustStorePassword", keyStorePassword);*/
            Agent agent = new Agent(agentConfiguration);
            //create data publisher
            dataPublisher = new DataPublisher("tcp://" + this.serverIp + ":" + this.serverPort,
                                              this.userName, this.password, agent);
            //dataPublisher = new DataPublisher("tcp://localhost:7612", "admin", "admin", agent);

            //Define event stream
            streamId = dataPublisher.defineEventStream("{" +
                                                       "  'name':'" + this.streamName + "'," +
                                                       "  'version':'"+ this.streamVersion + "'," +
                                                       "  'nickName': 'Log'," +
                                                       "  'description': 'log to bam'," +
                                                       "  'metaData':[" +
                                                       "          {'name':'ProductName','type':'STRING'}" +
                                                       "  ]," +
                                                       "  'payloadData':[" +
                                                       "          {'name':'TenantId','type':'INT'}," +
                                                       "          {'name':'MessageId','type':'STRING'}," +
                                                       "          {'name':'SOAPHeaddr','type':'STRING'}," +
                                                       "          {'name':'SOAPBody','type':'STRING'}" +
                                                       "  ]" +
                                                       "}");
            log.info("Event Stream Created.");
        }
        //Publish event for a valid stream
        if (streamId != null && !streamId.isEmpty()) {
            log.info("Stream ID: " + streamId);
            // Event for the message
            Event eventJohnOne = new Event(streamId, System.currentTimeMillis(),
                                           new Object[]{"external"},
                                           null,
                                           new Object[]{tenantId, messageContext.getMessageID(),
                                                        messageContext.getEnvelope().getHeader().toString(),
                                                        messageContext.getEnvelope().getBody().toString()}
            );
            dataPublisher.publish(eventJohnOne);
        } else {
            log.info("streamId is empty.");
        }
    }

    public String getType() {
        return null;
    }

    public void setTraceState(int traceState) {
        traceState = 0;
    }

    public int getTraceState() {
        return 0;
    }

    public void setDescription(String s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getDescription() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setServerIP(String newValue) {
        serverIp = newValue;
    }

    public String getServerIP() {
        return serverIp;
    }

    public void setUserName(String newValue){
        userName = newValue;
    }

    public String getUserName(){
        return password;
    }

    public void setPassword(String newValue){
        password = newValue;
    }

    public String getPassword(){
        return password;
    }

    public void setServerProfile(String newValue){
        serverProfile = newValue;
    }

    public String getServerProfile(){
        return serverProfile;
    }

    public void setStreamName(String newValue){
        this.streamName = newValue;
    }

    public String getStreamName(){
        return this.streamName;
    }

    public void setStreamVersion(String newValue){
        this.streamVersion = newValue;
    }

    public String getStreamVersion(){
        return this.streamVersion;
    }

    public void setServerPort(String newValue) {
        serverPort = newValue;
    }

    public String getServerPort() {
        return serverPort;
    }

    public void setProperties(List<Property> newValue){
        properties = newValue;
    }

    public List<Property> getProperties(){
        return properties;
    }

}