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
package org.wso2.carbon.registry.extensions.handlers;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.beans.BusinessServiceInfo;
import org.wso2.carbon.registry.extensions.handlers.utils.*;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Handler to process the service.
 */
public class ServiceMediaTypeHandler extends Handler {
    private static final Log log = LogFactory.getLog(ServiceMediaTypeHandler.class);
    private static final String TRUNK = "trunk";

    private String defaultEnvironment;

    private boolean disableWSDLValidation = false;
    private List<String> smartLifecycleLinks = new LinkedList<String>();

    private String defaultServiceVersion = CommonConstants.SERVICE_VERSION_DEFAULT_VALUE;

    public void setDefaultServiceVersion(String defaultServiceVersion) {
        this.defaultServiceVersion = defaultServiceVersion;
    }

    public void setSmartLifecycleLinks(OMElement locationConfiguration) throws RegistryException {
        Iterator confElements = locationConfiguration.getChildElements();
        while (confElements.hasNext()) {
            OMElement confElement = (OMElement)confElements.next();
            if (confElement.getQName().equals(new QName("key"))) {
                smartLifecycleLinks.add(confElement.getText());
            }
        }
    }

    public void put(RequestContext requestContext) throws RegistryException {
        WSDLProcessor wsdl = null;

        if (!CommonUtil.isUpdateLockAvailable()) {
            return;
        }
        CommonUtil.acquireUpdateLock();
        try {
            Registry registry = requestContext.getRegistry();
            Resource resource = requestContext.getResource();
            if (resource == null) {
                throw new RegistryException("The resource is not available.");
            }
            String originalServicePath = requestContext.getResourcePath().getPath();
            String resourceName = RegistryUtils.getResourceName(originalServicePath);

            OMElement serviceInfoElement,
                previousServiceInfoElement = null;
            Object resourceContent = resource.getContent();
            String serviceInfo;
            if (resourceContent instanceof String) {
                serviceInfo = (String) resourceContent;
            } else {
                serviceInfo = RegistryUtils.decodeBytes((byte[]) resourceContent);
            }
            try {
                XMLStreamReader reader = XMLInputFactory.newInstance().
                        createXMLStreamReader(new StringReader(serviceInfo));
                StAXOMBuilder builder = new StAXOMBuilder(reader);
                serviceInfoElement = builder.getDocumentElement();
            } catch (Exception e) {
                String msg = "Error in parsing the service content of the service. " +
                        "The requested path to store the service: " + originalServicePath + ".";
                log.error(msg);
                throw new RegistryException(msg, e);
            }
            // derive the service path that the service should be saved.
            String serviceName = CommonUtil.getServiceName(serviceInfoElement);
            String serviceNamespace = CommonUtil.getServiceNamespace(serviceInfoElement);

            String servicePath = "";
            if(serviceInfoElement.getChildrenWithLocalName("newServicePath").hasNext()){
                Iterator OmElementIterator = serviceInfoElement.getChildrenWithLocalName("newServicePath");

                while (OmElementIterator.hasNext()) {
                    OMElement next = (OMElement) OmElementIterator.next();
                    servicePath = next.getText();
                    break;
                }
            }
            else{
                servicePath = RegistryUtils.getAbsolutePath(registry.getRegistryContext(),
                        registry.getRegistryContext().getServicePath() +
                                (serviceNamespace == null ? "" :
                                        CommonUtil.derivePathFragmentFromNamespace(serviceNamespace)) +
                                serviceName);
            }
            String serviceVersion =
                    org.wso2.carbon.registry.common.utils.CommonUtil.getServiceVersion(
                            serviceInfoElement);

            if (serviceVersion.length() == 0) {
                serviceVersion = defaultServiceVersion;
                CommonUtil.setServiceVersion(serviceInfoElement, serviceVersion);
                resource.setContent(serviceInfoElement.toString());
            }
            // saving the artifact id.
            String serviceId = resource.getUUID();
            if (serviceId == null) {
                // generate a service id
                serviceId = UUID.randomUUID().toString();
                resource.setUUID(serviceId);
            }

            if (registry.resourceExists(servicePath)) {
                Resource oldResource = registry.get(servicePath);
                String oldContent;
                Object content = oldResource.getContent();
                if (content instanceof String) {
                    oldContent = (String) content;
                } else {
                    oldContent = RegistryUtils.decodeBytes((byte[]) content);
                }
                OMElement oldServiceInfoElement = null;
                if (serviceInfo.equals(oldContent)) {
                    //TODO: This needs a better solution. This fix was put in place to avoid
                    // duplication of services under /_system/governance, when no changes were made.
                    // However, the fix is not perfect and needs to be rethought. Perhaps the logic
                    // below can be reshaped a bit, or may be we don't need to compare the
                    // difference over here with a little fix to the Governance API end. - Janaka.


                    //We have fixed this assuming that the temp path where services are stored is under /_system/governance/[serviceName]
                    //Hence if we are to change that location, then we need to change the following code segment as well
                    String tempPath = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH
                            + RegistryConstants.PATH_SEPARATOR + resourceName;

                    if (!originalServicePath.equals(tempPath)) {
                        String path = RegistryUtils.getRelativePathToOriginal(servicePath, RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH);
                        if(!ServiceUtils.getModifiedList().contains(path)){
                            ServiceUtils.addToModifiedList(path);
                        }
                        return;
                    }
                    requestContext.setProcessingComplete(true);
                    return;
                }
                if ("true".equals(resource.getProperty("registry.WSDLImport"))) {
                    resource.removeProperty("registry.WSDLImport");
                    try {
                        XMLStreamReader reader = XMLInputFactory.newInstance().
                                createXMLStreamReader(new StringReader(oldContent));
                        StAXOMBuilder builder = new StAXOMBuilder(reader);
                        oldServiceInfoElement = builder.getDocumentElement();
                        CommonUtil.setServiceName(oldServiceInfoElement, CommonUtil.getServiceName(serviceInfoElement));
                        CommonUtil.setServiceNamespace(oldServiceInfoElement,
                                CommonUtil.getServiceNamespace(serviceInfoElement));
                        CommonUtil.setWSDLURL(oldServiceInfoElement,
                                CommonUtil.getWSDLURL(serviceInfoElement));
                        CommonUtil.setEndpointEntries(oldServiceInfoElement,
                                CommonUtil.getEndpointEntries(serviceInfoElement));
                        CommonUtil.setServiceVersion(oldServiceInfoElement,
                                org.wso2.carbon.registry.common.utils.CommonUtil.getServiceVersion(
                                        serviceInfoElement));
                        serviceInfoElement = oldServiceInfoElement;
                        resource.setContent(serviceInfoElement.toString());
                    } catch (Exception e) {
                        String msg = "Error in parsing the service content of the service. " +
                                "The requested path to store the service: " + originalServicePath + ".";
                        log.error(msg);
                        throw new RegistryException(msg, e);
                    }
                }

                try {
                    previousServiceInfoElement = AXIOMUtil.stringToOM(oldContent);
                } catch (XMLStreamException e) {
                    String msg = "Error in parsing the service content of the service. " +
                            "The requested path to store the service: " + originalServicePath + ".";
                    log.error(msg);
                    throw new RegistryException(msg, e);
                }
            }
//            CommonUtil.addGovernanceArtifactEntryWithAbsoluteValues(
//                    CommonUtil.getUnchrootedSystemRegistry(requestContext),
//                    serviceId, servicePath);

                boolean alreadyAdded = false;
                String wsdlURL = CommonUtil.getWSDLURL(serviceInfoElement);
                if (wsdlURL != null && (wsdlURL.startsWith("http://") || wsdlURL.startsWith("https://"))) {
                    wsdl = buildWSDLProcessor(requestContext);
                    RequestContext context = new RequestContext(registry, requestContext.getRepository(),
                            requestContext.getVersionRepository());
                    context.setResourcePath(new ResourcePath(RegistryConstants.PATH_SEPARATOR + serviceName + ".wsdl"));
                    context.setSourceURL(wsdlURL);
                    context.setResource(new ResourceImpl());
                    String wsdlPath = wsdl.addWSDLToRegistry(context, wsdlURL, null, false, false,
                            disableWSDLValidation);
                    if (wsdlPath == null) {
                        // we will get the null value, if this is called within addWSDLToRegistry
                        return;
                    }
                    wsdlURL = RegistryUtils.getRelativePath(requestContext.getRegistryContext(), wsdlPath);
                    CommonUtil.setWSDLURL(serviceInfoElement, wsdlURL);
                    resource.setContent(RegistryUtils.decodeBytes((serviceInfoElement.toString()).getBytes()));
                    // updating the wsdl url
                    ((ResourceImpl) resource).prepareContentForPut();
                    persistServiceResource(registry, resource, servicePath);
                    alreadyAdded = true;
                    // and make the associations
                    registry.addAssociation(servicePath, wsdlPath, CommonConstants.DEPENDS);
                    registry.addAssociation(wsdlPath, servicePath, CommonConstants.USED_BY);

                } else if (wsdlURL != null && wsdlURL.startsWith(RegistryConstants.ROOT_PATH)) {
                    // it seems wsdlUrl is a registry path..
                    String wsdlPath = RegistryUtils.getAbsolutePath(requestContext.getRegistryContext(), wsdlURL);
                    if (!wsdlPath.startsWith(RegistryUtils.getAbsolutePath(
                            requestContext.getRegistryContext(),
                            RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH))) {

                        wsdlPath = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + wsdlPath;
                    }
                    boolean addItHere = false;
                    if (!registry.resourceExists(wsdlPath)) {
                        String msg = "Associating service to a non-existing WSDL. wsdl url: " + wsdlPath + ", " +
                                "service path: " + servicePath + ".";
                        log.error(msg);
                        throw new RegistryException(msg);
                    }
                    if (!registry.resourceExists(servicePath)) {
                        addItHere = true;
                    } else {
                        Association[] dependencies = registry.getAssociations(servicePath, CommonConstants.DEPENDS);
                        boolean dependencyFound = false;
                        if (dependencies != null) {
                            for (Association dependency : dependencies) {
                                if (wsdlPath.equals(dependency.getDestinationPath())) {
                                    dependencyFound = true;
                                }
                            }
                        }
                        if (!dependencyFound) {
                            addItHere = true;
                        }
                    }
                    if (addItHere) { // add the service right here..
                        ((ResourceImpl) resource).prepareContentForPut();
                        persistServiceResource(registry, resource, servicePath);
                        alreadyAdded = true;
                        // and make the associations

                        registry.addAssociation(servicePath, wsdlPath, CommonConstants.DEPENDS);
                        registry.addAssociation(wsdlPath, servicePath, CommonConstants.USED_BY);
                    }
                }
            if (!alreadyAdded) {
                // we are adding the resource anyway.
                ((ResourceImpl) resource).prepareContentForPut();
                persistServiceResource(registry, resource, servicePath);
            }

/*
            if (!servicePath.contains(registry.getRegistryContext().getServicePath())) {
                if (defaultEnvironment == null) {
                    String serviceDefaultEnvironment = registry.getRegistryContext().getServicePath();
                    String relativePath = serviceDefaultEnvironment.replace(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH, "");
                    relativePath = relativePath.replace(CommonUtil.derivePathFragmentFromNamespace(serviceNamespace),"");
                    defaultEnvironment = relativePath;
                }

                String currentRelativePath = servicePath.substring(0, servicePath.indexOf(CommonUtil.derivePathFragmentFromNamespace(serviceNamespace)));
                currentRelativePath = currentRelativePath.replace(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH,"");

                String endpointEnv = EndpointUtils.getEndpointLocation();

                String[] pathSegments = defaultEnvironment.split(RegistryConstants.PATH_SEPARATOR);

                for (String pathSegment : pathSegments) {
                    endpointEnv = endpointEnv.replace(pathSegment,"");
                    currentRelativePath = currentRelativePath.replace(pathSegment,"");
                }

                while(endpointEnv.startsWith(RegistryConstants.PATH_SEPARATOR)){
                    endpointEnv = endpointEnv.replaceFirst(RegistryConstants.PATH_SEPARATOR,"");
                }

                environment = currentRelativePath + endpointEnv;
            }
*/
            // Fixing CARBON-11713
            if(previousServiceInfoElement != null) {
                EndpointUtils.removeObsoleteWsdlEndpoint(previousServiceInfoElement, serviceInfoElement, servicePath,
                        registry);
            }

            if (servicePath.contains(registry.getRegistryContext().getServicePath())) {
                EndpointUtils.saveEndpointsFromServices(servicePath, serviceInfoElement, registry,
                        CommonUtil.getUnchrootedSystemRegistry(requestContext));
            }

            String symlinkLocation = RegistryUtils.getAbsolutePath(requestContext.getRegistryContext(),
                    requestContext.getResource().getProperty(RegistryConstants.SYMLINK_PROPERTY_NAME));
            if (!servicePath.equals(originalServicePath)) {
                // we are creating a sym link from service path to original service path.
                Resource serviceResource = requestContext.getRegistry().get(
                        RegistryUtils.getParentPath(originalServicePath));
                String isLink = serviceResource.getProperty("registry.link");
                String mountPoint = serviceResource.getProperty("registry.mountpoint");
                String targetPoint = serviceResource.getProperty("registry.targetpoint");
                String actualPath = serviceResource.getProperty("registry.actualpath");
                if (isLink != null && mountPoint != null && targetPoint != null) {
                    symlinkLocation = actualPath + RegistryConstants.PATH_SEPARATOR;
                }
                if (symlinkLocation != null) {
                    requestContext.getSystemRegistry().createLink(symlinkLocation + resourceName, servicePath);
                }
            }
            // in this flow the resource is already added. marking the process completed..
            requestContext.setProcessingComplete(true);

            if (wsdl != null && CommonConstants.ENABLE.equals(System.getProperty(CommonConstants.UDDI_SYSTEM_PROPERTY))) {
                //creating the business service info bean
                BusinessServiceInfo businessServiceInfo = new BusinessServiceInfo();
                businessServiceInfo.setServiceName(serviceName.trim());
                businessServiceInfo.setServiceNamespace(serviceNamespace.trim());
                businessServiceInfo.setServiceDescription(CommonUtil.getServiceDescription(serviceInfoElement));
                WSDLInfo wsdlInfo = wsdl.getMasterWSDLInfo();
                businessServiceInfo.setServiceWSDLInfo(wsdlInfo);
                businessServiceInfo.setServiceEndpoints(CommonUtil.getEndpointEntries(serviceInfoElement));
                businessServiceInfo.setDocuments(CommonUtil.getDocLinks(serviceInfoElement));

                UDDIPublisher publisher = new UDDIPublisher(businessServiceInfo);
                publisher.publishBusinessService();
            }

            String path = RegistryUtils.getRelativePathToOriginal(servicePath, RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH);
            ServiceUtils.addToModifiedList(path);
        } finally {
            CommonUtil.releaseUpdateLock();
        }
    }

    /**
     * Method to customize the WSDL Processor.
     * @param requestContext the request context for the import/put operation
     * @return the WSDL Processor instance.
     */
    @SuppressWarnings("unused")
    protected WSDLProcessor buildWSDLProcessor(RequestContext requestContext) {
        return new WSDLProcessor(requestContext);
    }

    private void persistServiceResource(Registry registry, Resource resource,
                                        String servicePath) throws RegistryException {
        registry.put(servicePath, resource);
    }

    public void setDisableWSDLValidation(String disableWSDLValidation) {
        this.disableWSDLValidation = Boolean.toString(true).equals(disableWSDLValidation);
    }

    public String mergeServiceContent(String newContent, String oldContent) {

        return newContent;
    }

    @Override
    public void delete(RequestContext requestContext) throws RegistryException {
        if (!CommonUtil.isUpdateLockAvailable()) {
            return;
        }
        CommonUtil.acquireUpdateLock();
        try {
            Registry registry = requestContext.getRegistry();
            ResourcePath resourcePath = requestContext.getResourcePath();
            if (resourcePath == null) {
                throw new RegistryException("The resource path is not available.");
            }
            Resource resource = registry.get(resourcePath.getPath());
        } finally {
            CommonUtil.releaseUpdateLock();
        }
    }
}
