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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axis2.AxisFault;
import org.apache.axis2.databinding.utils.BeanUtil;
import org.jaxen.JaxenException;
import org.wso2.carbon.cep.core.XpathDefinition;
import org.wso2.carbon.cep.core.exception.CEPEventProcessingException;
import org.wso2.carbon.cep.core.internal.process.ReflectionBasedObjectSupplier;
import org.wso2.carbon.cep.core.mapping.property.XMLProperty;
import org.wso2.carbon.databridge.commons.Event;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XMLInputMapping extends InputMapping {

    private List<XpathDefinition> xpathDefinitionList;

    private List<XMLProperty> properties;


    public XMLInputMapping() {
        this.writeMethodMap = new HashMap<String, Method>();
        this.xpathDefinitionList = new ArrayList<XpathDefinition>();
        this.properties = new ArrayList<XMLProperty>();
        mappingClass=Map.class;
    }

    public void addXpathDefinition(XpathDefinition xpathDefinition) {
        xpathDefinitionList.add(xpathDefinition);
    }

    public void putWriteMethod(String name, Method writeMethod) {
        this.writeMethodMap.put(name, writeMethod);
    }

    public void addProperty(XMLProperty property) {
        this.properties.add(property);
    }

    public List<XpathDefinition> getXpathNamespacePrefixes() {
        return xpathDefinitionList;
    }

    @Override
    protected Map convertToEventMap(Object event) throws CEPEventProcessingException {
        Map<String, Object> mapEvent = new HashMap<String, Object>();
        for (XMLProperty property : this.properties) {
            Object propertyValue = getValue((OMElement) event,
                                            property.getXpath(),
                                            property.getType(),
                                            getXpathNamespacePrefixes());
            mapEvent.put(property.getName(), propertyValue);
        }
        return mapEvent;
    }

    @Override
    protected Object convertToEventObject(Object event, Object resultEvent)
            throws CEPEventProcessingException {
        for (XMLProperty property : this.properties) {
            Object propertyValue = getValue((OMElement) event,
                                            property.getXpath(),
                                            property.getType(),
                                            getXpathNamespacePrefixes());
            try {
                this.writeMethodMap.get(property.getName()).invoke(resultEvent, propertyValue);
            } catch (Exception e) {
                throw new CEPEventProcessingException("Cannot invoke " + property.getName() + " in Event class " + this.mappingClass.getName(), e);
            }
        }
        return resultEvent;
    }

    @Override
    protected Event convertToEventTuple(Object event) throws CEPEventProcessingException {
        Object[] eventData = new Object[properties.size()];


        for (int i = 0, properties1Size = properties.size(); i < properties1Size; i++) {
            XMLProperty property = properties.get(i);
            eventData[i] = getValue((OMElement) event,
                                    property.getXpath(),
                                    property.getType(),
                                    getXpathNamespacePrefixes());
        }
        Event tupleEvent = new Event();
        tupleEvent.setStreamId(this.getStream());
        tupleEvent.setTimeStamp(System.currentTimeMillis());
        ((Event) tupleEvent).setPayloadData(eventData);
        return (Event) tupleEvent;
    }

    public static Object getValue(OMElement eventOMElement,
                                  String xpathString,
                                  String type,
                                  List<XpathDefinition> namespacePrefixes)
            throws CEPEventProcessingException {

        try {
            AXIOMXPath xpath = new AXIOMXPath(xpathString);
            // here if user has given prefix namespace mappings
            // we use it. otherwise get from the om element existing
            // mappings
            if ((namespacePrefixes == null) || namespacePrefixes.isEmpty()) {
                xpath.addNamespaces(eventOMElement);
            } else {
                for (XpathDefinition xpathDefinition : namespacePrefixes) {
                    xpath.addNamespace(xpathDefinition.getPrefix(), xpathDefinition.getNamespace());
                }
            }
            // we always expect to return an omElement from the xpath expression
            OMElement omElementToUse = (OMElement) xpath.selectSingleNode(eventOMElement);
            Class beanClass = Class.forName(type);
            return BeanUtil.deserialize(beanClass,
                                        omElementToUse, new ReflectionBasedObjectSupplier(), null);
        } catch (ClassNotFoundException e) {
            throw new CEPEventProcessingException("Can not load the class " + type, e);
        } catch (AxisFault axisFault) {
            throw new CEPEventProcessingException("Error in parsing the omelement ", axisFault);
        } catch (JaxenException e) {
            throw new CEPEventProcessingException("Can not evaluate the xpath ", e);
        }
    }

    public List<XpathDefinition> getXpathDefinitionList() {
        return xpathDefinitionList;
    }

    public void setXpathDefinitionList(List<XpathDefinition> xpathDefinitionList) {
        this.xpathDefinitionList = xpathDefinitionList;
    }

    public List<XMLProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<XMLProperty> properties) {
        this.properties = properties;
    }


}
