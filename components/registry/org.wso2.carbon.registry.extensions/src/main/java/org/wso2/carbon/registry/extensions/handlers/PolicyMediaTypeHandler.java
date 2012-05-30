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

import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.core.utils.AuthorizationUtils;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;
import org.wso2.carbon.user.mgt.UserMgtConstants;
import org.apache.axiom.om.OMElement;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.net.URL;
import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.util.UUID;

public class PolicyMediaTypeHandler extends Handler {
    private static final Log log = LogFactory.getLog(PolicyMediaTypeHandler.class);

    private String location = "/policies/";
    private String locationTag = "location";
    private OMElement locationConfiguration;

    public OMElement getPolicyLocationConfiguration() {
        return locationConfiguration;
    }

    public void setPolicyLocationConfiguration(OMElement locationConfiguration) throws RegistryException {
        Iterator confElements = locationConfiguration.getChildElements();
        while (confElements.hasNext()) {
            OMElement confElement = (OMElement)confElements.next();
            if (confElement.getQName().equals(new QName(locationTag))) {
                location = confElement.getText();
                if (!location.startsWith(RegistryConstants.PATH_SEPARATOR)) {
                    location = RegistryConstants.PATH_SEPARATOR + location;
                }
                if (!location.endsWith(RegistryConstants.PATH_SEPARATOR)) {
                    location = location + RegistryConstants.PATH_SEPARATOR;
                }
            }
        }
        AuthorizationUtils.addAuthorizeRoleListener(
                RegistryConstants.ADD_POLICY_AUTHORIZE_ROLE_LISTENER_EXECUTION_ORDER_ID,
                getChrootedLocation(RegistryContext.getBaseInstance()),
                UserMgtConstants.UI_ADMIN_PERMISSION_ROOT + "manage/resources/govern/metadata/add",
                UserMgtConstants.EXECUTE_ACTION);
        AuthorizationUtils.addAuthorizeRoleListener(
                RegistryConstants.LIST_POLICY_AUTHORIZE_ROLE_LISTENER_EXECUTION_ORDER_ID, 
                getChrootedLocation(RegistryContext.getBaseInstance()),
                UserMgtConstants.UI_ADMIN_PERMISSION_ROOT + "manage/resources/govern/metadata/list",
                UserMgtConstants.EXECUTE_ACTION, new String[]{ActionConstants.GET});
        this.locationConfiguration = locationConfiguration;
    }

    public void put(RequestContext requestContext) throws RegistryException {
        if (!CommonUtil.isUpdateLockAvailable()) {
            return;
        }
        CommonUtil.acquireUpdateLock();
        try {
            if (requestContext == null) {
                throw new RegistryException("The request context is not available.");
            }
            String path = requestContext.getResourcePath().getPath();
            Resource resource = requestContext.getResource();
            Registry registry = requestContext.getRegistry();

            Object newContent = resource.getContent();
            if (newContent instanceof String) {
                newContent = ((String)newContent).getBytes();
            }
            try {
                // If the policy is already there, we don't need to re-run this handler unless the content is changed.
                // Re-running this handler causes issues with downstream handlers and other behaviour (ex:- lifecycles).
                // If you need to do a replace programatically, delete-then-replace.
                if (registry.resourceExists(path)) {
                    Resource oldResource = registry.get(path);
                    Object oldContent = oldResource.getContent();
                    if ((newContent == null && oldContent == null) ||
                            (newContent != null && newContent.equals(oldContent))) {
                        // this will continue adding from the default path.
                        return;
                    }
                }
            } catch (Exception e) {
                String msg = "Error in comparing the policy content updates. policy path: " + path + ".";
                log.error(msg, e);
                throw new RegistryException(msg, e);
            }
            if (newContent != null) {
                InputStream inputStream = new ByteArrayInputStream((byte[])newContent);
                addPolicyToRegistry(requestContext, inputStream);
            }
        } finally {
            CommonUtil.releaseUpdateLock();
        }
    }

    public void importResource(RequestContext requestContext) throws RegistryException {
        if (!CommonUtil.isUpdateLockAvailable()) {
            return;
        }
        CommonUtil.acquireUpdateLock();
        try {
            String sourceURL = requestContext.getSourceURL();
            InputStream inputStream;
            try {
                if (sourceURL != null && sourceURL.toLowerCase().startsWith("file:")) {
                    String msg = "The source URL must not be file in the server's local file system";
                    throw new RegistryException(msg);
                }
                inputStream = new URL(sourceURL).openStream();
            } catch (IOException e) {
                throw new RegistryException("The URL " + sourceURL + " is incorrect.", e);
            }
            addPolicyToRegistry(requestContext, inputStream);
        } finally {
            CommonUtil.releaseUpdateLock();
        }
    }

    private void addPolicyToRegistry(RequestContext requestContext, InputStream inputStream) throws RegistryException {
        Resource policyResource;
        if (requestContext.getResource() == null) {
            policyResource = new ResourceImpl();
            policyResource.setMediaType("application/policy+xml");
        } else {
            policyResource = requestContext.getResource();
        }
        Registry registry = requestContext.getRegistry();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int nextChar;
        try {
            while ((nextChar = inputStream.read()) != -1) {
                outputStream.write(nextChar);
            }
            outputStream.flush();
        } catch (IOException e) {
            throw new RegistryException("Exception occured while reading policy content", e);
        }
        policyResource.setContent(outputStream.toByteArray());

        try {
            AXIOMUtil.stringToOM(new String(outputStream.toByteArray()));   
        } catch (Exception e) {
            throw new RegistryException("The given policy file does not contain valid XML.");
        }

        String resourcePath = requestContext.getResourcePath().getPath();
        String policyFileName = resourcePath.substring(resourcePath.lastIndexOf(RegistryConstants.PATH_SEPARATOR) + 1);
        Registry systemRegistry = CommonUtil.getUnchrootedSystemRegistry(requestContext);
        String commonLocation = getChrootedLocation(requestContext.getRegistryContext());
        if (!systemRegistry.resourceExists(commonLocation)) {
            systemRegistry.put(commonLocation, systemRegistry.newCollection());
        }

        String policyPath;
        if(!resourcePath.startsWith(commonLocation)
                && !resourcePath.equals(RegistryConstants.PATH_SEPARATOR + policyFileName)
                && !resourcePath.equals(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH
                + RegistryConstants.PATH_SEPARATOR +policyFileName)){
            policyPath = resourcePath;
        }else{
            policyPath = commonLocation + extractResourceFromURL(policyFileName, ".xml");
        }


        String policyId = policyResource.getUUID();
        if (policyId == null) {
            // generate a service id
            policyId = UUID.randomUUID().toString();
            policyResource.setUUID(policyId);
        }
//        CommonUtil.addGovernanceArtifactEntryWithAbsoluteValues(
//                    CommonUtil.getUnchrootedSystemRegistry(requestContext),
//                    policyId, policyPath);

        String relativeArtifactPath = RegistryUtils.getRelativePath(registry.getRegistryContext(), policyPath);
        // adn then get the relative path to the GOVERNANCE_BASE_PATH
        relativeArtifactPath = RegistryUtils.getRelativePathToOriginal(relativeArtifactPath,
                RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH);
        addPolicyToRegistry(requestContext, policyPath, requestContext.getSourceURL(),
                policyResource, registry);
        ((ResourceImpl)policyResource).setPath(relativeArtifactPath);

        String symlinkLocation = RegistryUtils.getAbsolutePath(requestContext.getRegistryContext(),
                policyResource.getProperty(RegistryConstants.SYMLINK_PROPERTY_NAME));
        if (symlinkLocation != null) {
            Resource resource = requestContext.getRegistry().get(symlinkLocation);
            if (resource != null) {
                String isLink = resource.getProperty("registry.link");
                String mountPoint = resource.getProperty("registry.mountpoint");
                String targetPoint = resource.getProperty("registry.targetpoint");
                String actualPath = resource.getProperty("registry.actualpath");
                if (isLink != null && mountPoint != null && targetPoint != null) {
                    symlinkLocation = actualPath + RegistryConstants.PATH_SEPARATOR;
                }
            }
            requestContext.getSystemRegistry().createLink(symlinkLocation + policyFileName, policyPath);
        }
        requestContext.setProcessingComplete(true);
    }

    /**
     * Method that gets called instructing a policy to be added the registry.
     *
     * @param context  the request context for this request.
     * @param path     the path to add the resource to.
     * @param url      the path from which the resource was imported from.
     * @param resource the resource to be added.
     * @param registry the registry instance to use.
     *
     * @throws RegistryException if the operation failed.
     */
    protected void addPolicyToRegistry(RequestContext context, String path, String url,
                                       Resource resource, Registry registry) throws RegistryException {
        context.setActualPath(path);
        registry.put(path, resource);
    }

    private String extractResourceFromURL(String policyURL, String suffix) {
        String resourceName = policyURL;
        if (policyURL.lastIndexOf("?") > 0) {
            resourceName = policyURL.substring(0, policyURL.indexOf("?")) + suffix;
        } else if (policyURL.indexOf(".") > 0) {
            resourceName = policyURL.substring(0, policyURL.lastIndexOf(".")) + suffix;
        } else if (!policyURL.endsWith(suffix)) {
            resourceName = policyURL + suffix;
        }
        return resourceName;
    }

    private String getChrootedLocation(RegistryContext registryContext) {
        return RegistryUtils.getAbsolutePath(registryContext, 
                RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + location);
    }
}
