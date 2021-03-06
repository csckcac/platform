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


import com.google.gson.Gson;
import org.wso2.carbon.agent.commons.AttributeType;
import org.wso2.carbon.agent.commons.Event;
import org.wso2.carbon.agent.commons.thrift.data.ThriftEventBundle;
import org.wso2.carbon.agent.commons.utils.EventConverter;
import org.wso2.carbon.agent.server.exception.EventConversionException;
import org.wso2.carbon.agent.server.internal.EventStreamTypeHolder;

import java.util.LinkedList;
import java.util.List;

/**
 * the util class that converts Events and its definitions in to various forms
 */
public final class ThriftEventConverter {
    private static Gson gson = new Gson();

    private ThriftEventConverter() {

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
                        String stringValue = thriftEventBundle.getStringAttributeList().get(indexCounter.getStringCount());
                        if (stringValue.equals(EventConverter.nullString)) {
                            objects[i] = null;
                        } else {
                            objects[i] = stringValue;
                        }
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

    public static List<Event> createEventList(Object eventBundle,
                                              EventStreamTypeHolder eventStreamTypeHolder) {
        if (eventBundle instanceof ThriftEventBundle) {
            return createEventList((ThriftEventBundle) eventBundle, eventStreamTypeHolder);
        } else {
            throw new EventConversionException("Wrong type event relieved " + eventBundle.getClass());
        }

    }

    private static List<Event> createEventList(ThriftEventBundle thriftEventBundle,
                                               EventStreamTypeHolder eventStreamTypeHolder) {

        IndexCounter indexCounter = new IndexCounter();
        List<Event> eventList = new LinkedList<Event>();
        String streamId = null;
        try {
            for (int i = 0; i < thriftEventBundle.getEventNum(); i++) {
                Event event = new Event();
                streamId = thriftEventBundle.getStringAttributeList().get(indexCounter.getStringCount());
                indexCounter.incrementStringCount();
                event.setStreamId(streamId);
                long timeStamp = thriftEventBundle.getLongAttributeList().get(indexCounter.getLongCount());
                indexCounter.incrementLongCount();
                event.setTimeStamp(timeStamp);
                AttributeType[] metaAttributeTypeOrder = eventStreamTypeHolder.getMetaDataType(streamId);
                AttributeType[] correlationAttributeTypeOrder = eventStreamTypeHolder.getCorrelationDataType(streamId);
                AttributeType[] payloadAttributeTypeOrder = eventStreamTypeHolder.getPayloadDataType(streamId);
                event.setMetaData(ThriftEventConverter.toObjectArray(thriftEventBundle, metaAttributeTypeOrder, indexCounter));
                event.setCorrelationData(ThriftEventConverter.toObjectArray(thriftEventBundle, correlationAttributeTypeOrder, indexCounter));
                event.setPayloadData(ThriftEventConverter.toObjectArray(thriftEventBundle, payloadAttributeTypeOrder, indexCounter));
                eventList.add(event);

            }
        } catch (RuntimeException re) {
            throw new EventConversionException("Error when converting " + streamId + " of event bundle with events " + thriftEventBundle.getEventNum(), re);
        }
        return eventList;
    }

}
