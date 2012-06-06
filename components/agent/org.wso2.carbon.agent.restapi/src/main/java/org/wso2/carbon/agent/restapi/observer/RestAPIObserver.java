package org.wso2.carbon.agent.restapi.observer;

import org.wso2.carbon.agent.commons.Event;
import org.wso2.carbon.agent.commons.EventStreamDefinition;

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

/**
 * This  call back will be used for aliases for the rest app, event stream definitions and events
 */
public interface RestAPIObserver {

    /**
     * will get called  when types are defined
     *
     * @param eventStreamDefinition   TypeDefinition of event streams
     * @param userName of the user defining the event stream definition
     * @param password of the user defining the event stream definition
     * @param domainName to which the event stream is defined
     */
    public void defineEventStream(EventStreamDefinition eventStreamDefinition, String userName,
                            String password, String domainName);


    /**
     * will get called when Events arrive
     *
     * @param eventList Arrived event list
     * @param userName of the user sending the events
     * @param password of the user sending the events
     * @param domainName to which the events is sent
     */
    public void receive(List<Event> eventList, String userName,
                 String password, String domainName);


}
