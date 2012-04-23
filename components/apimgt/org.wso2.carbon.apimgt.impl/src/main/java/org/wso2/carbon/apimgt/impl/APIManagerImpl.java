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
import org.wso2.carbon.apimgt.api.dto.UserApplicationAPIUsage;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.api.model.Tag;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.internal.APIManagerComponent;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Comment;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public APIManagerImpl(HttpServletRequest request, ServletConfig config)
            throws APIManagementException {
        HttpSession session = request.getSession();
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        try {
            this.registry = GovernanceUtils.getGovernanceUserRegistry(
                    new WSRegistryServiceClient(backendServerURL, cookie),
                    (String) session.getAttribute("logged-user"));
            startKeepAliveTask();
        } catch (RegistryException e) {
            handleException("Unable to obtain an instance of the registry", e);
        }
    }

    public APIManagerImpl(String user,
                          String pass,
                          String remoteAdd) throws APIManagementException {
        this.registry = getRegistry(user, pass, remoteAdd);
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
     * @param subscriberId id of the Subscriber
     * @return Subscriber
     * @throws APIManagementException if failed to get Subscriber
     */
    public Subscriber getSubscriber(String subscriberId) throws APIManagementException {
        Subscriber subscriber = null;
        try {
            subscriber = apiMgtDAO.getSubscriber(subscriberId);
        } catch (APIManagementException e) {
            handleException("Failed to get Subscriber", e);
        }
        return subscriber;
    }

    /**
     * Returns a list of #{@link org.wso2.carbon.apimgt.api.model.API} bearing the selected tag
     *
     * @param tag name of the tag
     * @return set of API having the given tag name
     * @throws APIManagementException if failed to get set of API
     */
    public Set<API> getAPIsWithTag(String tag) throws APIManagementException {
        Set<API> apiSet = new HashSet<API>();
        try {
            artifactManager = getArtifactManager(APIConstants.API_KEY);
            GenericArtifact[] genericArtifacts = artifactManager.getAllGenericArtifacts();
            if (genericArtifacts == null || genericArtifacts.length == 0) {
                return apiSet;
            }
            for (GenericArtifact artifact : genericArtifacts) {
                String status = artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS);
                if (!status.equals(APIConstants.PUBLISHED)) {
                    continue;
                }
                String artifactPath = artifact.getPath();
                org.wso2.carbon.registry.core.Tag[] tags = registry.getTags(artifactPath);
                if (tags == null || tags.length == 0) {
                    break;
                }
                for (org.wso2.carbon.registry.core.Tag tag1 : tags) {
                    if (tag.equals(tag1.getTagName())) {
                        apiSet.add(APIUtils.getAPI(artifact, registry));
                    }
                }
            }
        } catch (RegistryException e) {
            handleException("Failed to get API for tag " + tag, e);
        }
        return apiSet;
    }

    /**
     * Returns a list of all #{@link org.wso2.carbon.apimgt.api.model.Provider} available on the system.
     *
     * @return Set<Provider>
     * @throws APIManagementException if failed to get Providers
     */
    public Set<Provider> getAllProviders() throws APIManagementException {
        Set<Provider> providerSet = new HashSet<Provider>();
        artifactManager = getArtifactManager(APIConstants.PROVIDER_KEY);
        try {
            GenericArtifact[] genericArtifact = artifactManager.getAllGenericArtifacts();
            if (genericArtifact == null || genericArtifact.length == 0) {
                return providerSet;
            }
            for (GenericArtifact artifact : genericArtifact) {
                Provider provider =
                        new Provider(artifact.getAttribute(APIConstants.PROVIDER_OVERVIEW_NAME));
                provider.setDescription(APIConstants.PROVIDER_OVERVIEW_DESCRIPTION);
                provider.setEmail(APIConstants.PROVIDER_OVERVIEW_EMAIL);
                providerSet.add(provider);
            }
        } catch (GovernanceException e) {
            handleException("Failed to get all providers", e);
        }
        return providerSet;
    }


    /**
     * Returns a list of all published APIs. If a given API has multiple APIs,
     * only the latest version will be included in this list.
     *
     * @return set of API
     * @throws APIManagementException if failed to API set
     */
    public Set<API> getAllPublishedAPIs() throws APIManagementException {
        SortedSet<API> apiSortedSet = new TreeSet<API>(new APIComparator());
        try {
            artifactManager = getArtifactManager(APIConstants.API_KEY);
            GenericArtifact[] genericArtifacts = artifactManager.getAllGenericArtifacts();
            if (genericArtifacts == null || genericArtifacts.length == 0) {
                return apiSortedSet;
            }
            for (GenericArtifact artifact : genericArtifacts) {
                // adding the API provider can mark the latest API .
                String status = artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS);
                if (status.equals(APIConstants.PUBLISHED)) {
                    apiSortedSet.add(APIUtils.getAPI(artifact, registry));
                }
            }
        } catch (RegistryException e) {
            handleException("Failed to get all publishers", e);
        }
        return apiSortedSet;
    }

    /**
     * Get a list of APIs published by the given provider. If a given API has multiple APIs,
     * only the latest version will be included in this list.
     *
     * @param providerId , provider id
     * @return set of API
     * @throws APIManagementException if failed to get set of API
     */
    public List<API> getAPIsByProvider(String providerId) throws APIManagementException {

        List<API> apiSortedList = new ArrayList<API>();

        try {
            String providerPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                    providerId;

            artifactManager = getArtifactManager(APIConstants.API_KEY);
            Association[] associations = registry.getAssociations(providerPath,
                    APIConstants.PROVIDER_ASSOCIATION);
            for (Association association : associations) {
                String apiPath = association.getDestinationPath();
                Resource resource = registry.get(apiPath);
                String apiArtifactId = resource.getProperty(GovernanceConstants.ARTIFACT_ID_PROP_KEY);
                if (apiArtifactId != null) {
                    GenericArtifact apiArtifact = artifactManager.getGenericArtifact(apiArtifactId);
                    apiSortedList.add(APIUtils.getAPI(apiArtifact, registry));
                } else {
                    throw new GovernanceException("artifact id is null of " + apiPath);
                }
            }

        } catch (RegistryException e) {
            handleException("Failed to get APIs for provider : " + providerId, e);
        }
        Collections.sort(apiSortedList, new APIComparator());

        return apiSortedList;

    }

    /**
     * Returns top rated APIs
     *
     * @param limit if -1, no limit. Return everything else, limit the return list to specified value.
     * @return Set of API
     * @throws APIManagementException if failed to get top rated APIs
     */
    public Set<API> getTopRatedAPIs(int limit) throws APIManagementException {
        int returnLimit = 0;
        SortedSet<API> apiSortedSet = new TreeSet<API>(new APIComparator());
        try {
            artifactManager = getArtifactManager(APIConstants.API_KEY);
            GenericArtifact[] genericArtifacts = artifactManager.getAllGenericArtifacts();
            if (genericArtifacts == null || genericArtifacts.length == 0) {
                return apiSortedSet;
            }
            for (GenericArtifact genericArtifact : genericArtifacts) {
                String status = genericArtifact.getAttribute(APIConstants.API_OVERVIEW_STATUS);
                if (status.equals(APIConstants.PUBLISHED)) {
                    String artifactPath = genericArtifact.getPath();

                    float rating = registry.getAverageRating(artifactPath);
                    if (rating > APIConstants.TOP_TATE_MARGIN && (returnLimit < limit)) {
                        returnLimit++;
                        apiSortedSet.add(APIUtils.getAPI(genericArtifact, registry));
                    }
                }
            }
        } catch (RegistryException e) {
            handleException("Failed to get top rated API", e);
        }
        return apiSortedSet;
    }

    /**
     * Get recently added APIs to the store
     *
     * @param limit if -1, no limit. Return everything else, limit the return list to specified value.
     * @return set of API
     * @throws APIManagementException if failed to get recently added APIs
     */
    public Set<API> getRecentlyAddedAPIs(int limit) throws APIManagementException {

        int start = 0;
        SortedSet<API> apiSortedSet = new TreeSet<API>(new APIComparator());
        Map<Date, GenericArtifact> apiMap = new HashMap<Date, GenericArtifact>();
        List<Date> dateList = new ArrayList<Date>();
        try {
            artifactManager = getArtifactManager(APIConstants.API_KEY);
            GenericArtifact[] genericArtifact = artifactManager.getAllGenericArtifacts();
            if (genericArtifact == null || genericArtifact.length == 0) {
                return apiSortedSet;
            }

            for (GenericArtifact artifact : genericArtifact) {
                String status = artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS);
                if (status.equals(APIConstants.PUBLISHED)) {
                    String artifactPath = artifact.getPath();
                    Resource resource = registry.get(artifactPath);
                    Date createdDate = resource.getCreatedTime();
                    apiMap.put(createdDate, artifact);
                    dateList.add(createdDate);
                }
            }
            Collections.sort(dateList);
            if (limit < dateList.size()) {
                start = dateList.size() - limit;
            }
            for (int i = start; i < dateList.size(); i++) {
                GenericArtifact genericArtifact1 = apiMap.get(dateList.get(i));

                apiSortedSet.add(APIUtils.getAPI(genericArtifact1, registry));
            }

        } catch (RegistryException e) {
            handleException("Failed to get recently added APIs", e);
        }
        return apiSortedSet;
    }

    /**
     * @return a list of all Tags applied to all APIs published.
     * @throws APIManagementException if failed to get All the tags
     */
    public Set<Tag> getAllTags() throws APIManagementException {
        Set<Tag> tagSet = new HashSet<Tag>();
        try {
            artifactManager = getArtifactManager(APIConstants.API_KEY);
            GenericArtifact[] genericArtifact = artifactManager.getAllGenericArtifacts();

            if (genericArtifact == null || genericArtifact.length == 0) {
                return tagSet;
            }
            for (GenericArtifact artifact : genericArtifact) {
                String path = artifact.getPath();
                org.wso2.carbon.registry.core.Tag[] regTags = registry.getTags(path);
                String status = artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS);
                if (status.equals(APIConstants.PUBLISHED) && regTags !=null && regTags.length>0) {
                        for (org.wso2.carbon.registry.core.Tag regTag : regTags) {
                            tagSet.add(new Tag(regTag.getTagName()));
                    }
                }
            }
        } catch (RegistryException e) {
            handleException("Failed to get all the tags", e);
        }
        return tagSet;
    }

    /**
     * Rate a particular API. This will be called when subscribers rate an API
     *
     * @param apiId  The API identifier
     * @param rating The rating provided by the subscriber
     * @throws APIManagementException If an error occurs while rating the API
     */
    public void rateAPI(APIIdentifier apiId, APIRating rating) throws APIManagementException {
        String path = APIUtils.getAPIPath(apiId);
        /*
           * String apiPath = apiId.getProviderName() +
           * RegistryConstants.PATH_SEPARATOR + apiId.getApiName() +
           * RegistryConstants.PATH_SEPARATOR + apiId.getVersion();
           */
        try {
            registry.rateResource(path, rating.getRating());
        } catch (RegistryException e) {
            handleException("Failed to rate API : " + path, e);
        }
    }

    /**
     * Search matching APIs for given search terms
     *
     * @param searchTerm , name of the search term
     * @return Set<API>
     * @throws APIManagementException if failed to get APIs for given search term
     */
    public Set<API> searchAPI(String searchTerm) throws APIManagementException {
        Set<API> apiSet = new HashSet<API>();
        String regex = "[a-zA-Z0-9_.-|]*" + searchTerm + "[a-zA-Z0-9_.-|]*";
        Pattern pattern;
        Matcher matcher;
        try {
            artifactManager = getArtifactManager(APIConstants.API_KEY);
            GenericArtifact[] genericArtifacts = artifactManager
                    .getAllGenericArtifacts();
            if (genericArtifacts == null || genericArtifacts.length == 0) {
                return apiSet;
            }
            for (GenericArtifact artifact : genericArtifacts) {
                String status = artifact
                        .getAttribute(APIConstants.API_OVERVIEW_STATUS);
                String apiName = artifact
                        .getAttribute(APIConstants.API_OVERVIEW_NAME);
                pattern = Pattern.compile(regex);
                matcher = pattern.matcher(apiName);
                if (matcher.matches() && status.equals(APIConstants.PUBLISHED)) {
                        apiSet.add(APIUtils.getAPI(artifact, registry));
                }
            }
        } catch (RegistryException e) {
            handleException("Failed to Search APIs", e);
        }
        return apiSet;
    }

    public Set<API> searchAPI(String searchTerm, String searchType) throws APIManagementException {
        Set<API> apiSet = new HashSet<API>();
        String regex = "[a-zA-Z0-9_.-|]*" + searchTerm + "[a-zA-Z0-9_.-|]*";
        Pattern pattern;
        Matcher matcher;
        try {
            artifactManager = getArtifactManager(APIConstants.API_KEY);
            GenericArtifact[] genericArtifacts = artifactManager
                    .getAllGenericArtifacts();
            if (genericArtifacts == null || genericArtifacts.length == 0) {
                return apiSet;
            }
            for (GenericArtifact artifact : genericArtifacts) {
                String status = artifact
                        .getAttribute(APIConstants.API_OVERVIEW_STATUS);

                pattern = Pattern.compile(regex);


                if (searchType.equals("APIProvider")) {
                    String api = artifact
                            .getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
                    matcher = pattern.matcher(api);
                } else if (searchType.equals("APIVersion")) {
                    String api = artifact
                            .getAttribute(APIConstants.API_OVERVIEW_VERSION);
                    matcher = pattern.matcher(api);
                } else if (searchType.equals("APIContext")) {
                    String api = artifact
                            .getAttribute(APIConstants.API_OVERVIEW_CONTEXT);
                    matcher = pattern.matcher(api);
                } else {
                    String apiName = artifact
                            .getAttribute(APIConstants.API_OVERVIEW_NAME);
                    matcher = pattern.matcher(apiName);
                }
                if (matcher.matches() && status.equals(APIConstants.PUBLISHED)) {
                        apiSet.add(APIUtils.getAPI(artifact, registry));
                }
            }
        } catch (RegistryException e) {
            handleException("Failed to search APIs with type", e);
        }
        return apiSet;
    }

    /**
     * Returns a list of APIs purchased by the given Subscriber
     *
     * @param subscriber Subscriber
     * @return Set<API>
     * @throws APIManagementException if failed to get API for subscriber
     */
    public Set<SubscribedAPI> getSubscribedAPIs(Subscriber subscriber) throws APIManagementException {
        Set<SubscribedAPI> subscribedAPIs = null;
        try {
            subscribedAPIs = apiMgtDAO.getSubscribedAPIs(subscriber);
        } catch (APIManagementException e) {
            handleException("Failed to get APIs of " + subscriber.getName(), e);
        }
        return subscribedAPIs;
    }

    /**
     * Get SubscribedAPI set for given subscriber and APIIdentifier
     *
     * @param subscriber Subscriber
     * @param identifier APIIdentifier
     * @return Set<SubscribedAPI>
     * @throws APIManagementException if failed to get set of SubscribedAPI of given Subscriber and
     *                                APIIdentifier
     */
    public Set<SubscribedAPI> getSubscribedIdentifiers(Subscriber subscriber, APIIdentifier identifier)
            throws APIManagementException {
        Set<SubscribedAPI> subscribedAPISet = new HashSet<SubscribedAPI>();
        Set<SubscribedAPI> subscribedAPIs = getSubscribedAPIs(subscriber);
        for (SubscribedAPI api : subscribedAPIs) {
            if (api.getApiId().equals(identifier)) {
                subscribedAPISet.add(api);
            }
        }
        return subscribedAPISet;
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
        String apiPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                identifier.getApiName() + RegistryConstants.PATH_SEPARATOR +
                identifier.getVersion() + APIConstants.API_RESOURCE_NAME;
        try {
            artifactManager = getArtifactManager(APIConstants.API_KEY);
            Resource apiResource = registry.get(apiPath);
            String artifactId = apiResource.getProperty(GovernanceConstants.ARTIFACT_ID_PROP_KEY);
            if (artifactId == null) {
                throw new APIManagementException("artifact id is null for : " + apiPath);
            }
            GenericArtifact apiArtifact = artifactManager.getGenericArtifact(artifactId);
            api = APIUtils.getAPI(apiArtifact, registry);

        } catch (RegistryException e) {
            handleException("Failed to get API from : " + apiPath, e);
        }

        return api;
    }

    /**
     * Get a list of all the consumers for all APIs
     *
     * @param providerId if of the provider
     * @return Set<Subscriber>
     * @throws APIManagementException if failed to get subscribed APIs of given provider
     */
    public Set<Subscriber> getSubscribersOfProvider(String providerId)
            throws APIManagementException {

        Set<Subscriber> subscriberSet = null;
        try {
            subscriberSet = apiMgtDAO.getSubscribersOfProvider(providerId);
        } catch (APIManagementException e) {
            handleException("Failed to get Subscribers for : " + providerId, e);
        }
        return subscriberSet;
    }

    /**
     * get details of provider
     *
     * @param providerName name of the provider
     * @return Provider
     * @throws APIManagementException if failed to get Provider
     */
    public Provider getProvider(String providerName) throws APIManagementException {
        Provider provider = null;
        String providerPath = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                APIConstants.PROVIDERS_PATH + RegistryConstants.PATH_SEPARATOR + providerName;
        try {
            artifactManager = getArtifactManager(APIConstants.PROVIDER_KEY);
            Resource providerResource = registry.get(providerPath);
            String artifactId =
                    providerResource.getProperty(GovernanceConstants.ARTIFACT_ID_PROP_KEY);
            if (artifactId == null) {
                throw new APIManagementException("artifact it is null");
            }
            GenericArtifact providerArtifact = artifactManager.getGenericArtifact(artifactId);
            provider = APIUtils.getProvider(providerArtifact);

        } catch (RegistryException e) {
            handleException("Failed to get Provider form : " + providerName, e);
        }
        return provider;
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
     * @param apiIdentifier APIIdentifier
     * @return Usage
     */
    public Usage getUsageByAPI(APIIdentifier apiIdentifier) {
        return null;
    }

    /**
     * @param providerId if of the provider
     * @param apiName    name of the API
     * @return Usage
     */
    public Usage getAPIUsageByUsers(String providerId, String apiName) {
        return null;
    }

    /**
     * Shows how a given consumer uses the given API.
     *
     * @param apiIdentifier APIIdentifier
     * @param consumerEmail E-mal Address of consumer
     * @return Usage
     */
    public Usage getAPIUsageBySubscriber(APIIdentifier apiIdentifier, String consumerEmail) {
        return null;
    }

    /**
     * Add new Subscriber
     *
     * @param identifier APIIdentifier
     * @param userId     id of the user
     * @throws APIManagementException if failed to add subscription details to database
     */
    public void addSubscription(APIIdentifier identifier, String userId, int applicationId)
            throws APIManagementException {
        apiMgtDAO.addSubscription(identifier, userId, applicationId);
    }

    /**
     * Remove a Subscriber
     *
     * @param identifier APIIdentifier
     * @param userId     id of the user
     * @throws APIManagementException if failed to add subscription details to database
     */
    public void removeSubscriber(APIIdentifier identifier, String userId)
            throws APIManagementException {
        //TODO @sumedha : implement unsubscription
    }

    /**
     * Returns full list of Subscribers of an API
     *
     * @param identifier APIIdentifier
     * @return Set<Subscriber>
     * @throws APIManagementException if failed to get Subscribers
     */
    public Set<Subscriber> getSubscribersOfAPI(APIIdentifier identifier)
            throws APIManagementException {

        Set<Subscriber> subscriberSet = null;
        try {
            subscriberSet = apiMgtDAO.getSubscribersOfAPI(identifier);
        } catch (APIManagementException e) {
            handleException("Failed to get subscribers for API : " + identifier.getApiName(), e);
        }
        return subscriberSet;
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
     * this method returns the Set<APISubscriptionCount> for given provider and api
     *
     * @param identifier APIIdentifier
     * @return Set<APISubscriptionCount>
     * @throws APIManagementException if failed to get APISubscriptionCountByAPI
     */
    public long getAPISubscriptionCountByAPI(APIIdentifier identifier)
            throws APIManagementException {
        long count = 0L;
        try {
            count = apiMgtDAO.getAPISubscriptionCountByAPI(identifier);
        } catch (APIManagementException e) {
            handleException("Failed to get APISubscriptionCount for: " + identifier.getApiName(), e);            
        }
        return count;
    }

//    /**
//     * Get list of APIs purchased by a consumer.
//     *
//     * @param subscriberEmail email
//     * @return Set<SubscribedAPI>
//     * @throws APIManagementException if failed to get subscribed API
//     */
//    @Override
//    public Set<SubscribedAPI> getSubscribedAPIsBySubscriber(String subscriberEmail)
//            throws APIManagementException {
//
//        Set<SubscribedAPI> subscribedAPIs;
//        try {
//            subscribedAPIs = apiMgtDAO.getSubscribedAPIsBySubscriber(subscriberEmail);
//        } catch (APIManagementException e) {
//            String msg = "Failed to get Subscribed APIs By Subscriber" + subscriberEmail;
//            throw new APIManagementException(msg, e);
//        }
//        return subscribedAPIs;
//    }

    /**
     * @param username Name of the user
     * @param password Password of the user
     * @return login status
     */
    public boolean login(String username, String password) {
        boolean result = false;
//        //TODO this is not finish
//        String epr = "";
//        try {
//            AuthenticationClient client = new AuthenticationClient(epr);
//            result = client.login(username, password);
//        } catch (AxisFault axisFault) {
//            axisFault.printStackTrace();
//        } catch (AuthenticationException e) {
//            e.printStackTrace();
//        }
        return result;
    }

    /**
     * Log out user
     *
     * @param username name of the user
     */
    public void logout(String username) {
        //TODO this is not finish
//        String epr = "";
//        try {
//            AuthenticationClient client = new AuthenticationClient(epr);
//            client.logout();
//        } catch (AxisFault axisFault) {
//            axisFault.printStackTrace();
//        } catch (AuthenticationException e) {
//            e.printStackTrace();
//        }

    }

    /**
     * Returns a list of pre-defined # {@link org.wso2.carbon.apimgt.api.model.Tier} in the system.
     *
     * @return Set<Tier>
     */
    public Set<Tier> getTiers() {
        return null;
    }

    /**
     * This method is to update the subscriber.
     *
     * @throws APIManagementException if failed to update subscription
     */
    public void updateSubscriptions(APIIdentifier identifier, String userId, int applicationId)
            throws APIManagementException {
        apiMgtDAO.updateSubscriptions(identifier, userId, applicationId);
    }

    /**
     * Adds a new API to the Store
     *
     * @param api API
     * @throws APIManagementException if failed to add API
     */
    public void addAPI(API api) throws APIManagementException {

        artifactManager = getArtifactManager(APIConstants.API_KEY);
        try {
            GenericArtifact genericArtifact =
                    artifactManager.newGovernanceArtifact(new QName(api.getId().getApiName()));
            GenericArtifact artifact = APIUtils.createAPIArtifactContent(genericArtifact, api);
            artifactManager.addGenericArtifact(artifact);
            String artifactPath = GovernanceUtils.getArtifactPath(registry, artifact.getId());
            String providerPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                    api.getId().getProviderName();
            //provider ------provides----> API
            registry.addAssociation(providerPath, artifactPath, APIConstants.PROVIDER_ASSOCIATION);
            Set<String> tagSet = api.getTags();
            if (tagSet != null && tagSet.size() > 0) {
                for (String tag : tagSet) {
                    registry.applyTag(artifactPath, tag);
                }
            }
            //Setting context name as property of API
            Resource resource = registry.get(artifactPath);
            resource.setProperty(APIConstants.API_CONTEXT_ID, api.getContext());
            registry.put(artifactPath, resource);
        } catch (RegistryException e) {
            handleException("Error while adding API", e);
        }
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

        AuthorizationManager accessControlAdmin = APIManagerComponent.getRegistryService().
                getUserRealm(IdentityUtil.getTenantIdOFUser(
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
     * Updates an existing API
     *
     * @param api API
     * @throws APIManagementException if failed to update API
     */
    public void updateAPI(API api) throws APIManagementException {
        if (api.getContext() == null) {
            APIIdentifier apiIdentifier = api.getId();
            String path = APIUtils.getAPIPath(apiIdentifier);
            try {
                Resource resource = registry.get(path);
                api.setContext(resource.getProperty(APIConstants.API_CONTEXT_ID));
            } catch (RegistryException e) {
                handleException("Failed set context id when updating the API", e);
            }
        }
        addAPI(api);
    }

    /**
     * Create a new version of the <code>api</code>, with version <code>newVersion</code>
     *
     * @param api        The API to be copied
     * @param newVersion The version of the new API
     * @throws org.wso2.carbon.apimgt.api.model.DuplicateAPIException
     *          If the API trying to be created already exists
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          If an error occurs while trying to create the new version of the API
     */
    public void createNewAPIVersion(API api, String newVersion) throws DuplicateAPIException,
            APIManagementException {
        String apiSourcePath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
                api.getId().getProviderName() + RegistryConstants.PATH_SEPARATOR +
                api.getId().getApiName() + RegistryConstants.PATH_SEPARATOR +
                api.getId().getVersion() + APIConstants.API_RESOURCE_NAME;

        String targetPath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
                api.getId().getProviderName() +
                RegistryConstants.PATH_SEPARATOR + api.getId().getApiName() +
                RegistryConstants.PATH_SEPARATOR + newVersion +
                APIConstants.API_RESOURCE_NAME;
        try {
            if (registry.resourceExists(targetPath)) {
                throw new DuplicateAPIException("API version already exist with version :"
                        + newVersion);
            } else {
                Resource apiSourceArtifact = registry.get(apiSourcePath);
                GenericArtifact artifact = artifactManager.getGenericArtifact(
                        apiSourceArtifact.getProperty(GovernanceConstants.ARTIFACT_ID_PROP_KEY));

                //Create new API version
                artifact.setId(UUID.randomUUID().toString());
                artifact.setAttribute(APIConstants.API_OVERVIEW_VERSION, newVersion);

                //Check the status of the existing api,if its not in 'CREATED' status set
                //the new api status as "CREATED"
                String status=artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS);
                if(!status.equals(APIConstants.CREATED)){
                artifact.setAttribute(APIConstants.API_OVERVIEW_STATUS,APIConstants.CREATED);
                }
                artifactManager.addGenericArtifact(artifact);
                registry.addAssociation(APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR
                        + api.getId().getProviderName(), targetPath,
                        APIConstants.PROVIDER_ASSOCIATION);
            }
        } catch (RegistryException e) {
            String msg = "Failed to create new version : " + newVersion + " of : "
                    + api.getId().getApiName();
            handleException(msg, e);
        }
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
        String apiResourcePath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                apiId.getProviderName() + RegistryConstants.PATH_SEPARATOR + apiId.getApiName() +
                RegistryConstants.PATH_SEPARATOR + apiId.getVersion() + APIConstants.API_RESOURCE_NAME;
        try {
            Association[] docAssociations = registry.getAssociations(apiResourcePath,
                    APIConstants.DOCUMENTATION_ASSOCIATION);
            for (Association association : docAssociations) {
                String docPath = association.getDestinationPath();
                Resource docResource = registry.get(docPath);
                artifactManager = new GenericArtifactManager(registry, APIConstants.DOCUMENTATION_KEY);
                GenericArtifact docArtifact = artifactManager.getGenericArtifact(
                        docResource.getProperty(GovernanceConstants.ARTIFACT_ID_PROP_KEY));
                Documentation doc = APIUtils.getDocumentation(docArtifact);
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
        String docPath = apiId.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                apiId.getApiName() + RegistryConstants.PATH_SEPARATOR + apiId.getVersion() +
                RegistryConstants.PATH_SEPARATOR + APIConstants.DOC_DIR +
                RegistryConstants.PATH_SEPARATOR + docName;
        artifactManager = getArtifactManager(APIConstants.DOCUMENTATION_KEY);
        try {
            Association[] associations = registry.getAssociations(docPath, docType.getType());
            for (Association association : associations) {
                Resource docResource = registry.get(association.getSourcePath());
                GenericArtifact artifact = artifactManager.getGenericArtifact(docResource.getId());
                documentation = APIUtils.getDocumentation(artifact);
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
     * Removes a given documentation
     *
     * @param apiId   APIIdentifier
     * @param docType the type of the documentation
     * @param docName name of the document
     * @throws APIManagementException if failed to remove documentation
     */
    public void removeDocumentation(APIIdentifier apiId, String docName, String docType)
            throws APIManagementException {
        String docPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                apiId.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                apiId.getApiName() + RegistryConstants.PATH_SEPARATOR + apiId.getVersion() +
                RegistryConstants.PATH_SEPARATOR + APIConstants.DOC_DIR +
                RegistryConstants.PATH_SEPARATOR + docName;
        try {
            Association[] associations = registry.getAssociations(docPath,
                    APIConstants.DOCUMENTATION_KEY);
            for (Association association : associations) {
                registry.delete(association.getDestinationPath());
            }
        } catch (RegistryException e) {
            handleException("Failed to delete documentation", e);
        }
    }

    /**
     * Adds Documentation to an API
     * addgetget
     *
     * @param apiId         APIIdentifier
     * @param documentation Documentation
     * @throws APIManagementException if failed to add documentation
     */
    public void addDocumentation(APIIdentifier apiId, Documentation documentation)
            throws APIManagementException {
        try {
            artifactManager = new GenericArtifactManager(registry, APIConstants.DOCUMENTATION_KEY);
            GenericArtifact artifact =
                    artifactManager.newGovernanceArtifact(new QName(documentation.getName()));
            artifact.setAttribute(APIConstants.DOC_NAME, documentation.getName());
            artifact.setAttribute(APIConstants.DOC_SUMMARY, documentation.getSummary());
            artifact.setAttribute(APIConstants.DOC_TYPE, documentation.getType().getType());

            Documentation.DocumentSourceType sourceType = documentation.getSourceType();

            switch (sourceType) {
                case INLINE:
                    sourceType = Documentation.DocumentSourceType.INLINE;
                    break;
                case URL:
                    sourceType = Documentation.DocumentSourceType.URL;
                    break;
            }
            artifact.setAttribute(APIConstants.DOC_SOURCE_TYPE, sourceType.name());
            artifact.setAttribute(APIConstants.DOC_SOURCE_URL, documentation.getSourceUrl());
            String basePath = apiId.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                    apiId.getApiName() + RegistryConstants.PATH_SEPARATOR +
                    apiId.getVersion();
            artifact.setAttribute(APIConstants.DOC_API_BASE_PATH, basePath);
            artifactManager.addGenericArtifact(artifact);

            String apiPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                    apiId.getProviderName() + RegistryConstants.PATH_SEPARATOR + apiId.getApiName() +
                    RegistryConstants.PATH_SEPARATOR + apiId.getVersion() + APIConstants.API_RESOURCE_NAME;
            //Adding association from api to documentation . (API -----> doc)
            registry.addAssociation(apiPath, artifact.getPath(), APIConstants.DOCUMENTATION_ASSOCIATION);

        } catch (RegistryException e) {
            handleException("Failed to add documentation", e);
        }
    }

    /**
     * This method used to save the documentation content
     *
     * @param identifier, API identifier
     * @param documentationName, name of the inline documentation
     * @param  text, content of the inline documentation
     * @throws APIManagementException if failed to add the document as a resource to registry
     */
    public void addDocumentationContent(APIIdentifier identifier, String documentationName, String text)
            throws APIManagementException {
        String documentationPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                identifier.getApiName() + RegistryConstants.PATH_SEPARATOR +
                identifier.getVersion() + RegistryConstants.PATH_SEPARATOR +
                APIConstants.DOC_DIR + RegistryConstants.PATH_SEPARATOR + documentationName;

        String contentPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                identifier.getApiName() + RegistryConstants.PATH_SEPARATOR +
                identifier.getVersion() + RegistryConstants.PATH_SEPARATOR +
                APIConstants.DOC_DIR + RegistryConstants.PATH_SEPARATOR +
                APIConstants.INLINE_DOCUMENT_CONTENT_DIR + RegistryConstants.PATH_SEPARATOR +documentationName;

        try {
            Resource docContent = registry.newResource();
            docContent.setContent(text);
            registry.put(contentPath, docContent);
            registry.addAssociation(documentationPath, contentPath,
                    APIConstants.DOCUMENTATION_CONTENT_ASSOCIATION);
        } catch (RegistryException e) {
            String msg = "Failed to add the documentation content of : "
                    + documentationName + " of API :"+identifier.getApiName();
            handleException(msg, e);
        }
    }

    /**
     * Updates a given documentation
     *
     * @param apiId         APIIdentifier
     * @param documentation Documentation
     * @throws APIManagementException if failed to update docs
     */
    public void updateDocumentation(APIIdentifier apiId, Documentation documentation)
            throws APIManagementException {
        try {
            addDocumentation(apiId, documentation);
        } catch (APIManagementException e) {
            handleException("Failed to update doc", e);
        }
    }

    /**
     * Copies current Documentation into another version of the same API.
     *
     * @param toVersion Version to which Documentation should be copied.
     * @param apiId     id of the APIIdentifier
     * @throws APIManagementException if failed to copy docs
     */
    public void copyAllDocumentation(APIIdentifier apiId, String toVersion)
            throws APIManagementException {

        String oldVersion = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                apiId.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                apiId.getApiName() + RegistryConstants.PATH_SEPARATOR + apiId.getVersion() +
                RegistryConstants.PATH_SEPARATOR + APIConstants.DOC_DIR;

        String newVersion = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                apiId.getProviderName() + RegistryConstants.PATH_SEPARATOR + apiId.getApiName() +
                RegistryConstants.PATH_SEPARATOR + toVersion + RegistryConstants.PATH_SEPARATOR +
                APIConstants.DOC_DIR;

        try {
            Resource resource = registry.get(oldVersion);
            if (resource instanceof Collection) {
                String[] docsPaths = ((Collection) resource).getChildren();

                for (String docPath : docsPaths) {
                    registry.copy(docPath, newVersion);
                }
            }
        } catch (RegistryException e) {
            handleException("Failed to copy docs to new version : " + newVersion, e);
        }

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
     * @param identifier Api identifier
     * @param s          comment text
     * @throws APIManagementException if failed to add comment for API
     */
    public void addComment(APIIdentifier identifier, String s) throws APIManagementException {
        String apiPath = APIUtils.getAPIPath(identifier);
        Comment comment = new Comment(s);
        try {
            registry.addComment(apiPath, comment);
        } catch (RegistryException e) {
            handleException("Failed to add comment for api " + apiPath, e);
        }
    }

    /**
     * @param identifier Api identifier
     * @return Comments
     * @throws APIManagementException if failed to get comments for identifier
     */
    public org.wso2.carbon.apimgt.api.model.Comment[] getComments(APIIdentifier identifier)
            throws APIManagementException {
        List<org.wso2.carbon.apimgt.api.model.Comment> commentList =
                new ArrayList<org.wso2.carbon.apimgt.api.model.Comment>();
        Comment[] comments;
        String apiPath = APIUtils.getAPIPath(identifier);
        try {
            comments = registry.getComments(apiPath);
            for (Comment comment : comments) {
                org.wso2.carbon.apimgt.api.model.Comment comment1 =
                        new org.wso2.carbon.apimgt.api.model.Comment();
                comment1.setText(comment.getText());
                comment1.setUser(comment.getUser());
                commentList.add(comment1);
            }
            return commentList.toArray(new org.wso2.carbon.apimgt.api.model.Comment[commentList.size()]);
        } catch (RegistryException e) {
            handleException("Failed to get comments for api " + apiPath, e);
        }
        return null;
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

    private Registry getRegistry(String user, String pass, String url) throws APIManagementException {
        WSRegistryServiceClient client;
        try {
//            client = new WSRegistryServiceClient(url, user, pass,
//                    ConfigurationContextFactory.createConfigurationContextFromFileSystem(
//                            ServerConfiguration.getInstance().
//                                    getFirstProperty("Axis2Config.clientAxis2XmlLocation")));
            //TODO need to verify the registry initialization
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

    /**
     * Returns true if a given user has subscribed to the API
     *
     * @param apiIdentifier APIIdentifier
     * @param userId        user id
     * @return true, if giving api identifier is already subscribed
     * @throws APIManagementException if failed to check the subscribed state
     */
    public boolean isSubscribed(APIIdentifier apiIdentifier, String userId) throws APIManagementException {
        boolean isSubscribed;
        try {
            isSubscribed = apiMgtDAO.isSubscribed(apiIdentifier, userId);
        } catch (APIManagementException e) {
            String msg = "Failed to check if user(" + userId + ") has subscribed to " + apiIdentifier;
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
        return isSubscribed;
    }

    /**
     * @param providerName Name of the API provider
     * @return An array of UserApplicationAPIUsage instances
     */
    public UserApplicationAPIUsage[] getAllAPIUsageByProvider(
            String providerName) throws APIManagementException {
        return apiMgtDAO.getAllAPIUsageByProvider(providerName);
    }

    /**
     * This method returns the set of APIs for given subscriber
     *
     * @param subscriber subscriber A valid Subscriber instance
     * @return Set<API> A Set of APIs associated with the subscriber
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to get SubscribedAPIs
     */
    public Set<API> getSubscriberAPIs(Subscriber subscriber) throws APIManagementException {
        SortedSet<API> apiSortedSet = new TreeSet<API>(new APIComparator());
        Set<SubscribedAPI> subscribedAPIs = apiMgtDAO.getSubscribedAPIs(subscriber);

        for (SubscribedAPI subscribedAPI : subscribedAPIs) {
            String apiPath = APIUtils.getAPIPath(subscribedAPI.getApiId());
            Resource resource;
            try {
                resource = registry.get(apiPath);
                artifactManager = new GenericArtifactManager(registry, APIConstants.API_KEY);
                GenericArtifact artifact = artifactManager.getGenericArtifact(
                        resource.getProperty(GovernanceConstants.ARTIFACT_ID_PROP_KEY));
                API api = APIUtils.getAPI(artifact, registry);
                apiSortedSet.add(api);
            } catch (RegistryException e) {
                String msg = "Failed to get api";
                throw new APIManagementException(msg, e);
            }
        }
        return apiSortedSet;
    }

    /**
     * @param application An Application instance to be added
     * @param userId Current user ID
     * @throws APIManagementException on error
     */
    public void addApplication(Application application, String userId) throws APIManagementException {
        apiMgtDAO.addApplication(application, userId);
    }

    /**
     * @param subscriber A valid Subscriber instance
     * @return All applications associated with the provided subscriber
     * @throws APIManagementException
     */
    public Application[] getApplications(Subscriber subscriber) throws APIManagementException {
        return apiMgtDAO.getApplications(subscriber);
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

    /**
     * This comparator used to order APIs by name.
     */
    private class APIComparator implements Comparator<API> {

        public int compare(API api1, API api2) {
            return api1.getId().getApiName().compareTo(api2.getId().getApiName());
        }
    }
}
