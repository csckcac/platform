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
package org.wso2.carbon.apimgt.usage.publisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.usage.publisher.dto.DataBridgeRequestPublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.dto.DataBridgeResponsePublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.dto.RequestPublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.dto.ResponsePublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.internal.UsageComponent;
import org.wso2.carbon.apimgt.usage.publisher.service.APIMGTConfigReaderService;
import org.wso2.carbon.databridge.agent.thrift.Agent;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.conf.AgentConfiguration;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import java.net.MalformedURLException;

public class APIMgtUsageDataBridgeDataPublisher implements APIMgtUsageDataPublisher{

    private static final Log log   = LogFactory.getLog(APIMgtUsageDataBridgeDataPublisher.class);

    private DataPublisher dataPublisher;
    private String requestStreamId;
    private String responseStreamId;

    public void init(){
        try {
            log.debug("Initializing APIMgtUsageDataBridgeDataPublisher");
            this.dataPublisher = getDataPublisher();
            this.requestStreamId = DataBridgeRequestPublisherDTO.addStreamId(dataPublisher);
            this.responseStreamId = DataBridgeResponsePublisherDTO.addStreamId(dataPublisher);
        }catch (Exception e){
            log.error("Error initializing APIMgtUsageDataBridgeDataPublisher", e);
        }
    }

    public void publishEvent(RequestPublisherDTO requestPublisherDTO) {
        DataBridgeRequestPublisherDTO dataBridgeRequestPublisherDTO = new DataBridgeRequestPublisherDTO(requestPublisherDTO);
        Event event = new Event(requestStreamId, System.currentTimeMillis(), new Object[]{"external"}, null,
                (Object[]) dataBridgeRequestPublisherDTO.createPayload());
        try {
            dataPublisher.publish(event);
        } catch(AgentException e){
            log.error("Error while publishing request event", e);
        }

    }

    public void publishEvent(ResponsePublisherDTO responsePublisherDTO) {
        DataBridgeResponsePublisherDTO dataBridgeResponsePublisherDTO = new DataBridgeResponsePublisherDTO(responsePublisherDTO);
        Event event = new Event(responseStreamId, System.currentTimeMillis(), new Object[]{"external"}, null,
                (Object[]) dataBridgeResponsePublisherDTO.createPayload());
        try {
            dataPublisher.publish(event);
        } catch (AgentException e) {
            log.error("Error while publishing response event", e);
        }

    }

   private DataPublisher getDataPublisher()
            throws AgentException, MalformedURLException, AuthenticationException,
                   TransportException {
        APIMGTConfigReaderService apimgtConfigReaderService = UsageComponent.getApiMgtConfigReaderService();
        //expect to read data receiver URL something like "tcp://host:7611"
        AgentConfiguration agentConfiguration = new AgentConfiguration();
        Agent agent = new Agent(agentConfiguration);
        return new DataPublisher(apimgtConfigReaderService.getBamServerURL(),
            apimgtConfigReaderService.getBamServerUser(),
            apimgtConfigReaderService.getBamServerPassword(),
            agent);
    }
}
