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
import org.wso2.carbon.apimgt.usage.publisher.dto.BAMRequestPublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.dto.BAMResponsePublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.dto.RequestPublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.dto.ResponsePublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.internal.UsageComponent;
import org.wso2.carbon.apimgt.usage.publisher.service.APIMGTConfigReaderService;
import org.wso2.carbon.bam.agent.conf.AgentConfiguration;
import org.wso2.carbon.bam.agent.core.Agent;
import org.wso2.carbon.bam.agent.publish.EventReceiver;
import org.wso2.carbon.bam.service.Event;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * APIMgtUsageBAMDataPublisher that publish data to bam server
 */
public class APIMgtUsageBAMDataPublisher implements APIMgtUsageDataPublisher {

  /**
     * EventReceiver instance that holds config of the receiver in BAM server
     */

    private static final Log log   = LogFactory.getLog(APIMgtUsageBAMDataPublisher.class);

    private EventReceiver eventReceiver;

    private Agent agent;

    @Override
    public void init() {
        log.debug("Initializing APIMgtUsageDataBridgeDataPublisher");
        this.eventReceiver = getEventReceiver();
        AgentConfiguration configuration = new AgentConfiguration();
        agent = new Agent(configuration);
    }

  /**
     * publishes even to BAM server by creating an event
     * @param requestPublisherDTO  <code>RequestPublisherDTO</code> that is needed to be published
     */
    @Override
    public void publishEvent(RequestPublisherDTO requestPublisherDTO) {
        BAMRequestPublisherDTO bamRequestPublisherDTO = new BAMRequestPublisherDTO(requestPublisherDTO);
        Event event = new Event();
        event.setCorrelation(createCorrelationMap());
        event.setEvent(bamRequestPublisherDTO.createEventDataMap());
        event.setMeta(createMetaDataMap());
        List<Event> events = new ArrayList<Event>();
        events.add(event);
        log.debug("Publishing request event to BAM");
        agent.publish(events, eventReceiver);
    }

    @Override
    public void publishEvent(ResponsePublisherDTO responsePublisherDTO) {
        BAMResponsePublisherDTO bamResponsePublisherDTO = new BAMResponsePublisherDTO(responsePublisherDTO);
        Event event = new Event();
        event.setCorrelation(createCorrelationMap());
        event.setEvent(bamResponsePublisherDTO.createEventDataMap());
        event.setMeta(createMetaDataMap());
        List<Event> events = new ArrayList<Event>();
        events.add(event);
        log.debug("Publishing response event to BAM");
        agent.publish(events, eventReceiver);
    }

    private Map<String, ByteBuffer> createMetaDataMap() {
        Map<String, ByteBuffer> metaDataMap = new HashMap<String, ByteBuffer>();
        // not used, but need as it is not sure how BAM will work.
        metaDataMap.put("metaKey", ByteBuffer.wrap("metaValue".getBytes()));
        return metaDataMap;
    }

    private  Map<String, ByteBuffer> createCorrelationMap() {
        Map<String, ByteBuffer> correlationMap = new HashMap<String, ByteBuffer>();
        // not used, but need as it is not sure how BAM will work.
        correlationMap.put("correlationKey", ByteBuffer.wrap("correlationValue".getBytes()));
        return correlationMap;
    }

    private EventReceiver getEventReceiver() {
        APIMGTConfigReaderService apimgtConfigReaderService = UsageComponent.getApiMgtConfigReaderService();
        EventReceiver eventReceiver = new EventReceiver();
        eventReceiver.setUrl(apimgtConfigReaderService.getBamServerURL());
        eventReceiver.setUserName(apimgtConfigReaderService.getBamServerUser());
        eventReceiver.setPassword(apimgtConfigReaderService.getBamServerPassword());
        eventReceiver.setPort(Integer.parseInt(
                apimgtConfigReaderService.getBamServerThriftPort()));
        eventReceiver.setSocketTransportEnabled(true);
        return eventReceiver;
    }

}
