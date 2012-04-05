/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.wsdl2code;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.util.CommandLineOption;
import org.apache.axis2.util.CommandLineOptionConstants;
import org.apache.axis2.util.CommandLineOptionParser;
import org.apache.axis2.wsdl.codegen.CodeGenerationEngine;
import org.apache.axis2.wsdl.util.WSDL2JavaOptionsValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.JaxenException;
import org.jaxen.XPath;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.core.transports.http.HttpTransportListener;
import org.wso2.carbon.utils.*;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Tool that generate code for the given options
 */
public class WSDL2Code extends AbstractAdmin {

    private static Log log = LogFactory.getLog(WSDL2Code.class);
    private static final String CODEGEN_POM_XSL = "org/wso2/carbon/wsdl2code/codegen-pom.xsl";

    /**
     * This method will generate the code based on the options array. Options arrya should be as
     * follows,
     * new String[] {"-uri", "location of wsdl", "-g" ...}. Thus, the incoming XML should be as
     * follows,
     * <p/>
     * <ns:codegenRequest xmlns:ns="http://org.wso2.wsf/tools">
     * <options>-uri</options>
     * <options>file://foo</options>
     * ...
     * </ns:codegenRequest>
     * <p/>
     * Once codegenerated, location of genereated code will be send as an ID, thus, one could easily
     * download artifact as a zip file or jar file.
     *
     * @param options
     * @return String
     * @throws AxisFault
     */
    public CodegenDownloadData codegen(String[] options) throws AxisFault {

        String uuid = String.valueOf(System.currentTimeMillis() + Math.random());
        ConfigurationContext configContext = getConfigContext();
        String codegenOutputDir = configContext.getProperty(ServerConstants.WORK_DIR) + File.separator +
                "tools_codegen" + File.separator + uuid + File.separator;
        System.getProperties().remove("project.base.dir");
        System.getProperties().remove("name");
        System.setProperty("project.base.dir", codegenOutputDir);

        ArrayList<String> optionsList = new ArrayList<String>();
        for (int j = 0; j < options.length; j++) {
            String option = options[j];
            optionsList.add(option);
        }
        optionsList.add("-o");
        optionsList.add(codegenOutputDir);
        String[] args = optionsList.toArray(new String[optionsList.size()]);
        Map allOptions;
        try {
            CommandLineOptionParser commandLineOptionParser = new CommandLineOptionParser(args);
            allOptions = commandLineOptionParser.getAllOptions();
            //validation
            List list = commandLineOptionParser.getInvalidOptions(new WSDL2JavaOptionsValidator());
            if (list.size() > 0) {
                String faultOptions = "";
                for (Iterator iterator = list.iterator(); iterator.hasNext();) {
                    CommandLineOption commandLineOption = (CommandLineOption) iterator.next();
                    String optionValue = commandLineOption.getOptionValue();
                    faultOptions += "Invalid input for [ " + commandLineOption.getOptionType() +
                                    (optionValue != null ? " : " + optionValue + " ]" : " ]") +
                                    "\n";
                }

                log.error(faultOptions);
                throw new AxisFault(faultOptions);
            }
            CommandLineOption commandLineOption = (CommandLineOption) allOptions.get("uri");
            if (commandLineOption == null) {
                throw new AxisFault("WSDL URI or Path Cannot be empty");
            }
            String uriValue = commandLineOption.getOptionValue().trim();
            if ("".equals(uriValue)) {
                throw new AxisFault("WSDL URI or Path Cannot be empty");
            } else if (!(uriValue.startsWith("https://") || uriValue.startsWith("http://"))) {
                File file = new File(uriValue);
                if (!(file.exists() && file.isFile())) {
                    throw new AxisFault("The wsdl uri should be a URL or a valid path on the file system");
                }
            }
            new CodeGenerationEngine(commandLineOptionParser).generate();
        } catch (Exception e) {
            String rootMsg = "Code generation failed";
            Throwable throwable = e.getCause();
            if (throwable != null) {
                String msg = throwable.getMessage();
                if (msg != null) {
                    log.error(rootMsg + " " + msg, throwable);
                    throw new AxisFault(throwable.toString());
                }
            }
            log.error(rootMsg, e);
            throw AxisFault.makeFault(e);
        }
        //set the output name
        CommandLineOption option =
                (CommandLineOption) allOptions.
                        get(CommandLineOptionConstants.WSDL2JavaConstants.SERVICE_NAME_OPTION);

        try {
            //achive destination
            uuid = String.valueOf(System.currentTimeMillis() + Math.random());
            File destDir = new File(configContext.getProperty(ServerConstants.WORK_DIR) + File.separator +
                    "tools_codegen" +
                    File.separator +
                    uuid);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
            String destFileName = uuid.substring(2) + ".zip";
            String destArchive = destDir.getAbsolutePath() + File.separator + destFileName;
            InputStream pomXslInputStream = getClass().getResourceAsStream(CODEGEN_POM_XSL);
            if (pomXslInputStream == null) {
                pomXslInputStream = Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream(CODEGEN_POM_XSL);
            }

            String name = uuid;
            boolean isBuildXml = false;
            String version = "1.0";
            File buildXml = new File(codegenOutputDir, "build.xml");
            if (buildXml.exists() && buildXml.isFile()) {
                isBuildXml = true;
                InputStream buildInputStream = new FileInputStream(buildXml);
                XMLStreamReader streamReader =
                        XMLInputFactory.newInstance().createXMLStreamReader(buildInputStream);
                StAXOMBuilder builder = new StAXOMBuilder(streamReader);
                XPath xp = new AXIOMXPath("/project/property[@name='name']");
                OMElement documentElement = builder.getDocumentElement();
                OMElement nameEle = (OMElement) xp.selectSingleNode(documentElement);
                if (nameEle != null) {
                    OMAttribute omAttribute = nameEle.getAttribute(new QName("value"));
                    String nameVal = omAttribute.getAttributeValue();
                    if (nameVal != null) {
                        name = nameVal;
                    }
                }
            }
            OMFactory fac = OMAbstractFactory.getOMFactory();
            OMElement infoEle = fac.createOMElement(new QName("info"));
            OMElement nameEle = fac.createOMElement(new QName("name"));
            nameEle.setText(formatServiceName(name));
            infoEle.addChild(nameEle);
            OMElement isBuildXmlEle = fac.createOMElement(new QName("isBuildXml"));
            isBuildXmlEle.setText(Boolean.valueOf(isBuildXml).toString());
            infoEle.addChild(isBuildXmlEle);
            OMElement versionEle = fac.createOMElement(new QName("version"));
            versionEle.setText(version);
            infoEle.addChild(versionEle);
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            infoEle.serialize(bao);
            InputStream xmlInputStream = new ByteArrayInputStream(bao.toByteArray());

            if (pomXslInputStream != null) {
                File pomFileOut = new File(codegenOutputDir, "pom.xml");
                FileOutputStream pomFileOutputStream = new FileOutputStream(pomFileOut);
                Source xmlSource = new StreamSource(xmlInputStream);
                Source xslSource = new StreamSource(pomXslInputStream);
                Result result = new StreamResult(pomFileOutputStream);
                Transformer transformer =
                        TransformerFactory.newInstance().newTransformer(xslSource);
                transformer.transform(xmlSource, result);
            }

            new ArchiveManipulator().archiveDir(destArchive, new File(codegenOutputDir).getPath());
            FileManipulator.deleteDir(new File(codegenOutputDir));

            DataHandler handler;
            if (destArchive != null) {
                File file = new File(destArchive);
                FileDataSource datasource = new FileDataSource(file);
                handler = new DataHandler(datasource);

                CodegenDownloadData data = new CodegenDownloadData();
                data.setFileName(file.getName());
                data.setCodegenFileData(handler);
                return data;
            } else {
                return null;
            }

        } catch (IOException e) {
            String msg = WSDL2Code.class.getName() + " IOException has occured.";
            log.error(msg, e);
            throw new AxisFault(msg, e);
        } catch (XMLStreamException e) {
            String msg =
                    WSDL2Code.class.getName() + " error encountred while reading the build.xml";
            log.error(msg, e);
            throw new AxisFault(msg, e);
        } catch (JaxenException e) {
            String msg = WSDL2Code.class.getName() + " xpath error has occured";
            log.error(msg, e);
            throw new AxisFault(msg, e);
        } catch (TransformerConfigurationException e) {
            String msg = WSDL2Code.class.getName() + " transformation error has occured";
            log.error(msg, e);
            throw new AxisFault(msg, e);
        } catch (TransformerException e) {
            String msg = WSDL2Code.class.getName() + " transformation error has occured";
            log.error(msg, e);
            throw new AxisFault(msg, e);
        }
    }

    private String getWsdlInformation(String serviceName, AxisConfiguration axisConfig)
            throws AxisFault {
        String ip;
        try {
            ip = NetworkUtils.getLocalHostname();
        } catch (SocketException e) {
            throw new AxisFault("Cannot get local host name", e);
        }
        TransportInDescription http = axisConfig.getTransportIn("http");
        if (http != null) {
            EndpointReference epr =
                    ((HttpTransportListener) http.getReceiver()).
                            getEPRForService(serviceName, ip);
            String wsdlUrlPrefix = epr.getAddress();
            if (wsdlUrlPrefix.endsWith("/")) {
                wsdlUrlPrefix = wsdlUrlPrefix.substring(0, wsdlUrlPrefix.length() - 1);
            }
            return wsdlUrlPrefix + "?wsdl";
        }
        return null;
    }

    /**
     * When the service is a hierarchical service, the service name contains '/' charactors. But
     * if the artifact id of the generated pom.xml file contains '/' charactors, it will fail to
     * build. Therefore, we have to replace '/' with '-'.
     *
     * @param name - original service name
     * @return - formatted name
     */
    private String formatServiceName(String name) {
        String newName = name;
        if (newName.indexOf('/') != -1) {
            newName = newName.replace('/', '-');
        }
        return newName;
    }

}
