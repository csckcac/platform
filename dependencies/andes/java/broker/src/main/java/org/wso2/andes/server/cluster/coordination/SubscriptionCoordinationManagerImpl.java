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

import java.util.ArrayList;
import java.util.List;

public class SubscriptionCoordinationManagerImpl implements SubscriptionCoordinationManager{


    private static Log log = LogFactory.getLog(SubscriptionCoordinationManagerImpl.class);

    private String zkHost;

    private int zkPort;


    private ZooKeeperAgent zooKeeperAgent;

    private SubscriptionParentDataChangeListener subscriptionParentDataChangeListener;

    private List<SubscriptionListener> subscriptionListeners = new ArrayList<SubscriptionListener>();



    @Override
    public void init() throws CoordinationException {
        try {
            ClusterConfiguration clusterConfiguration = ClusterResourceHolder.getInstance().getClusterConfiguration();
            if(clusterConfiguration.isClusteringEnabled()) {
                this.zkHost = clusterConfiguration.getZookeeperHost();
                this.zkPort = clusterConfiguration.getZookeeperPort();
                this.zooKeeperAgent  = new ZooKeeperAgent(zkHost,zkPort);
                this.zooKeeperAgent.initSubscriptionCoordination();
                ZooKeeper zk = zooKeeperAgent.getZooKeeper();
                this.subscriptionParentDataChangeListener = new SubscriptionParentDataChangeListener();
                zk.getData(CoordinationConstants.SUBSCRIPTION_COORDINATION_PARENT,subscriptionParentDataChangeListener,null);
            }
        } catch (Exception e) {
            throw new CoordinationException("Error while initializing " +
                    "SubscriptionCoordinationManagerImpl");
        }


    }

    @Override
    public void notifySubscriptionChange()  {

        log.debug("Notifying subscribers on Subscription changes ");

        for(SubscriptionListener listener : subscriptionListeners) {
            listener.subscriptionsChanged();
        }

    }

    @Override
    public void handleSubscriptionChange() throws CoordinationException {

       if(ClusterResourceHolder.getInstance().getClusterConfiguration().isClusteringEnabled()) {

           // Notify global listeners
           ZooKeeper zooKeeper = zooKeeperAgent.getZooKeeper();
            if(zooKeeper != null) {
                try {
                    zooKeeper.setData(CoordinationConstants.SUBSCRIPTION_COORDINATION_PARENT,new byte[]{(byte)1},-1);

                } catch (Exception e) {
                    throw new CoordinationException("Error while handling subscription change");

                }

            } else {
                throw new CoordinationException("Subscription Coordination Manager not initialized yet");
            }

       }

       // Notify Local Listeners
       notifySubscriptionChange();

    }

    @Override
    public void registerSubscriptionListener(SubscriptionListener listener) {
        if(listener == null) {
            throw new RuntimeException("Error while registering subscribers : invalid argument listener = null");
        }

        this.subscriptionListeners.add(listener);
    }

    @Override
    public void removeSubscriptionListener(SubscriptionListener listener) {
        if(this.subscriptionListeners.contains(listener)) {
            this.subscriptionListeners.remove(listener);
        }
    }

    private class SubscriptionParentDataChangeListener implements Watcher {

        @Override
        public void process(WatchedEvent watchedEvent) {

            log.debug("Subscription data change event received : " + watchedEvent);
            if(Event.EventType.NodeDataChanged == watchedEvent.getType()) {
                try {


                    zooKeeperAgent.getZooKeeper().getData(CoordinationConstants.SUBSCRIPTION_COORDINATION_PARENT,
                            subscriptionParentDataChangeListener, null);
                    notifySubscriptionChange();
                } catch (Exception e) {
                    log.error("Error while processing subscription Data Change event");
                }
            }
        }
    }


}
