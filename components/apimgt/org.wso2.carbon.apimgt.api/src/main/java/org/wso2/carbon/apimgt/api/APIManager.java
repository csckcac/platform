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
package org.wso2.carbon.apimgt.api;

import org.wso2.carbon.apimgt.api.dto.UserApplicationAPIUsage;
import org.wso2.carbon.apimgt.api.model.*;

import java.util.List;
import java.util.Set;

/**
 * Manager responsible for providing helper functionality on Subscribers, Providers, APIs.
 */
public interface APIManager {

    /**
     * @param subscriberId id of the Subscriber
     * @return Subscriber
     * @throws APIManagementException if failed to get Subscriber
     */
    public Subscriber getSubscriber(String subscriberId) throws APIManagementException;

    /**
     * Returns a list of #{@link org.wso2.carbon.apimgt.api.model.API} bearing the selected tag
     *
     * @param tag name of the tag
     * @return set of API having the given tag name
     * @throws APIManagementException if failed to get set of API
     */
    public Set<API> getAPIsWithTag(String tag) throws APIManagementException;

    /**
     * Returns a list of all #{@link org.wso2.carbon.apimgt.api.model.Provider} available on the system.
     *
     * @return Set<Provider>
     * @throws APIManagementException if failed to get Providers
     */
    public Set<Provider> getAllProviders() throws APIManagementException;

    /**
     * Returns a list of all published APIs. If a given API has multiple APIs,
     * only the latest version will be included
     * in this list.
     *
     * @return set of API
     * @throws APIManagementException if failed to API set
     */
    public Set<API> getAllPublishedAPIs() throws APIManagementException;

    /**
     * Get a list of APIs published by the given provider. If a given API has multiple APIs,
     * only the latest version will
     * be included in this list.
     *
     *
     * @param providerId , provider id
     * @return set of API
     * @throws APIManagementException if failed to get set of API
     */
    public List<API> getAPIsByProvider(String providerId) throws APIManagementException;

    /**
     * Returns top rated APIs
     *
     * @param limit if -1, no limit. Return everything else, limit the return list to specified value.
     * @return Set of API
     * @throws APIManagementException if failed to get top rated APIs
     */
    public Set<API> getTopRatedAPIs(int limit) throws APIManagementException;

    /**
     * Get recently added APIs to the store
     *
     * @param limit if -1, no limit. Return everything else, limit the return list to specified value.
     * @return set of API
     * @throws APIManagementException if failed to get recently added APIs
     */
    public Set<API> getRecentlyAddedAPIs(int limit) throws APIManagementException;

    /**
     * Get all tags of published APIs
     *
     * @return a list of all Tags applied to all APIs published.
     * @throws APIManagementException if failed to get All the tags
     */
    public Set<Tag> getAllTags() throws APIManagementException;

    /**
     * Rate a particular API. This will be called when subscribers rate an API
     *
     * @param apiId  The API identifier
     * @param rating The rating provided by the subscriber
     * @throws APIManagementException If an error occurs while rating the API
     */
    public void rateAPI(APIIdentifier apiId, APIRating rating) throws APIManagementException;

    /**
     * Search matching APIs for given search terms
     *
     * @param searchTerm , name of the search term
     * @return Set<API>
     * @throws APIManagementException if failed to get APIs for given search term
     */
    public Set<API> searchAPI(String searchTerm) throws APIManagementException;

    /**
     * Returns a set of SubscribedAPI purchased by the given Subscriber
     *
     * @param subscriber Subscriber
     * @throws APIManagementException if failed to get API for subscriber
     * @return Set<API>
     */
    public Set<SubscribedAPI> getSubscribedAPIs(Subscriber subscriber) throws APIManagementException;

    /**
     * Returns a set of APIs purchased by the given Subscriber
     *
     * @param subscriber Subscriber
     * @throws APIManagementException if failed to get API for subscriber
     * @return Set<API>
     */
    public Set<API> getSubscriberAPIs(Subscriber subscriber) throws APIManagementException;

    /**
     * Returns true if a given user has subscribed to the API
     *
     * @param apiIdentifier  APIIdentifier
     * @param userId user id
     * @return true, if giving api identifier is already subscribed
     * @throws APIManagementException  if failed to check the subscribed state
     */
    public boolean isSubscribed(APIIdentifier apiIdentifier,String userId) throws APIManagementException;

    /**
     * returns details of an API
     *
     * @param identifier APIIdentifier
     * @return API
     * @throws APIManagementException if failed get API from APIIdentifier
     */
    public API getAPI(APIIdentifier identifier) throws APIManagementException;


    /**
     * Get a list of all the consumers for all APIs
     *
     * @param providerId if of the provider
     * @return Set<Subscriber>
     * @throws APIManagementException if failed to get subscribed APIs of given provider
     */
    public Set<Subscriber> getSubscribersOfProvider(String providerId)
            throws APIManagementException;

    /**
     * get details of provider
     *
     * @param providerName name of the provider
     * @return Provider
     * @throws APIManagementException if failed to get Provider
     */
    public Provider getProvider(String providerName) throws APIManagementException;


    /**
     * Check the Availability of given APIIdentifier
     *
     * @param identifier APIIdentifier
     * @return true, if already exists. False, otherwise
     * @throws APIManagementException if failed to get API availability
     */
    public boolean isAPIAvailable(APIIdentifier identifier) throws APIManagementException;

    /**
     * Return Usage of given APIIdentifier
     *
     * @param apiIdentifier APIIdentifier
     * @return Usage
     */
    public Usage getUsageByAPI(APIIdentifier apiIdentifier);

    /**
     * Return Usage of given provider and API
     *
     * @param providerId if of the provider
     * @param apiName  name of the API
     * @return Usage
     */
    public Usage getAPIUsageByUsers(String providerId, String apiName);


    /**
     * Returns usage details of all APIs published by a provider
     *
     * @param providerId  Provider Id
     * @return  UserApplicationAPIUsages for given provider
     * @throws APIManagementException If failed to get UserApplicationAPIUsage
     */
    public UserApplicationAPIUsage[] getAllAPIUsageByProvider(String providerId)
            throws APIManagementException;


    /**
     * Shows how a given consumer uses the given API.
     *
     * @param apiIdentifier  APIIdentifier
     * @param consumerEmail  E-mal Address of consumer
     * @return Usage
     */
    public Usage getAPIUsageBySubscriber(APIIdentifier apiIdentifier, String consumerEmail);

    /**
     * Add new Subscriber
     *
     * @param identifier APIIdentifier
     * @param userId id of the user
     * @param applicationId Application Id
     * @throws APIManagementException if failed to add subscription details to database
     */
    public void addSubscription(APIIdentifier identifier, String userId, int applicationId)
            throws APIManagementException;

   /**
     * Remove a Subscriber
    *
     * @param identifier APIIdentifier
     * @param userId id of the user
     * @throws APIManagementException if failed to add subscription details to database
     */
    public void removeSubscriber(APIIdentifier identifier, String userId)
            throws APIManagementException;

    /**
     * Returns full list of Subscribers of an API
     *
     * @param identifier APIIdentifier
     * @return Set<Subscriber>
     * @throws APIManagementException if failed to get Subscribers
     */
    public Set<Subscriber> getSubscribersOfAPI(APIIdentifier identifier)
            throws APIManagementException;

    /**
     * this method return Set of versions for given provider and api
     *
     * @param providerName name of the provider
     * @param apiName      name of the api
     * @return Set of version
     * @throws APIManagementException if failed to get version for api
     */
    public Set<String> getAPIVersions(String providerName, String apiName)
            throws APIManagementException;

    /**
     * this method returns the Set<APISubscriptionCount> for given provider and api
     *
     * @param identifier APIIdentifier
     * @return Set<APISubscriptionCount>
     * @throws APIManagementException if failed to get APISubscriptionCountByAPI
     */
    public long getAPISubscriptionCountByAPI(APIIdentifier identifier)
            throws APIManagementException;

    /**
     * @param username Name of the user
     * @param password Password of the user
     * @return login status
     */
    public boolean login(String username, String password);

    /**
     * Log out user
     * @param username  name of the user
     */
    public void logout(String username);


    /**
     * Returns a list of pre-defined # {@link org.wso2.carbon.apimgt.api.model.Tier} in the system.
     *
     * @return  Set<Tier>
     */
    public Set<Tier> getTiers();

    //*********************************
    // write operations - API Store
    //*********************************

    /**
     * This method is to update the subscriber.
     *
     * @param identifier APIIdentifier
     * @param userId user id
     * @param applicationId Application Id
     * @throws APIManagementException if failed to update subscription
     */
    public void updateSubscriptions(APIIdentifier identifier, String userId, int applicationId)
            throws APIManagementException;

    //**************************************
    // write operations - Publisher Console
    //**************************************

    /**
     * Adds a new API to the Store
     *
     * @param api API
     * @throws APIManagementException if failed to add API
     */
    public void addAPI(API api) throws APIManagementException;

    /**
     * Updates an existing API
     *
     * @param api API
     * @throws APIManagementException if failed to update API
     */
    public void updateAPI(API api) throws APIManagementException;


    /**
     * Create a new version of the <code>api</code>, with version <code>newVersion</code>
     *
     * @param api        The API to be copied
     * @param newVersion The version of the new API
     * @throws DuplicateAPIException  If the API trying to be created already exists
     * @throws APIManagementException If an error occurs while trying to create
     *                                the new version of the API
     */
    public void createNewAPIVersion(API api, String newVersion) throws DuplicateAPIException,
            APIManagementException;

    //********************
    // Documentation
    //********************

    /**
     * Returns a list of all Documentation attached to a particular API Version
     *
     * @param apiId APIIdentifier
     * @return List<Documentation>
     * @throws APIManagementException if failed to get Documentations
     */
    public List<Documentation> getAllDocumentation(APIIdentifier apiId) throws APIManagementException;

    /**
     * Returns a given documentation
     *
     * @param apiId   APIIdentifier
     * @param docType type of the documentation
     * @param docName name of the doc
     * @return Documentation
     * @throws APIManagementException if failed to get Documentation
     */
    public Documentation getDocumentation(APIIdentifier apiId,
                                          DocumentationType docType,
                                          String docName) throws APIManagementException;

    /**
     * This method used to get the content of a documentation
     *
     * @param identifier, API identifier
     * @param documentationName, name of the inline documentation
     * @throws APIManagementException if the asking documentation content is unavailable
     * @return if failed to get doc content
     */
    public String getDocumentationContent(APIIdentifier identifier, String documentationName)
            throws APIManagementException;

    /**
     * Removes a given documentation
     *
     * @param apiId   APIIdentifier
     * @param docType the type of the documentation
     * @param docName name of the document
     * @throws APIManagementException if failed to remove documentation
     */
    public void removeDocumentation(APIIdentifier apiId,
                                    String docType, String docName) throws APIManagementException;

    /**
     * Adds Documentation to an API
     *
     * @param apiId         APIIdentifier
     * @param documentation Documentation
     * @throws APIManagementException if failed to add documetation
     */
    public void addDocumentation(APIIdentifier apiId,
                                 Documentation documentation) throws APIManagementException;

    /**
     * This method used to save the documentation content
     *
     * @param identifier, API identifier
     * @param documentationName, name of the inline documentation
     * @param  text, content of the inline documentation
     * @throws APIManagementException if failed to add the document as a resource to registry
     */
    public void addDocumentationContent(APIIdentifier identifier, String documentationName, String text)
            throws APIManagementException;

    /**
     * Updates a given documentation
     *
     * @param apiId         APIIdentifier
     * @param documentation Documentation
     * @throws APIManagementException if failed to update docs
     */
    public void updateDocumentation(APIIdentifier apiId,
                                    Documentation documentation) throws APIManagementException;

    /**
     * Copies current Documentation into another version of the same API.
     *
     * @param toVersion Version to which Documentation should be copied.
     * @param apiId     id of the APIIdentifier
     * @throws APIManagementException if failed to copy docs
     */
    public void copyAllDocumentation(APIIdentifier apiId, String toVersion)
            throws APIManagementException;

    /**
     * Get the Subscriber from access token
     * @param accessToken Subscriber key
     * @return Subscriber
     * @throws APIManagementException if failed to get Subscriber from access token
     */
    public Subscriber getSubscriberById(String accessToken) throws APIManagementException;

    /**
     *
     * @param identifier  Api identifier
     * @param s  comment text
     * @throws APIManagementException  if failed to add comment for API
     */
    public void addComment(APIIdentifier  identifier , String s) throws APIManagementException;

    /**
     *
     * @param identifier Api identifier
     * @return  Comments
     * @throws APIManagementException if failed to get comments for identifier
     */
    public Comment[] getComments(APIIdentifier identifier) throws APIManagementException;

    /**
     * Adds an application 
     * @param application Application
     * @param userId User Id
     * @throws APIManagementException  if failed to add Application
     */
    public void addApplication(Application application, String userId) throws APIManagementException;

    /**
     * Returns a list of applications for a given subscriber
     * @param subscriber Subscriber
     * @return  Applications
     * @throws APIManagementException if failed to applications for given subscriber
     */
    public Application[] getApplications(Subscriber subscriber) throws APIManagementException;
        
    /**
     * Creates a new subscriber, the newly created subscriber id will be set in the given object.
     * @param subscriber The subscriber to be added
     * @throws APIManagementException if failed add subscriber
     */
    public void addSubscriber(Subscriber subscriber) throws APIManagementException;
    
    /**
     * Updates the given subscriber.
     * @param subscriber The subscriber to be updated
     * @throws APIManagementException  if failed to update subscriber
     */
    public void updateSubscriber(Subscriber subscriber) throws APIManagementException;
    
    /**
     * Returns the subscriber with the given subscriber id.
     * @param subscriberId The subscriber id of the subscriber to be returned
     * @return The looked up subscriber or null if the requested subscriber does not exist
     * @throws APIManagementException if faild to get Subscriber
     */
    public Subscriber getSubscriber(int subscriberId) throws APIManagementException;
    
}
