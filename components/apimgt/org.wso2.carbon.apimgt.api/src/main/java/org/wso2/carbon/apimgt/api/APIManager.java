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

import java.io.InputStream;
import java.util.List;
import java.util.Set;

/**
 * Core API management interface which provides functionality related to APIs, API metadata
 * and API subscribers (consumers).
 */
public interface APIManager {

    /**
     * Returns details of an API
     *
     * @param identifier APIIdentifier
     * @return An API object related to the given identifier or null
     * @throws APIManagementException if failed get API from APIIdentifier
     */
    public API getAPI(APIIdentifier identifier) throws APIManagementException;

    /**
     * Checks the Availability of given APIIdentifier
     *
     * @param identifier APIIdentifier
     * @return true, if already exists. False, otherwise
     * @throws APIManagementException if failed to get API availability
     */
    public boolean isAPIAvailable(APIIdentifier identifier) throws APIManagementException;

    /**
     * Checks whether the given API context is already registered in the system
     *
     * @param context A String representing an API context
     * @return true if the context already exists and false otherwise
     * @throws APIManagementException if failed to check the context availability
     */
    public boolean isContextExist(String context) throws APIManagementException;

    /**
     * Returns a set of API versions for the given provider and API name
     *
     * @param providerName name of the provider (common)
     * @param apiName      name of the api
     * @return Set of version strings (possibly empty)
     * @throws APIManagementException if failed to get version for api
     */
    public Set<String> getAPIVersions(String providerName, String apiName)
            throws APIManagementException;

    /**
     * Returns a list of documentation attached to a particular API
     *
     * @param apiId APIIdentifier
     * @return List<Documentation>
     * @throws APIManagementException if failed to get Documentations
     */
    public List<Documentation> getAllDocumentation(APIIdentifier apiId) throws APIManagementException;

    /**
     * Returns the specified document attached to the given API
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
     * Retrieves the subscriber from the given access token
     *
     * @param accessToken Subscriber key
     * @return Subscriber
     * @throws APIManagementException if failed to get Subscriber from access token
     */
    public Subscriber getSubscriberById(String accessToken) throws APIManagementException;

    /**
     * Creates a new subscriber. The newly created subscriber id will be set in the given object.
     *
     * @param subscriber The subscriber to be added
     * @throws APIManagementException if failed add subscriber
     */
    public void addSubscriber(Subscriber subscriber) throws APIManagementException;

    /**
     * Updates the details of the given subscriber.
     *
     * @param subscriber The subscriber to be updated
     * @throws APIManagementException if failed to update subscriber
     */
    public void updateSubscriber(Subscriber subscriber) throws APIManagementException;

    /**
     * Returns the subscriber for the given subscriber id.
     *
     * @param subscriberId The subscriber id of the subscriber to be returned
     * @return The looked up subscriber or null if the requested subscriber does not exist
     * @throws APIManagementException if failed to get Subscriber
     */
    public Subscriber getSubscriber(int subscriberId) throws APIManagementException;

    /**
     * Returns a set of APIs purchased by the given Subscriber
     *
     * @param subscriber Subscriber
     * @return Set<API>
     * @throws APIManagementException if failed to get API for subscriber
     */
    public Set<API> getSubscriberAPIs(Subscriber subscriber) throws APIManagementException;

    /**
     * Associates the given icon image with the specified API
     * 
     * @param identifier an ID representing an API
     * @param in InputStream for an image
     * @param contentType Content type (media type) for the image
     * @return a String URL pointing to the image that was added
     * @throws APIManagementException if an error occurs while adding the icon image
     */
    public String addIcon(APIIdentifier identifier, InputStream in,
                        String contentType) throws APIManagementException;

    /**
     * Retrieves the icon image associated with a particular API as a stream.
     *
     * @param identifier ID representing the API
     * @return an InputStream for an image or null of an image does not exist
     * @throws APIManagementException if an error occurs while retrieving the image
     */
    public InputStream getIcon(APIIdentifier identifier) throws APIManagementException;

    /**
     * Cleans up any resources acquired by this APIManager instance. It is recommended
     * to call this method once the APIManager instance is no longer required.
     *
     * @throws APIManagementException if an error occurs while cleaning up
     */
    public void cleanup() throws APIManagementException;

}
