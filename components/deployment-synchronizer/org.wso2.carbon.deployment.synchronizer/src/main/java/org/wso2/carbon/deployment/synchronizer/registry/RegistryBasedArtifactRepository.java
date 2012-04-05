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

package org.wso2.carbon.deployment.synchronizer.registry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.deployment.synchronizer.ArtifactRepository;
import org.wso2.carbon.deployment.synchronizer.DeploymentSynchronizerException;
import org.wso2.carbon.deployment.synchronizer.internal.util.ServiceReferenceHolder;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.synchronization.RegistrySynchronizer;
import org.wso2.carbon.registry.synchronization.SynchronizationException;

/**
 * Use this class in conjunction with the DeploymentSynchronizer to synchronize a file system
 * repository against a repository stored in the registry.
 */
public class RegistryBasedArtifactRepository implements ArtifactRepository {

    private static final Log log = LogFactory.getLog(RegistryBasedArtifactRepository.class);

    private UserRegistry registry;
    private String registryPath;
    private String basePath;

    private String subscriptionId;

    public RegistryBasedArtifactRepository(UserRegistry registry,
                                           String registryPath, String basePath) {
        this.registry = registry;
        this.registryPath = registryPath;
        this.basePath = basePath;
    }

    public void init(int tenantId) throws DeploymentSynchronizerException {
        try {
            if (!registry.resourceExists(registryPath)) {
                Collection collection = registry.newCollection();
                registry.put(registryPath, collection);
                collection.discard();
            }
        } catch (RegistryException e) {
            handleException("Error while creating the registry collection at: " + registryPath, e);
        }
    }

    public boolean commit(String filePath) throws DeploymentSynchronizerException {
        if (log.isDebugEnabled()) {
            log.debug("Committing artifacts at " + filePath + " to the collection at " +
                    registryPath);
        }

        try {
            RegistrySynchronizer.checkIn(registry, filePath, false);
            // TODO: RegistrySynchronizer.checkIn should return true/false to indicate whether a checkin
            // was performed
            return true;
        } catch (SynchronizationException e) {
            handleException("Error while committing artifacts to the registry", e);
        }
        return false;
    }

    public boolean checkout(String filePath) throws DeploymentSynchronizerException {
        if (log.isDebugEnabled()) {
            log.debug("Checking out artifacts from " + registryPath + " to the file system " +
                    "at " + filePath);
        }

        try {
            if (RegistrySynchronizer.isCheckedOut(filePath)) {
                RegistrySynchronizer.update(registry, filePath, false);
            } else {
                RegistrySynchronizer.checkOut(registry, filePath, registryPath);
            }
            // TODO: RegistrySynchronizer.update & RegistrySynchronizer.checkOut should return true
            // if files were actually checked out
            return true;
        } catch (SynchronizationException e) {
            handleException("Error while updating artifacts in the file system from the registry", e);
        }
        return false;
    }

    public void initAutoCheckout(boolean useEventing) throws DeploymentSynchronizerException {
        // In the registry based implementation we can subscribe for registry events
        if (useEventing && subscriptionId == null && ServiceReferenceHolder.getEventingService() != null) {
            String absolutePath = RegistryUtils.getAbsoluteRegistryPath(registryPath, basePath);
            subscriptionId = RegistryUtils.subscribeForRegistryEvents(registry, absolutePath,
                    RegistryUtils.getEventReceiverEndpoint());
            if (log.isDebugEnabled()) {
                log.debug("Subscribed for registry events on the collection: " + absolutePath +
                        " with the subscription ID: " + subscriptionId);
            }
        }
    }

    public void cleanupAutoCheckout() {
        if (subscriptionId == null) {
            return;
        }

        boolean unsubscribe = RegistryUtils.unsubscribeForRegistryEvents(subscriptionId,
                        registry.getTenantId());
        if (!unsubscribe) {
            log.warn("Subscription for registry events could not be removed");
        } else if (log.isDebugEnabled()) {
            log.debug("Unsubscribed from registry events with the ID: " + subscriptionId);
        }
        subscriptionId = null;
    }

    private void handleException(String msg, Exception e) throws DeploymentSynchronizerException {
        log.error(msg, e);
        throw new DeploymentSynchronizerException(msg, e);
    }
}
