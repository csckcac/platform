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
package org.apache.qpid.server.cluster;

import org.apache.qpid.management.common.mbeans.ClusterManagementInformation;
import org.apache.qpid.management.common.mbeans.annotations.MBeanConstructor;
import org.apache.qpid.management.common.mbeans.annotations.MBeanOperationParameter;
import org.apache.qpid.server.management.AMQManagedObject;

import javax.management.JMException;
import java.util.List;


/**
 * <code>ClusterManagementInformationMBean</code> The the JMS MBean that expose cluster management information
 */
public class ClusterManagementInformationMBean extends AMQManagedObject implements ClusterManagementInformation {

    private ClusterManager clusterManager;

    @MBeanConstructor("Creates an MBean exposing an Cluster Manager")
    public ClusterManagementInformationMBean(ClusterManager clusterManager) throws JMException {
        super(ClusterManagementInformation.class , ClusterManagementInformation.TYPE);
        this.clusterManager = clusterManager;
    }

    public String getZkServerAddress() {
        return clusterManager.getZkServerAddress();
    }

    public int getZkServerPort() {
        return clusterManager.getZkServerPort();
    }

    public String getObjectInstanceName() {
        return clusterManager.getZkServerAddress();
    }

    public String[] getQueues(int nodeId) {
        return clusterManager.getQueues(nodeId);
    }

    public List<Integer> getZkNodes() {
        return clusterManager.getZkNodes();
    }

    public int getMessageCount(@MBeanOperationParameter(name = "queueName", description = "Name of the queue which message count is required") String queueName) {
        return clusterManager.numberOfMessagesInQueue(queueName);
    }

    public List<String> getTopics() {
        return clusterManager.getTopics();
    }

    public List<String> getSubscribers(String topic){
        return clusterManager.getSubscribers(topic);
    }

    public int getSubscriberCount(@MBeanOperationParameter(name = "Topic", description = "Topic name") String topic) {
        return clusterManager.getSubscriberCount(topic);
    }
}
