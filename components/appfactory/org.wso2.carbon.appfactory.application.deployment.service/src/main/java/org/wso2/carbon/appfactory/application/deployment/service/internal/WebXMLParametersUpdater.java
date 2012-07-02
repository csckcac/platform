/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.appfactory.application.deployment.service.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.wso2.carbon.appfactory.application.deployment.service.ApplicationDeploymentExceptions;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 *
 */
public class WebXMLParametersUpdater {
    private static final Log log = LogFactory.getLog(WebXMLParametersUpdater.class);
    public static final String DB_URL = "databaseUrl";
    public static final String DB_USERNAME = "databaseUsername";
    public static final String DB_PASSWORD = "databasePassword";

    public boolean updateParameters(String rootDir, Map<String, String> parameters)
            throws ApplicationDeploymentExceptions {

        File carProjectFiles = new File(rootDir);
        if (carProjectFiles!=null && carProjectFiles.isDirectory()) {
            for (File artifact : carProjectFiles.listFiles()) {
                if (artifact.isDirectory()) {
                    //a new artifact
                    File pom = new File(artifact + File.separator + "pom.xml");
                    if (pom.exists()) {
                        if (isWebApp(pom)) {
                            updateWebXML(artifact, parameters);
                        }
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("no pom.xml file in " + artifact.getName());
                        }
                    }
                }
            }
            return true;
        } else {
            log.error("There are no project file in the directory " + carProjectFiles);
        }
        return false;
    }

    private void updateWebXML(File artifact, Map<String, String> parameters)
            throws ApplicationDeploymentExceptions {
        File webXML = new File(artifact + File.separator + "src" + File.separator + "main" +
                               File.separator + "webapp" + File.separator + "WEB-INF" +
                               File.separator + "web.xml");
        Document document = getDocument(webXML);
        XPathExpression contextValue;
        contextValue = compile("//context-param");
        NodeList contextParams;
        try {
            contextParams = (NodeList) contextValue.evaluate(document, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            String msg = "Error occurred during xpath expression evaluation " + contextValue.toString();
            log.error(msg, e);
            throw new ApplicationDeploymentExceptions(msg, e);
        }
        if (contextParams.getLength() > 0) {
            for (int i = 0; i < contextParams.getLength(); i++) {
                String name = contextParams.item(i).getChildNodes().item(1)
                        .getChildNodes().item(0).getNodeValue();
                String value = contextParams.item(i).getChildNodes().item(3)
                        .getChildNodes().item(0).getNodeValue();
                System.out.println("name :" + name + "val :" + value);
                if ("databaseUrl".equals(name)) {
                    value = value.replace("${databaseUrl}", parameters.get("databaseUrl"));
                    System.out.println("replacing db url..");
                } else {
                    if (parameters.get(name) != null) {
                        value = parameters.get(name);
                        System.out.println("replacing " + name);
                    }
                }
                contextParams.item(i).getChildNodes().item(3)
                        .getChildNodes().item(0).setNodeValue(value);
            }
            saveBack(document, webXML);
        }
    }

    private XPathExpression compile(String s) throws ApplicationDeploymentExceptions {
        XPath xPath = XPathFactory.newInstance().newXPath();
        XPathExpression expression;
        try {
            expression = xPath.compile(s);
        } catch (XPathExpressionException e) {
            String msg = "Error occurred during xpath expression compilation " + s;
            log.error(msg, e);
            throw new ApplicationDeploymentExceptions(msg, e);
        }
        return expression;
    }

    private void saveBack(Document document, File webXML) throws ApplicationDeploymentExceptions {
        DOMSource source = new DOMSource(document);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = transformerFactory.newTransformer();
            Writer writer = new FileWriter(webXML);
            StreamResult streamResult = new StreamResult(writer);
            transformer.transform(source, streamResult);
        } catch (TransformerConfigurationException e) {
            String msg = "Error occurred during initialization of transformer";
            log.error(msg, e);
            throw new ApplicationDeploymentExceptions(msg, e);
        } catch (TransformerException e) {
            String msg = "Error occurred during saving the web.xml back";
            log.error(msg, e);
            throw new ApplicationDeploymentExceptions(msg, e);
        } catch (IOException e) {
            String msg = "Error occurred in opening file web.xml";
            log.error(msg, e);
            throw new ApplicationDeploymentExceptions(msg, e);
        }
    }

    private boolean isWebApp(File pom) throws ApplicationDeploymentExceptions {
        Document document = getDocument(pom);
        XPathExpression expression = null;
        try {
            expression = compile("//packaging/text()");
            Object result = expression.evaluate(document, XPathConstants.NODESET);
            NodeList list = (NodeList) result;
            System.out.println("nodes " + list.item(0).getNodeValue());
            if (list.getLength() > 0) {
                return ("war".equals(list.item(0).getNodeValue()));
            }
        } catch (XPathExpressionException e) {
            String msg = "Error occurred during evaluation of xpath expression " + expression;
            log.error(msg, e);
            throw new ApplicationDeploymentExceptions(msg, e);
        }
        return false;
    }


    private Document getDocument(File file) throws ApplicationDeploymentExceptions {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(false);
        DocumentBuilder builder;
        Document doc;
        try {
            builder = documentBuilderFactory.newDocumentBuilder();
            doc = builder.parse(file);
        } catch (ParserConfigurationException e1) {
            String msg = "Error occurred during initialization of Document builder";
            log.error(msg, e1);
            throw new ApplicationDeploymentExceptions(msg, e1);
        } catch (SAXException e1) {
            String msg = "Error occurred during parsing the file " + file;
            log.error(msg, e1);
            throw new ApplicationDeploymentExceptions(msg, e1);
        } catch (IOException e1) {
            String msg = "Error occurred during reading the file " + file;
            log.error(msg, e1);
            throw new ApplicationDeploymentExceptions(msg, e1);
        }
        return doc;
    }
}

