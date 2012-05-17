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

import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.governance.registry.extensions.handlers.utils.HandlerConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;

import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

public class PolicyUriHandler extends Handler {
    private static final Log log = LogFactory.getLog(PolicyUriHandler.class);
    private Registry systemGovernanceRegistry;

    public PolicyUriHandler() throws RegistryException {
        this.systemGovernanceRegistry = RegistryCoreServiceComponent.getRegistryService().getGovernanceSystemRegistry();
    }

    public void importResource(RequestContext requestContext, String sourceURL) throws RegistryException {
        if (!CommonUtil.isUpdateLockAvailable()) {
            return;
        }
        CommonUtil.acquireUpdateLock();
        try {

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
            addPolicyToRegistry(requestContext, inputStream, sourceURL);
        } finally {
            CommonUtil.releaseUpdateLock();
        }
    }

    private void addPolicyToRegistry(RequestContext requestContext, InputStream inputStream, String sourceURL) throws RegistryException {
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
        String policyPath = commonLocation + extractResourceFromURL(policyFileName, ".xml");


        String policyId = policyResource.getProperty(CommonConstants.ARTIFACT_ID_PROP_KEY);
        if (policyId == null) {
            // generate a service id
            policyId = UUID.randomUUID().toString();
            policyResource.setProperty(CommonConstants.ARTIFACT_ID_PROP_KEY, policyId);
        }
        CommonUtil.addGovernanceArtifactEntryWithAbsoluteValues(
                CommonUtil.getUnchrootedSystemRegistry(requestContext),
                policyId, policyPath);

        String relativeArtifactPath = RegistryUtils.getRelativePath(registry.getRegistryContext(), policyPath);
        // adn then get the relative path to the GOVERNANCE_BASE_PATH
        relativeArtifactPath = RegistryUtils.getRelativePathToOriginal(relativeArtifactPath,
                RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH);
        addPolicyToRegistry(requestContext, policyPath, sourceURL,
                policyResource, registry);
        ((ResourceImpl)policyResource).setPath(relativeArtifactPath);

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
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException if the operation failed.
     */
    protected void addPolicyToRegistry(RequestContext context, String path, String url,
                                       Resource resource, Registry registry) throws RegistryException {
        String source = getSource(url);
        GenericArtifactManager genericArtifactManager = new GenericArtifactManager(systemGovernanceRegistry, "uri");
        GenericArtifact policy = genericArtifactManager.newGovernanceArtifact(new QName(source));
        policy.setAttribute("overview_name", source);
        policy.setAttribute("overview_uri", url);
        policy.setAttribute("overview_type", HandlerConstants.POLICY);
        genericArtifactManager.addGenericArtifact(policy);
        Resource artifactResource = registry.get(
                RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + GovernanceConstants.GOVERNANCE_ARTIFACT_INDEX_PATH);
        artifactResource.setProperty(policy.getId(), HandlerConstants.POLICY_LOCATION + source);
        registry.put(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                GovernanceConstants.GOVERNANCE_ARTIFACT_INDEX_PATH, artifactResource); //TODO
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
                RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + HandlerConstants.POLICY_LOCATION);
    }

    public static String getSource(String uri){
        return uri.split("/")[uri.split("/").length -1];
    }
}
