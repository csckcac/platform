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
package org.wso2.carbon.claim.mgt;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.core.util.AdminServicesUtil;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.claim.Claim;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.claim.ClaimMapping;

public class ClaimManagerHandler {

    private static Log log = LogFactory.getLog(ClaimManagerHandler.class);

    // Maintains a single instance of UserStore.
    private static ClaimManagerHandler claimManagerHandler;

    // To enable attempted thread-safety using double-check locking
    private static Object lock = new Object();

    // Making the class singleton
    private ClaimManagerHandler() throws Exception {
    }

    public static ClaimManagerHandler getInstance() throws Exception {

        // Enables attempted thread-safety using double-check locking
        if (claimManagerHandler == null) {
            synchronized (lock) {
                if (claimManagerHandler == null) {
                    claimManagerHandler = new ClaimManagerHandler();
                    if (log.isDebugEnabled()) {
                        log.debug("ClaimManagerHandler singleton instance created successfully");
                    }
                }
            }
        }
        return claimManagerHandler;
    }


    /**
     * Returns all supported claims.
     * 
     * @return
     * @throws Exception
     */
    public Claim[] getAllSupportedClaims() throws Exception {
        ClaimManager claimManager = null;

        try {
            UserRealm realm = getRealm();
            claimManager = realm.getClaimManager();
            if (claimManager != null) {
                // There can be cases - we get a request for an external user store - where we don'
                // have a claims administrator.
                return (Claim[]) realm.getClaimManager().getAllSupportClaimsByDefault();
            }
        } catch (UserStoreException e) {
            log.error("Error occurred while loading supported claims", e);
            getException("Error occurred while loading supported claima", e);
        }

        return new Claim[0];
    }

    /**
     * 
     * @return
     * @throws Exception
     */
    public ClaimMapping[] getAllSupportedClaimMappings() throws Exception {
        ClaimMapping[] claimMappings = new ClaimMapping[0];
        ClaimMapping claimMapping = null;
        Claim[] claims = null;
        ClaimManager claimManager = null;

        try {
            UserRealm realm = getRealm();
            claimManager = realm.getClaimManager();
            if (claimManager == null) {
                // There can be cases - we get a request for an external user store - where we don'
                // have a claims administrator.
                return new ClaimMapping[0];
            }

            claims = (Claim[]) claimManager.getAllSupportClaimsByDefault();
            if (claims != null) {
                claimMappings = new ClaimMapping[claims.length];
                for (int i = 0; i < claims.length; i++) {
                    claimMapping = new ClaimMapping(null, null);
                    claimMapping.setClaim(claims[i]);
                    claimMapping.setMappedAttribute(realm.getClaimManager().getAttributeName(
                            claims[i].getClaimUri()));
                    claimMappings[i] = claimMapping;
                }
            }

        } catch (UserStoreException e) {
            log.error("Error occurred while loading supported claims", e);
            getException("Error occurred while loading supported claima", e);
        }

        return claimMappings;
    }

    /**
     * 
     * @return
     * @throws Exception
     */
    public ClaimMapping[] getAllClaimMappings() throws Exception {
        ClaimMapping[] claimMappings = new ClaimMapping[0];
        ClaimMapping claimMapping = null;
        Claim[] claims = null;
        ClaimManager claimManager = null;

        try {
            UserRealm realm = getRealm();
            claimManager = realm.getClaimManager();
            if (claimManager == null) {
                // There can be cases - we get a request for an external user store - where we don'
                // have a claims administrator.
                return new ClaimMapping[0];
            }

            claims = (Claim[]) claimManager.getAllClaims();
            if (claims != null) {
                claimMappings = new ClaimMapping[claims.length];
                for (int i = 0; i < claims.length; i++) {
                    claimMapping = new ClaimMapping(null, null);
                    claimMapping.setClaim(claims[i]);
                    claimMapping.setMappedAttribute(claimManager.getAttributeName(claims[i]
                            .getClaimUri()));
                    claimMappings[i] = claimMapping;
                }
            }

        } catch (UserStoreException e) {
            log.error("Error occurred while loading supported claims", e);
            getException("Error occurred while loading supported claima", e);
        }

        return claimMappings;
    }

    public ClaimMapping getClaimMapping(String claimURI) throws Exception {
        ClaimMapping claimMapping = null; 
        ClaimManager claimManager = null;
        try {
            UserRealm realm = getRealm();
            claimManager = realm.getClaimManager();
            if (claimManager != null) {
                claimMapping = (ClaimMapping) claimManager.getClaimMapping(claimURI);
                
            }
        } catch (UserStoreException e) {
            log.error("Error occurred while loading supported claims", e);
            getException("Error occurred while retrieving claim", e);
        }
        return claimMapping;
    }
    /**
     * 
     * @return
     * @throws Exception
     */
    public ClaimMapping[] getAllSupportedClaimMappings(String dialectUri)
            throws Exception {
        ClaimMapping[] claimMappings = new ClaimMapping[0];
        ClaimMapping claimMapping = null;
        Claim[] claims = null;
        ClaimManager claimManager = null;

        try {
            UserRealm realm = getRealm();
            claimManager = realm.getClaimManager();
            if (claimManager == null) {
                // There can be cases - we get a request for an external user store - where we don'
                // have a claims administrator.
                return new ClaimMapping[0];
            }
            claims = getAllSupportedClaims(dialectUri);
            if (claims != null && claims.length > 0) {
                claimMappings = new ClaimMapping[claims.length];
                for (int i = 0; i < claims.length; i++) {
                    claimMapping = new ClaimMapping(null, null);
                    claimMapping.setClaim(claims[i]);
                    claimMapping.setMappedAttribute(claimManager.getAttributeName(claims[i]
                            .getClaimUri()));
                    claimMappings[i] = claimMapping;
                }
            }

        } catch (UserStoreException e) {
            log.error("Error occurred while loading supported claims", e);
            getException("Error occurred while loading supported claima", e);
        }

        return claimMappings;
    }

    /**
     * Returns all supported claims for the given dialect.
     * 
     * @return
     * @throws Exception
     */
    public Claim[] getAllSupportedClaims(String dialectUri) throws Exception {
        Claim[] claims = new Claim[0];
        ArrayList<Claim> reqClaims = null;
        ClaimManager claimManager = null;

        try {
            UserRealm realm = getRealm();
            claimManager = realm.getClaimManager();
            if (claimManager == null) {
                // There can be cases - we get a request for an external user store - where we don'
                // have a claims administrator.
                return claims;
            }
            claims = (Claim[]) claimManager.getAllSupportClaimsByDefault();
            reqClaims = new ArrayList<Claim>();
            for (int i = 0; i < claims.length; i++) {
                if (dialectUri.equals(claims[i].getDialectURI())) {
                    reqClaims.add(claims[i]);
                }
            }

            return reqClaims.toArray(new Claim[reqClaims.size()]);
        } catch (UserStoreException e) {
            log.error("Error occurred while loading supported claims from the dialect "
                    + dialectUri, e);
            getException("Error occurred while loading supported claims from the dialect "
                    + dialectUri, e);
        }

        return claims;
    }

    /**
     * @param mapping
     * @throws Exception
     */
    public void upateClaimMapping(ClaimMapping mapping) throws Exception {
        ClaimManager claimManager = null;
        try {
            UserRealm realm = getRealm();
            claimManager = realm.getClaimManager();
            if (claimManager != null) {
                // There can be cases - we get a request for an external user store - where we don'
                // have a claims administrator.
                claimManager.updateClaimMapping(mapping);
            }
        } catch (UserStoreException e) {
            log.error("Error occurred while updating claim mapping", e);
            getException("Error occurred while updating claim mapping", e);
        }
    }

    /**
     * 
     * @param mapping
     * @throws Exception
     */
    public void addNewClaimMapping(ClaimMapping mapping) throws Exception {
        ClaimManager claimManager = null;
        try {
            UserRealm realm = getRealm();
            claimManager = realm.getClaimManager();
            if (claimManager != null) {
                // There can be cases - we get a request for an external user store - where we don'
                // have a claims administrator.
                claimManager.addNewClaimMapping(mapping);
            }
        } catch (UserStoreException e) {
            log.error("Error occurred while adding new claim mapping", e);
            getException("Error occurred while adding new claim mapping", e);
        }
    }

    /**
     * 
     * @param dialectUri
     * @param claimUri
     * @throws Exception
     */
    public void removeClaimMapping(String dialectUri, String claimUri)
            throws Exception {
        ClaimMapping mapping = null;
        Claim claim = null;
        ClaimManager claimManager = null;
        try {
            UserRealm realm = getRealm();
            claimManager = realm.getClaimManager();
            if (claimManager != null) {
                // There can be cases - we get a request for an external user store - where we don'
                // have a claims administrator.
                claim = new Claim();
                claim.setClaimUri(claimUri);
                claim.setDialectURI(dialectUri);
                mapping = new ClaimMapping(claim, null);
                claimManager.deleteClaimMapping(mapping);
            }
        } catch (UserStoreException e) {
            log.error("Error occurred while removing new claim mapping", e);
            getException("Error occurred while removing new claim mapping", e);
        }
    }

    /**
     * 
     * @param mappings
     */
    public void addNewClaimDialect(ClaimDialect mappings) throws Exception {
        ClaimMapping[] mapping = null;
        ClaimManager claimManager = null;
        try {
            UserRealm realm = getRealm();
            claimManager = realm.getClaimManager();
            if (claimManager != null) {
                mapping = mappings.getClaimMapping();
                for (int i = 0; i < mapping.length; i++) {
                    claimManager.addNewClaimMapping(mapping[i]);
                }
            }
        } catch (UserStoreException e) {
            log.error("Error occurred while removing new claim mapping", e);
            getException("Error occurred while removing new claim mapping", e);
        }
    }

    /**
     * 
     * @param dialectUri
     * @throws Exception
     */
    public void removeClaimDialect(String dialectUri) throws Exception {
        Claim[] claims = null;
        ClaimMapping mapping = null;
        ClaimManager claimManager = null;
        try {
            UserRealm realm = getRealm();
            claimManager = realm.getClaimManager();
            if (claimManager != null) {
                claims = (Claim[]) claimManager.getAllClaims(dialectUri);
                for (int i = 0; i < claims.length; i++) {
                    mapping = new ClaimMapping(claims[i], null);
                    claimManager.deleteClaimMapping(mapping);
                }
            }
        } catch (UserStoreException e) {
            log.error("Error occurred while removing new claim dialect", e);
            getException("Error occurred while removing new claim dialect", e);
        }
    }

    private UserRealm getRealm() throws Exception{
        try {
            return AdminServicesUtil.getUserRealm();
        } catch (CarbonException e) {
            // already logged
            throw new Exception(e.getMessage(), e);
        }
    }
    
    /**
     * Creates an IdentityException instance wrapping the given error message and
     * 
     * @param message Error message
     * @param e Exception
     * @throws Exception
     */
    private void getException(String message, Exception e) throws Exception {
        log.error(message, e);
        throw new Exception(message, e);
    }

}
