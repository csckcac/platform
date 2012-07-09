/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.automation.core.utils.environmentutils;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.llom.OMElementImpl;
import org.wso2.carbon.automation.core.utils.frameworkutils.productvariables.ProductVariables;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ClusterReader {
    public String getProductName(String id) {
        String productName = null;
        DataHandler handler;
        XMLStreamReader parser = null;
        try {
            URL clusterXmlURL = ClusterReader.class.getResource("/clusters.xml");
            handler = new DataHandler(clusterXmlURL);
            parser = XMLInputFactory.newInstance().createXMLStreamReader(handler.getInputStream());
        } catch (XMLStreamException e) {

        } catch (IOException e) {

        }
        StAXOMBuilder builder = new StAXOMBuilder(parser);
        OMElement endPointElem = builder.getDocumentElement();
        OMNode node;
        Iterator children = endPointElem.getChildElements();
        while (children.hasNext()) {
            node = (OMNode) children.next();
            String product = ((OMElementImpl) node).getLocalName();
            Iterator loadBalanceIterator = ((OMElementImpl) node).getChildElements();
            while (loadBalanceIterator.hasNext()) {
                OMNode node2 = (OMNode) loadBalanceIterator.next();
                String pid = ((OMElementImpl) node2).getAttribute(new QName("id")).getAttributeValue();
                if (id.equals(pid)) {
                    productName = product;
                }
            }
        }
        return productName;
    }

    public ProductVariables getNodeProperties(String custerId) {
        String host = null;
        String httpPort = null;
        String httpsPort = null;
        String nhttpPort = null;
        String webcontextRoot = null;
        String nhttpsPort = null;
        String qpidPort = null;
        ProductVariables nodeVariables = null;
        DataHandler handler = null;
        XMLStreamReader parser = null;
        try {
            URL clusterXmlURL = ClusterReader.class.getResource("/clusters.xml");
            handler = new DataHandler(clusterXmlURL);
            parser = XMLInputFactory.newInstance().createXMLStreamReader(handler.getInputStream());
        } catch (XMLStreamException e) {

        } catch (IOException e) {

        }


        StAXOMBuilder builder = new StAXOMBuilder(parser);
        OMElement endPointElem = builder.getDocumentElement();
        OMNode node;
        Iterator children = endPointElem.getChildElements();
        while (children.hasNext()) {
            node = (OMNode) children.next();
            Iterator loadBalanceIterator = ((OMElementImpl) node).getChildElements();
            while (loadBalanceIterator.hasNext()) {
                OMNode node2 = (OMNode) loadBalanceIterator.next();
                String pid = ((OMElementImpl) node2).getAttribute(new QName("id")).getAttributeValue();
                if (custerId.equals(pid)) {
                    Iterator param = ((OMElementImpl) node2).getChildElements();
                    while (param.hasNext()) {
                        OMNode node3 = (OMNode) param.next();
                        String attrib = ((OMElementImpl) node3).getLocalName();
                        if (attrib.equals("host")) {
                            host = ((OMElementImpl) node3).getText();
                        } else if (attrib.equals("httpport")) {
                            httpPort = ((OMElementImpl) node3).getText();
                        } else if (attrib.equals("httpsport")) {
                            httpsPort = ((OMElementImpl) node3).getText();
                        } else if (attrib.equals("nhttpport")) {
                            nhttpPort = ((OMElementImpl) node3).getText();
                        } else if (attrib.equals("nhttpsport")) {
                            nhttpsPort = ((OMElementImpl) node3).getText();
                        } else if (attrib.equals("qpidport")) {
                            qpidPort = ((OMElementImpl) node3).getText();
                        }
                    }

                }
            }
        }
        nodeVariables = setProductVariables(host, httpPort, httpsPort,
                                            webcontextRoot, nhttpPort, nhttpsPort, qpidPort);
        return nodeVariables;
    }

    private ProductVariables setProductVariables(String host, String httpPort, String httpsPort,
                                                 String webcontextRoot, String nhttpPort,
                                                 String nhttpsPort, String qpidPort) {
        ProductVariables productVariable = new ProductVariables();
        ProductUrlGeneratorUtil urlGeneratorUtil = new ProductUrlGeneratorUtil();
        String backendUrl = urlGeneratorUtil.getBackendUrl(httpsPort, host, webcontextRoot);
        if (nhttpPort != null && nhttpsPort != null && qpidPort == null) {
            productVariable.setProductVariables(host, httpPort, httpsPort, webcontextRoot,
                                                nhttpPort, nhttpsPort, backendUrl);
        } else if (nhttpPort != null && nhttpsPort != null && qpidPort != null) {
            productVariable.setProductVariables(host, httpPort, httpsPort, webcontextRoot,
                                                nhttpPort, nhttpsPort, qpidPort, backendUrl);
        } else if (nhttpPort == null && nhttpsPort == null && qpidPort != null) {
            productVariable.setProductVariables(host, httpPort, httpsPort, webcontextRoot,
                                                qpidPort, backendUrl);
        } else {
            productVariable.setProductVariables(host, httpPort, httpsPort, webcontextRoot,
                                                backendUrl);
        }
        return productVariable;
    }

    public String getActiveClusterNode(String product) {
        DataHandler handler = null;
        XMLStreamReader parser = null;
        try {
            URL clusterXmlURL = ClusterReader.class.getResource("/clusters.xml");
            handler = new DataHandler(clusterXmlURL);
            parser = XMLInputFactory.newInstance().createXMLStreamReader(handler.getInputStream());
        } catch (XMLStreamException e) {

        } catch (IOException e) {

        }
        StAXOMBuilder builder = new StAXOMBuilder(parser);
        OMElement endPointElem = builder.getDocumentElement();
        OMNode node;
        Iterator children = endPointElem.getChildElements();
        String pid = null;
        while (children.hasNext()) {
            node = (OMNode) children.next();
            String clsProduct = ((OMElementImpl) node).getLocalName();
            if (clsProduct.equalsIgnoreCase(product)) {
                Iterator loadbalanceIterator = ((OMElementImpl) node).getChildElements();
                while (loadbalanceIterator.hasNext()) {
                    OMNode node2 = (OMNode) loadbalanceIterator.next();
                    if (((OMElementImpl) node2).getAttribute(new QName("state")).getAttributeValue().equals("active")) {
                        pid = ((OMElementImpl) node2).getAttribute(new QName("id")).getAttributeValue();
                    }
                }
            }
        }
        return pid;
    }

    public List<String> getClusterList() {
        List<String> clusterList = new ArrayList<String>();
        DataHandler handler = null;
        XMLStreamReader parser = null;
        try {
            URL clusterXmlURL = ClusterReader.class.getResource("/clusters.xml");
            handler = new DataHandler(clusterXmlURL);
            parser = XMLInputFactory.newInstance().createXMLStreamReader(handler.getInputStream());
        } catch (XMLStreamException e) {

        } catch (IOException e) {

        }
        StAXOMBuilder builder = new StAXOMBuilder(parser);
        OMElement endPointElem = builder.getDocumentElement();
        OMNode node;
        Iterator children = endPointElem.getChildElements();
        String pid = null;
        while (children.hasNext()) {
            node = (OMNode) children.next();
            String clsProduct = ((OMElementImpl) node).getLocalName();

            Iterator loadBalanceIterator = ((OMElementImpl) node).getChildElements();
            while (loadBalanceIterator.hasNext()) {
                OMNode node2 = (OMNode) loadBalanceIterator.next();
                pid = ((OMElementImpl) node2).getAttribute(new QName("id")).getAttributeValue();
                clusterList.add(pid);
            }
        }
        return clusterList;
    }
}
