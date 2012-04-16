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

package org.wso2.andes.server.configuration;


/**
 * <class>ClusterConfiguration</class> Holds all the cluster specific Configurations;
 */
public class ClusterConfiguration {

    /**
     * keeps sever configuration object
     */
    private final ServerConfiguration serverConfig;

    private final int zookeeperPort;

    private final String zookeeperHost;





    /**
     * Create cluster configuration object
     * @param serverConfig
     */
    public ClusterConfiguration (final ServerConfiguration serverConfig) {
         this.serverConfig = serverConfig;
         zookeeperPort = Integer.parseInt(serverConfig.getZookeeperPort());
         zookeeperHost = serverConfig.getZookeeperhost();
    }

    /**
     *
     * @return  whether clustering is enabled
     */
    public Boolean isClusteringEnabled() {
         return  serverConfig.getClusteringEnabled();
    }

    /**
     *
     * @return  Zookeeper port
     */
    public Integer getZookeeperPort() {
         return zookeeperPort;
    }

    /**
     * @return   Zookeeper host name
     */
    public String getZookeeperHost() {
         return zookeeperHost;
    }

    public boolean isOnceInOrderSupportEnabled() {
        return serverConfig.isOnceInOrderSupportEnabled();
    }

    public int getMessageBatchSizeForSubscribersQueues() {
        return serverConfig.getMessageBatchSizeForSubscribersQueues();
    }

    public int getMessageBatchSizeForSubscribers() {
        return serverConfig.getMessageBatchSizeForSubscribers();
    }


    public int getFlusherPoolSize() {
        return serverConfig.getFlusherPoolSize();
    }

    public int getSubscriptionPoolSize() {
        return serverConfig.getSubscriptionPoolSize();
    }

    public int getMaxAckWaitTime() {
        return serverConfig.getMaxAckWaitTime();
    }

    public int getMaxAckWaitTimeForBatch() {
        return serverConfig.getMaxAckWaitTimeForBatch();
    }

    public int getQueueWorkerInterval() {
        return serverConfig.getQueueWorkerInterval();
    }

    public int getPubSubMessageRemovalTaskInterval() {
        return serverConfig.getPubSubMessageRemovalTaskInterval();
    }

    public int getContentRemovalTaskInterval() {
        return serverConfig.getContentRemovalTaskInterval();
    }


    public int getContentRemovalTimeDifference() {
        return serverConfig.getContentRemovalTimeDifference();
    }

    public int getVirtualHostSyncTaskInterval() {
        return serverConfig.getVirtualHostSyncTaskInterval();
    }

    public String getReferenceTime() {
        return serverConfig.getReferenceTime();
    }





}
