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
package org.wso2.carbon.profiles.mgt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.core.util.AdminServicesUtil;
import org.wso2.carbon.profiles.mgt.dto.AvailableProfileConfigurationDTO;
import org.wso2.carbon.profiles.mgt.dto.ClaimConfigurationDTO;
import org.wso2.carbon.profiles.mgt.dto.DetailedProfileConfigurationDTO;
import org.wso2.carbon.profiles.mgt.dto.DialectDTO;
import org.wso2.carbon.profiles.mgt.dto.ProfileConfigurationDTO;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.claim.Claim;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.config.RealmConfiguration;
import org.wso2.carbon.user.core.ldap.LDAPConstants;
import org.wso2.carbon.user.core.profile.ProfileConfiguration;
import org.wso2.carbon.user.core.profile.ProfileConfigurationManager;

public class ProfileMgtAdmin {

    private static final Log log = LogFactory.getLog(ProfileMgtAdmin.class);
    
    /**
     * 
     * @param userStore
     * @return
     * @throws ProfileManagementException
     */
    public AvailableProfileConfigurationDTO getAllAvailableProfileConfiguraionsForUserStore(
            String userStore) throws ProfileManagementException {
        AvailableProfileConfigurationDTO availableConfiguration = null;

        validateInputParameters(new String[]{userStore});

        try {
            availableConfiguration = new AvailableProfileConfigurationDTO();
            availableConfiguration.setDialects(getDialectsForUserStore());
            return availableConfiguration;
        } catch (Exception e) {
            log.error("Error while loading available profile configurations for user store "
                    + userStore, e);
            throw new ProfileManagementException(
                    "Error while loading available profile configurations for user store "
                            + userStore, e);
        }
    }


    /**
     * 
     * @return
     * @throws ProfileManagementException
     */
    public AvailableProfileConfigurationDTO getAllAvailableProfileConfiguraions()
            throws ProfileManagementException {
        AvailableProfileConfigurationDTO availableConfiguration = null;

        try {
            availableConfiguration = new AvailableProfileConfigurationDTO();
            availableConfiguration.setDialects(getDialects());
            return availableConfiguration;
        } catch (Exception e) {
            log.error("Error while loading available profile configurations", e);
            throw new ProfileManagementException(
                    "Error while loading available profile configurations", e);
        }
    }

    /**
     *
     * @param dialect
     * @return
     * @throws ProfileManagementException
     */
    public DetailedProfileConfigurationDTO getAllAvailableProfileConfiguraionsForDialect(
            String dialect) throws ProfileManagementException {
        UserRealm realm = null;
        ProfileConfigurationManager profileManager = null;
        // This map contains profile configurations against their corresponding
        // configuration name.
        ProfileConfiguration[] profileConfigs = null;
        // To iterate over profileConfigurations
        Iterator<Entry<String, ProfileConfiguration>> iterator = null;
        // This is the profile configuration corresponding to the given dialect
        // to act on the given
        // user store.
        DetailedProfileConfigurationDTO detailedConfiguration = null;
        List<ProfileConfigurationDTO> configurations = null;

        validateInputParameters(new String[] { dialect });

        try {

            // Get available profile configurations for internal user store.
            realm = getRealm();
            profileManager = realm.getProfileConfigurationManager();

            if (profileManager == null) {
                String message = "No profile configurations defined ";
                if (log.isDebugEnabled()) {
                    log.debug(message);
                }
                return null;
            }

            // Get all profile configurations defined for the given user store.
            // profileConfigurations = profileManager.getProfileConfigs();
            detailedConfiguration = new DetailedProfileConfigurationDTO();
            profileConfigs = (ProfileConfiguration[]) profileManager.getAllProfiles();
            // We are interested on the given dialect only.
            detailedConfiguration.setDialect(dialect);
            configurations = new ArrayList<ProfileConfigurationDTO>();
            for (ProfileConfiguration profileConfig : profileConfigs) {
                if (profileConfig.getDialectName().equals(dialect)) {
                    // This is a profile configuration defined for the given
                    // dialect.
                    ProfileConfigurationDTO profile = null;
                    ClaimConfigurationDTO claim = null;
                    List<ClaimConfigurationDTO> claimList = null;
                    List<String> hiddenClaims = null;
                    List<String> overriddenClaims = null;
                    List<String> inheritedClaims = null;
                    // A container for all the claims applicable to the current
                    // profile
                    // configuration.
                    claimList = new ArrayList<ClaimConfigurationDTO>();
                    hiddenClaims = profileConfig.getHiddenClaims();

                    if (hiddenClaims != null) {
                        for (Iterator<String> iter = hiddenClaims.iterator(); iter.hasNext();) {
                            String string = iter.next();
                            claim = new ClaimConfigurationDTO();
                            claim.setClaimUri(string);
                            claim.setBehavior(UserCoreConstants.CLAIM_HIDDEN);
                            claimList.add(claim);
                        }
                    }

                    overriddenClaims = profileConfig.getOverriddenClaims();

                    if (overriddenClaims != null) {
                        for (Iterator<String> iter = overriddenClaims.iterator(); iter.hasNext();) {
                            String string = iter.next();
                            claim = new ClaimConfigurationDTO();
                            claim.setClaimUri(string);
                            claim.setBehavior(UserCoreConstants.CLAIM_OVERRIDEN);
                            claimList.add(claim);
                        }
                    }

                    inheritedClaims = profileConfig.getInheritedClaims();

                    if (inheritedClaims != null) {
                        for (Iterator<String> iter = inheritedClaims.iterator(); iter.hasNext();) {
                            String string = iter.next();
                            claim = new ClaimConfigurationDTO();
                            claim.setClaimUri(string);
                            claim.setBehavior(UserCoreConstants.CLAIM_INHERITED);
                            claimList.add(claim);
                        }
                    }

                    // A given profile configuration has a claim set and a name.
                    profile = new ProfileConfigurationDTO();
                    profile.setConfigurationName(profileConfig.getProfileName());
                    profile.setClaimConfiguration(claimList
                            .toArray(new ClaimConfigurationDTO[claimList.size()]));
                    // Here comes one more profile configuration defined under
                    // the given
                    // dialect.
                    configurations.add(profile);
                }

                // By now we should have all the profile configurations defined
                // under the given
                // dialect.
            }

            detailedConfiguration.setProfileConfiguartions(configurations
                    .toArray(new ProfileConfigurationDTO[configurations.size()]));
            return detailedConfiguration;
        } catch (Exception e) {
            String message = "Error while loading available profile configurations for dialect "
                    + dialect;
            log.error(message, e);
            throw new ProfileManagementException(message, e);
        }
    }

    /**
     * 
     * @param dialect
     * @param profileConfig
     * @return
     */
    public ClaimConfigurationDTO[] getProfileConfiguration(String dialect, String profileConfig)
            throws ProfileManagementException {
        UserRealm realm = null;
        ProfileConfigurationManager profileManager = null;
        ClaimManager claimManager = null;
        ProfileConfiguration profileConfiguration = null;
        ClaimConfigurationDTO claim = null;
        List<ClaimConfigurationDTO> claimList = null;
        List<String> hiddenClaims = null;
        List<String> overriddenClaims = null;
        List<String> inheritedClaims = null;
        Claim[] definedClaims = null;
        List<String> definedClaimsList = null;

        validateInputParameters(new String[] { dialect, profileConfig });

        try {
            realm = getRealm();
            profileManager = realm.getProfileConfigurationManager();

            if (profileManager == null) {
                String message = "No profile configurations defined";
                if (log.isDebugEnabled()) {
                    log.debug(message);
                }
                return null;
            }

            claimManager = realm.getClaimManager();

            if (claimManager == null) {
                String message = "No claim configurations defined";
                if (log.isDebugEnabled()) {
                    log.debug(message);
                }
                throw new ProfileManagementException(message);
            }

            definedClaimsList = new ArrayList<String>();
            definedClaims = (Claim[]) claimManager.getAllClaims(dialect);

            for (int i = 0; i < definedClaims.length; i++) {
                definedClaimsList.add(definedClaims[i].getClaimUri());
            }

            profileConfiguration = (ProfileConfiguration) profileManager.getProfileConfig(profileConfig);

            // A container for all the claims applicable to the current profile
            // configuration.
            claimList = new ArrayList<ClaimConfigurationDTO>();
            hiddenClaims = profileConfiguration.getHiddenClaims();

            if (hiddenClaims != null) {
                for (Iterator<String> iter = hiddenClaims.iterator(); iter.hasNext();) {
                    String string = iter.next();
                    claim = new ClaimConfigurationDTO();
                    claim.setClaimUri(string);
                    claim.setBehavior(UserCoreConstants.CLAIM_HIDDEN);
                    if (definedClaimsList.contains(string)) {
                        // If this claim being removed, then we are not going
                        // add this.
                        claimList.add(claim);
                        definedClaimsList.remove(string);
                    }
                }
            }

            overriddenClaims = profileConfiguration.getOverriddenClaims();

            if (overriddenClaims != null) {
                for (Iterator<String> iter = overriddenClaims.iterator(); iter.hasNext();) {
                    String string = iter.next();
                    claim = new ClaimConfigurationDTO();
                    claim.setClaimUri(string);
                    claim.setBehavior(UserCoreConstants.CLAIM_OVERRIDEN);
                    if (definedClaimsList.contains(string)) {
                        // If this claim being removed, then we are not going
                        // add this.
                        claimList.add(claim);
                        definedClaimsList.remove(string);
                    }
                }
            }

            inheritedClaims = profileConfiguration.getInheritedClaims();

            if (inheritedClaims != null) {
                for (Iterator<String> iter = inheritedClaims.iterator(); iter.hasNext();) {
                    String string = iter.next();
                    claim = new ClaimConfigurationDTO();
                    claim.setClaimUri(string);
                    claim.setBehavior(UserCoreConstants.CLAIM_INHERITED);
                    if (definedClaimsList.contains(string)) {
                        // If this claim being removed, then we are not going
                        // add this.
                        claimList.add(claim);
                        definedClaimsList.remove(string);
                    }
                }
            }

            // Okay - we have now claims from the profile configuration.
            // If, we have new claims defined, then we need to add those.
            for (String definedClaim : definedClaimsList) {
                ClaimConfigurationDTO dto = null;
                dto = new ClaimConfigurationDTO();
                dto.setClaimUri(definedClaim);
                dto.setBehavior(UserCoreConstants.CLAIM_INHERITED);
                claimList.add(dto);
            }

        } catch (Exception e) {
            String message = "Error while loading available profile configurations for dialect "
                    + dialect + "Profile configuration " + profileConfig;
            log.error(message, e);
            throw new ProfileManagementException(message, e);
        }

        return claimList.toArray(new ClaimConfigurationDTO[claimList.size()]);
    }

    /**
     * 
     * @return
     * @throws ProfileManagementException
     */
    public DialectDTO[] getDialects() throws ProfileManagementException {
        UserRealm realm = null;
        ProfileConfigurationManager profileManager = null;
        // This map contains profile configurations against their corresponding
        // configuration name.
        ProfileConfiguration[] profileConfigs = null;
        // To iterate over profileConfigurations
        Iterator<Entry<String, ProfileConfiguration>> iterator = null;
        Map<String, DialectDTO> basicConfigurations = null;
        String[] dialects = null;

        try {

            // Get available profile configurations for internal user store.
            realm = getRealm();
            profileManager = realm.getProfileConfigurationManager();

            if (profileManager == null) {
                String message = "No profile configurations defined ";
                if (log.isDebugEnabled()) {
                    log.debug(message);
                }
                return null;
            }

            // Get all profile configurations defined for the given user store.
            profileConfigs = (ProfileConfiguration[]) profileManager.getAllProfiles();
            basicConfigurations = new HashMap<String, DialectDTO>();

            // Okay - we have profile configurations.
            for (ProfileConfiguration profileConfig : profileConfigs) {
                // Okay - we have the first profile configuration in.
                List<String> names = null;
                DialectDTO dialectdto = null;
                String dialect = null;
                dialect = profileConfig.getDialectName();

                if (basicConfigurations.containsKey(dialect)) {
                    dialectdto = basicConfigurations.get(dialect);
                } else {
                    dialectdto = new DialectDTO();
                    dialectdto.setDialectUri(dialect);
                    dialectdto.setProfileConfigurations(new String[0]);
                    basicConfigurations.put(dialect, dialectdto);
                }

                names = new ArrayList<String>(Arrays.asList(dialectdto.getProfileConfigurations()));
                names.add(profileConfig.getProfileName());
                dialectdto.setProfileConfigurations(names.toArray(new String[names.size()]));
            }

            dialects = getAllClaimDailects();

            for (int i = 0; i < dialects.length; i++) {
                if (!basicConfigurations.containsKey(dialects[i])) {
                    DialectDTO dialectdto = null;
                    dialectdto = new DialectDTO();
                    dialectdto.setDialectUri(dialects[i]);
                    dialectdto.setProfileConfigurations(new String[0]);
                    if (UserCoreConstants.DEFAULT_CARBON_DIALECT.equals(dialects[i])) {
                        basicConfigurations.put(dialects[i], dialectdto);
                    }
                }
            }

            return basicConfigurations.values().toArray(
                    new DialectDTO[basicConfigurations.values().size()]);
        } catch (Exception e) {
            String message = "Error while loading available dialects ";
            log.error(message, e);
            throw new ProfileManagementException(message, e);
        }
    }

    public void deleteProfileConfiguraiton(String dialect, String profileName)
            throws ProfileManagementException {
        UserRealm realm = getRealm();
        try {
            if (UserCoreConstants.DEFAULT_PROFILE.equals(profileName)) {
                throw new ProfileManagementException("Cannot delete default profile");
            }
            ProfileConfigurationManager admin = realm.getProfileConfigurationManager();
            ProfileConfiguration profileConfig = new ProfileConfiguration(profileName, dialect,
                    new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>());
            admin.deleteProfileConfig(profileConfig);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new ProfileManagementException("Could not delete profil configuration.", e);
        }
    }

    /**
     * 
     * @return
     * @throws Exception
     */
    private String[] getAllClaimDailects() throws Exception {
        ClaimManager claimsManager;
        UserRealm realm;
        Claim[] claims;
        List<String> dialects;

        realm = getRealm();
        claimsManager = realm.getClaimManager();

        if (claimsManager == null) {
            String message = "No claim configurations defined";
            if (log.isDebugEnabled()) {
                log.debug(message);
            }
            return null;
        }

        claims = (Claim[]) claimsManager.getAllClaims();
        dialects = new ArrayList<String>();

        for (int i = 0; i < claims.length; i++) {
            if (!dialects.contains(claims[i].getDialectURI())) {
                dialects.add(claims[i].getDialectURI());
            }
        }

        return dialects.toArray(new String[dialects.size()]);
    }

    /**
     * 
     * @param profileName
     * @param claimsConfiguration
     * @throws ProfileManagementException
     */
    public void addProfile(String profileName, String dialectName,
            ClaimConfigurationDTO[] claimsConfiguration) throws ProfileManagementException {

        validateInputParameters(new String[] { profileName, dialectName });

        try {
            UserRealm realm = null;
            ProfileConfigurationManager profileManager = null;
            realm = getRealm();
            profileManager = realm.getProfileConfigurationManager();

            if (profileManager == null) {
                String message = "No profile configurations defined";
                if (log.isDebugEnabled()) {
                    log.debug(message);
                }
                throw new ProfileManagementException(message);
            }

            ProfileConfiguration profConfig =
                    (ProfileConfiguration) profileManager.getProfileConfig(profileName);
            if (profConfig != null) {
                String message = "Profile configuration already exist for the user store. " +
                                 "Please select a different name.";
                if (log.isDebugEnabled()) {
                    log.debug(message);
                }
                throw new ProfileManagementException(message);
            }
            List<String> hidden = new ArrayList<String>();
            List<String> overridden = new ArrayList<String>();
            List<String> inherited = new ArrayList<String>();

            for (ClaimConfigurationDTO claim : claimsConfiguration) {
                String claimURI = claim.getClaimUri();
                String behavior = claim.getBehavior();
                if (UserCoreConstants.CLAIM_HIDDEN.equals(behavior)) {
                    hidden.add(claimURI);
                } else if (UserCoreConstants.CLAIM_OVERRIDEN.equals(behavior)) {
                    overridden.add(claimURI);
                } else {
                    inherited.add(claimURI);
                }
            }

            ProfileConfiguration config = new ProfileConfiguration(profileName, hidden, overridden,
                    inherited);
            config.setDialectName(dialectName);
            profileManager.addProfileConfig(config);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            String message = "Error while adding profile configurations for dialect " + dialectName;
            log.error(message, e);
            throw new ProfileManagementException(message, e);
        }
    }

    public DialectDTO[] getDialectsForUserStore() throws ProfileManagementException {
        UserRealm realm = null;
        ProfileConfigurationManager profileAdmin = null;
        // This map contains profile configurations against their corresponding
        // configuration name.
        ProfileConfiguration[] profileConfigs = null;
        // To iterate over profileConfigurations
        Iterator<Entry<String, ProfileConfiguration>> iterator = null;
        Map<String, DialectDTO> basicConfigurations = null;
        String[] dialects = null;

        try {

            // Get available profile configurations for internal user store.
            realm = getRealm();
            profileAdmin = realm.getProfileConfigurationManager();

            if (profileAdmin == null) {
                String message = "No profile configurations defined";
                if (log.isDebugEnabled()) {
                    log.debug(message);
                }
                return null;
            }

            // Get all profile configurations defined for the given user store.
            profileConfigs = (ProfileConfiguration[]) profileAdmin.getAllProfiles();
            basicConfigurations = new HashMap<String, DialectDTO>();

            // Okay - we have profile configurations.
            for (ProfileConfiguration profileConfig : profileConfigs) {
                // Okay - we have the first profile configuration in.
                List<String> names = null;
                DialectDTO dialectdto = null;
                String dialect = null;
                dialect = profileConfig.getDialectName();

                if (basicConfigurations.containsKey(dialect)) {
                    dialectdto = basicConfigurations.get(dialect);
                } else {
                    dialectdto = new DialectDTO();
                    dialectdto.setDialectUri(dialect);
                    dialectdto.setProfileConfigurations(new String[0]);
                    basicConfigurations.put(dialect, dialectdto);
                }

                names = new ArrayList<String>(Arrays.asList(dialectdto.getProfileConfigurations()));
                names.add(profileConfig.getProfileName());
                dialectdto.setProfileConfigurations(names.toArray(new String[names.size()]));
            }

            dialects = getAllClaimDailects();

            for (int i = 0; i < dialects.length; i++) {
                if (!basicConfigurations.containsKey(dialects[i])) {
                    DialectDTO dialectdto = null;
                    dialectdto = new DialectDTO();
                    dialectdto.setDialectUri(dialects[i]);
                    dialectdto.setProfileConfigurations(new String[0]);
                    if (UserCoreConstants.DEFAULT_CARBON_DIALECT.equals(dialects[i])) {
                        basicConfigurations.put(dialects[i], dialectdto);
                    }
                }
            }

            return basicConfigurations.values().toArray(
                    new DialectDTO[basicConfigurations.values().size()]);
        } catch (Exception e) {
            String message = "Error while loading available dialects";
            log.error(message, e);
            throw new ProfileManagementException(message, e);
        }
    }

    /**
     * 
     * @param profileName
     * @param claimsConfiguration
     * @throws ProfileManagementException
     */
    public void updateClaimMappingBehavior(String profileName,
            ClaimConfigurationDTO[] claimsConfiguration) throws ProfileManagementException {
        UserRealm realm = null;
        ProfileConfigurationManager profileManager = null;

        validateInputParameters(new String[] { profileName });

        try {
            realm = getRealm();
            profileManager = realm.getProfileConfigurationManager();

            if (profileManager == null) {
                String message = "No profile configurations defined";
                if (log.isDebugEnabled()) {
                    log.debug(message);
                }
                throw new ProfileManagementException(message);
            }

            ProfileConfiguration config =
                    (ProfileConfiguration) profileManager.getProfileConfig(profileName);
            List<String> hidden = new ArrayList<String>();
            List<String> overridden = new ArrayList<String>();
            List<String> inherited = new ArrayList<String>();

            for (ClaimConfigurationDTO claim : claimsConfiguration) {
                String claimURI = claim.getClaimUri();
                String behavior = claim.getBehavior();
                if (UserCoreConstants.CLAIM_HIDDEN.equals(behavior)) {
                    hidden.add(claimURI);
                } else if (UserCoreConstants.CLAIM_OVERRIDEN.equals(behavior)) {
                    overridden.add(claimURI);
                } else {// inherited
                    inherited.add(claimURI);
                }
            }
            config.setHiddenClaims(hidden);
            config.setInheritedClaims(inherited);
            config.setOverriddenClaims(overridden);
            profileManager.updateProfileConfig(config);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            String message = "Error occurred while upating profile configurations" + profileName;
            log.error(message, e);
            throw new ProfileManagementException(message, e);
        }
    }

    /**
     * 
     * @param dialect
     * @return
     * @throws ProfileManagementException
     */
    public ClaimConfigurationDTO[] getClaimConfigurations(String dialect)
            throws ProfileManagementException {
        UserRealm realm = null;
        ClaimManager claimsManager = null;
        Claim[] claims = null;
        List<ClaimConfigurationDTO> claimList = null;

        validateInputParameters(new String[] { dialect });

        try {
            realm = getRealm();
            claimsManager = realm.getClaimManager();

            if (claimsManager == null) {
                String message = "No claim configurations defined";
                if (log.isDebugEnabled()) {
                    log.debug(message);
                }
                throw null;
            }

            claims = (Claim[]) claimsManager.getAllClaims(dialect);
            claimList = new ArrayList<ClaimConfigurationDTO>();

            for (int i = 0; i < claims.length; i++) {
                ClaimConfigurationDTO claim = new ClaimConfigurationDTO();
                claim.setClaimUri(claims[i].getClaimUri());
                claim.setBehavior(UserCoreConstants.CLAIM_HIDDEN);
                claimList.add(claim);
            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            log.error("Error occured while loading claim configurations", e);
            throw new ProfileManagementException(
                    "Error occured while loading claim configurations", e);
        }

        return claimList.toArray(new ClaimConfigurationDTO[claimList.size()]);
    }

    /**
     * This checks the user store property to support multiple profile configurations.
     * This is used by client decide whether to enable/disable 'Add Profile Configuration'
     * @return
     * @throws ProfileManagementException
     */
    public boolean isAddProfileEnabled() throws ProfileManagementException {
        UserStoreManager userStoreManager = null;
        try {
            UserRealm realm = getRealm();
            userStoreManager = realm.getUserStoreManager();

        } catch (ProfileManagementException e) {
            String errorMessage = "Error obtaining user realm.";
            log.error(errorMessage, e);
            throw new ProfileManagementException(errorMessage, e);

        } catch (UserStoreException e) {
            String errorMessage = "Error obtaining UserStoreManager.";
            log.error(errorMessage, e);
            throw new ProfileManagementException(errorMessage, e);

        }
        return userStoreManager.isMultipleProfilesAllowed();
    }

    private UserRealm getRealm() throws  ProfileManagementException{
        try {
            return AdminServicesUtil.getUserRealm();
        } catch (CarbonException e) {
            // already logged
            throw new ProfileManagementException(e.getMessage(), e);
        }
    }
    /**
     * 
     * @param params
     * @throws ProfileManagementException
     */
    private void validateInputParameters(String[] params) throws ProfileManagementException {
        for (int i = 0; i < params.length; i++) {
            if (params[i] == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Invalid input parameters");
                }
                throw new ProfileManagementException("Invalid input parameters");
            }
        }
    }
}
