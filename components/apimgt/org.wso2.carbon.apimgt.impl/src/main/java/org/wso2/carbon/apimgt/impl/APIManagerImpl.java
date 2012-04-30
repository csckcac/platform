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

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIManager;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserStoreException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

/**
 * This class represent the implementation of APIManager interface.
 */
public class APIManagerImpl implements APIManager {
    
    private static  Log log = LogFactory.getLog(APIManagerImpl.class);
    
    private static final long KEEP_ALIVE_TASK_PERIOD = 950000L;
    
    private GenericArtifactManager artifactManager;
    private Registry registry;
    private ApiMgtDAO apiMgtDAO = new ApiMgtDAO();
    
    private ScheduledExecutorService scheduler;
    private Future keepAliveTask;

    public APIManagerImpl(String user,
                          String pass,
                          String remoteAdd) throws APIManagementException {
        this.registry = getRegistry();
        startKeepAliveTask();
    }

    private void startKeepAliveTask() {
        scheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            public Thread newThread(Runnable r) {
                return new Thread(r, "am-registry-keep-alive-task");
            }
        });
        keepAliveTask = scheduler.scheduleAtFixedRate(new Runnable() {
            public void run() {
                if (log.isDebugEnabled()) {
                    log.debug("Executing registry keep-alive task");
                }
                try {
                    registry.resourceExists("/");
                } catch (RegistryException e) {
                    log.warn("Error occurred while checking registry availability", e);
                }
            }
        }, KEEP_ALIVE_TASK_PERIOD, KEEP_ALIVE_TASK_PERIOD, TimeUnit.MILLISECONDS);
    }

    /**
     * This method use to clean up the spawned
     * RegistryKeepAliveTask thread in a server shutdown
     */
    public void cleanup() {
        if (keepAliveTask != null) {
            keepAliveTask.cancel(true);
        }
        scheduler.shutdownNow();
    }    

    /**
     * returns details of an API
     *
     * @param identifier APIIdentifier
     * @return API
     * @throws APIManagementException if failed get API from APIIdentifier
     */
    public API getAPI(APIIdentifier identifier) throws APIManagementException {
        API api = null;
        String apiPath = APIUtil.getAPIPath(identifier);
        try {
            artifactManager = getArtifactManager(APIConstants.API_KEY);
            Resource apiResource = registry.get(apiPath);
            String artifactId = apiResource.getProperty(GovernanceConstants.ARTIFACT_ID_PROP_KEY);
            if (artifactId == null) {
                throw new APIManagementException("artifact id is null for : " + apiPath);
            }
            GenericArtifact apiArtifact = artifactManager.getGenericArtifact(artifactId);
            api = APIUtil.getAPI(apiArtifact, registry);

        } catch (RegistryException e) {
            handleException("Failed to get API from : " + apiPath, e);
        }

        return api;
    }

    /**
     * Check the Availability of given APIIdentifier
     *
     * @param identifier APIIdentifier
     * @return true, if already exists. False, otherwise
     * @throws APIManagementException if failed to get API availability
     */
    public boolean isAPIAvailable(APIIdentifier identifier) throws APIManagementException {
        boolean availability = false;
        String path = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                identifier.getApiName() + RegistryConstants.PATH_SEPARATOR + identifier.getVersion();
        try {
            if (registry.resourceExists(path)) {
                availability = true;
            }
        } catch (RegistryException e) {
            handleException("Failed to check availability of api :" + path, e);
        }
        return availability;
    }

    /**
     * this method return Set of versions for given provider and api
     *
     * @param providerName name of the provider
     * @param apiName      name of the api
     * @return Set of version
     * @throws APIManagementException if failed to get version for api
     */
    public Set<String> getAPIVersions(String providerName, String apiName)
            throws APIManagementException {

        Set<String> versionSet = new HashSet<String>();
        String apiPath = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + APIConstants.API_LOCATION +
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
                throw new APIManagementException("API version should be a collection " + apiName);
            }
        } catch (RegistryException e) {
            handleException("Failed to get versions for API: " + apiName, e);            
        }
        return versionSet;
    }

    /**
     * @param username Name of the user
     * @param password Password of the user
     * @return login status
     */
    public boolean login(String username, String password) {
        boolean result = false;
//        //TODO this is not finish
        return result;
    }

    /**
     * Log out user
     *
     * @param username name of the user
     */
    public void logout(String username) {
        //TODO this is not finish
    }

    public String addApiThumb(API api, FileItem fileItem) throws RegistryException, IOException,
            APIManagementException, UserStoreException, IdentityException {
        Resource thumb = registry.newResource();
        thumb.setContentStream(fileItem.getInputStream());
        thumb.setMediaType(fileItem.getContentType());

        String artifactPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                api.getId().getProviderName() + RegistryConstants.PATH_SEPARATOR + api.getId().getApiName()
                + RegistryConstants.PATH_SEPARATOR + api.getId().getVersion();

        String thumbPath = artifactPath + RegistryConstants.PATH_SEPARATOR + fileItem.getName();

        //AuthorizationManager accessControlAdmin =
        //CarbonContext.getCurrentContext().getUserRealm().getAuthorizationManager();

        AuthorizationManager accessControlAdmin = ServiceReferenceHolder.getInstance().
                getRegistryService().getUserRealm(IdentityUtil.getTenantIdOFUser(
                api.getId().getProviderName())).getAuthorizationManager();

        registry.put(thumbPath, thumb);

        if (!accessControlAdmin.isRoleAuthorized(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME,
                RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + thumbPath, ActionConstants.GET)) {
            accessControlAdmin.authorizeRole(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME,
                    RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + thumbPath, ActionConstants.GET);
        }

        return RegistryConstants.PATH_SEPARATOR + "registry"
                + RegistryConstants.PATH_SEPARATOR + "resource"
                + RegistryConstants.PATH_SEPARATOR + "_system"
                + RegistryConstants.PATH_SEPARATOR + "governance"
                + thumbPath;
    }

    /**
     * Returns a list of all Documentation attached to a particular API Version
     *
     * @param apiId APIIdentifier
     * @return List<Documentation>
     * @throws APIManagementException if failed to get Documentations
     */
    public List<Documentation> getAllDocumentation(APIIdentifier apiId) throws APIManagementException {
        List<Documentation> documentationList = new ArrayList<Documentation>();
        String apiResourcePath =APIUtil.getAPIPath(apiId);
        try {
            Association[] docAssociations = registry.getAssociations(apiResourcePath,
                    APIConstants.DOCUMENTATION_ASSOCIATION);
            for (Association association : docAssociations) {
                String docPath = association.getDestinationPath();
                Resource docResource = registry.get(docPath);
                artifactManager = new GenericArtifactManager(registry, APIConstants.DOCUMENTATION_KEY);
                GenericArtifact docArtifact = artifactManager.getGenericArtifact(
                        docResource.getProperty(GovernanceConstants.ARTIFACT_ID_PROP_KEY));
                Documentation doc = APIUtil.getDocumentation(docArtifact);
                doc.setLastUpdated(docResource.getLastModified());
                documentationList.add(doc);
            }
        } catch (RegistryException e) {
            handleException("Failed to get documentations for api ", e);
        }
        return documentationList;
    }

    /**
     * Returns a given documentation
     *
     * @param apiId   APIIdentifier
     * @param docType type of the documentation
     * @param docName name of the doc
     * @return Documentation
     * @throws APIManagementException if failed to get Documentation
     */
    public Documentation getDocumentation(APIIdentifier apiId, DocumentationType docType,
                                          String docName) throws APIManagementException {
        Documentation documentation = null;
        String docPath = APIUtil.getAPIDocPath(apiId) + docName;
        artifactManager = getArtifactManager(APIConstants.DOCUMENTATION_KEY);
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

    /**
     * This method used to get the content of a documentation
     *
     * @param identifier, API identifier
     * @param documentationName, name of the inline documentation
     * @throws APIManagementException if the asking documentation content is unavailable
     */
    public String getDocumentationContent(APIIdentifier identifier, String documentationName)
            throws APIManagementException {
        String contentPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                identifier.getApiName() + RegistryConstants.PATH_SEPARATOR +
                identifier.getVersion() + RegistryConstants.PATH_SEPARATOR +
                APIConstants.DOC_DIR + RegistryConstants.PATH_SEPARATOR +
                APIConstants.INLINE_DOCUMENT_CONTENT_DIR + RegistryConstants.PATH_SEPARATOR +
                documentationName;
        try {
            Resource docContent = registry.get(contentPath);
            return new String((byte[])docContent.getContent());
        } catch (RegistryException e) {
            String msg = "No document content found for documentation : "
                    + documentationName + " of API :"+identifier.getApiName();
            handleException(msg, e);
        }
        return null;
    }

    /**
     * Get the Subscriber from access token
     *
     * @param accessToken Subscriber key
     * @return Subscriber
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to get Subscriber from access token
     */
    public Subscriber getSubscriberById(String accessToken) throws APIManagementException {
        return apiMgtDAO.getSubscriberById(accessToken);
    }

    /**
     * this method used to initialized the ArtifactManager
     *
     * @param key , key name of the key
     * @return GenericArtifactManager
     * @throws APIManagementException if failed to initialized GenericArtifactManager
     */
    private GenericArtifactManager getArtifactManager(String key) throws APIManagementException {
        try {
            artifactManager = new GenericArtifactManager(registry, key);
        } catch (RegistryException e) {
            String msg = "Failed to initialized GenericArtifactManager";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
        return artifactManager;
    }

    private Registry getRegistry() throws APIManagementException {
        try {
            registry = RegistryCoreServiceComponent.getRegistryService().getGovernanceSystemRegistry();
        } catch (RegistryException e) {
            handleException("Error while obtaining a registry instance", e);
        }
        return registry;
    }

    /**
     * @param context api context url
     * @return context availability
     * @throws APIManagementException if failed to check context availability
     */
    public boolean isContextExist(String context) throws APIManagementException {
        boolean available = false;
        try {
            artifactManager = new GenericArtifactManager(registry, APIConstants.API_KEY);
            GenericArtifact[] artifacts = artifactManager.getAllGenericArtifacts();
            for (GenericArtifact artifact : artifacts) {
                String artifactContext = artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT);
                if (artifactContext.equals(context)) {
                    available = true;
                    break;
                }
            }
        } catch (RegistryException e) {
            String msg = "Failed to check context availability : " + context;
            throw new APIManagementException(msg, e);
        }
        return available;
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

    public String getThumbAsString(String thumbPath) throws RegistryException, IOException {
        Resource res = registry.get(thumbPath);
        res.getContent();
        InputStreamReader r = new InputStreamReader(res.getContentStream());
        return IOUtils.toString(res.getContentStream(), r.getEncoding());
    }
    
    private void handleException(String msg, Exception e) throws APIManagementException {
        log.error(msg, e);
        throw new APIManagementException(msg, e);
    }
}
