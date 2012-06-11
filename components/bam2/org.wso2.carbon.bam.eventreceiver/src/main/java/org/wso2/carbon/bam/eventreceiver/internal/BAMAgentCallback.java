/*
 * Copyright 2012 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.bam.eventreceiver.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.agent.commons.Event;
import org.wso2.carbon.agent.commons.EventStreamDefinition;
import org.wso2.carbon.agent.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.agent.server.AgentCallback;
import org.wso2.carbon.bam.eventreceiver.datastore.CassandraConnector;

import java.util.List;


public class BAMAgentCallback implements AgentCallback {
    private static Log log = LogFactory.getLog(BAMAgentCallback.class);

   private CassandraConnector cassandraConnector;
    private static final String WSO2_CARBON_STAND_ALONE = "WSO2-CARBON-STAND-ALONE";

    public BAMAgentCallback(){
        cassandraConnector = new CassandraConnector();

    }

    @Override
    public void definedEventStream(EventStreamDefinition eventStreamDefinition, String userName, String userPassword, String domainName) {

        if(domainName == null){
            domainName = WSO2_CARBON_STAND_ALONE;
        }
        cassandraConnector.insertEventDefinition(userName,userPassword,eventStreamDefinition);
        cassandraConnector.createEventStreamColumnFamily(domainName, eventStreamDefinition.getName(), userName, userPassword);
    }

    @Override
    public void receive(List<Event> events, String userName, String userPassword, String domainName) {
        for(Event event:events){
            try {
                cassandraConnector.insertEvent(event,userName,userPassword, domainName);
            } catch (MalformedStreamDefinitionException e) {
                e.printStackTrace();
            }
        }
    }
}
