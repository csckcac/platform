package org.wso2.carbon.agent.restapi.observer;

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

/**
 * this class represents as the interface between the agent server and agent
 * server implementations.
 */
public interface RestAPIServer {

    /**
     * Gets the alias of where the app should be deployed
     * @return alias
     */
    public String getAlias();

    /**
     * CEP/BAM can subscribe for Event Streams
     *
     * @param agentCallback callbacks of the subscribers
     */
//    void subscribe(AgentCallback agentCallback);
//
//    /**
//     * To get the stream definition
//     *
//     * @param domainName    the domain name
//     * @param streamName    the stream name
//     * @param streamVersion the stream version
//     * @return Event Stream Definition
//     */
//    EventStreamDefinition getStreamDefinition(String domainName, String streamName,
//                                              String streamVersion)
//            throws StreamDefinitionNotFoundException;
//
//    /**
//     * To get the stream definition
//     *
//     * @param domainName the domain name
//     * @param streamId   the stream id
//     * @return Event Stream Definition
//     */
//    EventStreamDefinition getStreamDefinition(String domainName, String streamId)
//            throws StreamDefinitionNotFoundException;
//
//    /**
//     * To get all stream definitions
//     *
//     * @param domainName the domain name
//     * @return list of Event Stream Definitions
//     * @throws StreamDefinitionNotFoundException
//     *
//     */
//    List<EventStreamDefinition> getAllStreamDefinition(String domainName)
//            throws StreamDefinitionNotFoundException;
//
//    /**
//     * Save new Event Stream Definitions
//     * @param domainName
//     * @param eventStreamDefinition
//     */
//    void saveEventStreamDefinition(String domainName, String eventStreamDefinition)
//            throws MalformedStreamDefinitionException,
//            DifferentStreamDefinitionAlreadyDefinedException;

}
