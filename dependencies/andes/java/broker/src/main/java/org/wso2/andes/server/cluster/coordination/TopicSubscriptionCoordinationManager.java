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

package org.wso2.andes.server.cluster.coordination;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.wso2.andes.server.ClusterResourceHolder;
import org.wso2.andes.server.configuration.ClusterConfiguration;

public class TopicSubscriptionCoordinationManager {

    private static Log log = LogFactory.getLog(SubscriptionCoordinationManagerImpl.class);


    private ZooKeeperAgent zooKeeperAgent;

    private TopicSubscriptionParentDataChangeListener topicSubscriptionDataChangeListener;

    public void init() throws CoordinationException {
        try {
            ClusterConfiguration clusterConfiguration = ClusterResourceHolder.getInstance().getClusterConfiguration();
            if(clusterConfiguration.isClusteringEnabled()) {
                String zkServer = clusterConfiguration.getZookeeperConnection();
                this.zooKeeperAgent  = new ZooKeeperAgent(zkServer);
                this.zooKeeperAgent.initTopicSubscriptionCoordination();
                ZooKeeper zk = zooKeeperAgent.getZooKeeper();
                this.topicSubscriptionDataChangeListener = new TopicSubscriptionParentDataChangeListener();
                zk.getData(CoordinationConstants.TOPIC_SUBSCRIPTION_COORDINATION_PARENT,topicSubscriptionDataChangeListener,null);
            }
        } catch (Exception e) {
            throw new CoordinationException("Error while initializing " +
                    "TopicSubscriptionCoordinationManager" ,e

            );
        }


    }

    public void handleSubscriptionChange(String topicOfSubscriptionChanged) throws CoordinationException {

        if(ClusterResourceHolder.getInstance().getClusterConfiguration().isClusteringEnabled()) {

            // Notify global listeners
            ZooKeeper zooKeeper = zooKeeperAgent.getZooKeeper();
            if(zooKeeper != null) {
                try {
                    //topic on which subscription changed
                    String topic = topicOfSubscriptionChanged;
                    zooKeeper.setData(CoordinationConstants.TOPIC_SUBSCRIPTION_COORDINATION_PARENT,topic.getBytes(),-1);

                } catch (Exception e) {
                    throw new CoordinationException("Error while handling topic subscription change");

                }

            } else {
                throw new CoordinationException("Topic Subscription Coordination Manager not initialized yet");
            }
        }
        else {
            notifyTopicSubscriptionChange(topicOfSubscriptionChanged);
        }
    }

    public void notifyTopicSubscriptionChange(String topicChanged)  {
        try {
            if(log.isDebugEnabled()) {
                log.debug("Notifying topic Subscriptions has been modified. Load fresh from database nad updating memory map");
            }
        ClusterResourceHolder.getInstance().getCassandraMessageStore().syncTopicSubscriptionsWithDatabase(topicChanged);
        } catch (Exception e) {
            log.error("Error in synchronizing topic subscriptions with database. This may cause serious issues with topic operations",e);
        }
    }

    private class TopicSubscriptionParentDataChangeListener implements Watcher {

        @Override
        public void process(WatchedEvent watchedEvent) {

            log.debug("Topic subscription data change event received : " + watchedEvent);
            if(Event.EventType.NodeDataChanged == watchedEvent.getType()) {
                try {


                    byte[] data = zooKeeperAgent.getZooKeeper().getData(CoordinationConstants.TOPIC_SUBSCRIPTION_COORDINATION_PARENT,
                            topicSubscriptionDataChangeListener, null);
                    String topicChanged = new String(data);
                    notifyTopicSubscriptionChange(topicChanged);
                } catch (Exception e) {
                    log.error("Error while processing topic subscription Data Change event");
                }
            }
        }
    }

}
