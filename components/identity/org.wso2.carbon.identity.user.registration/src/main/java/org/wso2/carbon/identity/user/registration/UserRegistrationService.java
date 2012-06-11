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
package org.wso2.carbon.identity.user.registration;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.carbon.identity.base.IdentityConstants;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.IdentityClaimManager;
import org.wso2.carbon.identity.core.persistence.IdentityPersistenceManager;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.relyingparty.RelyingPartyData;
import org.wso2.carbon.identity.relyingparty.RelyingPartyException;
import org.wso2.carbon.identity.relyingparty.TokenVerifierConstants;
import org.wso2.carbon.identity.relyingparty.saml.SAMLTokenVerifier;
import org.wso2.carbon.identity.user.registration.dto.InfoCarDTO;
import org.wso2.carbon.identity.user.registration.dto.OpenIDDTO;
import org.wso2.carbon.identity.user.registration.dto.UserDTO;
import org.wso2.carbon.identity.user.registration.dto.UserFieldDTO;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.user.core.*;
import org.wso2.carbon.user.core.claim.Claim;
import org.wso2.carbon.user.mgt.UserMgtConstants;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class UserRegistrationService {

    public UserFieldDTO[] readUserFieldsForUserRegistration(String dialect)
            throws IdentityException {
        IdentityClaimManager claimManager = null;
        Claim[] claims = null;
        List<UserFieldDTO> claimList = null;
        UserRealm realm = null;

        claimManager = IdentityClaimManager.getInstance();
        realm = IdentityTenantUtil.getRealm(null, null);
        claims = claimManager.getAllSupportedClaims(dialect, realm);

        if (claims == null || claims.length == 0) {
            return new UserFieldDTO[0];
        }

        claimList = new ArrayList<UserFieldDTO>();

        for (Claim claim : claims) {
            if (claim.getDisplayTag() != null
                    && !IdentityConstants.PPID_DISPLAY_VALUE.equals(claim.getDisplayTag())) {
                if(UserCoreConstants.ClaimTypeURIs.ACCOUNT_STATUS.equals(claim.getClaimUri())){
                    continue;
                }
                claimList.add(getUserFieldDTO(claim.getClaimUri(), claim.getDisplayTag(),
                        claim.isRequired(), claim.getDisplayOrder(), claim.getRegEx()));
            }
        }

        return claimList.toArray(new UserFieldDTO[claimList.size()]);
    }

    public void addUser(UserDTO user) throws Exception {
        UserFieldDTO[] userFieldDTOs = null;
        Map<String, String> userClaims = null;

        userFieldDTOs = user.getUserFields();
        userClaims = new HashMap<String, String>();

        if (userFieldDTOs != null) {
            for (UserFieldDTO userFieldDTO : userFieldDTOs) {
                userClaims.put(userFieldDTO.getClaimUri(), userFieldDTO.getFieldValue());
            }
        }

        UserRealm realm = null;
        realm = IdentityTenantUtil.getRealm(null, null);
        Registry registry = IdentityTenantUtil.getRegistry(null, null);
        addUser(user.getUserName(), user.getPassword(), userClaims, null, realm);

        // OpenId Sign-Up if necessary.
        if (user.getOpenID() != null) {
            IdentityPersistenceManager persistentManager = IdentityPersistenceManager
                    .getPersistanceManager();
            persistentManager.doOpenIdSignUp(registry, realm, user.getOpenID(), user.getUserName());
        }

    }

    public boolean isAddUserEnabled() throws Exception {

        UserRealm userRealm = IdentityTenantUtil.getRealm(null, null);
        if (userRealm != null) {
            UserStoreManager userStoreManager = userRealm.getUserStoreManager();
            if (userStoreManager != null) {
                return !userStoreManager.isReadOnly();
            }
        }

        return false;

    }

    public boolean isAddUserWithOpenIDEnabled() throws Exception {
        return false;
    }

    public boolean isAddUserWithInfoCardEnabled() throws Exception {
        return false;
    }

    private UserFieldDTO getUserFieldDTO(String claimUri, String displayName, boolean isRequired,
            int displayOrder, String regex) {
        UserFieldDTO fieldDTO = null;
        fieldDTO = new UserFieldDTO();
        fieldDTO.setClaimUri(claimUri);
        fieldDTO.setFieldName(displayName);
        fieldDTO.setRequired(isRequired);
        fieldDTO.setDisplayOrder(displayOrder);
        fieldDTO.setRegEx(regex);
        return fieldDTO;
    }

    private void addUser(String userName, String password, Map<String, String> claimList,
            String profileName, UserRealm realm) throws IdentityException {
        UserStoreManager admin = null;
        Permission permission = null;
        try {
            admin = realm.getUserStoreManager();
            // add user
            admin.addUser(userName, password, null, claimList, profileName);
            // if this is the first time a user signs up, needs to create role
            if (!admin.isExistingRole(IdentityConstants.IDENTITY_DEFAULT_ROLE)) {
                permission = new Permission("/permission/admin/login",
                        UserMgtConstants.EXECUTE_ACTION);
                admin.addRole(IdentityConstants.IDENTITY_DEFAULT_ROLE, new String[] { userName },
                        new Permission[] { permission });
            } else {
                // if role already exists, just add user to role
                admin.updateUserListOfRole(IdentityConstants.IDENTITY_DEFAULT_ROLE,
                        new String[] {}, new String[] { userName });
            }
        } catch (UserStoreException e) {
            throw new IdentityException("Error occurred while adding user : " + userName, e);
        }
    }

}
