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

package org.wso2.platform.test.core.utils.endpointutils;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.llom.OMElementImpl;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkProperties;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

public class EsbEndpointSetter {
    public OMElement setEndpointURL(DataHandler dh) throws IOException, XMLStreamException {
        DataHandler dataHandler = null;

        XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(dh.getInputStream());
        StAXOMBuilder builder = new StAXOMBuilder(parser);
        OMElement endPointElem = builder.getDocumentElement();
        OMNode node;
        Iterator children = endPointElem.getChildElements();
        OMAttribute attribute = null;
        OMAttribute attribute2 = null;
        boolean changed = false;
        while (children.hasNext() == true) {
            node = (OMNode) children.next();
            if (((OMElementImpl) node).getLocalName().equals("loadbalance")) {
                Iterator loadbalanceIterator = ((OMElementImpl) node).getChildElements();
                while (loadbalanceIterator.hasNext()) {
                    OMNode loadbalanceNode = (OMNode) loadbalanceIterator.next();
                    Iterator urlIterator = ((OMElementImpl) loadbalanceNode).getChildElements();
                    while (urlIterator.hasNext()) {
                        OMNode urlNode = (OMNode) urlIterator.next();
                        String uri = ((OMElementImpl) urlNode).getAttribute(new QName("uri")).getAttributeValue();
                        attribute = ((OMElementImpl) urlNode).getAttribute(new QName("uri"));
                        ((OMElementImpl) urlNode).getAttribute(new QName("uri")).setAttributeValue(getUrl(uri));
                        attribute2 = ((OMElementImpl) urlNode).getAttribute(new QName("uri"));
                        System.out.println("LoadBalance");
                    }
                    endPointElem.removeAttribute(attribute);
                    endPointElem.addAttribute(attribute2);
                }
            } else if (((OMElementImpl) node).getLocalName().equals("failover")) {
                Iterator failoverIterator = ((OMElementImpl) node).getChildElements();
                while (failoverIterator.hasNext()) {
                    OMNode loadbalanceNode = (OMNode) failoverIterator.next();
                    Iterator urlIterator = ((OMElementImpl) loadbalanceNode).getChildElements();
                    while (urlIterator.hasNext()) {
                        OMNode urlNode = (OMNode) urlIterator.next();
                        String uri = ((OMElementImpl) urlNode).getAttribute(new QName("uri")).getAttributeValue();
                        attribute = ((OMElementImpl) urlNode).getAttribute(new QName("uri"));
                        ((OMElementImpl) urlNode).getAttribute(new QName("uri")).setAttributeValue(getUrl(uri));
                        attribute2 = ((OMElementImpl) urlNode).getAttribute(new QName("uri"));
                        System.out.println("failover");
                    }
                    endPointElem.removeAttribute(attribute);
                    endPointElem.addAttribute(attribute2);
                }
            } else {
                if (((OMElementImpl) node).getLocalName().equals("wsdl")) {
                    String urlValue = ((OMElementImpl) node).getAttribute(new QName("uri")).getAttributeValue();

                    attribute = ((OMElementImpl) node).getAttribute(new QName("uri"));
                    System.out.println(((OMElementImpl) node).getAttribute(new QName("uri")));

                    ((OMElementImpl) node).getAttribute(new QName("uri")).setAttributeValue(getUrl(urlValue));
                    attribute2 = ((OMElementImpl) node).getAttribute(new QName("uri"));
                    changed = true;
                } else if (((OMElementImpl) node).getLocalName().equals("address")) {
                    String urlValue = ((OMElementImpl) node).getAttribute(new QName("uri")).getAttributeValue();

                    attribute = ((OMElementImpl) node).getAttribute(new QName("uri"));
                    System.out.println(((OMElementImpl) node).getAttribute(new QName("uri")));

                    ((OMElementImpl) node).getAttribute(new QName("uri")).setAttributeValue(getUrl(urlValue));
                    attribute2 = ((OMElementImpl) node).getAttribute(new QName("uri"));
                    changed = true;
                }

            }
        }
        if (changed) {
            endPointElem.removeAttribute(attribute);
            endPointElem.addAttribute(attribute2);
        }
        return endPointElem;
    }

    public String getUrl(String endpoint) throws IOException, XMLStreamException {
        String newEndPoint = endpoint;

        DataHandler dh = new DataHandler(new URL("file://" + ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + File.separator +
                                                 "artifacts" + File.separator + "ESB" + File.separator + "endpointlookup.xml"));
        XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(dh.getInputStream());
        StAXOMBuilder builder = new StAXOMBuilder(parser);
        OMElement endPointElem = builder.getDocumentElement();
        Iterator productList = endPointElem.getChildElements();
        boolean endpointFound = false;
        while (productList.hasNext()) {
            OMNode productNode = (OMNode) productList.next();
            Iterator urlList = ((OMElementImpl) productNode).getChildrenWithLocalName("url");
            while (urlList.hasNext()) {
                OMNode urlNode = (OMNode) urlList.next();
                String endpointType = ((OMElementImpl) urlNode).getAttribute(new QName("type")).getAttributeValue();
                String codedEndpoint = ((OMElementImpl) urlNode).getText();
                String product = ((OMElementImpl) productNode).getLocalName();
                if (endpointType.equals("")) {

                }
                if (codedEndpoint.contains(endpoint)) {
                    String service = endpoint.substring(endpoint.indexOf("services/"));
                    newEndPoint = productLookup(product) + service;
                    endpointFound = true;
                    break;
                }
                if (endpointFound) {
                    break;
                }
            }
        }
        ((OMElementImpl) endPointElem.getChildElements().next()).getChildElements();
        return newEndPoint;
    }

    public String productLookup(String product) {
        String url = null;
        FrameworkProperties properties = null;
        if (product.equals("as")) {
            properties = FrameworkFactory.getFrameworkProperties(ProductConstant.APP_SERVER_NAME);
        } else if (product.equals("esb")) {
            properties = FrameworkFactory.getFrameworkProperties(ProductConstant.ESB_SERVER_NAME);
        } else if (product.equals("bps")) {
            properties = FrameworkFactory.getFrameworkProperties(ProductConstant.BPS_SERVER_NAME);
        } else if (product.equals("is")) {
            properties = FrameworkFactory.getFrameworkProperties(ProductConstant.IS_SERVER_NAME);
        } else if (product.equals("bam")) {
            properties = FrameworkFactory.getFrameworkProperties(ProductConstant.BAM_SERVER_NAME);
        } else if (product.equals("brs")) {
            properties = FrameworkFactory.getFrameworkProperties(ProductConstant.BRS_SERVER_NAME);
        } else if (product.equals("ds")) {
            properties = FrameworkFactory.getFrameworkProperties(ProductConstant.DSS_SERVER_NAME);
        } else if (product.equals("greg")) {
            properties = FrameworkFactory.getFrameworkProperties(ProductConstant.GREG_SERVER_NAME);
        } else if (product.equals("gs")) {
            properties = FrameworkFactory.getFrameworkProperties(ProductConstant.GS_SERVER_NAME);
        } else {

        }
        url = properties.getProductVariables().getHostName() + ":" + properties.getProductVariables().getHttpPort() + File.separator;
        return url;
    }
}