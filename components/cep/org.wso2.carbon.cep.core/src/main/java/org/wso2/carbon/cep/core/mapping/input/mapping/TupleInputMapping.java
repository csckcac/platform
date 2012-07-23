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
import org.wso2.carbon.cep.core.internal.util.CEPConstants;
import org.wso2.carbon.cep.core.mapping.property.TupleProperty;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.Event;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TupleInputMapping extends InputMapping {


    private List<TupleProperty> properties;
    private int[][] propertyPositions = null; //int[meta=0,correlation=1,payload=2][position]

    private Map<String, Method> writeMethodMap;

    public TupleInputMapping() {
        this.writeMethodMap = new HashMap<String, Method>();
        this.properties = new ArrayList<TupleProperty>();
        mappingClass=Event.class;
    }


    public void putWriteMethod(String name, Method writeMethod) {
        this.writeMethodMap.put(name, writeMethod);
    }

    public void addProperty(TupleProperty property) {
        this.properties.add(property);
    }

    @Override
    protected Map convertToEventMap(Object event) {
        Map<String, Object> mapEvent = new HashMap<String, Object>();
        for (int i = 0, size = properties.size(); i < size; i++) {
            mapEvent.put(properties.get(i).getName(), getValue((Event) event, i));
        }
        return mapEvent;
    }

    @Override
    protected Object convertToEventObject(Object event, Object resultEvent)
            throws CEPEventProcessingException {

        for (int i = 0, size = properties.size(); i < size; i++) {
            Object propertyValue = getValue((Event) event, i);
            try {
                this.writeMethodMap.get(properties.get(i).getName()).invoke(resultEvent, propertyValue);
            } catch (Exception e) {
                throw new CEPEventProcessingException("Cannot invoke " + properties.get(i).getName() +
                                                      " in Event class " + this.mappingClass.getName(), e);
            }
        }
        return resultEvent;
    }

    @Override
    protected Event convertToEventTuple(Object event) {
        if (propertyPositions == null) {
            initPropertyPositions();
        }
        int propertySize = properties.size();
        Object[] eventData = new Object[propertySize];

        for (int i = 0; i < propertySize; i++) {
            eventData[i] = getValue((Event) event, i);
        }
        if(((Event)event).getTimeStamp()==0){
            ((Event)event).setTimeStamp(System.currentTimeMillis());
        }
        ((Event) event).setMetaData(null);
        ((Event) event).setCorrelationData(null);
        ((Event) event).setPayloadData(eventData);
        return (Event) event;
    }

    public Object getValue(Event event, int i) {
        if (propertyPositions == null) {
            initPropertyPositions();
        }
        int[] position = propertyPositions[i];
        switch (position[0]) {
            case 0:
                return event.getMetaData()[position[1]];
            case 1:
                return event.getCorrelationData()[position[1]];
            case 2:
                return event.getPayloadData()[position[1]];
        }
        return null;
    }

    private void initPropertyPositions() {
        propertyPositions = new int[properties.size()][2];
        for (int i = 0, propertySize = properties.size(); i < propertySize; i++) {
            TupleProperty property = properties.get(i);
            if (property.getDataType().equals(CEPConstants.CEP_CONF_ELE_TUPLE_DATA_TYPE_PAYLOAD)) {
                List<Attribute> payloadData = eventStreamDefinition.getPayloadData();
                propertyPositions[i][0] = 2;
                propertyPositions[i][1] = getPosition(property, payloadData);
            } else if (property.getDataType().equals(CEPConstants.CEP_CONF_ELE_TUPLE_DATA_TYPE_META)) {
                List<Attribute> metaData = eventStreamDefinition.getMetaData();
                propertyPositions[i][0] = 0;
                propertyPositions[i][1] = getPosition(property, metaData);
            } else if (property.getDataType().equals(CEPConstants.CEP_CONF_ELE_TUPLE_DATA_TYPE_CORRELATION)) {
                List<Attribute> correlationData = eventStreamDefinition.getCorrelationData();
                propertyPositions[i][0] = 1;
                propertyPositions[i][1] = getPosition(property, correlationData);
            }
        }
    }

    private int getPosition(TupleProperty property, List<Attribute> payloadData) {

        for (int i = 0, payloadDataSize = payloadData.size(); i < payloadDataSize; i++) {
            Attribute attribute = payloadData.get(i);
            if (attribute.getName().equals(property.getName())) {
                return i;
            }
        }
        return -1;
    }

    public List<TupleProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<TupleProperty> properties) {
        this.properties = properties;
    }


}
