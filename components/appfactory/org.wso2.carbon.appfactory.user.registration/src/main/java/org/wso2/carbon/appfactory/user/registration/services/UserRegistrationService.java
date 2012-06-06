/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.appfactory.user.registration.services;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.user.registration.beans.UserRegistrationInfoBean;
import org.wso2.carbon.appfactory.user.registration.util.Util;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.exceptions.ResourceNotFoundException;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.Permission;
import org.wso2.carbon.user.core.UserRealm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the implementation of UserRegistrationService
 */
public class UserRegistrationService extends AbstractAdmin {
    private static final Log log = LogFactory.getLog(UserRegistrationService.class);
    public static final String USER_VALIDATION_KEY_PATH = "org.wso2.carbon.appfactory.user" +
                                                          ".registration-validation-key";

    public String registerUser(UserRegistrationInfoBean user) throws UserRegistrationException {
        if (!doesUserExist(user.getUserName())) {
            return addUser(user);
        }
        return null;
    }

    private String addUser(UserRegistrationInfoBean user) throws UserRegistrationException {
        UserStoreManager userStoreManager = getUserStoreManager();
        UserRealm realm = getUserRealm();
        String[] roles = new String[1];
        Map<String, String> claims = new HashMap<String, String>();

        claims.put("http://wso2.org/claims/emailaddress", user.getEmail());
        claims.put("http://wso2.org/claims/givenname", user.getFirstName());
        claims.put("http://wso2.org/claims/lastname", user.getLastName());

        try {
            roles[0] = realm.getRealmConfiguration().getEveryOneRoleName();
            userStoreManager.addUser(user.getUserName(), user.getAdminPassword(),
                                     roles, claims, null);
        } catch (UserStoreException e) {
            handleException("Error in adding the user to user store", e);
        }
        return generateKey(user.getUserName(), user.getEmail());
    }


    public boolean activateUser(String confirmationKey, String userName, String email)
            throws UserRegistrationException {
        Registry superTenantRegistry = getSuperTenantRegistry();
        String userValidationKeyPath = UserRegistrationService.USER_VALIDATION_KEY_PATH +
                                       RegistryConstants.PATH_SEPARATOR + userName;
        Resource resource = null;
        try {
            resource = superTenantRegistry.get(userValidationKeyPath);
        } catch (ResourceNotFoundException e) {
            handleException("Confirmation key is not found for " + userName, e);
        } catch (RegistryException e) {
            handleException("Error in accessing registry collection " + userValidationKeyPath, e);
        }
        String userEmail = resource.getProperty("email");
        Object userConfirmationKeyObject = null;
        String userConfirmationKey = null;
        try {
            userConfirmationKeyObject = resource.getContent();
        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            handleException("Error while adding resource to registry", e);
        }
        if (userConfirmationKeyObject instanceof String) {
            userConfirmationKey = (String) userConfirmationKeyObject;
        } else if (userConfirmationKeyObject instanceof byte[]) {
            userConfirmationKey = new String((byte[]) userConfirmationKeyObject);
        }
        if ((email.equals(userEmail)) && (confirmationKey.equals(userConfirmationKey))) {
            try {
                superTenantRegistry.delete(userValidationKeyPath);
                updateUserRole(userName);
            } catch (RegistryException e) {
                handleException("Could not delete confirmation key for " + userName, e);
            } catch (AppFactoryException e) {
                handleException("Failed to add/update default role for user:" + userName, e);
            }
            return true;
        }

        return false;
    }

    private void updateUserRole(String userName)
            throws UserRegistrationException, AppFactoryException {
        UserRealm realm = getUserRealm();
        try {
            UserStoreManager userStoreManager = realm.getUserStoreManager();

            String defaultRoleName = Util.getConfiguration().getFirstProperty(
                    AppFactoryConstants.DEFAULT_APPLICATION_USER_ROLE);

            if (!userStoreManager.isExistingRole(defaultRoleName)) {
                String defaultPermissionString = Util.getConfiguration().getFirstProperty(
                        AppFactoryConstants.DEFAULT_APPLICATION_USER_ROLE + "." + defaultRoleName +
                        "." + AppFactoryConstants.PERMISSION);
                if (defaultPermissionString == null) {
                    throw new AppFactoryException("Failed to get default application user role permissions.");
                }

                String[] resourceIds = defaultPermissionString.split(",");
                List<Permission> permissionList = new ArrayList<Permission>();
                for (String resourceId : resourceIds) {
                    Permission permission = new Permission(resourceId,
                                                           CarbonConstants.UI_PERMISSION_ACTION);
                    permissionList.add(permission);
                }
                userStoreManager.addRole(defaultRoleName, new String[]{userName},
                                         new Permission[permissionList.size()]);
            } else {
                userStoreManager.updateRoleListOfUser(userName, null, new String[]{defaultRoleName});
            }
        } catch (UserStoreException e) {
            handleException("Failed to get add/update roles of user:" + userName, e);
        }
    }


    public boolean doesUserExist(String userName) throws UserRegistrationException {
        UserStoreManager userStoreManager = getUserStoreManager();
        try {
            return userStoreManager.isExistingUser(userName);
        } catch (UserStoreException e) {
            log.error("Error while checking user existance in user store");
            throw new UserRegistrationException();
        }

    }

    private Registry getSuperTenantRegistry() throws UserRegistrationException {
        Registry superTenantRegistry;
        superTenantRegistry = getGovernanceUserRegistry();

        if (superTenantRegistry == null) {
            String msg = "Error while retrieving registry";
            throw new UserRegistrationException(msg);
        }
        return superTenantRegistry;
    }

    private void handleException(String msg, Exception e) throws UserRegistrationException {
        log.error(msg, e);
        throw new UserRegistrationException(msg, e);
    }

    private UserStoreManager getUserStoreManager() throws UserRegistrationException {
        UserStoreManager manager = null;

        try {

            manager = getUserRealm().getUserStoreManager();
        } catch (UserStoreException e) {
            handleException("Error in initialising user store", e);
        }
        return manager;
    }

    private String generateKey(String userName, String email) throws UserRegistrationException {
        Registry superTenantRegistry = getSuperTenantRegistry();
        String userValidationKeyPath = UserRegistrationService.USER_VALIDATION_KEY_PATH +
                                       RegistryConstants.PATH_SEPARATOR + userName;
        String confirmationKey = UUIDGenerator.generateUUID();
        Resource resource;
        try {
            resource = superTenantRegistry.newResource();
            resource.setContent(confirmationKey);
            resource.setProperty("confirmationFlag", "false");
            resource.setProperty("email", email);
            superTenantRegistry.put(userValidationKeyPath, resource);
        } catch (Exception e) {
            handleException("Error while adding resource to registry", e);
        }
        return confirmationKey;

    }
}
