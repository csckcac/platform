package org.wso2.carbon.eventbridge.core.subscriber;


import org.wso2.carbon.eventbridge.core.beans.Event;
import org.wso2.carbon.eventbridge.core.beans.EventStreamDefinition;
import org.wso2.carbon.eventbridge.core.exceptions.DifferentStreamDefinitionAlreadyDefinedException;

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
public interface EventSubscriber {


       public void saveStreamDefinition(String domainName,
                                        EventStreamDefinition eventStreamDefinition, String username, String password)
               throws DifferentStreamDefinitionAlreadyDefinedException;


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
