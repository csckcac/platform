/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.rulecep.commons.utils;

import org.apache.axiom.om.*;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.llom.factory.OMXMLBuilderFactory;
import org.apache.axis2.databinding.typemapping.SimpleTypeMapper;
import org.apache.axis2.databinding.utils.BeanUtil;
import org.apache.axis2.description.java2wsdl.TypeTable;
import org.apache.axis2.util.StreamWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.wso2.carbon.rulecep.commons.LoggedRuntimeException;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Iterator;

/**
 * Contains helper methods related to the operations on OMElement such as create an OMElement
 * from different sources and vise visa
 */
@SuppressWarnings("unused")
public class OMElementHelper {

    private static final Log log = LogFactory.getLog(OMElementHelper.class);

    private static final OMElementHelper OM_ELEMENT_HELPER = new OMElementHelper();

    private OMElementHelper() {
    }

    public static OMElementHelper getInstance() {
        return OM_ELEMENT_HELPER;
    }

    /**
     * Converts an XML string into an OMElement
     *
     * @param xml String representation of the XML
     * @return <code>OMElement</code> instance
     */
    public OMElement toOM(String xml) {

        try {
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(
                    new StringReader(xml));
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            return builder.getDocumentElement();

        } catch (XMLStreamException e) {
            throw new LoggedRuntimeException("Error creating a OMElement from text : " + xml,
                    e, log);
        }
    }

    /**
     * Converts an XML inputStream into an OMElement
     *
     * @param inputStream the XML inputStream to be converted
     * @return <code>OMElement</code> instance
     */
    public OMElement toOM(InputStream inputStream) {

        try {
            XMLStreamReader reader =
                    XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            return builder.getDocumentElement();

        } catch (XMLStreamException e) {
            throw new LoggedRuntimeException("Error creating a OMElement from an input stream : ",
                    e, log);
        }
    }

    /**
     * Creates an OMElement wrapping the given binary data and wrapper
     *
     * @param binaryWrapper the XML inputStream to be converted
     * @param dataHandler   binary data
     * @return <code>OMElement</code> instance
     */
    public OMElement toOM(QName binaryWrapper, DataHandler dataHandler) {

        OMFactory omFactory = OMAbstractFactory.getOMFactory();
        OMElement omElement = omFactory
                .createOMElement(binaryWrapper);
        OMText text = omFactory.createOMText(dataHandler, true);
        omElement.addChild(text);
        return omElement;

    }

    /**
     * Converts  an OMElement into a DOM
     *
     * @param element OMElement instance to be converted into DOM
     * @return <code>OMElement</code> instance
     * @throws Exception for any errors during the conversion
     */
    public Element toDOM(OMElement element) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        element.serialize(baos);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder().parse(bais).getDocumentElement();
    }

    /**
     * Converts a POJO into an XML
     *
     * @param value     POJO to be converted
     * @param root      the parent tag's QName
     * @param typeTable defining types
     * @return XML for the given POJO
     */
    public OMElement convertToOM(Object value, QName root, TypeTable typeTable) {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        if (SimpleTypeMapper.isSimpleType(value)) {
            OMElement child = factory.createOMElement(root);
            child.addChild(factory.createOMText(child, SimpleTypeMapper.getStringValue(value)));
            return child;
        }
        XMLStreamReader reader = BeanUtil.getPullParser(
                value, root, typeTable, true, false);
        StreamWrapper parser = new StreamWrapper(reader);
        StAXOMBuilder stAXOMBuilder =
                OMXMLBuilderFactory.createStAXOMBuilder(
                        factory, parser);
        return stAXOMBuilder.getDocumentElement();
    }

    /**
     * Detaches the all children
     *
     * @param parent parent OMElement
     */
    public void detachChildren(OMElement parent) {
        Iterator children = parent.getChildren();
        while (children.hasNext()) {
            OMNode omNode = (OMNode) children.next();
            if (omNode != null) {
                omNode.detach();
            }
        }
    }
}
