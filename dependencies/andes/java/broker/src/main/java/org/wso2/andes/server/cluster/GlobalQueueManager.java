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
package org.wso2.andes.server.cluster;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.andes.server.ClusterResourceHolder;
import org.wso2.andes.server.store.CassandraMessageStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <code>GlobalQueueManager</code> Manage the Global queues
 */
public class GlobalQueueManager {

    private List<String> queueNameList = new ArrayList<String>();
    private CassandraMessageStore cassandraMessageStore;


    private Map<String,GlobalQueueWorker> queueWorkerMap =
            new ConcurrentHashMap<String,GlobalQueueWorker>();


    private ExecutorService executorService ;

    private static Log log = LogFactory.getLog(GlobalQueueManager.class);

    public GlobalQueueManager(CassandraMessageStore store) {
        this.cassandraMessageStore = store;
        this.executorService = Executors.newCachedThreadPool();
    }


    public void addGlobalQueue(String queueName) {

        if(!queueNameList.contains(queueName)) {
            queueNameList.add(queueName);
              ClusterManager clusterManager = ClusterResourceHolder.getInstance().getClusterManager();
            log.debug("Adding Global Queue worker for queue : " + queueName);
            scheduleWork(queueName);
        }
    }



    private void scheduleWork(String queueName) {
        int batchSize = ClusterResourceHolder.getInstance().getClusterConfiguration().
                getGlobalQueueWorkerMessageBatchSize();
        GlobalQueueWorker worker = new GlobalQueueWorker(queueName,cassandraMessageStore,batchSize);
        worker.setRunning(true);
        queueWorkerMap.put(queueName, worker);
        log.debug("Starting Global Queue worker for queue : " + queueName);
        System.out.println("Starting Global Queue worker for queue : " + queueName);
        executorService.execute(worker);
    }


    public void removeWorker(String queueName) {

        log.debug("Removing Queue worker for queue : " + queueName);
        ClusterManager clusterManager = ClusterResourceHolder.getInstance().getClusterManager();
        GlobalQueueWorker worker = queueWorkerMap.get(queueName);
        if (worker != null) {
            worker.setRunning(false);
            queueWorkerMap.remove(queueName);
        }
    }

    public int getMessageCount(String queueName){
        return cassandraMessageStore.getMessageCountOfGlobalQueue(queueName)+getMessageCountOfUserQueues(queueName);
    }

    public int getMessageCountOfUserQueues(String globalQueueName){
        return cassandraMessageStore.getMessageCountOfUserQueues(globalQueueName);
    }

    public List<String> getTopics() throws Exception {
        return cassandraMessageStore.getTopics();
    }

    public List<String> getSubscribers(String topic) throws Exception {
        return cassandraMessageStore.getRegisteredSubscribersForTopic(topic);
    }

    public int getSubscriberCount(String queueName) throws Exception{
        return cassandraMessageStore.getUserQueues(queueName).size();
    }

}
