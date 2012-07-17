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
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public abstract class InputMapping {

    protected String stream;

    protected Class mappingClass ;

    protected Map<String, Method> writeMethodMap;
    protected StreamDefinition  eventStreamDefinition;

    protected InputMapping() {
        this.writeMethodMap = new HashMap<String, Method>();
    }

    public Object convert(Object event) throws CEPEventProcessingException {
        if (this.mappingClass != null) {
            if (mappingClass == Event.class) {
                return convertToEventTuple(event);
            } else if (mappingClass == Map.class) {
                return convertToEventMap(event);
            } else {
                try {
                    return convertToEventObject(event, this.mappingClass.newInstance());
                } catch (Exception e) {
                    throw new CEPEventProcessingException("Event class " + this.mappingClass +
                                                          " cannot be initialised", e);
                }
            }
        } else {
            return convertToEventMap(event);
        }
    }

    protected abstract Map convertToEventMap(Object event) throws CEPEventProcessingException;

    protected abstract Object convertToEventObject(Object event, Object resultEvent)
            throws CEPEventProcessingException;

    protected abstract Event convertToEventTuple(Object event) throws CEPEventProcessingException;

    public String getStream() {
        return stream;
    }

    public void setStream(String stream) {
        this.stream = stream;
    }

    public Class getMappingClass() {
        return mappingClass;
    }

    public void setMappingClass(Class mappingClass) {
        this.mappingClass = mappingClass;
    }

    public Map<String, Method> getWriteMethodMap() {
        return writeMethodMap;
    }

    public void setWriteMethodMap(Map<String, Method> writeMethodMap) {
        this.writeMethodMap = writeMethodMap;
    }

    public void setEventDefinition(Object eventDef) {
        eventStreamDefinition =(StreamDefinition)eventDef;
    }
}
