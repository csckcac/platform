/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.wso2.carbon.agent.server;

import org.wso2.carbon.agent.commons.EventStreamDefinition;
import org.wso2.carbon.agent.server.exception.StreamDefinitionNotFoundException;

import java.util.List;

/**
 * this class represents the interface between the agent server and wso2 agent
 * server implementations.
 */
public interface AgentServer {

    /**
     * CEP/BAM can subscribe for Event Streams
     *
     * @param agentCallback callbacks of the subscribers
     */
    void subscribe(AgentCallback agentCallback);

    /**
     * To get the stream definition
     *
     * @param domainName    the domain name
     * @param streamName    the stream name
     * @param streamVersion the stream version
     * @return Event Stream Definition
     */
    EventStreamDefinition getStreamDefinition(String domainName, String streamName,
                                              String streamVersion)
            throws StreamDefinitionNotFoundException;

    /**
     * To get the stream definition
     *
     * @param domainName the domain name
     * @param streamId   the stream id
     * @return Event Stream Definition
     */
    EventStreamDefinition getStreamDefinition(String domainName, String streamId)
            throws StreamDefinitionNotFoundException;

    /**
     * To get all stream definitions
     *
     * @param domainName the domain name
     * @return list of Event Stream Definitions
     * @throws StreamDefinitionNotFoundException
     *
     */
    List<EventStreamDefinition> getAllStreamDefinition(String domainName)
            throws StreamDefinitionNotFoundException;

}

