/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.internal;

import org.apache.axis2.engine.ListenerManager;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.*;
import org.wso2.carbon.apimgt.impl.utils.RemoteAuthorizationManager;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.secure.AuthorizeRoleListener;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.AuthorizationUtils;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.listener.AuthorizationManagerListener;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.mgt.UserMgtConstants;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.FileUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

/**
 * @scr.component name="org.wso2.apimgt.impl.services" immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="user.realm.service"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 * @scr.reference name="listener.manager.service"
 * interface="org.apache.axis2.engine.ListenerManager" cardinality="0..1" policy="dynamic"
 * bind="setListenerManager" unbind="unsetListenerManager"
 */
public class APIManagerComponent {

    private static final Log log = LogFactory.getLog(APIManagerComponent.class);

    private ServiceRegistration registration;

    protected void activate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("API manager component activated");
        }
        
        try {
            addRxtConfigs();
            addTierPolicies();

            APIManagerConfiguration configuration = new APIManagerConfiguration();
            String filePath = CarbonUtils.getCarbonHome() + File.separator + "repository" +
                    File.separator + "conf" + File.separator + "api-manager.xml";
            configuration.load(filePath);
            APIManagerConfigurationServiceImpl configurationService =
                    new APIManagerConfigurationServiceImpl(configuration);
            ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(configurationService);
            registration = componentContext.getBundleContext().registerService(
                    APIManagerConfigurationService.class.getName(),
                    configurationService, new Properties());
            setupSelfRegistration(configuration);

            AuthorizationUtils.addAuthorizeRoleListener(APIConstants.AM_CREATOR_APIMGT_EXECUTION_ID,
                    RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
                            RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + APIConstants.API_APPLICATION_DATA_LOCATION),
                    APIConstants.Permissions.API_CREATE,
                    UserMgtConstants.EXECUTE_ACTION, null);
            AuthorizationUtils.addAuthorizeRoleListener(APIConstants.AM_CREATOR_GOVERNANCE_EXECUTION_ID,
                    RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
                            RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + "/trunk"),
                    APIConstants.Permissions.API_CREATE,
                    UserMgtConstants.EXECUTE_ACTION, null);
            AuthorizationUtils.addAuthorizeRoleListener(APIConstants.AM_PUBLISHER_APIMGT_EXECUTION_ID,
                    RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
                            RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + APIConstants.API_APPLICATION_DATA_LOCATION),
                    APIConstants.Permissions.API_PUBLISH,
                    UserMgtConstants.EXECUTE_ACTION, null);

            RemoteAuthorizationManager authorizationManager = RemoteAuthorizationManager.getInstance();
            authorizationManager.init();
        } catch (APIManagementException e) {
            log.fatal("Error while initializing the API manager component", e);
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Deactivating API manager component");
        }
        registration.unregister();
        APIManagerFactory.getInstance().clearAll();
        RemoteAuthorizationManager authorizationManager = RemoteAuthorizationManager.getInstance();
        authorizationManager.destroy();
    }

    protected void setRegistryService(RegistryService registryService) {
        if (registryService != null && log.isDebugEnabled()) {
            log.debug("Registry service initialized");
        }
        ServiceReferenceHolder.getInstance().setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        ServiceReferenceHolder.getInstance().setRegistryService(null);
    }

    protected void setRealmService(RealmService realmService) {
        if (realmService != null && log.isDebugEnabled()) {
            log.debug("Realm service initialized");
        }
        ServiceReferenceHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        ServiceReferenceHolder.getInstance().setRealmService(null);
    }

    protected void setListenerManager(ListenerManager listenerManager) {
        // We bind to the listener manager so that we can read the local IP
        // address and port numbers properly.
        log.debug("Listener manager bound to the API manager component");
        APIManagerConfigurationService service = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService();
        if (service != null) {
            service.getAPIManagerConfiguration().reloadSystemProperties();
        }
    }

    protected void unsetListenerManager(ListenerManager listenerManager) {
        log.debug("Listener manager unbound from the API manager component");
    }

    private void addRxtConfigs() throws APIManagementException {
        String rxtDir = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator +
                "resources" + File.separator + "rxts";
        File file = new File(rxtDir);
        //create a FilenameFilter
        FilenameFilter filenameFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                //if the file extension is .rxt return true, else false
                return name.endsWith(".rxt");
            }
        };
        String[] rxtFilePaths = file.list(filenameFilter);
        RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();
        UserRegistry systemRegistry;
        Registry registry;
        try {
            systemRegistry = registryService.getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME);
            registry = registryService.getRegistry();
        } catch (RegistryException e) {
            throw new APIManagementException("Failed to get registry", e);
        }

        for (String rxtPath : rxtFilePaths) {
            String resourcePath = GovernanceConstants.RXT_CONFIGS_PATH +
                    RegistryConstants.PATH_SEPARATOR + rxtPath;
            try {
                if (registry.resourceExists(resourcePath)) {
                    continue;
                }
                String rxt = FileUtil.readFileToString(rxtDir + File.separator + rxtPath);
                Resource resource = registry.newResource();
                resource.setContent(rxt.getBytes());
                resource.setMediaType(APIConstants.RXT_MEDIA_TYPE);
                systemRegistry.put(resourcePath, resource);
            } catch (IOException e) {
                String msg = "Failed to read rxt files";
                throw new APIManagementException(msg, e);
            } catch (RegistryException e) {
                String msg = "Failed to add rxt to registry ";
                throw new APIManagementException(msg, e);
            }
        }
    }
    
    private void addTierPolicies() throws APIManagementException {
        RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();
        try {
            UserRegistry registry = registryService.getGovernanceSystemRegistry();
            if (registry.resourceExists(APIConstants.API_TIER_LOCATION)) {
                log.debug("Tier policies already uploaded to the registry");
                return;
            }

            log.debug("Adding API tier policies to the registry");
            InputStream inputStream = APIManagerComponent.class.getResourceAsStream("/tiers/default-tiers.xml");
            byte[] data = IOUtils.toByteArray(inputStream);
            Resource resource = registry.newResource();
            resource.setContent(data);
            
            Properties descriptions = new Properties();
            descriptions.load(APIManagerComponent.class.getResourceAsStream(
                    "/tiers/default-tier-info.properties"));
            Set<String> names = descriptions.stringPropertyNames();
            for (String name : names) {
                resource.setProperty(APIConstants.TIER_DESCRIPTION_PREFIX + name,
                        descriptions.getProperty(name));
            }
            resource.setProperty(APIConstants.TIER_DESCRIPTION_PREFIX + APIConstants.UNLIMITED_TIER,
                    APIConstants.UNLIMITED_TIER_DESC);
            registry.put(APIConstants.API_TIER_LOCATION, resource);
        } catch (RegistryException e) {
            throw new APIManagementException("Error while saving policy information to the registry", e);
        } catch (IOException e) {
            throw new APIManagementException("Error while reading policy file content", e);
        }
    }

    private void setupSelfRegistration(APIManagerConfiguration config) throws APIManagementException {
        boolean enabled = Boolean.parseBoolean(config.getFirstProperty(APIConstants.SELF_SIGN_UP_ENABLED));
        if (!enabled) {
            return;
        }

        String role = config.getFirstProperty(APIConstants.SELF_SIGN_UP_ROLE);
        if (role == null) {
            // Required parameter missing - Throw an exception and interrupt startup
            throw new APIManagementException("Required subscriber role parameter missing " +
                    "in the self sign up configuration");
        }

        boolean create = Boolean.parseBoolean(config.getFirstProperty(APIConstants.SELF_SIGN_UP_CREATE_ROLE));
        if (create) {
            String[] permissions = new String[] {
                    "/permission/admin/login",
                    APIConstants.Permissions.API_SUBSCRIBE
            };
            try {
                RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();
                UserRealm realm = realmService.getBootstrapRealm();
                UserStoreManager manager = realm.getUserStoreManager();
                AuthorizationManager authorizationManager = realm.getAuthorizationManager();
                authorizationManager.clearRoleActionOnAllResources(role, UserMgtConstants.EXECUTE_ACTION);
                for (String permission : permissions) {
                    authorizationManager.authorizeRole(role, permission, UserMgtConstants.EXECUTE_ACTION);
                }
                if (!manager.isExistingRole(role)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Creating subscriber role: " + role);
                    }
                    manager.addRole(role, null, null);
                }
            } catch (UserStoreException e) {
                throw new APIManagementException("Error while creating subscriber role: " + role + " - " +
                        "Self registration might not function properly.", e);
            }
        }
    }
}
