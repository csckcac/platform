/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIManager;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APINameComparator;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserStoreException;

import java.io.InputStream;
import java.util.*;

/**
 * The basic abstract implementation of the core APIManager interface. This implementation uses
 * the governance system registry for storing APIs and related metadata.
 */
public abstract class AbstractAPIManager implements APIManager {
    
    protected Log log = LogFactory.getLog(getClass());
    
    protected Registry registry;
    protected ApiMgtDAO apiMgtDAO;

    public AbstractAPIManager() throws APIManagementException {
        apiMgtDAO = new ApiMgtDAO();
        try {
            this.registry = ServiceReferenceHolder.getInstance().
                    getRegistryService().getGovernanceSystemRegistry();
        } catch (RegistryException e) {
            handleException("Error while obtaining registry objects", e);
        }
    }

    public void cleanup() {

    }    

    public API getAPI(APIIdentifier identifier) throws APIManagementException {
        String apiPath = APIUtil.getAPIPath(identifier);
        try {
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,
                    APIConstants.API_KEY);
            Resource apiResource = registry.get(apiPath);
            String artifactId = apiResource.getUUID();
            if (artifactId == null) {
                throw new APIManagementException("artifact id is null for : " + apiPath);
            }
            GenericArtifact apiArtifact = artifactManager.getGenericArtifact(artifactId);
            return APIUtil.getAPI(apiArtifact, registry);

        } catch (RegistryException e) {
            handleException("Failed to get API from : " + apiPath, e);
            return null;
        }
    }

    public boolean isAPIAvailable(APIIdentifier identifier) throws APIManagementException {
        String path = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                identifier.getApiName() + RegistryConstants.PATH_SEPARATOR + identifier.getVersion();
        try {
            return registry.resourceExists(path);
        } catch (RegistryException e) {
            handleException("Failed to check availability of api :" + path, e);
            return false;
        }
    }

    public Set<String> getAPIVersions(String providerName, String apiName)
            throws APIManagementException {

        Set<String> versionSet = new HashSet<String>();
        String apiPath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
                providerName + RegistryConstants.PATH_SEPARATOR + apiName;
        try {
            Resource resource = registry.get(apiPath);
            if (resource instanceof Collection) {
                Collection collection = (Collection) resource;
                String[] versionPaths = collection.getChildren();
                if (versionPaths == null || versionPaths.length == 0) {
                    return versionSet;
                }
                for (String path : versionPaths) {
                    versionSet.add(path.split(apiPath)[1]);
                }
            } else {
                throw new APIManagementException("API version must be a collection " + apiName);
            }
        } catch (RegistryException e) {
            handleException("Failed to get versions for API: " + apiName, e);            
        }
        return versionSet;
    }

    public String addIcon(APIIdentifier identifier, InputStream in,
                        String contentType) throws APIManagementException {
        try {
            Resource thumb = registry.newResource();
            thumb.setContentStream(in);
            thumb.setMediaType(contentType);

            String artifactPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                    identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                    identifier.getApiName() + RegistryConstants.PATH_SEPARATOR + identifier.getVersion();

            String thumbPath = artifactPath + RegistryConstants.PATH_SEPARATOR + APIConstants.API_ICON_IMAGE;

            AuthorizationManager accessControlAdmin = ServiceReferenceHolder.getInstance().
                    getRegistryService().getUserRealm(IdentityUtil.getTenantIdOFUser(
                    identifier.getProviderName())).getAuthorizationManager();

            registry.put(thumbPath, thumb);

            if (!accessControlAdmin.isRoleAuthorized(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME,
                    RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + thumbPath, ActionConstants.GET)) {
                // Can we get rid of this?
                accessControlAdmin.authorizeRole(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME,
                        RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + thumbPath, ActionConstants.GET);
            }

            return RegistryConstants.PATH_SEPARATOR + "registry"
                    + RegistryConstants.PATH_SEPARATOR + "resource"
                    + RegistryConstants.PATH_SEPARATOR + "_system"
                    + RegistryConstants.PATH_SEPARATOR + "governance"
                    + thumbPath;
        } catch (RegistryException e) {
            handleException("Error while adding the icon image to the registry", e);
        } catch (UserStoreException e) {
            handleException("Error while obtaining the authorization manager", e);
        } catch (IdentityException e) {
            handleException("Error while checking user permissions", e);
        }
        return null;
    }

    public List<Documentation> getAllDocumentation(APIIdentifier apiId) throws APIManagementException {
        List<Documentation> documentationList = new ArrayList<Documentation>();
        String apiResourcePath =APIUtil.getAPIPath(apiId);
        try {
            Association[] docAssociations = registry.getAssociations(apiResourcePath,
                    APIConstants.DOCUMENTATION_ASSOCIATION);
            for (Association association : docAssociations) {
                String docPath = association.getDestinationPath();
                Resource docResource = registry.get(docPath);
                GenericArtifactManager artifactManager = new GenericArtifactManager(registry,
                        APIConstants.DOCUMENTATION_KEY);
                GenericArtifact docArtifact = artifactManager.getGenericArtifact(
                        docResource.getUUID());
                Documentation doc = APIUtil.getDocumentation(docArtifact);
                doc.setLastUpdated(docResource.getLastModified());
                documentationList.add(doc);
            }
        } catch (RegistryException e) {
            handleException("Failed to get documentations for api ", e);
        }
        return documentationList;
    }

    public Documentation getDocumentation(APIIdentifier apiId, DocumentationType docType,
                                          String docName) throws APIManagementException {
        Documentation documentation = null;
        String docPath = APIUtil.getAPIDocPath(apiId) + docName;
        GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,
                APIConstants.DOCUMENTATION_KEY);
        try {
            Association[] associations = registry.getAssociations(docPath, docType.getType());
            for (Association association : associations) {
                Resource docResource = registry.get(association.getSourcePath());
                GenericArtifact artifact = artifactManager.getGenericArtifact(docResource.getId());
                documentation = APIUtil.getDocumentation(artifact);
            }
        } catch (RegistryException e) {
            handleException("Failed to get documentation details", e);
        }
        return documentation;
    }

    public String getDocumentationContent(APIIdentifier identifier, String documentationName)
            throws APIManagementException {
        String contentPath = APIUtil.getAPIDocPath(identifier) +
                APIConstants.INLINE_DOCUMENT_CONTENT_DIR + RegistryConstants.PATH_SEPARATOR +
                documentationName;
        try {
            Resource docContent = registry.get(contentPath);
            return new String((byte[])docContent.getContent());
        } catch (RegistryException e) {
            String msg = "No document content found for documentation: "
                    + documentationName + " of API: "+identifier.getApiName();
            handleException(msg, e);
        }
        return null;
    }

    public Subscriber getSubscriberById(String accessToken) throws APIManagementException {
        return apiMgtDAO.getSubscriberById(accessToken);
    }

    public boolean isContextExist(String context) throws APIManagementException {
        try {
            GenericArtifactManager artifactManager = new GenericArtifactManager(registry,
                    APIConstants.API_KEY);
            GenericArtifact[] artifacts = artifactManager.getAllGenericArtifacts();
            for (GenericArtifact artifact : artifacts) {
                String artifactContext = artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT);
                if (artifactContext.equals(context)) {
                    return true;
                }
            }
        } catch (RegistryException e) {
            handleException("Failed to check context availability : " + context, e);
        }
        return false;
    }

    public void addSubscriber(Subscriber subscriber)
            throws APIManagementException {
        apiMgtDAO.addSubscriber(subscriber);
    }

    public void updateSubscriber(Subscriber subscriber)
            throws APIManagementException {
        apiMgtDAO.updateSubscriber(subscriber);
    }

    public Subscriber getSubscriber(int subscriberId)
            throws APIManagementException {
        return apiMgtDAO.getSubscriber(subscriberId);
    }

    public InputStream getIcon(APIIdentifier identifier) throws APIManagementException {
        String artifactPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                identifier.getApiName() + RegistryConstants.PATH_SEPARATOR + identifier.getVersion();

        String thumbPath = artifactPath + RegistryConstants.PATH_SEPARATOR + APIConstants.API_ICON_IMAGE;
        try {
            if (registry.resourceExists(thumbPath)) {
                Resource res = registry.get(thumbPath);
                return res.getContentStream();
            }
        } catch (RegistryException e) {
            handleException("Error while loading API icon from the registry", e);
        }
        return null;
    }

    public Set<API> getSubscriberAPIs(Subscriber subscriber) throws APIManagementException {
        SortedSet<API> apiSortedSet = new TreeSet<API>(new APINameComparator());
        Set<SubscribedAPI> subscribedAPIs = apiMgtDAO.getSubscribedAPIs(subscriber);

        for (SubscribedAPI subscribedAPI : subscribedAPIs) {
            String apiPath = APIUtil.getAPIPath(subscribedAPI.getApiId());
            Resource resource;
            try {
                resource = registry.get(apiPath);
                GenericArtifactManager artifactManager = new GenericArtifactManager(registry, APIConstants.API_KEY);
                GenericArtifact artifact = artifactManager.getGenericArtifact(
                        resource.getUUID());
                API api = APIUtil.getAPI(artifact, registry);
                apiSortedSet.add(api);
            } catch (RegistryException e) {
                handleException("Failed to get APIs for subscriber: " + subscriber.getName(), e);
            }
        }
        return apiSortedSet;
    }
    
    protected void handleException(String msg, Exception e) throws APIManagementException {
        log.error(msg, e);
        throw new APIManagementException(msg, e);
    }
}
