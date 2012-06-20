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
package org.wso2.carbon.governance.services.services;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.parsers.SAXParser;
import org.jaxen.JaxenException;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceFilter;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.services.util.Util;
import org.wso2.carbon.governance.services.util.XMLConfigValidatorUtil;
import org.wso2.carbon.registry.admin.api.governance.IManageServicesService;
import org.wso2.carbon.registry.common.CommonConstants;
import org.wso2.carbon.registry.common.services.RegistryAbstractAdmin;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;
import org.wso2.carbon.utils.CarbonUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.sql.SQLException;
import java.util.*;

@SuppressWarnings({"unused", "NonJaxWsWebServices", "ValidExternallyBoundObject"})
public class AddServicesService extends RegistryAbstractAdmin implements IManageServicesService {
    private static final Log log = LogFactory.getLog(AddServicesService.class);
    private static Map<String, Boolean> lifecycleAspects = new HashMap<String, Boolean>();
    private static final String TRUNK = "trunk";

    public String addService(String info) throws RegistryException {
        RegistryUtils.recordStatistics(info);
        Registry registry = getGovernanceRegistry();
        String currentPath = "";
        if (RegistryUtils.isRegistryReadOnly(registry.getRegistryContext())) {
            return currentPath;
        }
        try {
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(info));
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            OMElement serviceElement = builder.getDocumentElement();

            // get the details about the operations current name and the namespace before an update.
            OMElement operationElement = serviceElement.getFirstChildWithName(new QName("operation"));
            String operation;
            if (operationElement != null) {
                operation = operationElement.getText();
                // and then remove the operation child,
                operationElement.detach();
            } else {
                operation = "Add";
            }

            OMElement currentNameElement = serviceElement.getFirstChildWithName(new QName("currentName"));
            String currentName;
            if (currentNameElement != null && !(currentName = currentNameElement.getText()).equals("")) {
                currentNameElement.detach();
            } else {
                currentName = CommonUtil.getServiceName(serviceElement);
            }
            OMElement currentNamespaceElement = serviceElement.getFirstChildWithName(new QName("currentNamespace"));
            String currentNamespace;
            if (currentNamespaceElement != null && !(currentNamespace = currentNamespaceElement.getText()).equals("")) {
                currentNamespaceElement.detach();
            } else {
                currentNamespace = CommonUtil.getServiceNamespace(serviceElement);
            }

            ServiceManager serviceManager = new ServiceManager(registry);
            Service service = serviceManager.newService(serviceElement);
//            String lifeCycleName = service.getAttribute("serviceLifecycle_lifecycleName");

            if ("Edit".equals(operation)) {
                // this is an edit operation, retrieve the old namespace and name

                if (serviceElement.getChildrenWithLocalName("newServicePath").hasNext()) {
                    Iterator OmElementIterator = serviceElement.getChildrenWithLocalName("newServicePath");

                    while (OmElementIterator.hasNext()) {
                        OMElement next = (OMElement) OmElementIterator.next();
                        currentPath = next.getText();
                        break;
                    }
                } else {
                    currentPath = registry.getRegistryContext().getServicePath() +
                            CommonUtil.derivePathFragmentFromNamespace(currentNamespace) + currentName;
                }
                currentPath = RegistryUtils.getRelativePathToOriginal(currentPath,
                        RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH);
                Resource oldResource;
                if (registry.resourceExists(currentPath)) {
                    GovernanceArtifact oldArtifact =
                            GovernanceUtils.retrieveGovernanceArtifactByPath(registry, currentPath);
                    oldResource = registry.get(currentPath);
                    if (!(oldArtifact instanceof Service)) {
                        String msg = "The updated path is occupied by a non-service. path: " +
                                currentPath + ".";
                        log.error(msg);
                        throw new Exception(msg);
                    }
                    // id is used to differentiate the service
                    String id = oldArtifact.getId();
                    service.setId(id);
                    serviceManager.updateService(service);

                    Resource newResource = registry.get(currentPath);
                    Properties properties = oldResource.getProperties();

                    for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                        if (newResource.getProperty((String) entry.getKey()) == null) {
                            if (entry.getValue() instanceof List) {
                                newResource.setProperty((String) entry.getKey(), (List) entry.getValue());
                            } else {
                                newResource.setProperty((String) entry.getKey(), (String) entry.getValue());
                            }
                        }
                    }

                    newResource.setDescription(oldResource.getDescription());
                    registry.put(service.getPath(), newResource);

//                    Resource serviceResource = registry.get(service.getPath());
//                    String oldLifeCycleName = serviceResource.getProperty("registry.LC.name");
/*
                    if (lifeCycleName == null) {
                        removeAspect(registry, service.getPath(), oldLifeCycleName);
                    } else {
                        if (oldLifeCycleName != null) {
                            if (!oldLifeCycleName.equals(lifeCycleName)) {
                                // if user selects a different lifecycle for the service, delete
                                // the previous one and then associate the new one.
                                removeAspect(registry, service.getPath(), oldLifeCycleName);
                                registry.associateAspect(service.getPath(), lifeCycleName);
                            }
                        } else { // no lifecycle was there, but there is one now
                            registry.associateAspect(service.getPath(), lifeCycleName);
                        }
                    }
*/
                    return currentPath;
                }
            }

            currentPath = registry.getRegistryContext().getServicePath() +
                    CommonUtil.derivePathFragmentFromNamespace(currentNamespace) + currentName;
            currentPath = RegistryUtils.getRelativePathToOriginal(currentPath,
                    RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH);

            // Check to see whether any services with same name,version,namespace exists in all env
            final String ref_overview_name = service.getAttribute("overview_name");
            final String ref_overview_version = service.getAttribute("overview_version");
            final String ref_overview_namespace = service.getAttribute("overview_namespace");
            Service[] result_services = serviceManager.findServices(new ServiceFilter() {
                public boolean matches(Service _service) throws GovernanceException {
                    String _overview_name = _service.getAttribute("overview_name");
                    String _overview_version = _service.getAttribute("overview_version");
                    String _overview_namespace = _service.getAttribute("overview_namespace");
                    if ((_overview_name != null && _overview_name.equals(ref_overview_name)) &&
                            (_overview_version != null && _overview_version.equals(ref_overview_version)) &&
                            (_overview_namespace != null && _overview_namespace.equals(ref_overview_namespace))) {
                        return true;
                    }
                    return false;
                }
            });

            if (registry.resourceExists(currentPath)) {
                String msg = "A resource with the given name and namespace exists";
                log.warn(msg);
                return msg;
            } else if (result_services.length > 0) {
                String msg = "A resource with the given name and namespace exists";
                log.warn(msg);
                return msg;
            }

            serviceManager.addService(service);
//            if (lifeCycleName != null) {
//                registry.associateAspect(service.getPath(), lifeCycleName);
//            }

        } catch (Exception e) {
            String msg = "Unable to add service. ";
            if (e instanceof RegistryException) {
                throw (RegistryException) e;
            } else if (e instanceof OMException) {
                msg += "Unexpected character found in input-field name.";
                log.error(msg, e);
                throw new RegistryException(msg, e);
            }
            throw new RegistryException(msg + (e.getCause() instanceof SQLException ? "" : e.getCause().getMessage()), e);
        }

        return currentPath;
    }

    public String editService(String servicename) throws RegistryException {
        Registry registry = getGovernanceRegistry();
        String path = servicename;
        // resource path is created to make sure the version page doesn't give null values
        if (!registry.resourceExists(new ResourcePath(path).getPath())) {
            //if a service have a symLink and when click on the symLink
            if (Boolean.parseBoolean(getRootRegistry().get(servicename).getProperty(RegistryConstants.REGISTRY_LINK))) {
                return (new String((byte[]) getRootRegistry().get(servicename).getContent()));
            }
            throw new RegistryException("Resource does not exist path : " + path);
        }
        Resource resource = registry.get(path);
        String serviceinfo = new String((byte[]) resource.getContent());
        return serviceinfo;
    }

    public String getServiceConfiguration() throws RegistryException {
        try {
            Registry registry = getConfigSystemRegistry();
            return Util.getServiceConfig(registry);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean saveServiceConfiguration(String update) throws RegistryException {
        Registry registry = getConfigSystemRegistry();
        if (RegistryUtils.isRegistryReadOnly(registry.getRegistryContext())) {
            return false;
        }
        try {
            Util.validateOMContent(Util.buildOMElement(update));

            Resource resource = registry.get(RegistryConstants.GOVERNANCE_SERVICES_CONFIG_PATH + "service");
            resource.setContent(update);
            registry.put(RegistryConstants.GOVERNANCE_SERVICES_CONFIG_PATH + "service", resource);
            return true;
        } catch (Exception RegistryException) {
//            throw new RegistryException("Unable to save the xml configuration");
            return false;
        }
    }

    public String getServicePath() throws RegistryException {
        try {
            Registry registry = getGovernanceRegistry();
            return registry.getRegistryContext().getServicePath();
        } catch (Exception RegistryException) {
            return null;
        }
    }

    /* this method is useful when adding the service edit button, do a check before displaying service Edit button */
    public boolean canChange(String path) throws Exception {
        UserRegistry registry = (UserRegistry) getRootRegistry();
        if (registry.getUserName() != null && registry.getUserRealm() != null) {
            if (registry.getUserRealm().getAuthorizationManager().isUserAuthorized(
                    registry.getUserName(), path, ActionConstants.PUT)) {
                Resource resource = registry.get(path);
                String property = resource.getProperty(
                        CommonConstants.RETENTION_WRITE_LOCKED_PROP_NAME);
                return property == null || !Boolean.parseBoolean(property) ||
                        registry.getUserName().equals(
                                resource.getProperty(CommonConstants.RETENTION_USERNAME_PROP_NAME));

            }
        }
        return false;
    }

    /* get available aspects */
    public String[] getAvailableAspects() throws Exception {
        UserRegistry registry = (UserRegistry) getGovernanceRegistry();
        Registry systemRegistry = getConfigSystemRegistry();
        String[] aspectsToAdd = registry.getAvailableAspects();
        if (aspectsToAdd == null) {
            return new String[0];
        }
        List<String> lifecycleAspectsToAdd = new LinkedList<String>();
        boolean isTransactionStarted = false;
        String tempResourcePath = "/governance/lcm/" + UUIDGenerator.generateUUID();
        for (String aspectToAdd : aspectsToAdd) {
            if (systemRegistry.getRegistryContext().isReadOnly()) {
                lifecycleAspectsToAdd.add(aspectToAdd);
                continue;
            }
            Boolean isLifecycleAspect = lifecycleAspects.get(aspectToAdd);
            if (isLifecycleAspect == null) {
                if (!isTransactionStarted) {
                    registry.beginTransaction();
                    isTransactionStarted = true;
                }
                systemRegistry.put(tempResourcePath, systemRegistry.newResource());
                systemRegistry.associateAspect(tempResourcePath, aspectToAdd);
                Resource r = systemRegistry.get(tempResourcePath);
                Properties props = r.getProperties();
                Set keys = props.keySet();
                for (Object key : keys) {
                    String propKey = (String) key;
                    if (propKey.startsWith("registry.lifecycle.")
                            || propKey.startsWith("registry.custom_lifecycle.checklist.")) {
                        isLifecycleAspect = Boolean.TRUE;
                        break;
                    }
                }
                if (isLifecycleAspect == null) {
                    isLifecycleAspect = Boolean.FALSE;
                }
                lifecycleAspects.put(aspectToAdd, isLifecycleAspect);
            }
            if (isLifecycleAspect) {
                lifecycleAspectsToAdd.add(aspectToAdd);
            }
        }
        if (isTransactionStarted) {
            systemRegistry.delete(tempResourcePath);
            systemRegistry.rollbackTransaction();
        }
        return lifecycleAspectsToAdd.toArray(new String[lifecycleAspectsToAdd.size()]);
    }

    private void removeAspect(Registry registry, String path, String aspect) throws Exception {
        try {
            /* set all the variables to the resource */
            Resource resource = registry.get(path);
            Properties props = resource.getProperties();
            //List<Property> propList = new ArrayList<Property>();
            Iterator iKeys = props.keySet().iterator();
            ArrayList<String> propertiesToRemove = new ArrayList<String>();

            while (iKeys.hasNext()) {
                String propKey = (String) iKeys.next();

                if (propKey.startsWith("registry.custom_lifecycle.checklist.")
                        || propKey.startsWith("registry.LC.name")
                        || propKey.startsWith("registry.lifecycle.")
                        || propKey.startsWith("registry.Aspects")) {
                    propertiesToRemove.add(propKey);
                }
            }

            for (String propertyName : propertiesToRemove) {
                resource.removeProperty(propertyName);
            }

            registry.put(path, resource);

        } catch (RegistryException e) {

            String msg = "Failed to remove aspect " + aspect +
                    " on resource " + path + ". " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
    }
}

