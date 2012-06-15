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
package org.wso2.carbon.eventbridge.core;

import org.wso2.carbon.agent.commons.Credentials;
import org.wso2.carbon.agent.commons.EventStreamDefinition;
import org.wso2.carbon.agent.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.agent.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.eventbridge.core.exception.StreamDefinitionNotFoundException;

import java.util.List;

public interface EventBridgeSubscriberService {


    /**
     * CEP/BAM can subscribe for Event Streams
     *
     * @param agentCallback callbacks of the subscribers
     */
    public void subscribe(AgentCallback agentCallback);

    public EventStreamDefinition getStreamDefinition(Credentials credentials, String streamName,
                                                     String streamVersion)
            throws StreamDefinitionNotFoundException;

    public EventStreamDefinition getStreamDefinition(Credentials credentials, String streamId)
            throws StreamDefinitionNotFoundException;

    public List<EventStreamDefinition> getAllStreamDefinition(Credentials credentials)
            throws StreamDefinitionNotFoundException;

    public void saveEventStreamDefinition(Credentials credentials, String eventStreamDefinition)
            throws MalformedStreamDefinitionException,
                   DifferentStreamDefinitionAlreadyDefinedException;

    public String getStreamId(Credentials credentials, String streamName, String streamVersion)
            throws StreamDefinitionNotFoundException;

    public List<AgentCallback> getSubscribers();
}
