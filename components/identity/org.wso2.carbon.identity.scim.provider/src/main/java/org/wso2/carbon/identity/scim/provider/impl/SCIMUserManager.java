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
package org.wso2.carbon.identity.scim.provider.impl;

import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.scim.common.impl.AbstractProvisioningHandler;
import org.wso2.carbon.identity.scim.common.utils.AttributeMapper;
import org.wso2.carbon.identity.scim.common.utils.IdentitySCIMException;
import org.wso2.carbon.identity.scim.common.utils.SCIMCommonConstants;
import org.wso2.carbon.user.api.Claim;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.charon.core.attributes.Attribute;
import org.wso2.charon.core.attributes.SimpleAttribute;
import org.wso2.charon.core.exceptions.CharonException;
import org.wso2.charon.core.exceptions.NotFoundException;
import org.wso2.charon.core.extensions.UserManager;
import org.wso2.charon.core.objects.Group;
import org.wso2.charon.core.objects.User;

import org.apache.commons.logging.Log;
import org.wso2.charon.core.schema.ResourceSchema;
import org.wso2.charon.core.schema.SCIMConstants;
import org.wso2.charon.core.schema.SCIMSchemaDefinitions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SCIMUserManager extends AbstractProvisioningHandler implements UserManager {
    private UserStoreManager carbonUM = null;
    private ClaimManager carbonClaimManager = null;
    private String consumerName;

    private static Log log = LogFactory.getLog(SCIMUserManager.class);

    public SCIMUserManager(UserStoreManager carbonUserStoreManager, String userName,
                           ClaimManager claimManager) {
        this.initConfigManager();
        carbonUM = carbonUserStoreManager;
        consumerName = userName;
        carbonClaimManager = claimManager;
    }

    public User createUser(User user) throws CharonException {
        Map<String, String> claimsMap = AttributeMapper.getClaimsMap(user);
        //TODO: Do not accept the roles list - it is read only.
        try {
            carbonUM.addUser(user.getUserName(), user.getPassword(), null, claimsMap, null);
            log.info("User: " + user.getUserName() + " is created through SCIM.");
            //if a consumer is registered for this SCIM operation, provision as appropriate
            if (isSCIMConsumerEnabled(consumerName)) {
                this.provision(consumerName, user, SCIMConstants.POST);
            }
        } catch (UserStoreException e) {
            throw new CharonException("Error in adding the user: " + user.getUserName() +
                                      " to the user store..");
        } catch (IdentitySCIMException e) {
            throw new CharonException(e.getMessage());
        }
        return user;
    }

    public User getUser(String userId) throws CharonException {
        if (log.isDebugEnabled()) {
            log.debug("Retrieving user: " + userId);
        }
        User scimUser = null;
        try {
            //get the user name of the user with this id
            String[] userNames = carbonUM.getUserList(SCIMConstants.ID_URI, userId,
                                                      UserCoreConstants.DEFAULT_PROFILE);
            //we assume (since id is unique per user) only one user exists for a given id
            scimUser = this.getSCIMUser(userNames[0]);

            log.info("User: " + scimUser.getUserName() + " is retrieved through SCIM.");

        } catch (UserStoreException e) {
            throw new CharonException("Error in getting user information from Carbon User Store for" +
                                      "user: " + userId);
        }
        return scimUser;
    }

    public List<User> listUsers() throws CharonException {
        List<User> users = new ArrayList();
        try {
            String[] userNames = carbonUM.listUsers(null, -1);
            for (String userName : userNames) {
                User scimUser = this.getSCIMUser(userName);
                Map<String, Attribute> attrMap = scimUser.getAttributeList();
                if (attrMap != null && !attrMap.isEmpty()) {
                    users.add(this.getSCIMUser(userName));
                }
            }

        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            throw new CharonException("Error while retrieving users from user store..");
        }
        return users;
    }

    public List<User> listUsersByAttribute(Attribute attribute) {
        return null;
    }

    public List<User> listUsersByFilter(String s) {
        return null;
    }

    public List<User> listUsersBySort(String s, String s1) {
        return null;
    }

    public List<User> listUsersWithPagination(int i, int i1) {
        return null;
    }

    public User updateUser(User user) throws CharonException {
        if (log.isDebugEnabled()) {
            log.debug("Updating user: " + user.getUserName());
        }
        try {
            //get user claim values
            Map<String, String> claims = AttributeMapper.getClaimsMap(user);
            //set user claim values
            carbonUM.setUserClaimValues(user.getUserName(), claims, null);
            //if password is updated, set it separately
            if (user.getPassword() != null) {
                carbonUM.updateCredentialByAdmin(user.getUserName(), user.getPassword());
                log.info("User: " + user.getUserName() + " updated successfully.");
            }
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            throw new CharonException("Error while updating attributes of user: " + user.getUserName());
        }

        return user;
    }

    public User updateUser(List<Attribute> attributes) {
        return null;
    }

    public void deleteUser(String userId) throws NotFoundException, CharonException {
        if (log.isDebugEnabled()) {
            log.debug("Deleting user: " + userId);
        }
        //get the user name of the user with this id
        String[] userNames = new String[0];
        String userName = null;
        try {
            userNames = carbonUM.getUserList(SCIMConstants.ID_URI, userId,
                                             UserCoreConstants.DEFAULT_PROFILE);
            //we assume (since id is unique per user) only one user exists for a given id
            userName = userNames[0];
            carbonUM.deleteUser(userName);
            log.info("User: " + userName + " is deleted through SCIM.");

        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            throw new CharonException("Error in deleting user: " + userName);
        }
    }

    public Group createGroup(Group group) throws CharonException {
        try {
            /*if members are sent when creating the group, check whether users already exist in the
            user store*/
            List<String> members = group.getMembersWithDisplayName();
            if (members != null && !members.isEmpty()) {
                for (String member : members) {
                    if (!carbonUM.isExistingUser(member)) {
                        String error = "User: " + member + " doesn't exist in the user store. " +
                                       "Hence, can not add the group: " + group.getDisplayName();
                        throw new IdentitySCIMException(error);
                    }
                }
                carbonUM.addRole(group.getDisplayName(), members.toArray(new String[members.size()]), null);
                log.info("group: " + group.getDisplayName() + " is created through SCIM.");
            } else {
                carbonUM.addRole(group.getDisplayName(), null, null);
                log.info("group: " + group.getDisplayName() + " is created through SCIM.");
            }
            if (isSCIMConsumerEnabled(consumerName)) {
                this.provision(consumerName, group, SCIMConstants.POST);
            }
        } catch (UserStoreException e) {
            throw new CharonException(e.getMessage());
        } catch (IdentitySCIMException e) {
            throw new CharonException(e.getMessage());
        }
        //TODO:after the group is added, read it from user store and return
        return group;
    }

    public Group getGroup(String s) throws CharonException {
        return null;
    }

    public List<Group> listGroups() throws CharonException {
        return null;
    }

    public List<Group> listGroupsByAttribute(Attribute attribute) throws CharonException {
        return null;
    }

    public List<Group> listGroupsByFilter(String s) throws CharonException {
        return null;
    }

    public List<Group> listGroupsBySort(String s, String s1) throws CharonException {
        return null;
    }

    public List<Group> listGroupsWithPagination(int i, int i1) {
        return null;
    }

    public Group updateGroup(Group group) throws CharonException {
        return null;
    }

    public Group updateGroup(List<Attribute> attributes) throws CharonException {
        return null;
    }

    public void deleteGroup(String s) throws NotFoundException, CharonException {

    }

    @Override
    public boolean isSCIMConsumerEnabled(String consumerName) {
        //check if providers registered for consumerName
        return ((provisioningManager.isConsumerRegistered(consumerName)) &&
                (provisioningManager.isAppliedToSCIMOperation(consumerName)));
    }

    private User getSCIMUser(String userName) throws CharonException {
        User scimUser = null;
        try {
            //get claims related to SCIM claim dialect
            Claim[] claims = carbonClaimManager.getAllClaims(SCIMCommonConstants.SCIM_CLAIM_DIALECT);

            List<String> claimURIList = new ArrayList<String>();
            for (Claim claim : claims) {
                claimURIList.add(claim.getClaimUri());
            }
            //obtain user claim values
            Map<String, String> attributes = carbonUM.getUserClaimValues(
                    userName, claimURIList.toArray(new String[claimURIList.size()]), null);
            //get groups of user and add it as groups attribute
            String[] roles = carbonUM.getRoleListOfUser(userName);
            //TODO: for the moment, add groups under roles - change this to groups attribute
            // after group id support is implemented
            String roleValues = null;
            for (String role : roles) {
                if (roleValues != null) {
                    roleValues += role + ",";
                } else {
                    roleValues = role + ",";
                }
            }
            //construct the SCIM Object from the attributes
            scimUser = (User) AttributeMapper.constructSCIMObjectFromAttributes(
                    attributes, SCIMConstants.USER_INT);
        } catch (UserStoreException e) {
            throw new CharonException("Error in getting user information from Carbon User Store for" +
                                      "user: " + userName);
        } catch (CharonException e) {
            throw new CharonException("Error in getting user information from Carbon User Store for" +
                                      "user: " + userName);
        } catch (NotFoundException e) {
            throw new CharonException("Error in getting user information from Carbon User Store for" +
                                      "user: " + userName);
        }
        return scimUser;
    }
}
