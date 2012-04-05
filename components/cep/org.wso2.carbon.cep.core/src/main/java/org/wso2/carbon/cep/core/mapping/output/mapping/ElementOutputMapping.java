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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.databinding.typemapping.SimpleTypeMapper;
import org.apache.axis2.databinding.utils.BeanUtil;
import org.apache.axis2.util.StreamWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cep.core.exception.CEPEventProcessingException;
import org.wso2.carbon.cep.core.mapping.property.XMLProperty;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ElementOutputMapping extends OutputMapping {

    private static final Log log = LogFactory.getLog(ElementOutputMapping.class);
    /**
     * Document element of the element mapping
     */
    private String documentElement;

    /**
     * Namespace mapped for the document element
     */
    private String namespace;

    private List<XMLProperty> properties;

    public ElementOutputMapping() {
        properties = new ArrayList();
    }

     public Object convert(Object event) {

        //Create root element
        OMFactory omFactory = OMAbstractFactory.getOMFactory();
        OMNamespace omNamespace =
                omFactory.createOMNamespace(this.namespace, null);
        OMElement documentElement = omFactory.createOMElement(
                this.documentElement, omNamespace);
        for (XMLProperty property : this.properties) {
            try {

                if (property.getXmlFieldType().equals("element")) {
                    documentElement.addChild(
                            toOM(getPropertyValue(event, property.getName()).toString(),
                                 new QName(this.namespace, property.getName())));
                } else {
                    documentElement.addAttribute(toOMAttribute(getPropertyValue(event, property.getName()).toString(),
                                                               new QName(property.getName())));
                }
            } catch (CEPEventProcessingException e) {
                log.error("Error in read method with property name " + property.getName() + " from the output event " + event.getClass() + " to build the OM Element " + e);
                return null;
            }
        }

        return documentElement;
    }

     private static OMElement toOM(Object object, QName parentQname) {

        OMElement omElement = null;
        if (SimpleTypeMapper.isSimpleType(object)) {
            OMFactory omFactory = OMAbstractFactory.getOMFactory();
            OMNamespace omNamespace = omFactory.createOMNamespace(
                    parentQname.getNamespaceURI(), parentQname.getPrefix());
            omElement = omFactory.createOMElement(parentQname.getLocalPart(), omNamespace);
            omElement.addChild(omFactory.createOMText(SimpleTypeMapper.getStringValue(object)));
        } else {
            XMLStreamReader xmlStreamReader =
                    BeanUtil.getPullParser(object, parentQname, null, true, false);
            StAXOMBuilder stAXOMBuilder = new StAXOMBuilder(new StreamWrapper(xmlStreamReader));
            omElement = stAXOMBuilder.getDocumentElement();
        }
        return omElement;
    }

    private static OMAttribute toOMAttribute(Object object, QName parentQname) {
        OMAttribute omAttribute = null;
        if (SimpleTypeMapper.isSimpleType(object)) {
            OMFactory omFactory = OMAbstractFactory.getOMFactory();
            OMNamespace omNamespace = omFactory.createOMNamespace(
                    parentQname.getNamespaceURI(), parentQname.getPrefix());
            omAttribute = omFactory.createOMAttribute(parentQname.getLocalPart(),
                    omNamespace, SimpleTypeMapper.getStringValue(object));
        }
        return omAttribute;
    }

    public String getDocumentElement() {
        return documentElement;
    }

    public void setDocumentElement(String documentElement) {
        this.documentElement = documentElement;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public List<XMLProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<XMLProperty> properties) {
        this.properties = properties;
    }

    public void addProperty(XMLProperty property) {
        this.properties.add(property);
    }

}
