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
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.indexing.indexer.Indexer;
import org.wso2.carbon.registry.indexing.indexer.IndexerException;
import org.wso2.carbon.registry.indexing.solr.SolrClient;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * The run() method of this class takes files from a blocking queue and indexes them.
 * An instance of this class should be executed with a ScheduledExecutorService so that run() method
 * runs periodically.
 */
public class AsyncIndexer implements Runnable {

    private static Log log = LogFactory.getLog(AsyncIndexer.class);
    private final SolrClient client;
    private LinkedBlockingQueue<File2Index> queue = new LinkedBlockingQueue<File2Index>();

    @SuppressWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
    public static class File2Index {
        public byte[] data;
        public String mediaType;
        public String path;

        public int tenantId;

        public File2Index(byte[] data, String mediaType, String path, int tenantId) {
            this.data = data;
            this.mediaType = mediaType;
            this.path = path;
            this.tenantId = tenantId;
        }
    }

    public void addFile(File2Index file2Index) {
        queue.offer(file2Index);
    }

    protected AsyncIndexer() throws RegistryException {
        try {
            client = SolrClient.getInstance();
        } catch (IndexerException e) {
            throw new RegistryException("Error initializing Async Indexer " + e.getMessage(), e);
        }
    }

    public SolrClient getClient() {
        return client;
    }

    /**
     * This method retrieves resources submitted for indexing from a blocking queue and indexed them.
     * This handles interrupts properly so that it is compatible with the Executor framework.
     */
    public void run() {
        while (!Thread.currentThread().isInterrupted()) { //to be compatible with executor framework
            try {
                File2Index fileData = queue.take();
                Indexer indexer = IndexingManager.getInstance().getIndexerForMediaType(
                        fileData.mediaType);
                try {
                    getClient().indexDocument(fileData, indexer);
                } catch (Exception e) {
                    log.warn("Could not index the resource: path=" + fileData.path +
                            ", media type=" + fileData.mediaType); // to ease debugging
                }
            } catch (Throwable e) { // Throwable is caught to prevent the executor termination
                if (e instanceof InterruptedException) {
                    return; // to be compatible with executor framework. No need of logging anything
                } else {
                    log.error("Error while indexing.", e);
                }
            }
        }
    }
}
