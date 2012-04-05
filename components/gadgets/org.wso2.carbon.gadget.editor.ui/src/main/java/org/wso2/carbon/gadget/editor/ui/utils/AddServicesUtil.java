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
package org.wso2.carbon.gadget.editor.ui.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.wso2.carbon.registry.common.ui.UIException;
import org.wso2.carbon.registry.resource.ui.clients.ResourceServiceClient;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletConfig;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLInputFactory;
import java.io.InputStream;
import java.io.StringReader;

public class AddServicesUtil {
    private static final Log log = LogFactory.getLog(AddServicesUtil.class);

    public static boolean addServiceContent(
            OMElement info,HttpServletRequest request, ServletConfig config, HttpSession session) throws UIException {

        /*try {
            AddServicesServiceClient serviceClient = new AddServicesServiceClient(config, session);
            OMElement filledservice = AddServiceUIGenerator.getdatafromUI(info,request);
            return serviceClient.addService(filledservice.toString());
        } catch (Exception e) {
            String msg = "Failed to get service details. " + e.getMessage();
            log.error(msg, e);
            throw new UIException(msg, e);
        } */
        return true;
    }

    /*
    public static String getServicePath(ServletConfig config, HttpSession session)throws Exception{
       AddServicesServiceClient serviceClient = new AddServicesServiceClient(config, session);
       return serviceClient.getServicePath();
    }

    public static String getUniqueNameforNamespaceToRedirect(String commonSchemaLocation, String targetNamespace1) {
        String resourcePath;
        String targetNamespace =  targetNamespace1.replaceAll("\\s+$", "");
        targetNamespace = targetNamespace.replace("://", RegistryConstants.PATH_SEPARATOR);
        targetNamespace = targetNamespace.replace(".", RegistryConstants.PATH_SEPARATOR);

        if (commonSchemaLocation.endsWith(RegistryConstants.PATH_SEPARATOR)) {
            resourcePath = new StringBuilder()
                    .append(commonSchemaLocation)
                    .append(targetNamespace).toString();
        }
        else {
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
    public static boolean importwsdl(HttpServletRequest request,ServletConfig config)throws RegistryException{
        String cookie = (String) request.
                getSession().getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        ResourceServiceClient client =
                new ResourceServiceClient(cookie, config, request.getSession());
        String fetchURL = request.getParameter("Interface_WSDL-URL");
        String parentPath = "/";
        String resourceName = request.getParameter("Overview_Name");
        String description = request.getParameter("Overview_Description");
        try{
            client.importResource(parentPath, resourceName, "application/wsdl+xml", description, fetchURL,null,false);
            return true;
        }catch(Exception e){
            return false;
        }
    }
    public static String getServiceConfiguration(HttpSession session,ServletConfig config)throws Exception{
        AddServicesServiceClient serviceClient = new AddServicesServiceClient(config, session);
        return serviceClient.getServiceConfiguration();
    }
    public static String getNamespacefromcontent(OMElement head){
        return head.getFirstChildWithName(new
                QName("Overview")).getFirstChildWithName(new QName("Namespace")).getText();
    }
    public static String getNamefromcontent(OMElement head){
        return head.getFirstChildWithName(new
                QName("Overview")).getFirstChildWithName(new QName("Name")).getText();
    }
    public static OMElement getUIconfiguration(String filepath){
        InputStream stream = AddServiceUIGenerator.class.getResourceAsStream(filepath);
        StAXOMBuilder builder = null;
        OMElement omElement = null;
        try {
            builder = new StAXOMBuilder(stream);
            omElement = builder.getDocumentElement();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        return omElement;
    }
    public static OMElement addExtraElements(OMElement data,HttpServletRequest request){
        OMFactory fac = OMAbstractFactory.getOMFactory();
        //adding required fields at the top of the xml which will help to easilly read in service side
        OMElement operation = fac.createOMElement("operation",null);
        OMElement currentname = fac.createOMElement("currentname",null);
        OMElement currentnamespace = fac.createOMElement("currentnamespace",null);
        OMElement namespace = fac.createOMElement("namespace",null);
        OMElement nameelement = fac.createOMElement("name",null);
        OMElement wsdlurl = fac.createOMElement("wsdlURL",null);
        namespace.setText(request.getParameter("Overview_Namespace"));
        nameelement.setText(request.getParameter("Overview_Name"));
        operation.setText(request.getParameter("operation"));
        currentname.setText(request.getParameter("currentname"));
        currentnamespace.setText(request.getParameter("currentnamespace"));
        wsdlurl.setText(request.getParameter("Interface_WSDL-URL"));
        data.addChild(operation);
        data.addChild(currentname);
        data.addChild(currentnamespace);
        data.addChild(namespace);
        data.addChild(nameelement);
        data.addChild(wsdlurl);
        return data;
    }

    public static OMElement loadaddedservicecontent(String xmlcontent)throws Exception{
        try{
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(xmlcontent));
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            return builder.getDocumentElement();
        }catch (Exception ex){
            throw ex;
        }
    }   */
}
