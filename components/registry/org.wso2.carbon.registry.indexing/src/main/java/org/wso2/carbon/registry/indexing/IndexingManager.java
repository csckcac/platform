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

package org.wso2.carbon.registry.indexing;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.indexing.indexer.Indexer;
import org.wso2.carbon.registry.indexing.utils.IndexingUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * This singleton class manages indexing.
 */
@edu.umd.cs.findbugs.annotations.SuppressWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class IndexingManager {

    private static Log log = LogFactory.getLog(IndexingManager.class);
    private static volatile IndexingManager instance;

    private UserRegistry registry;
    private RegistryConfigLoader registryConfig;
    private AsyncIndexer indexer;
    private ScheduledExecutorService submittingExecutor;
    private ScheduledExecutorService indexingExecutor;
    private Date lastAccessTime;

    private IndexingManager() {
        try {
            registry = Utils.getRegistryService().getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME);
            registryConfig = new RegistryConfigLoader();
            indexer = new AsyncIndexer();
        } catch (RegistryException e) {
            log.error("Could not initialize registry for indexing", e);
        }
    }

    public static IndexingManager getInstance() {
        if (instance == null) {
            synchronized (IndexingManager.class) {
                instance = new IndexingManager();
            }
        }
        return instance;
    }

    public synchronized void startIndexing() {
        stopIndexing(); //stop executors if they are already running, otherwise they will never stop
        submittingExecutor = Executors.newSingleThreadScheduledExecutor();
        submittingExecutor.scheduleWithFixedDelay(new ResourceSubmitter(this),
                getStartingDelayInSecs(), getIndexingFreqInSecs(), TimeUnit.SECONDS);

        indexingExecutor = Executors.newSingleThreadScheduledExecutor();
        indexingExecutor.schedule(indexer, getStartingDelayInSecs(), TimeUnit.SECONDS);
        readLastAccessTime();
    }

    public synchronized void restartIndexing() {
        stopIndexing();
        try {
            registry.delete(getLastAccessTimeLocation());
        } catch (RegistryException e) {
            log.error("Could not delete last activity time to restart indexing", e);
        }
        startIndexing();
    }

    public synchronized void stopIndexing() {
        if (submittingExecutor != null) {
            submittingExecutor.shutdownNow();
            submittingExecutor = null;
        }
        if (indexingExecutor != null) {
            indexingExecutor.shutdownNow();
            indexingExecutor = null;
        }
        writeLastAccessTime();
    }

    public long getStartingDelayInSecs() {
        return registryConfig.getStartingDelayInSecs();
    }

    public boolean canIndex(String path) {
        Pattern[] patterns = registryConfig.getExclusionPatterns();
        for (Pattern pattern : patterns) {
            if (pattern.matcher(path).matches()) {
                return false;
            }
        }
        return true;
    }

    public long getIndexingFreqInSecs() {
        return registryConfig.getIndexingFreqInSecs();
    }

    public String getLastAccessTimeLocation() {
        return registryConfig.getLastAccessTimeLocation();
    }

    private AsyncIndexer getIndexer() throws RegistryException {
        return indexer;
    }

    public void deleteFromIndex(String oldPath, int tenantId) throws RegistryException {
        getIndexer().getClient().deleteFromIndex(oldPath, tenantId);
    }

    public void submitFileForIndexing(Resource resource, String path, String sourceURL)
            throws RegistryException {
        // TODO: path parameter is redundant
        //if media type is null, mostly it is not a file. We will skip.
        if (resource.getMediaType() == null ||
                getIndexerForMediaType(resource.getMediaType()) == null) {
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Submitting file " + path + " for Indexing");
        }
        getIndexer().addFile(new AsyncIndexer.File2Index(IndexingUtils.getByteContent(resource,
                sourceURL), resource.getMediaType(), path, MultitenantConstants.SUPER_TENANT_ID));

	//Here, we are checking whether a resource has a symlink associated to it, if so, we submit that symlink path
        //in the indexer. see CARBON-11510.
        String symlinkPath = resource.getProperty("registry.resource.symlink.path");
        if( symlinkPath != null)   {
            getIndexer().addFile(new AsyncIndexer.File2Index(IndexingUtils.getByteContent(resource,
                sourceURL), resource.getMediaType(), symlinkPath,
                    MultitenantConstants.SUPER_TENANT_ID));
        }
    }

    public Indexer getIndexerForMediaType(String mimeType) {
        for (Map.Entry<String, Indexer> entry : registryConfig.getIndexerMap().entrySet()) {
            if (Pattern.matches(entry.getKey(), mimeType)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public boolean isIndexable(Resource resource) {
        return resource != null &&
                resource.getMediaType() != null &&
                !(resource instanceof Collection) &&
                IndexingManager.getInstance().getIndexerForMediaType(resource.getMediaType()) != null;
    }

    public UserRegistry getRegistry() {
        return registry;
    }

    public Date getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(Date lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    private void writeLastAccessTime() {
        try {
            if (null != getLastAccessTime()) {
                Resource resource = registry.newResource();
                resource.setContent(String.valueOf(getLastAccessTime().getTime()));
                registry.put(getLastAccessTimeLocation(), resource);
            }
        } catch (RegistryException e) {
            log.error("Could not write last activity time when stopping indexing", e);
        }
    }

    private void readLastAccessTime() {
        try {
            final String lastAccessTimeLocation = getLastAccessTimeLocation();
            setLastAccessTime(
                    registry.resourceExists(lastAccessTimeLocation) ?
                        new Date(Long.parseLong(
                                RegistryUtils.decodeBytes((byte[]) registry.get(lastAccessTimeLocation).getContent())
                         )) : null
            );
        } catch (RegistryException e) {
            log.error("Could not read last activity time when starting indexing", e);
        }
    }
}
