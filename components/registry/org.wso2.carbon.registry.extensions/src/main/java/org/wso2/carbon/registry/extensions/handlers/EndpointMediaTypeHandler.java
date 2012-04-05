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
package org.wso2.carbon.registry.extensions.handlers;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.utils.AuthorizationUtils;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.handlers.utils.EndpointUtils;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;
import org.wso2.carbon.user.mgt.UserMgtConstants;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class EndpointMediaTypeHandler extends Handler {
    private static final Log log = LogFactory.getLog(EndpointMediaTypeHandler.class);

    private static final String LOCATION_TAG = "location";
    OMElement endpointLocationElement;

    public void setEndpointLocationConfiguration(OMElement endpointLocationElement) throws RegistryException {
        String endpointLocation = null;
        Iterator configElements = endpointLocationElement.getChildElements();
        while (configElements.hasNext()) {
            OMElement configElement = (OMElement)configElements.next();
            if (configElement.getQName().equals(new QName(LOCATION_TAG))) {
                endpointLocation = configElement.getText();
                if (!endpointLocation.startsWith(RegistryConstants.PATH_SEPARATOR)) {
                    endpointLocation = RegistryConstants.PATH_SEPARATOR + endpointLocation;
                }
                if (!endpointLocation.endsWith(RegistryConstants.PATH_SEPARATOR)) {
                    endpointLocation = endpointLocation + RegistryConstants.PATH_SEPARATOR;
                }
            }
        }
        EndpointUtils.setEndpointLocation(endpointLocation); 

        String absoluteEndpointLocation = RegistryUtils.getAbsolutePath(
                RegistryContext.getBaseInstance(),
                    RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                    endpointLocation);
        AuthorizationUtils.addAuthorizeRoleListener(
                RegistryConstants.ADD_ENDPOINT_AUTHORIZE_ROLE_LISTENER_EXECUTION_ORDER_ID,
                absoluteEndpointLocation,
                UserMgtConstants.UI_ADMIN_PERMISSION_ROOT + "manage/resources/govern/metadata/add",
                UserMgtConstants.EXECUTE_ACTION);
        AuthorizationUtils.addAuthorizeRoleListener(
                RegistryConstants.LIST_ENDPOINT_AUTHORIZE_ROLE_LISTENER_EXECUTION_ORDER_ID,
                absoluteEndpointLocation,
                UserMgtConstants.UI_ADMIN_PERMISSION_ROOT + "manage/resources/govern/metadata/list",
                UserMgtConstants.EXECUTE_ACTION, new String[]{ActionConstants.GET});
        this.endpointLocationElement = endpointLocationElement;
    }

    public OMElement getEndpointLocationConfiguration() {
        return endpointLocationElement;
    }

    public void setEndpointMediaType(String endpointMediaType) throws RegistryException {
        EndpointUtils.setEndpointMediaType(endpointMediaType);
    }

    public String getEndpointMediaType() throws RegistryException {
        return EndpointUtils.getEndpointMediaType();   
    }

    public void put(RequestContext requestContext) throws RegistryException {
        if (!CommonUtil.isUpdateLockAvailable()) {
            return;
        }
        CommonUtil.acquireUpdateLock();
        try {
            // we are not allowing to update the resource content for any reason, but they can just update properties
            Registry registry = requestContext.getRegistry();
            Resource resource = requestContext.getResource();

            Object resourceContentObj = resource.getContent();
            String resourceContent; // here the resource content is url
            if (resourceContentObj instanceof String) {
                resourceContent = (String)resourceContentObj;
                resource.setContent(resourceContent.getBytes());
            } else {
                resourceContent = new String((byte[])resourceContentObj);
            }

            String urlToPath = EndpointUtils.deriveEndpointFromUrl(resourceContent);            

            // so here the absolute path.
            String basePath = RegistryUtils.getAbsolutePath(registry.getRegistryContext(),
                    org.wso2.carbon.registry.core.RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                    EndpointUtils.getEndpointLocation());
            String path = basePath + urlToPath;

            String endpointId = resource.getProperty(CommonConstants.ARTIFACT_ID_PROP_KEY);
            if (registry.resourceExists(path)) {
                Resource oldResource = registry.get(path);
                byte[] oldContent = (byte[])oldResource.getContent();
                if (oldContent != null && !new String(oldContent).equals(resourceContent)) {
                    // oops somebody trying to update the endpoint resource content. that should not happen
                    String msg = "Endpoint content for endpoint resource is not allowed to change, " +
                            "path: " + path + ".";
                    log.error(msg);
                    throw new RegistryException(msg);
                }
            } else if (endpointId == null) {
                endpointId = UUID.randomUUID().toString();
                resource.setProperty(CommonConstants.ARTIFACT_ID_PROP_KEY, endpointId);
            }

            CommonUtil.addGovernanceArtifactEntryWithAbsoluteValues(
                    CommonUtil.getUnchrootedSystemRegistry(requestContext),
                    endpointId, path);

            String relativeArtifactPath = RegistryUtils.getRelativePath(registry.getRegistryContext(), path);
            // adn then get the relative path to the GOVERNANCE_BASE_PATH
            relativeArtifactPath = RegistryUtils.getRelativePathToOriginal(relativeArtifactPath,
                    RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH);
            Registry systemRegistry = CommonUtil.getUnchrootedSystemRegistry(requestContext);
            if (!systemRegistry.resourceExists(basePath)) {
                systemRegistry.put(basePath, systemRegistry.newCollection());
            }
            registry.put(path, resource);

//            if (!(resource instanceof Collection) &&
//               ((ResourceImpl) resource).isVersionableChange()) {
//                registry.createVersion(path);
//            }
            ((ResourceImpl)resource).setPath(relativeArtifactPath);
            requestContext.setProcessingComplete(true);
        } finally {
            CommonUtil.releaseUpdateLock();
        }
    }

    public String rename(RequestContext requestContext) throws RegistryException {
        return move(requestContext);
    }

    public String move(RequestContext requestContext) throws RegistryException {
        Registry registry = requestContext.getRegistry();
        String sourcePath = requestContext.getSourcePath();
        checkEndpointDependency(registry, sourcePath);
        return requestContext.getTargetPath();
    }

    public void delete(RequestContext requestContext) throws RegistryException {
        if (!CommonUtil.isDeleteLockAvailable()) {
            return;
        }
        CommonUtil.acquireDeleteLock();

        Registry registry = requestContext.getRegistry();
        String path = requestContext.getResourcePath().getPath();

        try {
            if (path == null) {
                throw new RegistryException("The resource path is not available.");
            }
            checkEndpointDependency(registry, path);
            Resource resource = registry.get(path);
            CommonUtil.removeArtifactEntry(requestContext.getSystemRegistry(),resource.getProperty(CommonConstants.ARTIFACT_ID_PROP_KEY));
        } finally {
            CommonUtil.releaseDeleteLock();
        }
    }

    public void checkEndpointDependency(Registry registry, String path) throws RegistryException {

        // here we are getting the associations for the endpoint.
        Association[] endpointDependents = registry.getAssociations(path, CommonConstants.USED_BY);

        // for each endpoint we are checking what the resource type is, if it is service, we check the
        // endpoint service, if it is wsdl, we check the wsdl
        List<String> dependents = new ArrayList<String>();
        for (Association endpointDependent: endpointDependents) {
            String targetPath = endpointDependent.getDestinationPath();
            if (registry.resourceExists(targetPath)) {
                Resource targetResource = registry.get(targetPath);

                String mediaType = targetResource.getMediaType();
                if (CommonConstants.WSDL_MEDIA_TYPE.equals(mediaType)) {
                    // so there are dependencies for wsdl media
                    dependents.add(targetPath);
                } else if ((CommonConstants.SERVICE_MEDIA_TYPE.equals(mediaType))) {
                    dependents.add(targetPath);
                }
            }
        }
        if (dependents.size() > 0) {
            // so there are dependencies, we are not allowing to delete endpoints if there are dependents
            String msg = "Error in deleting the endpoint resource. Please make sure detach the associations " +
                    "to the services and wsdls manually before deleting the endpoint. " +
                    "endpoint path: " + path + ".";

            log.error(msg);
            throw new RegistryException(msg);
        }
    }

    // adding the association should have a different lock    

    public void addAssociation(RequestContext requestContext) throws RegistryException {
        if (!CommonUtil.isAddingAssociationLockAvailable()) {
            return;
        }
        CommonUtil.acquireAddingAssociationLock();
        try {
            // here whenever a service is associated to a endpoint it will add the endpoint entry
            String targetPath = requestContext.getTargetPath();
            String sourcePath = requestContext.getSourcePath();

            Registry registry = requestContext.getRegistry();

            // get the target resource.
            Resource targetResource = registry.get(targetPath);
            if (CommonConstants.SERVICE_MEDIA_TYPE.equals(targetResource.getMediaType()) &&
                    CommonConstants.USED_BY.equals(requestContext.getAssociationType())) {
                // if so we are getting the service and add the endpoint to the source
                Resource sourceResource = registry.get(sourcePath);
                byte[] sourceContent = (byte[])sourceResource.getContent();
                if (sourceContent == null) {
                    return;
                }
                String endpointUrl = new String(sourceContent);
                String endpointEnv = sourceResource.getProperty(CommonConstants.ENDPOINT_ENVIRONMENT_ATTR);
                if (endpointEnv == null) {
                    endpointEnv = "";
                }
                if (endpointEnv.indexOf(",") > 0) {
                    for (String env : endpointEnv.split(",")) {
                        EndpointUtils.addEndpointToService(registry, targetPath, endpointUrl, env);
                    }
                } else {
                    EndpointUtils.addEndpointToService(registry, targetPath, endpointUrl, endpointEnv);
                }
            }
        } finally {
            CommonUtil.releaseAddingAssociationLock();
        }
    }

    public void removeAssociation(RequestContext requestContext) throws RegistryException {
        if (!CommonUtil.isAddingAssociationLockAvailable()) {
            return;
        }
        CommonUtil.acquireAddingAssociationLock();
        try {
            // here whenever a service is associated to a endpoint it will add the endpoint entry
            String targetPath = requestContext.getTargetPath();
            String sourcePath = requestContext.getSourcePath();

            Registry registry = requestContext.getRegistry();

            // get the target resource.
            Resource targetResource = registry.get(targetPath);
            if (CommonConstants.SERVICE_MEDIA_TYPE.equals(targetResource.getMediaType()) &&
                    CommonConstants.USED_BY.equals(requestContext.getAssociationType())) {
                // if so we are getting the service and add the endpoint to the source
                Resource sourceResource = registry.get(sourcePath);
                byte[] sourceContent = (byte[])sourceResource.getContent();
                if (sourceContent == null) {
                    return;
                }
                String endpointUrl = new String(sourceContent);
                String endpointEnv = sourceResource.getProperty(CommonConstants.ENDPOINT_ENVIRONMENT_ATTR);
                if (endpointEnv == null) {
                    endpointEnv = "";
                }
                if (endpointEnv.indexOf(",") > 0) {
                    for (String env : endpointEnv.split(",")) {
                        EndpointUtils.removeEndpointFromService(registry, targetPath, endpointUrl, env);
                    }
                } else {
                    EndpointUtils.removeEndpointFromService(registry, targetPath, endpointUrl, endpointEnv);
                }
            }
        } finally {
            CommonUtil.releaseAddingAssociationLock();
        }
    }
}
