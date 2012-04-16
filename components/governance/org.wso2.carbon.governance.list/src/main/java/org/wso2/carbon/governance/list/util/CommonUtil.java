/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.governance.list.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;

public class CommonUtil {

    private static final Log log = LogFactory.getLog(CommonUtil.class);

    private static RegistryService registryService;

    public static synchronized void setRegistryService(RegistryService service) {
        if (registryService == null) {
            registryService = service;
        }
    }

    public static RegistryService getRegistryService() {
        return registryService;
    }
    
    public static String getServiceName(Resource resource)throws RegistryException {
        String serviceInfo = new String((byte[])resource.getContent());
        try{
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(serviceInfo));
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            OMElement serviceInfoElement = builder.getDocumentElement();
            return getNameFromContent(serviceInfoElement);
        } catch(Exception e) {
            String msg = "Error in getting the service name. service path: " + resource.getPath() + ".";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
    }
    public static String getServiceNamespace(Resource resource)throws RegistryException{
        String serviceInfo = new String((byte[])resource.getContent());
        try{
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(serviceInfo));
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            OMElement serviceInfoElement = builder.getDocumentElement();
            return getNamespaceFromContent(serviceInfoElement);
        } catch(Exception e) {
            String msg = "Error in getting the service namespace. service path: " + resource.getPath() + ".";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
    }

    public static String getLifeCycleName(Resource resource){
        String lifeCycleName="";
        if(resource.getProperties()!=null){
            if (resource.getProperty("registry.LC.name") != null) {
                lifeCycleName = resource.getProperty("registry.LC.name");
            }
        }
        return lifeCycleName;
    }

    public static String getLifeCycleState(Resource resource){
        String lifeCycleState="";
        if(resource.getProperties()!=null){
            if(!getLifeCycleName(resource).equals("")){
                String LCStatePropertyName="registry.lifecycle."+getLifeCycleName(resource)+".state";
                if (resource.getProperty(LCStatePropertyName) != null) {
                    lifeCycleState = resource.getProperty(LCStatePropertyName);
                }
            }

        }
        return lifeCycleState;
    }
   
    public static String getResourceName(String path){
        String[] temp = path.split("/");
        return temp[temp.length -1];
    }

/*
     public static String getSchemaNamespace(String path,String defaultPrefix) {
        return getNamespace(path,"schemas",defaultPrefix);
     }
*/

/*
    public static String getWsdlNamespace(String path,String defaultPrefix) {
        return getNamespace(path,"wsdls",defaultPrefix);
    }
*/

/*
    private static String getNamespace(String path, String metadataType, String defaultPrefix) {
        String namespace = "";

        if (path.startsWith(defaultPrefix)) {
            namespace = path.substring(path.indexOf(metadataType) + metadataType.length() + 1, path.lastIndexOf("/"));
        } else {
            String tempPath = path.substring(0, path.lastIndexOf("/"));
            namespace = path.substring(path.indexOf(metadataType) + metadataType.length() + 1, tempPath.lastIndexOf("/"));
        }

        return namespace.replaceAll("/", ".");
    }
*/

    public static String getNamespaceFromContent(OMElement head){
        OMElement overview =  head.getFirstChildWithName(new
                QName("Overview"));
        if (overview != null) {
            return overview.getFirstChildWithName(new QName("Namespace")).getText();
        }
        return head.getFirstChildWithName(new
                QName(CommonConstants.SERVICE_ELEMENT_NAMESPACE, "overview")).
                getFirstChildWithName(new QName(CommonConstants.SERVICE_ELEMENT_NAMESPACE,
                        "namespace")).getText();
    }
    public static String getNameFromContent(OMElement head){
        OMElement overview =  head.getFirstChildWithName(new
                QName("Overview"));
        if (overview != null) {
            return overview.getFirstChildWithName(new QName("Name")).getText();
        }
        return head.getFirstChildWithName(new
                QName(CommonConstants.SERVICE_ELEMENT_NAMESPACE, "overview")).
                getFirstChildWithName(new QName(CommonConstants.SERVICE_ELEMENT_NAMESPACE,
                        "name")).getText();
    }
}


