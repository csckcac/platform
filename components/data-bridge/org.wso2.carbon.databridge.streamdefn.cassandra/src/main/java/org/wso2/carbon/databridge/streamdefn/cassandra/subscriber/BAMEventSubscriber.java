package org.wso2.carbon.databridge.streamdefn.cassandra.subscriber;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.Credentials;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.core.AgentCallback;
import org.wso2.carbon.databridge.streamdefn.cassandra.datastore.ClusterFactory;
import org.wso2.carbon.databridge.streamdefn.cassandra.internal.util.ServiceHolder;

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
public class BAMEventSubscriber implements AgentCallback {

    private static Log log = LogFactory.getLog(BAMEventSubscriber.class);

    @Override
    public void definedStream(StreamDefinition streamDefinition, Credentials credentials) {
        // this is not needed for BAM
    }

    @Override
    public void receive(List<Event> eventList, Credentials credentials) {
        try {
            ServiceHolder.getCassandraConnector().insertEventList(ClusterFactory.getCluster(credentials), eventList);
        } catch (Exception e) {
            String errorMsg = "Error processing event. ";
            log.error(errorMsg, e);
        }

//        for (Event event : eventList) {
//            try {
//                ServiceHolder.getCassandraConnector().insertEvent(ClusterFactory.getCluster(credentials), event);
//            } catch (Exception e) {
//                String errorMsg = "Error processing event. " + event.toString();
//                log.error(errorMsg, e);
//            }
//
//        }
    }
}
