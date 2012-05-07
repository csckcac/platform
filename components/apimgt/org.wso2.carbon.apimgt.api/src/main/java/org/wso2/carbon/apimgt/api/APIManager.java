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

import org.wso2.carbon.apimgt.api.model.*;

import java.util.List;
import java.util.Set;

/**
 * Manager responsible for providing helper functionality on Subscribers, Providers, APIs.
 */
public interface APIManager {

    /**
     * returns details of an API
     *
     * @param identifier APIIdentifier
     * @return API
     * @throws APIManagementException if failed get API from APIIdentifier
     */
    public API getAPI(APIIdentifier identifier) throws APIManagementException;

    /**
     * Check the Availability of given APIIdentifier
     *
     * @param identifier APIIdentifier
     * @return true, if already exists. False, otherwise
     * @throws APIManagementException if failed to get API availability
     */
    public boolean isAPIAvailable(APIIdentifier identifier) throws APIManagementException;

    /**
     * this method return Set of versions for given provider and api
     *
     * @param providerName name of the provider (common)
     * @param apiName      name of the api
     * @return Set of version
     * @throws APIManagementException if failed to get version for api
     */
    public Set<String> getAPIVersions(String providerName, String apiName)
            throws APIManagementException;

    /**
     * @param username Name of the user
     * @param password Password of the user
     * @return login status
     */
    public boolean login(String username, String password);

    /**
     * Log out user
     *
     * @param username name of the user
     */
    public void logout(String username);

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
     * @param identifier,        API identifier
     * @param documentationName, name of the inline documentation
     * @return if failed to get doc content
     * @throws APIManagementException if the asking documentation content is unavailable
     */
    public String getDocumentationContent(APIIdentifier identifier, String documentationName)
            throws APIManagementException;

    /**
     * Get the Subscriber from access token
     *
     * @param accessToken Subscriber key
     * @return Subscriber
     * @throws APIManagementException if failed to get Subscriber from access token
     */
    public Subscriber getSubscriberById(String accessToken) throws APIManagementException;

    /**
     * Creates a new subscriber, the newly created subscriber id will be set in the given object.
     *
     * @param subscriber The subscriber to be added
     * @throws APIManagementException if failed add subscriber
     */
    public void addSubscriber(Subscriber subscriber) throws APIManagementException;

    /**
     * Updates the given subscriber.
     *
     * @param subscriber The subscriber to be updated
     * @throws APIManagementException if failed to update subscriber
     */
    public void updateSubscriber(Subscriber subscriber) throws APIManagementException;

    /**
     * Returns the subscriber with the given subscriber id.
     *
     * @param subscriberId The subscriber id of the subscriber to be returned
     * @return The looked up subscriber or null if the requested subscriber does not exist
     * @throws APIManagementException if failed to get Subscriber
     */
    public Subscriber getSubscriber(int subscriberId) throws APIManagementException;

}
