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

import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.context.MessageContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.um.ws.service.internal.UMRemoteServicesDSComponent;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.claim.Claim;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.claim.ClaimMapping;
import org.wso2.carbon.user.core.service.RealmService;

public class ClaimManagerService extends AbstractAdmin {

    public void addNewClaimMapping(ClaimMapping mapping) throws UserStoreException {
        try {
            getClaimManager().addNewClaimMapping(mapping);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException(e);
        }
    }

    public void deleteClaimMapping(ClaimMapping mapping) throws UserStoreException {
        try {
            getClaimManager().deleteClaimMapping(mapping);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException(e);
        }
    }

    public Claim[] getAllClaims(String dialectUri) throws UserStoreException {
        try {
            if (dialectUri == null) {
                return (Claim[]) getClaimManager().getAllClaims();
            } else {
                return (Claim[]) getClaimManager().getAllClaims(dialectUri);
            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException(e);
        }
    }

    public String[] getAllClaimUris() throws UserStoreException {
        try {
            return getClaimManager().getAllClaimUris();
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException(e);
        }
    }

    public Claim[] getAllRequiredClaims() throws UserStoreException {
        try {
            return (Claim[]) getClaimManager().getAllRequiredClaims();
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException(e);
        }
    }

    public Claim[] getAllSupportClaimsByDefault() throws UserStoreException {
        try {
            return (Claim[]) getClaimManager().getAllSupportClaimsByDefault();
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException(e);
        }
    }

    public String getAttributeName(String claimURI) throws UserStoreException {
        try {
            return getClaimManager().getAttributeName(claimURI);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException(e);
        }
    }

    public Claim getClaim(String claimURI) throws UserStoreException {
        try {
            return (Claim) getClaimManager().getClaim(claimURI);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException(e);
        }
    }

    public ClaimMapping getClaimMapping(String claimURI) throws UserStoreException {
        try {
            return (ClaimMapping) getClaimManager().getClaimMapping(claimURI);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException(e);
        }
    }

    public void updateClaimMapping(ClaimMapping mapping) throws UserStoreException {
        try {
            getClaimManager().updateClaimMapping(mapping);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException(e);
        }
    }

    private ClaimManager getClaimManager() throws UserStoreException {
        try {
            UserRealm realm = super.getUserRealm();
            if (realm == null) {
                throw new UserStoreException("UserRealm is null");
            }
            return realm.getClaimManager();
        } catch (Exception e) {
            throw new UserStoreException(e);
        }
    }
}
