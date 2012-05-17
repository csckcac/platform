package org.wso2.carbon.bam.gadgetgenwizard.service;

import org.apache.axiom.om.*;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.jaxp.OMSource;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.bam.gadgetgenwizard.internal.GGWUtils;
import org.wso2.carbon.registry.common.services.RegistryAbstractAdmin;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceStub;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class GadgetGenService extends RegistryAbstractAdmin {

    public static final String GADGET_GEN_TRANSFORM_XSLT = "jaggery-app-generator.xslt";

    public static final String JQPLOT_CSS = "gadgetgen/css/jquery.jqplot.min.css";
    public static final String JQUERY_JS = "gadgetgen/js/jquery.min.js";
    public static final String JQPLOT_JS = "gadgetgen/js/jquery.jqplot.min.js";
    public static final String BARGRAPH_JS = "gadgetgen/js/plugins/jqplot.barRenderer.js";
    public static final String CATEGORY_AXIS_JS = "gadgetgen/js/plugins/jqplot.categoryAxisRenderer.js";
    public static final String[] GADGET_RESOURCES = {JQPLOT_JS, JQUERY_JS, JQPLOT_CSS, BARGRAPH_JS, CATEGORY_AXIS_JS};


    private static final Log log = LogFactory.getLog(GadgetGenService.class);
    public static final String JAGGERY_APP_DIR = CarbonUtils.getCarbonRepository()  + "jaggeryapps";
    public static final String GADGET_GEN_APP_DIR = JAGGERY_APP_DIR + File.separator + "gadgetgen";
    public static final String GADGETGEN_JAG_FILEPATH = GADGET_GEN_APP_DIR + File.separator + "gadgetgen.jag";

    public static final String GADGET_REGISTRY_PATH = "repository/components/org.wso2.carbon.bam.gadgetgen/";

    public static final String RESOURCE_ADMIN_SERVICE_NAME = "ResourceAdminService";

    public static final String ALLOW_PERMISSION_TYPE = "1";
    public static final String WSO2_ANONYMOUS_ROLE = "wso2.anonymous.role";

    public String createGadget(WSMap map) throws GadgetGenException {
        OMDocument intermediateXML = createIntermediateXML(map);
        OMElement gadgetXML = applyXSLTForGadgetXML(intermediateXML);
        OutputStream jagFile = applyXSLTForJaggeryScript(intermediateXML);
        OutputStream jagConf = applyXSLTForJaggeryConf(intermediateXML);
        copyGadgetFilesToRegistry(getConfigSystemRegistry(), GADGET_REGISTRY_PATH);
        return null;
    }

    private ResourceAdminServiceStub createRegistryAdminClient() throws GadgetGenException {
        try {

            String serviceURL = "local://services/" + RESOURCE_ADMIN_SERVICE_NAME;
            ResourceAdminServiceStub registryResourceStub = new ResourceAdminServiceStub(GGWUtils.getConfigurationContextService().getServerConfigContext(), serviceURL);
            ServiceClient client = registryResourceStub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            return registryResourceStub;
        } catch (AxisFault e) {
            String msg = "Error creating Resource Admin Stub. " + e.getMessage() ;
            log.error(msg, e);
            throw new GadgetGenException(msg, e);
        }
    }

    private void copyGadgetFilesToRegistry(Registry registry, String gadgetRegistryPath) throws GadgetGenException {
        try {
//            ResourceAdminServiceStub resourceAdminServiceStub = createRegistryAdminClient();
            AuthorizationManager authorizationManager = ((UserRegistry) getRootRegistry()).getUserRealm().getAuthorizationManager();
            for (int i = 0; i < GADGET_RESOURCES.length; i++) {
                String gadgetResource = GADGET_RESOURCES[i];

                Resource resource = convert(this.getClass().getClassLoader().getResourceAsStream(gadgetResource));
                String gadgetResourcePath = gadgetRegistryPath + gadgetResource;
                if (!registry.resourceExists(gadgetResourcePath)) {
                    registry.put(gadgetResourcePath, resource);
                    authorizationManager.authorizeRole(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME, "/_system/config/" + gadgetResourcePath, ActionConstants.GET);
                    setPermissionUpdateTimestamp();
//                    resourceAdminServiceStub.addRolePermission(gadgetResourcePath, CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME, ActionConstants.GET, ALLOW_PERMISSION_TYPE);
                }
            }
        } catch (RegistryException e) {
            String msg = "Error inserting resource to registry. " + e.getMessage();
            log.error(msg, e);
            throw new GadgetGenException(msg, e);
//        } catch (ResourceAdminServiceResourceServiceExceptionException e) {
//            String msg = "Error inserting permission for resource. " + e.getMessage() ;
//            log.error(msg, e);
//            throw new GadgetGenException(msg, e);
//        } catch (RemoteException e) {
//            String msg = "Error sending request to " +  RESOURCE_ADMIN_SERVICE_NAME + e.getMessage() ;
//            log.error(msg, e);
//            throw new GadgetGenException(msg, e);
        } catch (UserStoreException e) {
            String msg = "Cannot get authorization manager. " + e.getMessage() ;
            log.error(msg, e);
            throw new GadgetGenException(msg, e);
        }
    }

    private Resource convert(InputStream inputStream) throws GadgetGenException {
        try {
            if (inputStream == null) {
                throw new GadgetGenException("input stream cannot be null");
            }
            Resource resource = getConfigSystemRegistry().newResource();
            byte[] bytes = IOUtils.toByteArray(inputStream);
            resource.setContent(bytes);
            return resource;
        } catch (IOException e) {
            String msg = "Error converting file to byte array. " + e.getMessage() ;
            log.error(msg, e);
            throw new GadgetGenException(msg, e);
        } catch (RegistryException e) {
            String msg = "Error creating new resource. " + e.getMessage();
            log.error(msg, e);
            throw new GadgetGenException(msg, e);
        }
    }

    private OutputStream applyXSLTForJaggeryConf(OMDocument intermediateXML) {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    private OutputStream applyXSLTForJaggeryScript(OMDocument intermediateXML) throws GadgetGenException {


        try {
            InputStream xsltStream = this.getClass().getClassLoader().getResourceAsStream(GADGET_GEN_TRANSFORM_XSLT);
            StAXOMBuilder stAXOMBuilder = new StAXOMBuilder(xsltStream);

            Source xsltSource = new OMSource(stAXOMBuilder.getDocumentElement());
            Source xmlSource = new OMSource(intermediateXML.getOMDocumentElement());


            File gadgetGenAppDir = new File(GADGET_GEN_APP_DIR);
            if (!gadgetGenAppDir.exists()) {
                 FileUtils.forceMkdir(gadgetGenAppDir);
            }
            File gadgetGenFile = new File(GADGETGEN_JAG_FILEPATH);
            if (gadgetGenFile.exists()) {
                FileUtils.forceDelete(gadgetGenFile);
            }

            StreamResult result = new StreamResult(FileUtils.openOutputStream(gadgetGenFile));
            TransformerFactory transFact =
                    TransformerFactory.newInstance();
            Transformer transformer = transFact.newTransformer(xsltSource);
            transformer.transform(xmlSource, result);
        } catch (XMLStreamException e) {
            String error = "XML error reading XSLT file. " + e.getMessage() ;;
            log.error(error, e);
            throw new GadgetGenException(error, e);
        } catch (FileNotFoundException e) {
            String error = "XSLT file not found. This should be in the classpath. " + e.getMessage() ;;
            log.error(error, e);
            throw new GadgetGenException(error, e);
        } catch (TransformerException e) {
           String error = "XSLT transformation error during Code generation. " + e.getMessage() ;;
            log.error(error, e);
            throw new GadgetGenException(error, e);
        } catch (IOException e) {
            String error = "Error creating directory structure for jaggery app. " + e.getMessage() ;
            log.error(error, e);
            throw new GadgetGenException(error, e);
        }

        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    private OMElement applyXSLTForGadgetXML(OMDocument intermediateXML) {
        return null;
    }

    private OMDocument createIntermediateXML(WSMap map) {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMDocument omDocument = factory.createOMDocument();
        OMNamespace gadgetgenNamespace = factory.createOMNamespace("http://wso2.com/bam/gadgetgen", "gg");
        OMElement rootElement = factory.createOMElement("gadgetgen", gadgetgenNamespace);
        WSMapElement[] wsMapElements = map.getWsMapElements();
        boolean barChartFound = false;
        OMElement barChart = factory.createOMElement("BarChart", gadgetgenNamespace);
        for (int i = 0; i < wsMapElements.length; i++) {
            WSMapElement wsMapElement = wsMapElements[i];

            // check for bar graph properties
            if (wsMapElement.getKey().startsWith("bar")) {
                barChartFound = true;
                OMElement omElement = factory.createOMElement(wsMapElement.getKey(), gadgetgenNamespace);
                omElement.setText(wsMapElement.getValue());
                barChart.addChild(omElement);
            }

            OMElement omElement = factory.createOMElement(wsMapElement.getKey(), gadgetgenNamespace);
            omElement.setText(wsMapElement.getValue());
            rootElement.addChild(omElement);
        }
        if (barChartFound) {
           rootElement.addChild(barChart);
        }
        omDocument.addChild(rootElement);
        return omDocument;  //To change body of created methods use File | Settings | File Templates.
    }
}
