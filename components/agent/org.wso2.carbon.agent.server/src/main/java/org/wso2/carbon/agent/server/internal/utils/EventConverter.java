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

package org.wso2.carbon.agent.server.internal.utils;


import org.wso2.carbon.agent.commons.Attribute;
import org.wso2.carbon.agent.commons.AttributeType;
import org.wso2.carbon.agent.commons.Event;
import org.wso2.carbon.agent.commons.thrift.data.ThriftEventBundle;
import org.wso2.carbon.agent.server.internal.StreamDefinitionHolder;

import java.util.LinkedList;
import java.util.List;

/**
 * the util class that converts thrift objects to common format
 */
public class EventConverter {

    public static AttributeType[] generateAttributeTypeArray(List<Attribute> attributes) {
        if (attributes != null) {
            AttributeType[] attributeTypes = new AttributeType[attributes.size()];
            for (int i = 0, metaDataSize = attributes.size(); i < metaDataSize; i++) {
                Attribute attribute = attributes.get(i);
                attributeTypes[i] = attribute.getType();
            }

            return attributeTypes;
        } else {
            return null;
        }
    }

    public static Object[] toObjectArray(ThriftEventBundle thriftEventBundle,
                                         AttributeType[] attributeTypeOrder,
                                         IndexCounter indexCounter) {
        if (attributeTypeOrder != null) {
            Object[] objects = new Object[attributeTypeOrder.length];
            for (int i = 0; i < attributeTypeOrder.length; i++) {
                switch (attributeTypeOrder[i]) {
                    case INT:
                        objects[i] = thriftEventBundle.getIntAttributeList().get(indexCounter.getIntCount());
                        indexCounter.incrementIntCount();
                        break;
                    case LONG:
                        objects[i] = thriftEventBundle.getLongAttributeList().get(indexCounter.getLongCount());
                        indexCounter.incrementLongCount();
                        break;
                    case STRING:
                        objects[i] = thriftEventBundle.getStringAttributeList().get(indexCounter.getStringCount());
                        indexCounter.incrementStringCount();
                        break;
                    case DOUBLE:
                        objects[i] = thriftEventBundle.getDoubleAttributeList().get(indexCounter.getDoubleCount());
                        indexCounter.incrementDoubleCount();
                        break;
                    case FLOAT:
                        objects[i] = thriftEventBundle.getDoubleAttributeList().get(indexCounter.getDoubleCount()).floatValue();
                        indexCounter.incrementDoubleCount();
                        break;
                    case BOOL:
                        objects[i] = thriftEventBundle.getBoolAttributeList().get(indexCounter.getBoolCount());
                        indexCounter.incrementBoolCount();
                        break;
                }
            }
            return objects;
        } else {
            return null;
        }
    }

    public static List<Event> createEventList(ThriftEventBundle thriftEventBundle,
                                              StreamDefinitionHolder streamDefinitionHolder) {

        IndexCounter indexCounter = new IndexCounter();
        List<Event> eventList = new LinkedList<Event>();
        for (int i = 0; i < thriftEventBundle.getEventNum(); i++) {
            Event event = new Event();
            String streamId = thriftEventBundle.getStringAttributeList().get(indexCounter.getStringCount());
            indexCounter.incrementStringCount();
            event.setStreamId(streamId);
            long timeStamp = thriftEventBundle.getLongAttributeList().get(indexCounter.getLongCount());
            indexCounter.incrementLongCount();
            event.setTimeStamp(timeStamp);
            AttributeType[] metaAttributeTypeOrder = streamDefinitionHolder.getMetaDataType(streamId);
            AttributeType[] correlationAttributeTypeOrder = streamDefinitionHolder.getCorrelationDataType(streamId);
            AttributeType[] payloadAttributeTypeOrder = streamDefinitionHolder.getPayloadDataType(streamId);
            event.setMetaData(EventConverter.toObjectArray(thriftEventBundle, metaAttributeTypeOrder, indexCounter));
            event.setCorrelationData(EventConverter.toObjectArray(thriftEventBundle, correlationAttributeTypeOrder, indexCounter));
            event.setPayloadData(EventConverter.toObjectArray(thriftEventBundle, payloadAttributeTypeOrder, indexCounter));
            eventList.add(event);
        }
        return eventList;
    }
}
