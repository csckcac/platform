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

package org.wso2.carbon.agent.server.internal;


import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.agent.commons.Attribute;
import org.wso2.carbon.agent.commons.AttributeType;
import org.wso2.carbon.agent.commons.TypeDef;
import org.wso2.carbon.agent.commons.thrift.data.ThriftEventBundle;
import org.wso2.carbon.agent.commons.thrift.exception.ThriftDifferentTypeDefinitionAlreadyDefinedException;
import org.wso2.carbon.agent.server.AgentCallback;
import org.wso2.carbon.agent.server.internal.authentication.session.AgentSession;
import org.wso2.carbon.agent.server.internal.queue.EventQueue;
import org.wso2.carbon.agent.server.internal.utils.EventComposite;
import org.wso2.carbon.agent.server.internal.utils.EventConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventDispatcher {


    private static final Log log = LogFactory.getLog(EventDispatcher.class);

    List<AgentCallback> subscribers = new ArrayList<AgentCallback>();
    Map<String, StreamDefinitionHolder> streamDefinitionHolderMap = new HashMap<String, StreamDefinitionHolder>();
    EventQueue eventQueue;
    Gson gson = new Gson();

    public EventDispatcher() {
        this.eventQueue = new EventQueue(subscribers);
    }

    public void addCallback(AgentCallback agentCallback) {
        subscribers.add(agentCallback);
    }

    public String defineType(String streamDefinition, AgentSession agentSession)
            throws ThriftDifferentTypeDefinitionAlreadyDefinedException {
        TypeDef typeDef = gson.fromJson(streamDefinition.replaceAll("(?i)int", "INT").replaceAll("(?i)long", "LONG").
                replaceAll("(?i)float", "FLOAT").replaceAll("(?i)double", "DOUBLE").
                replaceAll("(?i)bool", "BOOL").replaceAll("(?i)string", "STRING"), TypeDef.class);
        if (streamDefinitionHolderMap.containsKey(agentSession.getDomainName()) && streamDefinitionHolderMap.get(agentSession.getDomainName()).getMetaDataTypeMap().containsKey(typeDef.getStreamId())) {
            StreamDefinitionHolder streamDefinitionHolder = streamDefinitionHolderMap.get(agentSession.getDomainName());
            checkAttributes(typeDef.getMetaData(), streamDefinitionHolder.getMetaDataTypeMap().get(typeDef.getStreamId()), "Meta Data");
            checkAttributes(typeDef.getCorrelationData(), streamDefinitionHolder.getCorrelationDataTypeMap().get(typeDef.getStreamId()), "Correlation Data");
            checkAttributes(typeDef.getPayloadData(), streamDefinitionHolder.getPayloadDataTypeMap().get(typeDef.getStreamId()), "Payload Data");
        } else {
            StreamDefinitionHolder streamDefinitionHolder = new StreamDefinitionHolder(agentSession.getDomainName());
            streamDefinitionHolder.setMetaDataType(typeDef.getStreamId(), EventConverter.generateAttributeTypeArray(typeDef.getMetaData()));
            streamDefinitionHolder.setCorrelationDataType(typeDef.getStreamId(), EventConverter.generateAttributeTypeArray(typeDef.getCorrelationData()));
            streamDefinitionHolder.setPayloadDataType(typeDef.getStreamId(), EventConverter.generateAttributeTypeArray(typeDef.getPayloadData()));
            streamDefinitionHolderMap.put(agentSession.getDomainName(), streamDefinitionHolder);
        }
        for (AgentCallback agentCallback : subscribers) {
            agentCallback.definedType(typeDef, agentSession.getSessionId());
        }
        return typeDef.getStreamId();
    }

    private void checkAttributes(List<Attribute> attributeList, AttributeType[] attributeTypes,
                                 String dataType)
            throws ThriftDifferentTypeDefinitionAlreadyDefinedException {
        if (attributeList == null && attributeTypes == null) {    //todo add old and new formats in json
            return;
        }
        if (attributeList == null) {
            throw new ThriftDifferentTypeDefinitionAlreadyDefinedException(dataType + " of the defining stream is null");
        }
        if (attributeTypes == null) {
            throw new ThriftDifferentTypeDefinitionAlreadyDefinedException(dataType + " of the existing stream is null");
        }
        if (attributeTypes.length != attributeList.size()) {
            throw new ThriftDifferentTypeDefinitionAlreadyDefinedException(dataType + " of the existing stream length (" + attributeList.size() + "), and defining stream length (" + attributeTypes.length + ") are not equal");
        }
        for (int i = 0, getLength = attributeTypes.length; i < getLength; i++) {
            if (!attributeTypes[i].equals(attributeList.get(i).getType())) {
                throw new ThriftDifferentTypeDefinitionAlreadyDefinedException(dataType + " definition of Attribute No. " + i + " (" + attributeTypes[i] + ") not match with the existing one (" + attributeList.get(i).getType() + ")");
            }

        }
    }

    public void publish(ThriftEventBundle thriftEventBundle, AgentSession agentSession) {
        eventQueue.publish(new EventComposite(thriftEventBundle, streamDefinitionHolderMap.get(agentSession.getDomainName())));
    }

    public List<AgentCallback> getSubscribers() {
        return subscribers;
    }
}
