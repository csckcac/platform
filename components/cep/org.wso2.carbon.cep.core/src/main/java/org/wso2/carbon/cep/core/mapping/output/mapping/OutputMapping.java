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

package org.wso2.carbon.cep.core.mapping.output.mapping;

import org.wso2.carbon.cep.core.exception.CEPEventProcessingException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class OutputMapping {

    //For object Events
    protected Map<Class, Map<String, Method>> methodCache;

    //For tuple Events
    protected Map<String, StreamDefinition> typeDefMap;
    protected Map<String, Map<String, Integer>> typeDefCache;

    protected OutputMapping() {
        typeDefMap = new HashMap<String, StreamDefinition>();
        methodCache = new HashMap<Class, Map<String, Method>>();
        typeDefCache = new HashMap<String, Map<String, Integer>>();
    }

    public abstract Object convert(Object event);

    protected Object getPropertyValue(Object event, String property)
            throws CEPEventProcessingException {


        if (event instanceof Map) {
            return ((Map) event).get(property);
        } else if (event instanceof Event) {
            Map<String, Integer> positions = typeDefCache.get(((Event) event).getStreamId());
            if (positions == null) {
                List<org.wso2.carbon.databridge.commons.Attribute> attributes = typeDefMap.get(((Event) event).getStreamId()).getPayloadData();
                positions = new HashMap<String, Integer>();
                for (int i = 0, attributesSize = attributes.size(); i < attributesSize; i++) {
                    positions.put(attributes.get(i).getName(), i);

                }
                typeDefCache.put(((Event) event).getStreamId(), positions);
            }
            return ((Event) event).getPayloadData()[positions.get(property)];
        } else {
            Map<String, Method> methodMap = methodCache.get(event.getClass());
            if (methodMap == null) {
                methodMap = new HashMap<String, Method>();
                methodCache.put(event.getClass(), methodMap);
            }
            Method propertyMethod = methodMap.get(property);
            if (propertyMethod == null) {
                BeanInfo beanInfo = null;
                try {
                    beanInfo = Introspector.getBeanInfo(event.getClass());
                } catch (IntrospectionException e) {
                    throw new CEPEventProcessingException("Cannot get the BeanInfo for class " + event.getClass(), e);
                }
                for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
                    if (property.equals(pd.getName())) {
                        propertyMethod = pd.getReadMethod();
                        methodMap.put(property, propertyMethod);
                        break;
                    }

                }
            }
            if (propertyMethod == null) {
                throw new CEPEventProcessingException("the class " + event.getClass()
                                                      + " does not contain a read method with description " + property);
            }
            try {
                return propertyMethod.invoke(event);
            } catch (Exception e) {
                throw new CEPEventProcessingException("Cannot invoke read method with description "
                                                      + property + " in class " + event.getClass());
            }
        }
    }

    protected Object getPropertyValue(Object event, int position) {
        return ((Event) event).getPayloadData()[position];
    }

    public Map<Class, Map<String, Method>> getMethodCache() {
        return methodCache;
    }

    public void setMethodCache(Map<Class, Map<String, Method>> methodCache) {
        this.methodCache = methodCache;
    }

    public void defineStream(StreamDefinition eventStreamDefinition) {
        typeDefMap.put(eventStreamDefinition.getName(), eventStreamDefinition);
    }

}
