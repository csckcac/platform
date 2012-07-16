/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.identity.oauth.callback;

import org.apache.amber.oauth2.common.message.types.GrantType;
import org.apache.amber.oauth2.common.message.types.ResponseType;

import javax.security.auth.callback.Callback;

/**
 * Represents a Authorization callback which represents an authorization action corresponding
 * to authorizing clients to access resources by a resource owners. This callback is used to check
 * whether the given resource owner is the rightful owner to grant authorization to the client to
 * access the given resource.
 */
public class OAuthCallback implements Callback {
    /**
     * Used to evaluate the type of the callback
     */
    public enum OAuthCallbackType { ACCESS_DELEGATION, SCOPE_VALIDATION }

    /**
     * Callback Type
     */
    private OAuthCallbackType callbackType;
    /**
     * Claimed resource owner
     */
    private String resourceOwner;

    /**
     * OAuth2 Grant Type
     */
    private ResponseType responseType;
    /**
     * OAuth2 grant type
     */
    private GrantType grantType;
    /**
     * Client who will be accessing the resource
     */
    private String client;
    /**
     * A set of strings represents the resource + action used in callback req.
     */
    private String[] requestedScope;

    /**
     * A set of strings represents the resource + action used in token req.
     */
    private String[] approvedScope;

    /**
     * Whether the callback is authorized
     */
    private boolean authorized;

    /**
     * Requested scope is invalid.
     */
    private boolean validScope;

    /**
     * Creates an instance of the OAuthCallback
     * @param resourceOwner Claimed resource owner
     * @param client    Client who will be accessing the resource
     * @param callbackType the callback type
     */
    public OAuthCallback(String resourceOwner,
                         String client,
                         OAuthCallbackType callbackType) {
        this.resourceOwner = resourceOwner;
        this.client = client;
        this.callbackType = callbackType;
    }

    /**
     * Get the resource owner
     * @return Identifier of the Resource Owner
     */
    public String getResourceOwner() {
        return resourceOwner;
    }

    /**
     * Returns the Client
     * @return Identifier of the Resource Owner
     */
    public String getClient() {
        return client;
    }

    /**
     * Returns the callback type
     * @return <Code>OAuthCallbackType</Code> of the callback type
     */
    public OAuthCallbackType getCallbackType() {
        return callbackType;
    }

    /**
     * Returns the Scope corresponding to the Authz Request
     * @return <Code>String</Code> array representing the scope
     */
    public String[] getRequestedScope() {
        return requestedScope;
    }

    /**
     * Returns the Scope corresponding to the token Request
     * @return <Code>String</Code> array representing the scope
     */
    public String[] getApprovedScope() {
        return approvedScope;
    }

    /**
     * Returns the OAuth2 Grant Type
     * @return
     */
    public ResponseType getResponseType() {
        return responseType;
    }

    /**
     * Whether the callback is authorized or not
     * @return <Code>true</Code> if the callback is authorized, <code>false</code> otherwise.
     */
    public boolean isAuthorized() {
        return authorized;
    }

    /**
     * Whether the requested scope is invalid.
     * @return <Code>true</Code> if scope is invalid, <code>false</code> otherwise.
     */
    public boolean isValidScope() {
        return validScope;
    }

    /**
     * Set whether the callback is authorized or not.
     * @param authorized <Code>true</Code> if callback is authorized, <code>false</code> otherwise.
     */
    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
    }

    /**
     * set whether the scope is invalid or not
     * @param invalidScope <code>true</code> if scope is invalid.
     */
    public void setValidScope(boolean invalidScope) {
        this.validScope = invalidScope;
    }

    /**
     * Set the scope for callback request
     * @param requestedScope <Code>String</Code> array representing the scope
     */
    public void setRequestedScope(String[] requestedScope) {
        this.requestedScope = requestedScope;
    }

    /**
     * Set the scope for token request
     * @param approvedScope <Code>String</Code> array representing the scope
     */
    public void setApprovedScope(String[] approvedScope) {
        this.approvedScope = approvedScope;
    }

    /**
     * Sets the response type
     * @param responseType
     */
    public void setResponseType(ResponseType responseType) {
        this.responseType = responseType;
    }

    /**
     * Returns the grant type
     * @return Corresponding <Code>GrantType</Code> of the access token request
     */
    public GrantType getGrantType() {
        return grantType;
    }

    /**
     * Set the Grant Type
     * @param grantType Corresponding <Code>GrantType</Code> of the access token request
     */
    public void setGrantType(GrantType grantType) {
        this.grantType = grantType;
    }
}
