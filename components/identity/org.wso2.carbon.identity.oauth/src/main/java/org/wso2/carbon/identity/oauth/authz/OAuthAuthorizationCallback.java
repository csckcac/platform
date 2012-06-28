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

package org.wso2.carbon.identity.oauth.authz;

import javax.security.auth.callback.Callback;

/**
 * Represents a Authorization callback which represents an authorization action corresponding
 * to authorizing clients to access resources by a resource owners. This callback is used to check
 * whether the given resource owner is the rightful owner to grant authorization to the client to
 * access the given resource.
 */
public class OAuthAuthorizationCallback implements Callback {
    /**
     * Claimed resource owner
     */
    private String resourceOwner;
    /**
     * Client who will be accessing the resource
     */
    private String client;
    /**
     * A set of strings represents the resource + action
     */
    private String[] scope;

    /**
     * Whether the callback is authorized
     */
    private boolean authorized;

    /**
     * Requested scope is invalid.
     */
    private boolean invalidScope;

    /**
     * Creates an instance of the OAuthAuthorizationCallback
     * @param resourceOwner Claimed resource owner
     * @param client    Client who will be accessing the resource
     * @param scope A set of strings represents the resource + action
     */
    public OAuthAuthorizationCallback(String resourceOwner, String client, String[] scope) {
        this.resourceOwner = resourceOwner;
        this.client = client;
        this.scope = scope;
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
     * Returns the scope
     * @return an array of strings representing the resources + actions
     */
    public String[] getScope() {
        return scope;
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
    public boolean isInvalidScope() {
        return invalidScope;
    }

    /**
     * Set the scope, in case of overriding the original scope
     * @param scope new scope values as an array of Strings
     */
    public void setScope(String[] scope) {
        this.scope = scope;
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
    public void setInvalidScope(boolean invalidScope) {
        this.invalidScope = invalidScope;
    }
}
