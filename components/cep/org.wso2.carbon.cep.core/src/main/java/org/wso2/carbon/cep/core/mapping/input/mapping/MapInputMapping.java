/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.cep.core.mapping.input.mapping;

import org.wso2.carbon.cep.core.exception.CEPEventProcessingException;
import org.wso2.carbon.cep.core.mapping.property.MapProperty;
import org.wso2.carbon.databridge.commons.Event;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapInputMapping extends InputMapping {

    private List<MapProperty> properties;

    private Map<String, Method> writeMethodMap;

    public MapInputMapping() {
        this.writeMethodMap = new HashMap<String, Method>();
        this.properties = new ArrayList<MapProperty>();
        mappingClass = Event.class;
    }


    public void putWriteMethod(String name, Method writeMethod) {
        this.writeMethodMap.put(name, writeMethod);
    }

    public void addProperty(MapProperty property) {
        this.properties.add(property);
    }

    @Override
    protected Map convertToEventMap(Object event) {
        Map<String, Object> mapEvent = new HashMap<String, Object>();
        for (MapProperty property : properties) {
            mapEvent.put(property.getName(), ((Map) event).get(property.getName()));
        }
        return mapEvent;
    }

    @Override
    protected Object convertToEventObject(Object event, Object resultEvent)
            throws CEPEventProcessingException {
        for (MapProperty property : properties) {
            Object propertyValue = ((Map) event).get(property.getName());
            try {
                this.writeMethodMap.get(property.getName()).invoke(resultEvent, propertyValue);
            } catch (Exception e) {
                throw new CEPEventProcessingException("Cannot invoke " + property.getName() +
                                                      " in Event class " + this.mappingClass.getName(), e);
            }
        }
        return resultEvent;
    }

    @Override
    protected Event convertToEventTuple(Object event) {
        int propertySize = properties.size();
        Object[] eventData = new Object[propertySize];

        for (int i = 0, propertiesSize = properties.size(); i < propertiesSize; i++) {
            eventData[i] = ((Map) event).get(properties.get(i).getName());
        }

        Event tupleEvent =new Event();
        tupleEvent.setTimeStamp(System.currentTimeMillis());
        tupleEvent.setMetaData(null);
        tupleEvent.setCorrelationData(null);
        tupleEvent.setPayloadData(eventData);
        return tupleEvent;
    }


    public List<MapProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<MapProperty> properties) {
        this.properties = properties;
    }


}
