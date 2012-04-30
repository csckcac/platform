package org.wso2.carbon.apimgt.api;

import org.wso2.carbon.apimgt.api.model.*;

import java.util.Set;

/**
 * Manager responsible for providing helper functionality for API consumer
 */
public interface APIConsumer {
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
     * Returns a list of all published APIs. If a given API has multiple APIs,
     * only the latest version will be included
     * in this list.
     *
     * @return set of API
     * @throws APIManagementException if failed to API set
     */
    public Set<API> getAllPublishedAPIs() throws APIManagementException;

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
     * @return Set<API>
     * @throws APIManagementException if failed to get API for subscriber
     */
    public Set<SubscribedAPI> getSubscribedAPIs(Subscriber subscriber) throws APIManagementException;

    /**
     * Returns a set of APIs purchased by the given Subscriber
     *
     * @param subscriber Subscriber
     * @return Set<API>
     * @throws APIManagementException if failed to get API for subscriber
     */
    public Set<API> getSubscriberAPIs(Subscriber subscriber) throws APIManagementException;

    /**
     * Returns true if a given user has subscribed to the API
     *
     * @param apiIdentifier APIIdentifier
     * @param userId        user id
     * @return true, if giving api identifier is already subscribed
     * @throws APIManagementException if failed to check the subscribed state
     */
    public boolean isSubscribed(APIIdentifier apiIdentifier, String userId) throws APIManagementException;

    /**
     * Add new Subscriber
     *
     * @param identifier    APIIdentifier
     * @param userId        id of the user
     * @param applicationId Application Id
     * @throws APIManagementException if failed to add subscription details to database
     */
    public void addSubscription(APIIdentifier identifier, String userId, int applicationId)
            throws APIManagementException;

    /**
     * Remove a Subscriber
     *
     * @param identifier APIIdentifier
     * @param userId     id of the user
     * @throws APIManagementException if failed to add subscription details to database
     */
    public void removeSubscriber(APIIdentifier identifier, String userId)
            throws APIManagementException;

    /**
     * This method is to update the subscriber.
     *
     * @param identifier    APIIdentifier
     * @param userId        user id
     * @param applicationId Application Id
     * @throws APIManagementException if failed to update subscription
     */
    public void updateSubscriptions(APIIdentifier identifier, String userId, int applicationId)
            throws APIManagementException;

    /**
     * @param identifier Api identifier
     * @param s          comment text
     * @throws APIManagementException if failed to add comment for API
     */
    public void addComment(APIIdentifier identifier, String s) throws APIManagementException;

    /**
     * @param identifier Api identifier
     * @return Comments
     * @throws APIManagementException if failed to get comments for identifier
     */
    public Comment[] getComments(APIIdentifier identifier) throws APIManagementException;

    /**
     * Adds an application
     *
     * @param application Application
     * @param userId      User Id
     * @throws APIManagementException if failed to add Application
     */
    public void addApplication(Application application, String userId) throws APIManagementException;

    /**
     * Returns a list of applications for a given subscriber
     *
     * @param subscriber Subscriber
     * @return Applications
     * @throws APIManagementException if failed to applications for given subscriber
     */
    public Application[] getApplications(Subscriber subscriber) throws APIManagementException;

    public Set<SubscribedAPI> getSubscribedIdentifiers(Subscriber subscriber,
                                                       APIIdentifier identifier) throws APIManagementException;
}
