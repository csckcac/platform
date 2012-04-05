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

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.LogEntry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.indexing.utils.IndexingUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

/**
 * run() method of this class checks the resources which have been changed since last index time and
 * submits them for indexing. This uses registry logs to detect resources that need to be indexed.
 * An instance of this class should be executed with a ScheduledExecutorService so that run() method
 * runs periodically.
 */
public class ResourceSubmitter implements Runnable {

    private static Log log = LogFactory.getLog(ResourceSubmitter.class);

    private IndexingManager indexingManager;

    protected ResourceSubmitter(IndexingManager indexingManager) {
        this.indexingManager = indexingManager;
    }

    /**
     * This method checks the resources which have been changed since last index time and
     * submits them for indexing. This uses registry logs to detect resources that need to be
     * indexed. This method handles interrupts properly so that it is compatible with the
     * Executor framework
     */
    @SuppressWarnings({"REC_CATCH_EXCEPTION"})
    public void run() {
        try {
            if (Thread.currentThread().isInterrupted()) {
                return; // To be compatible with shutdownNow() method on the executor service
            }
            UserRegistry registry = indexingManager.getRegistry();
            String lastAccessTimeLocation = indexingManager.getLastAccessTimeLocation();


            LogEntry[] entries = registry.getLogs(null, LogEntry.ALL, null, indexingManager.getLastAccessTime(),
                    new Date(), false);
            Arrays.sort(entries, new Comparator<LogEntry>() {
                public int compare(LogEntry o1, LogEntry o2) {
                    return o1.getDate().compareTo(o2.getDate());
                }
            });
            final Date currentTime;
            if (entries.length > 0) {
                currentTime = entries[entries.length - 1].getDate();
            } else {
                currentTime = indexingManager.getLastAccessTime();
            }
            for (LogEntry logEntry : entries) {
                String path = logEntry.getResourcePath();
                try {
                    Resource resourceToIndex;
                    if (path.equals(lastAccessTimeLocation)) {
                        continue;
                    }
                    if (logEntry.getAction() == (LogEntry.DELETE_RESOURCE)) {
                        indexingManager.deleteFromIndex(logEntry.getResourcePath(), 0);
                        if (log.isDebugEnabled()) {
                            log.debug("Resource Deleted: Resource at " + path +
                                    " will be deleted from Indexing Server");
                        }
                    } else if (indexingManager.canIndex(path) &&
                            IndexingUtils.isAuthorized(registry, path, ActionConstants.GET) &&
                            registry.resourceExists(path) &&
                            indexingManager.isIndexable(resourceToIndex = registry.get(path))) {
                        if (logEntry.getAction() == LogEntry.UPDATE) {
                            indexingManager.submitFileForIndexing(resourceToIndex, path, null);
                            if (log.isDebugEnabled()) {
                                log.debug("Resource Updated: Resource at " + path +
                                        " has been submitted to the Indexing Server");
                            }
                        } else if (logEntry.getAction() == (LogEntry.ADD)) {
                            indexingManager.submitFileForIndexing(resourceToIndex, path, null);
                            if (log.isDebugEnabled()) {
                                log.debug("Resource Inserted: Resource at " + path +
                                        " has been submitted to the Indexing Server");
                            }
                        } else if (logEntry.getAction() == (LogEntry.MOVE)) {
                            indexingManager.submitFileForIndexing(resourceToIndex, path, null);
                            indexingManager.deleteFromIndex(logEntry.getActionData(), 0);
                            if (log.isDebugEnabled()) {
                                log.debug("Resource Moved: Resource at " + path +
                                        " has been submitted to the Indexing Server");
                            }
                        } else if (logEntry.getAction() == (LogEntry.COPY)) {
                            path = logEntry.getActionData();
                            indexingManager.submitFileForIndexing(resourceToIndex, path, null);
                            if (log.isDebugEnabled()) {
                                log.debug("Resource Copied : Resource at " + path +
                                        " has been submitted to the Indexing Server");
                            }
                        }
                    }
                } catch (Exception e) { // to ease debugging
                    log.warn("An error occurred while submitting the resource for indexing, path: "
                            + path, e);
                }
            }
            if (log.isTraceEnabled()) {
                log.trace("last successfully indexed activity time is : " +
                        indexingManager.getLastAccessTime().toString());
            }
            indexingManager.setLastAccessTime(currentTime);
        } catch (Throwable e) {
            // Throwable is caught to prevent termination of the executor
            log.warn("An error occurred while submitting resources for indexing", e);
        }

    }
}
