/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.cassandra.server;

import org.apache.cassandra.auth.*;
import org.apache.cassandra.config.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.EnumSet;
import java.util.List;

/**
 * Carbon's authorization based implementation for the Cassandra's <coe>IAuthority</code>
 * Resources are mapped to a URL and make the authorization for it
 *
 * @see org.apache.cassandra.auth.IAuthority
 */
public class CarbonCassandraAuthority implements IAuthority {

    private static final Log log = LogFactory.getLog(CarbonCassandraAuthority.class);
    //TODO use constants from user manager
    private static final String ACTION_WRITE = "write";
    private static final String ACTION_READ = "read";

    /**
     * Authorize the given user for performing actions on the given resource
     *
     * @param authenticatedUser <code>AuthenticatedUser</code> instance
     * @param resource          Cassandra's resource such as cf, keyspace
     * @param action            The action is going to be performed by the user
     * @return A set of <code>Permission</code> the given user allowed for the given resource
     * @see #authorize(org.apache.cassandra.auth.AuthenticatedUser, java.util.List, org.apache.cassandra.auth.Action)
     */
    public EnumSet<Permission> authorize(AuthenticatedUser authenticatedUser, List<Object> resource, Action action) {

        if (resource.size() < 2 || !Resources.ROOT.equals(resource.get(0)) || !Resources.KEYSPACES.equals(resource.get(1))) {
            return Permission.NONE;
        }

        String resourcePath = "/" + Resources.ROOT + "/" + Resources.KEYSPACES;

        if (resource.size() == 2) {
            //nothing
        } else if (resource.size() == 3) {
            String keyspace = (String) resource.get(2);
            resourcePath += "/" + keyspace;
        } else if (resource.size() == 4) {
            String keyspace = (String) resource.get(2);
            String columnFamily = (String) resource.get(3);
            resourcePath += "/" + keyspace + "/" + columnFamily;
        } else {
            String msg = "Do not currently descend any lower in the hierarchy than the column family";
            log.error(msg);
            throw new UnsupportedOperationException(msg);
        }

        try {
            //TODO - get the realm for a special user that can be used to authorize other users - security team - already talked but need to implement
            UserRealm userRealm =
                    CassandraServerComponentManager.getInstance().getRealmForTenant(authenticatedUser.domainName);
            UserStoreManager userStoreManager = userRealm.getUserStoreManager();
            AuthorizationManager authorizationManager = userRealm.getAuthorizationManager();

            String tenantLessUsername = UserCoreUtil.getTenantLessUsername(authenticatedUser.username);

            switch (action) {
                case ADD: {
                    return authorizeForAdd(userStoreManager, authorizationManager, tenantLessUsername, resourcePath);
                }
                case UPDATE: {
                    return authorizeForWrite(authorizationManager, tenantLessUsername, resourcePath);
                }
                case READ: {
                    return authorizeForRead(authorizationManager, tenantLessUsername, resourcePath);
                }
                case DELETE: {
                    return authorizeForWrite(authorizationManager, tenantLessUsername, resourcePath);
                }
                case ALL: {
                    return authorizeForWrite(authorizationManager, tenantLessUsername, resourcePath); //TODO check read if need
                }
                default: {
                    log.error("Undefined action for resource" + resourcePath);
                    return Permission.NONE;
                }
            }
        } catch (UserStoreException e) {
            log.error("Error during authorizing a user for a resource" + resourcePath, e);
            return Permission.NONE;
        }
    }

    /**
     * Helper method to check the write operation on a resource for the current user
     *
     * @param authorizationManager <code>AuthorizationManager</code>
     * @param tenantLessUsername   username
     * @param resourcePath         resource
     * @return a set of <code>Permission</code>
     */
    private EnumSet<Permission> authorizeForWrite(AuthorizationManager authorizationManager,
                                                  String tenantLessUsername,
                                                  String resourcePath) {
        try {
            EnumSet<Permission> authorized = EnumSet.noneOf(Permission.class);
            boolean isAuthorized = authorizationManager.isUserAuthorized(tenantLessUsername,
                    resourcePath,
                    ACTION_WRITE);
            if (isAuthorized) {
                authorized.add(Permission.WRITE);
                return authorized;
            }
        } catch (UserStoreException e) {
            log.error("Authorization failure for user " + tenantLessUsername +
                    " for performing write on resource" + resourcePath);
        }

        return Permission.NONE;
    }

    /**
     * Helper method to check the read operation on a resource for the current user
     *
     * @param authorizationManager <code>AuthorizationManager</code>
     * @param tenantLessUsername   username
     * @param resourcePath         resource
     * @return a set of <code>Permission</code>
     */
    private EnumSet<Permission> authorizeForRead(AuthorizationManager authorizationManager,
                                                 String tenantLessUsername,
                                                 String resourcePath) {
        try {
            EnumSet<Permission> authorized = EnumSet.noneOf(Permission.class);
            boolean isAuthorized = authorizationManager.isUserAuthorized(tenantLessUsername,
                    resourcePath,
                    ACTION_READ);

            if (isAuthorized) {
                authorized.add(Permission.READ);
                return authorized;
            }
        } catch (UserStoreException e) {
            log.error("Authorization failure for user " + tenantLessUsername +
                    " for performing read on resource" + resourcePath);
        }
        return Permission.NONE;
    }

    /**
     * Helper method to authorize a user when adding a resorce. Here we create a role with the user's name
     * TODO This should be done at the user manager level.
     *
     * @param userStoreManager     <code>UserStoreManager</code>
     * @param authorizationManager <code>AuthorizationManager</code>
     * @param tenantLessUsername   user name
     * @param resourcePath         resource
     * @return a set of <code>Permission</code>
     */
    private EnumSet<Permission> authorizeForAdd(UserStoreManager userStoreManager,
                                                AuthorizationManager authorizationManager,
                                                String tenantLessUsername,
                                                String resourcePath) {
        try {
            if(authorizationManager.isUserAuthorized(tenantLessUsername,resourcePath,ACTION_READ) && authorizationManager.isUserAuthorized(tenantLessUsername,resourcePath,ACTION_WRITE)){
                  return  Permission.ALL;
              }
//            if (userStoreManager.isExistingRole(tenantLessUsername)) {
//                authorizationManager.authorizeRole(tenantLessUsername, resourcePath, ACTION_WRITE);
//                authorizationManager.authorizeRole(tenantLessUsername, resourcePath, ACTION_READ);
//            } else {
//                // TODO this code should be done by user manager component
//                org.wso2.carbon.user.api.Permission[] permissions = new org.wso2.carbon.user.api.Permission[2];
//                permissions[0] = new org.wso2.carbon.user.api.Permission(resourcePath, ACTION_WRITE);
//                permissions[1] = new org.wso2.carbon.user.api.Permission(resourcePath, ACTION_READ);
//
//                userStoreManager.addRole(tenantLessUsername, new String[]{tenantLessUsername}, permissions);
//            }
        } catch (UserStoreException e) {
            log.error("Authorization failure for user " + tenantLessUsername +
                    " for performing add resource" + resourcePath);
            return Permission.NONE;
        }
        return Permission.NONE;
    }

    public void validateConfiguration() throws ConfigurationException {
    }
}
