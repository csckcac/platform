/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.governance.registry.extensions.handlers;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.JaxenException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.registry.extensions.handlers.utils.HandlerConstants;
import org.wso2.carbon.governance.registry.extensions.handlers.utils.ResourceProcessor;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.handlers.utils.EndpointUtils;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class WadlMediaTypeHandler extends Handler {

    private static final Log log = LogFactory.getLog(WadlMediaTypeHandler.class);
    private Registry registry;

    public void put(RequestContext requestContext) throws RegistryException{
        if (!CommonUtil.isUpdateLockAvailable()) {
            return;
        }
        CommonUtil.acquireUpdateLock();
        String originalWadlPath = null;

        try {
            registry = requestContext.getRegistry();
            Resource resource = requestContext.getResource();
            originalWadlPath = requestContext.getResourcePath().getPath();
            registry.put(originalWadlPath, resource);

            OMElement wadlElement;
            Object resourceContent = resource.getContent();

            String wadlContent;
            if (resourceContent instanceof String) {
                wadlContent = (String) resourceContent;
            } else {
                wadlContent = new String((byte[]) resourceContent);
            }

            try {
                XMLStreamReader reader = XMLInputFactory.newInstance().
                        createXMLStreamReader(new StringReader(wadlContent));
                StAXOMBuilder builder = new StAXOMBuilder(reader);
                wadlElement = builder.getDocumentElement();
            } catch (XMLStreamException e) {
                String msg = "Error in reading the WADL content of the Process. " +
                        "The requested path to store the Process: " + originalWadlPath + ".";
                log.error(msg);
                throw new RegistryException(msg, e);
            }

            String wadlNamespace = wadlElement.getNamespace().getNamespaceURI();
            Registry governanceSystemRegistry =
                    RegistryCoreServiceComponent.getRegistryService().getGovernanceSystemRegistry();
            String wadlName = RegistryUtils.getResourceName(requestContext.getResourcePath().getPath());

            GenericArtifactManager genericArtifactManager =
                    new GenericArtifactManager(governanceSystemRegistry, "wadl");
            GenericArtifact wadlArtifact = genericArtifactManager.newGovernanceArtifact(
                    new QName(wadlName));
            wadlArtifact.addAttribute("overview_name", wadlName);
            wadlArtifact.addAttribute("overview_namespace", wadlNamespace);
            genericArtifactManager.addGenericArtifact(wadlArtifact);

            AXIOMXPath expression;

            expression = new AXIOMXPath("/ns:application/ns:resources");
            expression.addNamespace("ns", wadlNamespace);
            List elements = expression.selectNodes(wadlElement);
            for (int i = 0; i < elements .size(); i++) {
                OMElement element = (OMElement) elements .get(i);
                String base = element.getAttributeValue(new QName("base"));
                String endpointPath = saveEndpoint(base);

                ServiceManager serviceManager = new ServiceManager(governanceSystemRegistry);
                Service service = serviceManager.newService(new QName(wadlNamespace, getServiceName(base)));
                serviceManager.addService(service);
                String servicePath = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                        GovernanceUtils.getArtifactPath(governanceSystemRegistry, service.getId());
                addDependency(servicePath, endpointPath);

                String wadlPath = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                        GovernanceUtils.getArtifactPath(governanceSystemRegistry, wadlArtifact.getId());
                addDependency(servicePath, wadlPath);
                addDependency(wadlPath, endpointPath);

                saveResources(element, wadlPath, base);
            }

            requestContext.setProcessingComplete(true);
        } catch (JaxenException e) {
            String msg = "Error while parsing the WADL content of " + RegistryUtils.getResourceName(originalWadlPath);
            throw new RegistryException(msg, e);
        } finally {
            CommonUtil.releaseUpdateLock();
        }
    }

    private void addDependency(String source, String target) throws RegistryException {
        registry.addAssociation(source, target, CommonConstants.DEPENDS);
        registry.addAssociation(target, source, CommonConstants.USED_BY);
    }

    private String getServiceName(String url){
        if(url.endsWith("/")){
            return url.replaceAll(".*/(.*)/$","$1");
        }
        return url.replaceAll(".*/(.*)$", "$1");
    }

    public void saveResources(OMElement resourcesElement, String wadlPath, String basePath) throws JaxenException, RegistryException {
        Iterator<OMElement> resources = resourcesElement.getChildrenWithLocalName("resource");

        ResourceProcessor processor = new ResourceProcessor(registry);
        while (resources.hasNext()){
            String resourcePath = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + processor.addResource(resources.next(), basePath);
            addDependency(wadlPath, resourcePath);
        }
    }

    private String saveEndpoint(String url) throws RegistryException {
        String urlToPath = EndpointUtils.deriveEndpointFromUrl(url);

        String endpointAbsoluteBasePath = RegistryUtils.getAbsolutePath(registry.getRegistryContext(),
                org.wso2.carbon.registry.core.RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                        HandlerConstants.ENDPOINT_DEFAULT_LOCATION);

        if(urlToPath.startsWith(RegistryConstants.PATH_SEPARATOR)){
            urlToPath = urlToPath.replaceFirst(RegistryConstants.PATH_SEPARATOR,"");
        }

        String endpointAbsolutePath = endpointAbsoluteBasePath + urlToPath;
        return saveEndpointValues(url, endpointAbsolutePath);
    }

    private String saveEndpointValues(String url, String endpointAbsolutePath) throws RegistryException {
        Resource resource;
        if (registry.resourceExists(endpointAbsolutePath)) {
            resource = registry.get(endpointAbsolutePath);
            if(!url.getBytes().equals(resource.getContent())){
                throw new RegistryException("Failed to add the endpoint: Found another " +
                        "endpoint in the same location with different content");
            }
            return resource.getPath();
        }

        resource = registry.newResource();
        resource.setContent(url.getBytes());
        resource.setMediaType(CommonConstants.ENDPOINT_MEDIA_TYPE);
        resource.setUUID(UUID.randomUUID().toString());
        return  registry.put(endpointAbsolutePath, resource);
    }
}
