/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.governance.services.ui.utils;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.services.ui.clients.AddServicesServiceClient;
import org.wso2.carbon.registry.common.ui.UIException;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.resource.ui.Utils;
import org.wso2.carbon.registry.resource.ui.clients.ResourceServiceClient;
import org.wso2.carbon.ui.CarbonUIUtil;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Iterator;

public class AddServicesUtil {
    private static final Log log = LogFactory.getLog(AddServicesUtil.class);

    public static String addServiceContent(
            OMElement info, HttpServletRequest request, ServletConfig config, HttpSession session) throws UIException {

        try {
            AddServicesServiceClient serviceClient = new AddServicesServiceClient(config, session);
            OMElement filledService = new AddServiceUIGenerator().getDataFromUI(info, request);

            String newServicePath = request.getParameter("path");

            if (newServicePath != null) {
                filledService.build();
                OMFactory fac = OMAbstractFactory.getOMFactory();
                Iterator it = filledService.getChildrenWithLocalName("newServicePath");
                if (it.hasNext()) {
                    while (it.hasNext()) {
                        OMElement next = (OMElement) it.next();
                        next.setText(newServicePath);
                        break;
                    }
                } else {
                    OMElement operation = fac.createOMElement("newServicePath", filledService.getNamespace(), filledService);
                    operation.setText(newServicePath);
                }
            }


            return serviceClient.addService(filledService.toString());
        } catch (Exception e) {
            String msg = "Failed to get service details. " + e.getMessage();
            log.error(msg, e);
            throw new UIException(msg, e);
        }
    }

    public static String getServicePath(ServletConfig config, HttpSession session) throws Exception {
        AddServicesServiceClient serviceClient = new AddServicesServiceClient(config, session);
        return serviceClient.getServicePath();
    }

    public static String getGreatestChildVersion(ServletConfig config, HttpSession session,
                                                 String path) throws Exception {
        String[] nodes =
                Utils.getSortedChildNodes(
                        new ResourceServiceClient(config, session).getCollectionContent(
                                path));
        String last = "";
        for (String node : nodes) {
            String name = RegistryUtils.getResourceName(node);
            try {
                Integer.parseInt(name);
                last = name;
            } catch (NumberFormatException ignore) {
            }
        }
        return last;
    }

    public static int[] getAdjacentVersions(ServletConfig config, HttpSession session,
                                            String path, int current) throws Exception {
        String[] nodes =
                Utils.getSortedChildNodes(
                        new ResourceServiceClient(config, session).getCollectionContent(path));
        int[] versions = new int[2];
        versions[0] = -1;
        versions[1] = -1;
        int previous = -1;
        for (String node : nodes) {
            String name = RegistryUtils.getResourceName(node);
            try {
                int temp = Integer.parseInt(name);
                if (previous == current) {
                    // The last match was the current version. Therefore, the match is the version
                    // after the current.
                    versions[1] = temp;
                    break;
                }
                if (temp == current) {
                    // The match is the current version. Therefore, the last match was the version
                    // before the current.
                    versions[0] = previous;
                }
                previous = temp;
            } catch (NumberFormatException ignore) {
            }
        }
        return versions;
    }

    public static String getUniqueNameForNamespaceToRedirect(String commonSchemaLocation, String targetNamespace1) {
        String resourcePath;
        String targetNamespace = targetNamespace1.replaceAll("\\s+$", "");
        targetNamespace = targetNamespace.replace("://", RegistryConstants.PATH_SEPARATOR);
        targetNamespace = targetNamespace.replace(".", RegistryConstants.PATH_SEPARATOR);

        if (commonSchemaLocation.endsWith(RegistryConstants.PATH_SEPARATOR)) {
            resourcePath = new StringBuilder()
                    .append(commonSchemaLocation)
                    .append(targetNamespace).toString();
        } else {
            resourcePath = new StringBuilder()
                    .append(commonSchemaLocation)
                    .append(RegistryConstants.PATH_SEPARATOR)
                    .append(targetNamespace).toString();
        }

        if (!targetNamespace.endsWith(RegistryConstants.PATH_SEPARATOR)) {
            resourcePath = new StringBuilder().append(resourcePath).append(RegistryConstants.PATH_SEPARATOR).toString();
        }

        return resourcePath;
    }

    public static String getServiceConfiguration(HttpSession session, ServletConfig config) throws Exception {
        AddServicesServiceClient serviceClient = new AddServicesServiceClient(config, session);
        return serviceClient.getServiceConfiguration();
    }

    public static String getNamespaceFromContent(OMElement head) {
        OMElement overview = head.getFirstChildWithName(new
                QName("Overview"));
        if (overview != null) {
            return overview.getFirstChildWithName(new QName("Namespace")).getText();
        }
        overview = head.getFirstChildWithName(new
                QName(UIGeneratorConstants.DATA_NAMESPACE, "overview"));
        if (overview != null) {
            return overview.getFirstChildWithName(new QName(UIGeneratorConstants.DATA_NAMESPACE, "namespace")).getText();
        }
        return null;
    }

    public static String getNameFromContent(OMElement head) {
        OMElement overview = head.getFirstChildWithName(new
                QName("Overview"));
        if (overview != null) {
            return overview.getFirstChildWithName(new QName("Name")).getText();
        }
        overview = head.getFirstChildWithName(new
                QName(UIGeneratorConstants.DATA_NAMESPACE, "overview"));
        if (overview != null) {
            return overview.getFirstChildWithName(new QName(UIGeneratorConstants.DATA_NAMESPACE, "name")).getText();
        }
        return null;
    }

    public static OMElement getUIConfiguration(String filePath) {
        InputStream stream = AddServiceUIGenerator.class.getResourceAsStream(filePath);
        try {
            StAXOMBuilder builder = null;
            OMElement omElement = null;
            try {
                builder = new StAXOMBuilder(stream);
                omElement = builder.getDocumentElement();
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
            return omElement;
        } finally {
            try {
                stream.close();
            } catch (IOException ignore) {
            }
        }
    }

    public static OMElement addExtraElements(OMElement data, HttpServletRequest request) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        //adding required fields at the top of the xml which will help to easily read in service side
        OMElement operation = fac.createOMElement("operation", null);
        OMElement currentName = fac.createOMElement("currentName", null);
        OMElement currentNamespace = fac.createOMElement("currentNamespace", null);

        String operationValue = request.getParameter("operation");
        if (operationValue != null) {
            operation.setText(operationValue);
            data.addChild(operation);
        }
        String name = request.getParameter("currentname");
        if (name != null) {
            currentName.setText(name);
            data.addChild(currentName);
        }
        String namespace = request.getParameter("currentnamespace");
        if (namespace != null) {
            currentNamespace.setText(namespace);
            data.addChild(currentNamespace);
        }
        return data;
    }

    public static OMElement loadAddedServiceContent(String xmlContent) throws Exception {
        try {
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(xmlContent));
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            return builder.getDocumentElement();
        } catch (Exception ex) {
            throw ex;
        }
    }

    public static String getDataElementName(String widgetName) {
        if (widgetName == null || widgetName.length() == 0) {
            return null;
        }
        String[] nameParts = widgetName.split("_");
        String convertedName = null;
        //  making widget name camel case
        for (String namePart : nameParts) {
            int i;
            for (i = 0; i < namePart.length(); i++) {
                char c = namePart.charAt(i);
                if (!Character.isLetter(c) || Character.isLowerCase(c)) {
                    break;
                }
            }
            namePart = namePart.substring(0, i).toLowerCase() + namePart.substring(i);
            if (convertedName == null) {
                convertedName = namePart;
            } else {
                convertedName += "_" + namePart;
            }
        }
        if (convertedName == null) {
            return null;
        }

        return convertedName.replaceAll(" ", "").replaceAll("-", "");
    }

    public static OMElement getChildWithName(OMElement head, String widgetName, String namespace) {
        String adjustedName = getDataElementName(widgetName);
        if (adjustedName == null) {
            return null;
        }
        OMElement child = head.getFirstChildWithName(new QName(namespace, adjustedName));
        if (child == null) {
            // this piece of code is for the backward compatibility
            child = head.getFirstChildWithName(new QName(null,
                    widgetName.replaceAll(" ", "-")));
        }
        return child;
    }

    public static String decorateVersionElement(String version, String basicVersionElement,
                                                String path, String type, String append,
                                                String screenWidth,
                                                ServletConfig config, HttpSession session,
                                                HttpServletRequest request) {
        String hrefPrefix =
                "../resources/resource.jsp?region=region3&item=resource_browser_menu&path=";
        String hrefPostfix = (screenWidth != null) ? "&screenWidth=" + screenWidth : "";
        String patchPath = RegistryUtils.getParentPath(path);
        String minorPath = RegistryUtils.getParentPath(patchPath);
        String majorPath = RegistryUtils.getParentPath(minorPath);
        String servicePath = RegistryUtils.getParentPath(majorPath);
        String versions[] = version.split("[.]");
        StringBuffer sb = new StringBuffer("$1type=\"hidden\"$2");
        if (type.equals("collection")) {
            sb.append("<a href=\"").append(hrefPrefix).append(majorPath.replaceAll("&", "%26"))
                    .append(hrefPostfix).append(
                    "\">").append(versions[0]).append("</a>");
            sb.append(".");
            sb.append("<a href=\"").append(hrefPrefix).append(minorPath.replaceAll("&", "%26"))
                    .append(hrefPostfix).append(
                    "\">").append(versions[1]).append("</a>");
            sb.append(".");
            sb.append("<a href=\"").append(hrefPrefix).append(patchPath.replaceAll("&", "%26"))
                    .append(hrefPostfix).append(
                    "\">").append(versions[2]).append("</a>");
            sb.append(append);
        } else if (type.equals("patch")) {
            sb.append("<a href=\"").append(hrefPrefix).append(majorPath.replaceAll("&", "%26"))
                    .append(hrefPostfix).append(
                    "\">").append(versions[0]).append("</a>");
            sb.append(".");
            sb.append("<a href=\"").append(hrefPrefix).append(minorPath.replaceAll("&", "%26"))
                    .append(hrefPostfix).append(
                    "\">").append(versions[1]).append("</a>");
            sb.append(".");
            sb.append("<a href=\"javascript:void(0)\">").append(versions[2]).append("</a>");
            sb.append(append);
            try {
                int[] adjacentVersions =
                        getAdjacentVersions(config, session, minorPath,
                                Integer.parseInt(versions[2]));
                sb.append("&nbsp;");
                if (adjacentVersions[0] > -1) {
                    sb.append("<a class=\"icon-link\" style=\"background-image: ").append(
                            "url(../resources/images/arrow-up.gif);float:none !important;").append(
                            "margin-bottom:0px !important;margin-top:0px !important;").append(
                            "margin-left:0px !important\" href=\"").append(hrefPrefix)
                            .append(minorPath.replaceAll("&", "%26")).append(hrefPostfix)
                            .append("/").append(adjacentVersions[0]).append("\" title=\"").append(
                            CarbonUIUtil.geti18nString(
                                    "previous.version",
                                    "org.wso2.carbon.governance.services.ui.i18n.Resources",
                                    request.getLocale())).append(": ").append(versions[0]).append(
                            ".").append(versions[1]).append(".").append(adjacentVersions[0]).append(
                            "\"/>");
                }
                if (adjacentVersions[1] > -1) {
                    sb.append("<a class=\"icon-link\" style=\"background-image: ").append(
                            "url(../resources/images/arrow-right.gif);float:none !important;")
                            .append("margin-bottom:0px !important;margin-top:0px !important;")
                            .append("margin-left:0px !important\" href=\"").append(hrefPrefix)
                            .append(minorPath.replaceAll("&", "%26")).append(hrefPostfix)
                            .append("/").append(adjacentVersions[1]).append("\" title=\"").append(
                            CarbonUIUtil.geti18nString(
                                    "next.version",
                                    "org.wso2.carbon.governance.services.ui.i18n.Resources",
                                    request.getLocale())).append(": ").append(versions[0]).append(
                            ".").append(versions[1]).append(".").append(adjacentVersions[1]).append(
                            "\"/>");
                }
            } catch (Exception ignore) {
            }
        } else if (type.equals("minor")) {
            sb.append("<a href=\"").append(hrefPrefix).append(majorPath.replaceAll("&", "%26"))
                    .append(hrefPostfix).append(
                    "\">").append(versions[0]).append("</a>");
            sb.append(".");
            sb.append("<a href=\"javascript:void(0)\">").append(versions[1]).append("</a>");
            sb.append(".");
            sb.append("<a href=\"").append(hrefPrefix).append(patchPath.replaceAll("&", "%26"))
                    .append(hrefPostfix).append(
                    "\">").append(versions[2]).append("</a>");
            sb.append(append);
            try {
                int[] adjacentVersions =
                        getAdjacentVersions(config, session, majorPath,
                                Integer.parseInt(versions[1]));
                sb.append("&nbsp;");
                if (adjacentVersions[0] > -1) {
                    String latestPatch =
                            getGreatestChildVersion(config, session,
                                    majorPath + "/" + adjacentVersions[0]);
                    sb.append("<a class=\"icon-link\" style=\"background-image: ").append(
                            "url(../resources/images/arrow-up.gif);float:none !important;").append(
                            "margin-bottom:0px !important;margin-top:0px !important;").append(
                            "margin-left:0px !important\" href=\"").append(hrefPrefix)
                            .append(majorPath.replaceAll("&", "%26")).append(hrefPostfix)
                            .append("/").append(adjacentVersions[0]).append("\" title=\"").append(
                            CarbonUIUtil.geti18nString(
                                    "previous.version",
                                    "org.wso2.carbon.governance.services.ui.i18n.Resources",
                                    request.getLocale())).append(": ").append(versions[0]).append(
                            ".").append(adjacentVersions[0]).append(".").append(latestPatch).append(
                            "\"/>");
                }
                if (adjacentVersions[1] > -1) {
                    String latestPatch =
                            getGreatestChildVersion(config, session,
                                    majorPath + "/" + adjacentVersions[1]);
                    sb.append("<a class=\"icon-link\" style=\"background-image: ").append(
                            "url(../resources/images/arrow-right.gif);float:none !important;")
                            .append("margin-bottom:0px !important;margin-top:0px !important;")
                            .append("margin-left:0px !important\" href=\"").append(hrefPrefix)
                            .append(majorPath.replaceAll("&", "%26")).append(hrefPostfix)
                            .append("/").append(adjacentVersions[1]).append("\" title=\"").append(
                            CarbonUIUtil.geti18nString(
                                    "next.version",
                                    "org.wso2.carbon.governance.services.ui.i18n.Resources",
                                    request.getLocale())).append(": ").append(versions[0]).append(
                            ".").append(adjacentVersions[1]).append(".").append(latestPatch).append(
                            "\"/>");
                }
            } catch (Exception ignore) {
            }
        } else if (type.equals("major")) {
            sb.append("<a href=\"javascript:void(0)\">").append(versions[0]).append("</a>");
            sb.append(".");
            sb.append("<a href=\"").append(hrefPrefix).append(minorPath.replaceAll("&", "%26"))
                    .append(hrefPostfix).append(
                    "\">").append(versions[1]).append("</a>");
            sb.append(".");
            sb.append("<a href=\"").append(hrefPrefix).append(patchPath.replaceAll("&", "%26"))
                    .append(hrefPostfix).append(
                    "\">").append(versions[2]).append("</a>");
            sb.append(append);
            try {
                int[] adjacentVersions =
                        getAdjacentVersions(config, session, servicePath,
                                Integer.parseInt(versions[0]));
                sb.append("&nbsp;");
                if (adjacentVersions[0] > -1) {
                    String latestMinor =
                            getGreatestChildVersion(config, session,
                                    servicePath + "/" + adjacentVersions[0]);
                    String latestPatch =
                            getGreatestChildVersion(config, session,
                                    servicePath + "/" + adjacentVersions[0] + "/" + latestMinor);
                    sb.append("<a class=\"icon-link\" style=\"background-image: ").append(
                            "url(../resources/images/arrow-up.gif);float:none !important;").append(
                            "margin-bottom:0px !important;margin-top:0px !important;").append(
                            "margin-left:0px !important\" href=\"").append(hrefPrefix)
                            .append(servicePath.replaceAll("&", "%26")).append(hrefPostfix)
                            .append("/").append(adjacentVersions[0]).append("\" title=\"").append(
                            CarbonUIUtil.geti18nString(
                                    "previous.version",
                                    "org.wso2.carbon.governance.services.ui.i18n.Resources",
                                    request.getLocale())).append(": ").append(adjacentVersions[0])
                            .append(".").append(latestMinor).append(".").append(latestPatch).append(
                            "\"/>");
                }
                if (adjacentVersions[1] > -1) {
                    String latestMinor =
                            getGreatestChildVersion(config, session,
                                    servicePath + "/" + adjacentVersions[1]);
                    String latestPatch =
                            getGreatestChildVersion(config, session,
                                    servicePath + "/" + adjacentVersions[1] + "/" + latestMinor);
                    sb.append("<a class=\"icon-link\" style=\"background-image: ").append(
                            "url(../resources/images/arrow-right.gif);float:none !important;")
                            .append("margin-bottom:0px !important;margin-top:0px !important;")
                            .append("margin-left:0px !important\" href=\"").append(hrefPrefix)
                            .append(servicePath.replaceAll("&", "%26")).append(hrefPostfix)
                            .append("/").append(adjacentVersions[1]).append("\" title=\"").append(
                            CarbonUIUtil.geti18nString(
                                    "next.version",
                                    "org.wso2.carbon.governance.services.ui.i18n.Resources",
                                    request.getLocale())).append(": ").append(adjacentVersions[1])
                            .append(".").append(latestMinor).append(".").append(latestPatch).append(
                            "\"/>");
                }
            } catch (Exception ignore) {
            }
        }
        return basicVersionElement.replaceAll(
                "(<input[^>]*)type=\"text\"([^>]*id=\"id_Overview_Version\"[^>]*>)", sb.toString());
    }
}
