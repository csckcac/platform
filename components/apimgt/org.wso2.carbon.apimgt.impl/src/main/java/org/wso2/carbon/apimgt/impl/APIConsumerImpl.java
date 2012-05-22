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

import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APINameComparator;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.APIVersionComparator;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides the core API store functionality. It is implemented in a very
 * self-contained and 'pure' manner, without taking requirements like security into account,
 * which are subject to frequent change. Due to this 'pure' nature and the significance of
 * the class to the overall API management functionality, the visibility of the class has
 * been reduced to package level. This means we can still use it for internal purposes and
 * possibly even extend it, but it's totally off the limits of the users. Users wishing to
 * programmatically access this functionality should use one of the extensions of this
 * class which is visible to them. These extensions may add additional features like
 * security to this class.
 */
class APIConsumerImpl extends AbstractAPIManager implements APIConsumer {

    public APIConsumerImpl() throws APIManagementException {
        super();
    }    

    public Subscriber getSubscriber(String subscriberId) throws APIManagementException {
        Subscriber subscriber = null;
        try {
            subscriber = apiMgtDAO.getSubscriber(subscriberId);
        } catch (APIManagementException e) {
            handleException("Failed to get Subscriber", e);
        }
        return subscriber;
    }

    public Set<API> getAPIsWithTag(String tag) throws APIManagementException {
        Set<API> apiSet = new HashSet<API>();
        try {
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,APIConstants.API_KEY);
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
                    continue;
                }
                for (org.wso2.carbon.registry.core.Tag tag1 : tags) {
                    if (tag.equals(tag1.getTagName())) {
                        apiSet.add(APIUtil.getAPI(artifact, registry));
                        break;
                    }
                }
            }
        } catch (RegistryException e) {
            handleException("Failed to get API for tag " + tag, e);
        }
        return apiSet;
    }

    public Set<API> getAllPublishedAPIs() throws APIManagementException {
        SortedSet<API> apiSortedSet = new TreeSet<API>(new APINameComparator());
        try {
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,APIConstants.API_KEY);
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

    public Set<API> getTopRatedAPIs(int limit) throws APIManagementException {
        int returnLimit = 0;
        SortedSet<API> apiSortedSet = new TreeSet<API>(new APINameComparator());
        try {
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,APIConstants.API_KEY);
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

    public Set<API> getRecentlyAddedAPIs(int limit) throws APIManagementException {

        int start = 0;
        SortedSet<API> apiSortedSet = new TreeSet<API>(new APINameComparator());
        Map<Date, GenericArtifact> apiMap = new HashMap<Date, GenericArtifact>();
        List<Date> dateList = new ArrayList<Date>();
        try {
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,APIConstants.API_KEY);
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

            for (int i = dateList.size() - 1; i >= start; i--) {
                GenericArtifact genericArtifact1 = apiMap.get(dateList.get(i));
                apiSortedSet.add(APIUtil.getAPI(genericArtifact1, registry));
            }

        } catch (RegistryException e) {
            handleException("Failed to get recently added APIs", e);
        }
        return apiSortedSet;
    }

    public Set<Tag> getAllTags() throws APIManagementException {
        Set<Tag> tagSet = new HashSet<Tag>();
        try {
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,APIConstants.API_KEY);
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

    public void rateAPI(APIIdentifier apiId, APIRating rating,
                        String user) throws APIManagementException {
        String path = APIUtil.getAPIPath(apiId);
        try {
            UserRegistry userRegistry = ServiceReferenceHolder.getInstance().
                    getRegistryService().getGovernanceUserRegistry(user);
            userRegistry.rateResource(path, rating.getRating());
        } catch (RegistryException e) {
            handleException("Failed to rate API : " + path, e);
        }
    }

    public Set<API> searchAPI(String searchTerm) throws APIManagementException {
        Set<API> apiSet = new HashSet<API>();
        String regex = "[a-zA-Z0-9_.-|]*" + searchTerm + "[a-zA-Z0-9_.-|]*";
        Pattern pattern;
        Matcher matcher;
        try {
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,APIConstants.API_KEY);
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
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,APIConstants.API_KEY);
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

    public Set<SubscribedAPI> getSubscribedAPIs(Subscriber subscriber) throws APIManagementException {
        Set<SubscribedAPI> subscribedAPIs = null;
        try {
            subscribedAPIs = apiMgtDAO.getSubscribedAPIs(subscriber);
        } catch (APIManagementException e) {
            handleException("Failed to get APIs of " + subscriber.getName(), e);
        }
        return subscribedAPIs;
    }

    public APIIdentifier getAPIByConsumerKey(String accessToken) throws APIManagementException {
        try {
            return apiMgtDAO.getAPIByConsumerKey(accessToken);
        } catch (APIManagementException e) {
            handleException("Error while obtaining API from API key", e);
        }
        return null;
    }

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

    public void addSubscription(APIIdentifier identifier, String userId, int applicationId)
            throws APIManagementException {
        API api = getAPI(identifier);
        if (api.getStatus().equals(APIStatus.PUBLISHED)) {
            apiMgtDAO.addSubscription(identifier, userId, applicationId);
        } else {
            throw new APIManagementException("Subscriptions not allowed on APIs in the state: " +
                    api.getStatus().getStatus());
        }
    }

    public void removeSubscriber(APIIdentifier identifier, String userId)
            throws APIManagementException {
        throw new UnsupportedOperationException("Unsubscribe operation is not yet implemented");
    }

    public void updateSubscriptions(APIIdentifier identifier, String userId, int applicationId)
            throws APIManagementException {
        apiMgtDAO.updateSubscriptions(identifier, userId, applicationId);
    }

    public void addComment(APIIdentifier identifier, String s, String user) throws APIManagementException {
        String apiPath = APIUtil.getAPIPath(identifier);
        org.wso2.carbon.registry.core.Comment comment = new org.wso2.carbon.registry.core.Comment(s);
        try {
            UserRegistry userRegistry = ServiceReferenceHolder.getInstance().
                    getRegistryService().getGovernanceUserRegistry(user);
            userRegistry.addComment(apiPath, comment);
        } catch (RegistryException e) {
            handleException("Failed to add comment for api " + apiPath, e);
        }
    }

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
                comment1.setCreatedTime(comment.getCreatedTime());
                commentList.add(comment1);
            }
            return commentList.toArray(new org.wso2.carbon.apimgt.api.model.Comment[commentList.size()]);
        } catch (RegistryException e) {
            handleException("Failed to get comments for api " + apiPath, e);
        }
        return null;
    }

    public void addApplication(Application application, String userId)
            throws APIManagementException {
        apiMgtDAO.addApplication(application, userId);
    }

    public void updateApplication(Application application) throws APIManagementException {
        apiMgtDAO.updateApplication(application);
    }

    public Application[] getApplications(Subscriber subscriber) throws APIManagementException {
        return apiMgtDAO.getApplications(subscriber);
    }

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
}
