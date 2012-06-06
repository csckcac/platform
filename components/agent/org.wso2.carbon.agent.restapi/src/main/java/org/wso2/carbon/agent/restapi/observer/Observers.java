package org.wso2.carbon.agent.restapi.observer;

import org.wso2.carbon.agent.commons.Event;
import org.wso2.carbon.agent.commons.EventStreamDefinition;

import java.util.ArrayList;
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
public class Observers {

    private List<RestAPIObserver> observers = new ArrayList<RestAPIObserver>();

    public void addObserver(RestAPIObserver observer) {
        observers.add(observer);
    }

    public void defineEventStream(EventStreamDefinition eventStreamDefinition, String userName,
                            String password, String domainName) {
        for (RestAPIObserver observer : observers) {
            observer.defineEventStream(eventStreamDefinition, userName, password, domainName);
        }
    }

    public void receive(List<Event> eventList, String userName,
                 String password, String domainName) {
        for (RestAPIObserver observer : observers) {
            observer.receive(eventList, userName, password, domainName);
        }
    }
}
