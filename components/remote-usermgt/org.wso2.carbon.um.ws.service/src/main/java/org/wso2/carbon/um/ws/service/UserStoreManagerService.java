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
package org.wso2.carbon.um.ws.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.um.ws.service.internal.UMRemoteServicesDSComponent;
import org.wso2.carbon.user.core.Permission;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.claim.Claim;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.Tenant;
import org.wso2.carbon.user.mgt.common.ClaimValue;

public class UserStoreManagerService extends AbstractAdmin {

    private static Log log = LogFactory.getLog(UserStoreManagerService.class.getClass());

    public void addUser(String userName, String credential, String[] roleList, ClaimValue[] claims,
            String profileName, boolean requirePasswordChange) throws UserStoreException {
        getUserStoreManager().addUser(userName, credential, roleList,
                convertClaimValueToMap(claims), profileName, requirePasswordChange);
    }

    public void setUserClaimValues(String userName, ClaimValue[] claims, String profileName)
            throws UserStoreException {
        getUserStoreManager().setUserClaimValues(userName, convertClaimValueToMap(claims),
                profileName);

    }

    public ClaimValue[] getUserClaimValuesForClaims(String userName, String[] claims,
            String profileName) throws UserStoreException {
        return convertMapToClaimValue(getUserStoreManager().getUserClaimValues(userName, claims,
                profileName));
    }

    public void addRole(String roleName, String[] userList, Permission[] permissions)
            throws UserStoreException {
        getUserStoreManager().addRole(roleName, userList, permissions);
    }

    public Claim[] getUserClaimValues(String userName, String profileName)
            throws UserStoreException {
        return getUserStoreManager().getUserClaimValues(userName, profileName);
    }

    public boolean authenticate(String userName, String credential) throws UserStoreException {
        return getUserStoreManager().authenticate(userName, credential);
    }

    public void updateCredential(String userName, String newCredential, String oldCredential)
            throws UserStoreException {
        getUserStoreManager().updateCredential(userName, newCredential, oldCredential);

    }

    public void updateCredentialByAdmin(String userName, String newCredential)
            throws UserStoreException {
        getUserStoreManager().updateCredentialByAdmin(userName, newCredential);
    }

    public long getPasswordExpirationTime(String username) throws UserStoreException {
        Date date = getUserStoreManager().getPasswordExpirationTime(username);
        if (date != null) {
            return date.getTime();
        }
        return -1;
    }

    public void deleteRole(String roleName) throws UserStoreException {
        getUserStoreManager().deleteRole(roleName);

    }

    public void deleteUser(String userName) throws UserStoreException {
        getUserStoreManager().deleteUser(userName);

    }

    public void deleteUserClaimValue(String userName, String claimURI, String profileName)
            throws UserStoreException {
        getUserStoreManager().deleteUserClaimValue(userName, claimURI, profileName);

    }

    public void deleteUserClaimValues(String userName, String[] claims, String profileName)
            throws UserStoreException {
        getUserStoreManager().deleteUserClaimValues(userName, claims, profileName);

    }

    public String[] getAllProfileNames() throws UserStoreException {
        return getUserStoreManager().getAllProfileNames();
    }

    public String[] getHybridRoles() throws UserStoreException {
        return getUserStoreManager().getHybridRoles();
    }

    public String[] getProfileNames(String userName) throws UserStoreException {
        return getUserStoreManager().getProfileNames(userName);
    }

    public String[] getRoleListOfUser(String userName) throws UserStoreException {
        return getUserStoreManager().getRoleListOfUser(userName);
    }

    public String[] getRoleNames() throws UserStoreException {
        return getUserStoreManager().getRoleNames();
    }

    public int getTenantId() throws UserStoreException {
        return getUserStoreManager().getTenantId();
    }


    public int getTenantIdofUser(String username) throws UserStoreException {

        if (Util.isSuperTenant()) {
            return getUserStoreManager().getTenantId(username);
        } else {
            StringBuilder stringBuilder
                    = new StringBuilder("Unauthorized attempt to execute super tenant operation by tenant domain - ");
            stringBuilder.append(CarbonContext.getCurrentContext().getTenantDomain()).append(" tenant id - ")
                    .append(CarbonContext.getCurrentContext().getTenantId()).append(" user - ")
                    .append(CarbonContext.getCurrentContext().getUsername());
            log.warn(stringBuilder.toString());

            throw new UserStoreException("Access Denied");
        }

    }

    public String getUserClaimValue(String userName, String claim, String profileName)
            throws UserStoreException {
        return getUserStoreManager().getUserClaimValue(userName, claim, profileName);
    }

    public int getUserId(String username) throws UserStoreException {
        return getUserStoreManager().getUserId(username);
    }

    public String[] getUserListOfRole(String roleName) throws UserStoreException {
        return getUserStoreManager().getUserListOfRole(roleName);
    }

    public boolean isExistingRole(String roleName) throws UserStoreException {
        return getUserStoreManager().isExistingRole(roleName);
    }

    public boolean isExistingUser(String userName) throws UserStoreException {
        return getUserStoreManager().isExistingUser(userName);
    }

    public boolean isReadOnly() throws UserStoreException {
        return getUserStoreManager().isReadOnly();
    }

    public String[] listUsers(String filter, int maxItemLimit) throws UserStoreException {
        return getUserStoreManager().listUsers(filter, maxItemLimit);
    }

    public void setUserClaimValue(String userName, String claimURI, String claimValue,
            String profileName) throws UserStoreException {
        getUserStoreManager().setUserClaimValue(userName, claimURI, claimValue, profileName);

    }

    public void updateRoleListOfUser(String userName, String[] deletedRoles, String[] newRoles)
            throws UserStoreException {
        getUserStoreManager().updateRoleListOfUser(userName, deletedRoles, newRoles);

    }

    public void updateRoleName(String roleName, String newRoleName) throws UserStoreException {
        getUserStoreManager().updateRoleName(roleName, newRoleName);
    }

    public void updateUserListOfRole(String roleName, String[] deletedUsers, String[] newUsers)
            throws UserStoreException {
        getUserStoreManager().updateUserListOfRole(roleName, deletedUsers, newUsers);
    }

    private UserStoreManager getUserStoreManager() throws UserStoreException {
        try {
            UserRealm realm = super.getUserRealm();
            if (realm == null) {
                throw new UserStoreException("UserRealm is null");
            }
            return realm.getUserStoreManager();
        } catch (Exception e) {
            throw new UserStoreException(e);
        }
    }

    private Map<String, String> convertClaimValueToMap(ClaimValue[] values) {
        Map<String, String> map = new HashMap<String, String>();
        for (ClaimValue claimValue : values) {
            map.put(claimValue.getClaimURI(), claimValue.getValue());
        }
        return map;
    }

    private ClaimValue[] convertMapToClaimValue(Map<String, String> map) {
        ClaimValue[] claims = new ClaimValue[map.size()];
        Iterator<Map.Entry<String, String>> ite = map.entrySet().iterator();
        int i = 0;
        while (ite.hasNext()) {
            Map.Entry<String, String> entry = ite.next();
            claims[i] = new ClaimValue();
            claims[i].setClaimURI(entry.getKey());
            claims[i].setValue(entry.getValue());
            i++;
        }
        return claims;
    }

    public String[][] getProperties(Tenant tenant) throws UserStoreException {
        // TODO This method should only called by super tenant
        // Logic is not implemented yet

        if (!Util.isSuperTenant()) {
            StringBuilder stringBuilder
                    = new StringBuilder("Unauthorized attempt to execute super tenant operation by tenant domain - ");
            stringBuilder.append(CarbonContext.getCurrentContext().getTenantDomain()).append(" tenant id - ")
                    .append(CarbonContext.getCurrentContext().getTenantId()).append(" user - ")
                    .append(CarbonContext.getCurrentContext().getUsername());
            log.warn(stringBuilder.toString());

            throw new UserStoreException("Access Denied");
        }

        // TODO implement the logic
        return null;
    }

}
