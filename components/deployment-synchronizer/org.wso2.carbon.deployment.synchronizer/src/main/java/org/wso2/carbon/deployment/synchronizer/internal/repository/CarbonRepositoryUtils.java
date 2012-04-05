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

package org.wso2.carbon.deployment.synchronizer.internal.repository;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.util.JavaUtils;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.deployment.synchronizer.*;
import org.wso2.carbon.deployment.synchronizer.internal.DeploymentSynchronizationManager;
import org.wso2.carbon.deployment.synchronizer.internal.DeploymentSynchronizer;
import org.wso2.carbon.deployment.synchronizer.internal.DeploymentSynchronizerConstants;
import org.wso2.carbon.deployment.synchronizer.registry.RegistryBasedArtifactRepository;
import org.wso2.carbon.deployment.synchronizer.internal.util.DeploymentSynchronizerConfiguration;
import org.wso2.carbon.deployment.synchronizer.internal.util.ServiceReferenceHolder;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

/**
 * Utility methods for creating and managing DeploymentSynchronizer instances for Carbon
 * repositories
 */
public class CarbonRepositoryUtils {

    /**
     * Create and initialize a new DeploymentSynchronizer for the Carbon repository of the
     * specified tenant. This method first attempts to load the synchronizer configuration
     * from the registry. If a configuration does not exist in the registry, it will get the
     * configuration from the global ServerConfiguration of Carbon. Note that this method
     * does not start the created synchronizers. It only creates and initializes them using
     * the available configuration settings.
     *
     * @param tenantId ID of the tenant
     * @return a DeploymentSynchronizer instance or null if the synchronizer is disabled
     * @throws DeploymentSynchronizerException If an error occurs while initializing the synchronizer
     */
    public static DeploymentSynchronizer newCarbonRepositorySynchronizer(int tenantId)
            throws DeploymentSynchronizerException {

        DeploymentSynchronizerConfiguration config = getActiveSynchronizerConfiguration(tenantId);

        if (config.isEnabled()) {
            String filePath = MultitenantUtils.getAxis2RepositoryPath(tenantId);

            ArtifactRepository artifactRepository = createArtifactRepository(tenantId,
                    config.getRepositoryType());
            artifactRepository.init(tenantId);
            DeploymentSynchronizer synchronizer = DeploymentSynchronizationManager.getInstance().
                    createSynchronizer(artifactRepository, filePath);
            synchronizer.setAutoCommit(config.isAutoCommit());
            synchronizer.setAutoCheckout(config.isAutoCheckout());
            synchronizer.setPeriod(config.getPeriod());
            synchronizer.setUseEventing(config.isUseEventing());

            return synchronizer;
        }

        return null;
    }

    /**
     * Loads the deployment synchronizer configuration. It will attempt to get the configuration
     * from the registry of the tenant. Failing that, it will get the configuration from the
     * ServerConfiguration.
     *
     * @param tenantId Tenant ID
     * @return a DeploymentSynchronizerConfiguration instance
     * @throws DeploymentSynchronizerException if an error occurs while accessing the registry
     */
    public static DeploymentSynchronizerConfiguration getActiveSynchronizerConfiguration(
            int tenantId) throws DeploymentSynchronizerException {

        try {
            DeploymentSynchronizerConfiguration config =
                    getDeploymentSyncConfigurationFromRegistry(tenantId);
            if (config == null) {
                config = getDeploymentSyncConfiguration();
            }
            return config;
        } catch (RegistryException e) {
            throw new DeploymentSynchronizerException("Error while loading synchronizer " +
                    "configuration from the registry", e);
        }
    }

    /**
     * Load the deployment synchronizer configuration from the global ServerConfiguration
     * of Carbon.
     *
     * @return a DeploymentSynchronizerConfiguration instance
     */
    public static DeploymentSynchronizerConfiguration getDeploymentSyncConfiguration() {
        DeploymentSynchronizerConfiguration config = new DeploymentSynchronizerConfiguration();
        ServerConfiguration serverConfig = ServerConfiguration.getInstance();
        String value = serverConfig.getFirstProperty(DeploymentSynchronizerConstants.ENABLED);
        config.setEnabled(value != null && JavaUtils.isTrueExplicitly(value));

        value = serverConfig.getFirstProperty(DeploymentSynchronizerConstants.AUTO_CHECKOUT_MODE);
        config.setAutoCheckout(value != null && JavaUtils.isTrueExplicitly(value));

        value = serverConfig.getFirstProperty(DeploymentSynchronizerConstants.AUTO_COMMIT_MODE);
        config.setAutoCommit(value != null && JavaUtils.isTrueExplicitly(value));

        value = serverConfig.getFirstProperty(DeploymentSynchronizerConstants.USE_EVENTING);
        config.setUseEventing(value != null && JavaUtils.isTrueExplicitly(value));

        value = serverConfig.getFirstProperty(DeploymentSynchronizerConstants.AUTO_SYNC_PERIOD);
        if (value != null) {
            config.setPeriod(Long.parseLong(value));
        } else {
            config.setPeriod(DeploymentSynchronizerConstants.DEFAULT_AUTO_SYNC_PERIOD);
        }

        value = serverConfig.getFirstProperty(DeploymentSynchronizerConstants.REPOSITORY_TYPE);
        if (value != null) {
            config.setRepositoryType(value);
        } else {
            config.setRepositoryType(DeploymentSynchronizerConstants.DEFAULT_REPOSITORY_TYPE);
        }
        return config;
    }

    /**
     * Returns the Carbon/Axis2 repository path for the given ConfigurationContext
     *
     * @param cfgCtx A ConfigurationContext instance owned by super tenant or some other tenant
     * @return Axis2 repository path from which the configuration is read
     */
    public static String getCarbonRepositoryFilePath(ConfigurationContext cfgCtx) {
        int tenantId = MultitenantUtils.getTenantId(cfgCtx);
        return MultitenantUtils.getAxis2RepositoryPath(tenantId);
    }

    /**
     * Checks whether deployment synchronizer is enabled for the Carbon repository of the
     * specified tenant. This method first checks whether a synchronizer configuration exists
     * in the registry (created by an admin service). If not it will try to get the configuration
     * from the Carbon ServerConfiguration.
     *
     * @param tenantId Tenant ID
     * @return true if deployment synchronizer is enabled for the repository
     * @throws DeploymentSynchronizerException if an error occurs while loading configuration from the registry
     */
    public static boolean isSynchronizerEnabled(int tenantId) throws DeploymentSynchronizerException {
        DeploymentSynchronizerConfiguration config = getActiveSynchronizerConfiguration(tenantId);
        return config.isEnabled();
    }

    /**
     * Loads the deployment synchronizer configuration from the configuration registry of the
     * specified tenant.
     *
     * @param tenantId Tenant ID
     * @return a DeploymentSynchronizerConfiguration object or null
     * @throws RegistryException if the registry cannot be accessed
     */
    public static DeploymentSynchronizerConfiguration getDeploymentSyncConfigurationFromRegistry(
            int tenantId) throws RegistryException {

        UserRegistry localRepository = getLocalRepository(tenantId);
        if (!localRepository.resourceExists(DeploymentSynchronizerConstants.CARBON_REPOSITORY)) {
            return null;
        }

        Resource resource = localRepository.get(DeploymentSynchronizerConstants.CARBON_REPOSITORY);
        DeploymentSynchronizerConfiguration config = new DeploymentSynchronizerConfiguration();
        String status = new String((byte[]) resource.getContent());
        if ("enabled".equals(status)) {
            config.setEnabled(true);
        }

        config.setAutoCheckout(Boolean.valueOf(resource.getProperty(
                DeploymentSynchronizerConstants.AUTO_CHECKOUT_MODE)));
        config.setAutoCommit(Boolean.valueOf(resource.getProperty(
                DeploymentSynchronizerConstants.AUTO_COMMIT_MODE)));
        config.setPeriod(Long.valueOf(resource.getProperty(
                DeploymentSynchronizerConstants.AUTO_SYNC_PERIOD)));
        config.setUseEventing(Boolean.valueOf(resource.getProperty(
                DeploymentSynchronizerConstants.USE_EVENTING)));
        config.setRepositoryType(resource.getProperty(
                DeploymentSynchronizerConstants.REPOSITORY_TYPE));
        resource.discard();
        return config;
    }

    /**
     * Save the given DeploymentSynchronizerConfiguration to the registry. The target
     * configuration registry space will be selected using the specified tenant ID. As a result
     * the configuration will be stored in the configuration registry of the specified
     * tenant.
     *
     * @param config The configuration to be saved
     * @param tenantId Tenant ID to select the configuration registry
     * @throws RegistryException if an error occurs while accessing the registry
     */
    public static void persistConfiguration(DeploymentSynchronizerConfiguration config,
                                            int tenantId) throws RegistryException {

        Resource resource;
        UserRegistry localRepository = getLocalRepository(tenantId);
        if (!localRepository.resourceExists(DeploymentSynchronizerConstants.CARBON_REPOSITORY)) {
            resource = localRepository.newResource();
        } else {
            resource = localRepository.get(DeploymentSynchronizerConstants.CARBON_REPOSITORY);
        }

        resource.setProperty(DeploymentSynchronizerConstants.AUTO_COMMIT_MODE,
                String.valueOf(config.isAutoCommit()));
        resource.setProperty(DeploymentSynchronizerConstants.AUTO_CHECKOUT_MODE,
                String.valueOf(config.isAutoCheckout()));
        resource.setProperty(DeploymentSynchronizerConstants.AUTO_SYNC_PERIOD,
                String.valueOf(config.getPeriod()));
        resource.setProperty(DeploymentSynchronizerConstants.USE_EVENTING,
                String.valueOf(config.isUseEventing()));
        resource.setProperty(DeploymentSynchronizerConstants.REPOSITORY_TYPE,
                config.getRepositoryType());
        resource.setContent(config.isEnabled() ? "enabled" : "disabled");

        localRepository.put(DeploymentSynchronizerConstants.CARBON_REPOSITORY, resource);
        resource.discard();
    }

    private static String getRegistryPath(int tenantId) {
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            return DeploymentSynchronizerConstants.SUPER_TENANT_REGISTRY_PATH;
        } else {
            return DeploymentSynchronizerConstants.TENANT_REGISTRY_PATH;
        }
    }

    private static UserRegistry getLocalRepository(int tenantId) throws RegistryException {
        return ServiceReferenceHolder.getRegistryService().getLocalRepository(tenantId);
    }

    private static UserRegistry getConfigurationRegistry(int tenantId) throws RegistryException {
        return ServiceReferenceHolder.getRegistryService().getConfigSystemRegistry(tenantId);
    }

    private static ArtifactRepository createArtifactRepository(
            int tenantId, String repositoryType) throws DeploymentSynchronizerException {

        ArtifactRepository artifactRepository;
        if (DeploymentSynchronizerConstants.REPOSITORY_TYPE_REGISTRY.equals(repositoryType)) {
            try {
                UserRegistry registry = getConfigurationRegistry(tenantId);
                String registryPath = getRegistryPath(tenantId);
                artifactRepository = new RegistryBasedArtifactRepository(registry, registryPath,
                        RegistryConstants.CONFIG_REGISTRY_BASE_PATH);
            } catch (RegistryException e) {
                throw new DeploymentSynchronizerException("Error while accessing registry for " +
                        "tenant: " + tenantId, e);
            }
        } else {
            if (DeploymentSynchronizerConstants.REPOSITORY_TYPE_SVN.equals(repositoryType)) {
                repositoryType = "org.wso2.carbon.deployment.synchronizer.subversion.SVNBasedArtifactRepository";
            }

            try {
                Class clazz = CarbonRepositoryUtils.class.getClassLoader().loadClass(repositoryType);
                artifactRepository = (ArtifactRepository) clazz.newInstance();
            } catch (Exception e) {
                throw new DeploymentSynchronizerException("Error while initializing an object " +
                        "of type: " + repositoryType, e);
            }
        }

        return artifactRepository;
    }

}
