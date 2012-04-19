/**
 *
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import org.wso2.carbon.agent.commons.Event;
import org.wso2.carbon.agent.commons.EventStreamDefinition;

import java.util.List;

/**
 * The call back that notifies Event arrivals and EventStreamDefinition declarations
 */
public interface AgentCallback {


    /**
     * will get called  when types are defined
     *
     * @param eventStreamDefinition   TypeDefinition of event streams
     * @param userName of the user defining the event stream definition
     * @param password of the user defining the event stream definition
     * @param domainName to which the event stream is defined
     */
    void definedEventStream(EventStreamDefinition eventStreamDefinition, String userName,
                            String password, String domainName);


    /**
     * will get called when Events arrive
     *
     * @param eventList Arrived event list
     * @param userName of the user sending the events
     * @param password of the user sending the events
     * @param domainName to which the events is sent
     */
    void receive(List<Event> eventList, String userName,
                 String password, String domainName);

}
