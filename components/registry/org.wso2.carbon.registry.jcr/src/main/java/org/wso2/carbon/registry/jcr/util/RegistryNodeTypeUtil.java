/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.jcr.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.registry.jcr.RegistrySession;
import org.wso2.carbon.registry.jcr.nodetype.RegistryPropertyDefinitionTemplate;
import org.wso2.carbon.registry.jcr.util.nodetype.xml.NodeTypeReader;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.NodeTypeTemplate;
import javax.jcr.nodetype.PropertyDefinitionTemplate;
import javax.jcr.version.OnParentVersionAction;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class RegistryNodeTypeUtil {


//    public static void loadNodeTypesToJCRSystem(RegistrySession registrySession) {
//
//        Node nodeTypes, jcrSystem;
//        try {
//            jcrSystem = registrySession.getNode("/jcr:system");
//
//            if (jcrSystem.hasNode("jcr:nodeTypes")) {
//                nodeTypes = jcrSystem.getNode("jcr:nodeTypes");
//            } else {
//                nodeTypes = jcrSystem.addNode("jcr:nodeTypes", "nt:nodeType");
//            }
//        } catch (RepositoryException e) {
//
//        }
//    }

    public static PropertyDefinitionTemplate createJCRPrimaryTypeProperty(NodeTypeManager nodeTypeManager, String name) throws RepositoryException {
        PropertyDefinitionTemplate propertyDefinitionTemplate1 = nodeTypeManager.createPropertyDefinitionTemplate();
        propertyDefinitionTemplate1.setName("jcr:primaryType");
        propertyDefinitionTemplate1.setRequiredType(PropertyType.NAME);
        propertyDefinitionTemplate1.setDefaultValues(null);
        propertyDefinitionTemplate1.setAutoCreated(true);
        propertyDefinitionTemplate1.setMandatory(true);
        propertyDefinitionTemplate1.setOnParentVersion(OnParentVersionAction.COMPUTE);
        propertyDefinitionTemplate1.setProtected(true);
        propertyDefinitionTemplate1.setMultiple(false);
        ((RegistryPropertyDefinitionTemplate) propertyDefinitionTemplate1).setDeclaringNodeTypeName(name);
        return propertyDefinitionTemplate1;
    }

    public static void locadJCRBuiltInNodeTypesToSystemFromXML(NodeTypeManager nodeTypeManager) throws RepositoryException {

        String streamPath = System.getProperty("wso2.registry.nodetype.xml");

        OMElement processInfoElement;
        InputStream is = null;
        try {
            is = new FileInputStream(streamPath);
            XMLStreamReader reader = XMLInputFactory.newInstance().
                    createXMLStreamReader(is);
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            processInfoElement = builder.getDocumentElement();

            AXIOMXPath expression = new AXIOMXPath("/nodeTypes/nodeType");
            List attributes = expression.selectNodes(processInfoElement);

            for (Object o : attributes) {
                OMElement omNode = (OMElement) o;
                NodeTypeTemplate nodeTypeTemplate = new NodeTypeReader(nodeTypeManager).buildNodeType(omNode);
                nodeTypeManager.registerNodeType(nodeTypeTemplate, false); // allowUpdates - false
            }

            is.close();

        } catch (IOException e) {
            throw new RepositoryException("Exception occurred while reading from : "+streamPath);
        } catch (JaxenException e) {
            throw new RepositoryException("Exception occurred while reading from : "+streamPath);
        } catch (XMLStreamException e) {
            throw new RepositoryException("Exception occurred while reading from : "+streamPath);
        }
    }
    }
