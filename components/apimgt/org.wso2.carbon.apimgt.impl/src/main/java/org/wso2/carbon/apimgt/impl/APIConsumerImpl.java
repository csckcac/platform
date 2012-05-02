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
package org.wso2.carbon.apimgt.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.utils.APINameComparator;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.APIVersionComparator;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class APIConsumerImpl implements APIConsumer {

    private static Log log = LogFactory.getLog(APIConsumerImpl.class);
    private ApiMgtDAO apiMgtDAO;
    private GenericArtifactManager artifactManager;
    private Registry registry;

    public APIConsumerImpl() throws APIManagementException {
        this.apiMgtDAO = new ApiMgtDAO();
        try {
            this.registry = RegistryCoreServiceComponent.getRegistryService().
                    getGovernanceSystemRegistry();
        } catch (RegistryException e) {
            throw new APIManagementException("Failed to initialize the registry");
        }
    }

    /**
     * @param subscriberId id of the Subscriber
     * @return Subscriber
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to get Subscriber
     */
    @Override
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
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to get set of API
     */
    @Override
    public Set<API> getAPIsWithTag(String tag) throws APIManagementException {
        Set<API> apiSet = new HashSet<API>();
        try {
            artifactManager = APIUtil.getArtifactManager(registry,APIConstants.API_KEY);
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
                        apiSet.add(APIUtil.getAPI(artifact, registry));
                    }
                }
            }
        } catch (RegistryException e) {
            handleException("Failed to get API for tag " + tag, e);
        }
        return apiSet;
    }

    /**
     * Returns a list of all published APIs. If a given API has multiple APIs,
     * only the latest version will be included
     * in this list.
     *
     * @return set of API
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to API set
     */
    @Override
    public Set<API> getAllPublishedAPIs() throws APIManagementException {
        SortedSet<API> apiSortedSet = new TreeSet<API>(new APINameComparator());
        try {
            artifactManager = APIUtil.getArtifactManager(registry,APIConstants.API_KEY);
            GenericArtifact[] genericArtifacts = artifactManager.getAllGenericArtifacts();
            if (genericArtifacts == null || genericArtifacts.length == 0) {
                return apiSortedSet;
            }
            Map<String, API> latestPublishedAPIs = new HashMap<String, API>();
            Comparator<API> versionComparator = new APIVersionComparator();
            for (GenericArtifact artifact : genericArtifacts) {
                // adding the API provider can mark the latest API .
                String status = artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS);
                // We are only interested in published APIs here...
                if (status.equals(APIConstants.PUBLISHED)) {
                    API api = APIUtil.getAPI(artifact, registry);
                    String key = api.getId().getProviderName() + ":" + api.getId().getApiName();
                    API existingAPI = latestPublishedAPIs.get(key);
                    if (existingAPI != null) {
                        // If we have already seen an API with the same name, make sure
                        // this one has a higher version number
                        if (versionComparator.compare(api, existingAPI) > 0) {
                            latestPublishedAPIs.put(key, api);
                        }
                    } else {
                        // We haven't seen this API before
                        latestPublishedAPIs.put(key, api);
                    }
                }
            }

            for (API api : latestPublishedAPIs.values()) {
                apiSortedSet.add(api);
            }
        } catch (RegistryException e) {
            handleException("Failed to get all publishers", e);
        }
        return apiSortedSet;
    }

    /**
     * Returns top rated APIs
     *
     * @param limit if -1, no limit. Return everything else, limit the return list to specified value.
     * @return Set of API
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to get top rated APIs
     */
    @Override
    public Set<API> getTopRatedAPIs(int limit) throws APIManagementException {
        int returnLimit = 0;
        SortedSet<API> apiSortedSet = new TreeSet<API>(new APINameComparator());
        try {
            artifactManager = APIUtil.getArtifactManager(registry,APIConstants.API_KEY);
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
                        apiSortedSet.add(APIUtil.getAPI(genericArtifact, registry));
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
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to get recently added APIs
     */
    @Override
    public Set<API> getRecentlyAddedAPIs(int limit) throws APIManagementException {

        int start = 0;
        SortedSet<API> apiSortedSet = new TreeSet<API>(new APINameComparator());
        Map<Date, GenericArtifact> apiMap = new HashMap<Date, GenericArtifact>();
        List<Date> dateList = new ArrayList<Date>();
        try {
            artifactManager = APIUtil.getArtifactManager(registry,APIConstants.API_KEY);
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

                apiSortedSet.add(APIUtil.getAPI(genericArtifact1, registry));
            }

        } catch (RegistryException e) {
            handleException("Failed to get recently added APIs", e);
        }
        return apiSortedSet;
    }

    /**
     * Get all tags of published APIs
     *
     * @return a list of all Tags applied to all APIs published.
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to get All the tags
     */
    @Override
    public Set<Tag> getAllTags() throws APIManagementException {
        Set<Tag> tagSet = new HashSet<Tag>();
        try {
            artifactManager = APIUtil.getArtifactManager(registry,APIConstants.API_KEY);
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
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          If an error occurs while rating the API
     */
    @Override
    public void rateAPI(APIIdentifier apiId, APIRating rating) throws APIManagementException {
        String path = APIUtil.getAPIPath(apiId);
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
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to get APIs for given search term
     */
    @Override
    public Set<API> searchAPI(String searchTerm) throws APIManagementException {
        Set<API> apiSet = new HashSet<API>();
        String regex = "[a-zA-Z0-9_.-|]*" + searchTerm + "[a-zA-Z0-9_.-|]*";
        Pattern pattern;
        Matcher matcher;
        try {
            artifactManager = APIUtil.getArtifactManager(registry,APIConstants.API_KEY);
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
                    apiSet.add(APIUtil.getAPI(artifact, registry));
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
            artifactManager = APIUtil.getArtifactManager(registry,APIConstants.API_KEY);
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
                        apiSet.add(APIUtil.getAPI(artifact, registry));
                }
            }
        } catch (RegistryException e) {
            handleException("Failed to search APIs with type", e);
        }
        return apiSet;
    }

    /**
     * Returns a set of SubscribedAPI purchased by the given Subscriber
     *
     * @param subscriber Subscriber
     * @return Set<API>
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to get API for subscriber
     */
    @Override
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
     * Returns a set of APIs purchased by the given Subscriber
     *
     * @param subscriber Subscriber
     * @return Set<API>
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to get API for subscriber
     */
    @Override
    public Set<API> getSubscriberAPIs(Subscriber subscriber) throws APIManagementException {
        SortedSet<API> apiSortedSet = new TreeSet<API>(new APINameComparator());
        Set<SubscribedAPI> subscribedAPIs = apiMgtDAO.getSubscribedAPIs(subscriber);

        for (SubscribedAPI subscribedAPI : subscribedAPIs) {
            String apiPath = APIUtil.getAPIPath(subscribedAPI.getApiId());
            Resource resource;
            try {
                resource = registry.get(apiPath);
                artifactManager = new GenericArtifactManager(registry, APIConstants.API_KEY);
                GenericArtifact artifact = artifactManager.getGenericArtifact(
                        resource.getProperty(GovernanceConstants.ARTIFACT_ID_PROP_KEY));
                API api = APIUtil.getAPI(artifact, registry);
                apiSortedSet.add(api);
            } catch (RegistryException e) {
                String msg = "Failed to get api";
                throw new APIManagementException(msg, e);
            }
        }
        return apiSortedSet;
    }

    /**
     * Returns true if a given user has subscribed to the API
     *
     * @param apiIdentifier APIIdentifier
     * @param userId        user id
     * @return true, if giving api identifier is already subscribed
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to check the subscribed state
     */
    @Override
    public boolean isSubscribed(APIIdentifier apiIdentifier, String userId)
            throws APIManagementException {
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
     * Add new Subscriber
     *
     * @param identifier    APIIdentifier
     * @param userId        id of the user
     * @param applicationId Application Id
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to add subscription details to database
     */
    @Override
    public void addSubscription(APIIdentifier identifier, String userId, int applicationId)
            throws APIManagementException {
        apiMgtDAO.addSubscription(identifier, userId, applicationId);
    }

    /**
     * Remove a Subscriber
     *
     * @param identifier APIIdentifier
     * @param userId     id of the user
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to add subscription details to database
     */
    @Override
    public void removeSubscriber(APIIdentifier identifier, String userId)
            throws APIManagementException {
        //TODO @sumedha : implement unsubscription
    }

    /**
     * This method is to update the subscriber.
     *
     * @param identifier    APIIdentifier
     * @param userId        user id
     * @param applicationId Application Id
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to update subscription
     */
    @Override
    public void updateSubscriptions(APIIdentifier identifier, String userId, int applicationId)
            throws APIManagementException {
        apiMgtDAO.updateSubscriptions(identifier, userId, applicationId);
    }

    /**
     * @param identifier Api identifier
     * @param s          comment text
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to add comment for API
     */
    @Override
    public void addComment(APIIdentifier identifier, String s) throws APIManagementException {
        String apiPath = APIUtil.getAPIPath(identifier);
        org.wso2.carbon.registry.core.Comment comment = new org.wso2.carbon.registry.core.Comment(s);
        try {
            registry.addComment(apiPath, comment);
        } catch (RegistryException e) {
            handleException("Failed to add comment for api " + apiPath, e);
        }
    }

    /**
     * @param identifier Api identifier
     * @return Comments
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to get comments for identifier
     */
    @Override
    public org.wso2.carbon.apimgt.api.model.Comment[] getComments(APIIdentifier identifier)
            throws APIManagementException {
        List<org.wso2.carbon.apimgt.api.model.Comment> commentList =
                new ArrayList<org.wso2.carbon.apimgt.api.model.Comment>();
        org.wso2.carbon.registry.core.Comment[] comments;
        String apiPath = APIUtil.getAPIPath(identifier);
        try {
            comments = registry.getComments(apiPath);
            for (org.wso2.carbon.registry.core.Comment comment : comments) {
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
     * Adds an application
     *
     * @param application Application
     * @param userId      User Id
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to add Application
     */
    @Override
    public void addApplication(Application application, String userId)
            throws APIManagementException {
        apiMgtDAO.addApplication(application, userId);
    }

    /**
     * Returns a list of applications for a given subscriber
     *
     * @param subscriber Subscriber
     * @return Applications
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to applications for given subscriber
     */
    @Override
    public Application[] getApplications(Subscriber subscriber) throws APIManagementException {
        return apiMgtDAO.getApplications(subscriber);
    }

    @Override
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

    private void handleException(String msg, Exception e) throws APIManagementException {
        log.error(msg, e);
        throw new APIManagementException(msg, e);
    }
}
