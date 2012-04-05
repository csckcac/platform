package org.wso2.carbon.bam.agent.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.agent.conf.AgentConfiguration;
import org.wso2.carbon.bam.agent.publish.DataPublisher;
import org.wso2.carbon.bam.agent.publish.EventPublisher;
import org.wso2.carbon.bam.agent.publish.EventReceiver;
import org.wso2.carbon.bam.agent.queue.EventQueue;
import org.wso2.carbon.bam.agent.queue.EventReceiverComposite;
import org.wso2.carbon.bam.service.Event;

import java.util.List;

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
public class Agent implements DataAgent {

    private static Log log = LogFactory.getLog(Agent.class);

    private EventQueue eventQueue;
    private EventPublisher eventPublisher;

    public Agent() {
        this(new AgentConfiguration());
    }

    public Agent(AgentConfiguration agentConfiguration) {
       eventPublisher = new DataPublisher(agentConfiguration);
       eventQueue = new EventQueue(eventPublisher, agentConfiguration);
    }


    @Override
    public void publish(List<Event> events, EventReceiver eventReceiver) {
        eventQueue.enqueue(new EventReceiverComposite(events, eventReceiver));
    }

    @Override
    public void shutdown() {
        // shut down queue and worker threads first to finish publishing events
        eventQueue.shutdown();
        // shut down publisher and connection pool afterwards
        eventPublisher.shutdown();
    }

}
