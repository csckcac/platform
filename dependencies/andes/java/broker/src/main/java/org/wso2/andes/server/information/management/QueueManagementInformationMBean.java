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
package org.wso2.andes.server.information.management;

import org.wso2.andes.management.common.mbeans.QueueManagementInformation;
import org.wso2.andes.management.common.mbeans.annotations.MBeanOperationParameter;
import org.wso2.andes.server.ClusterResourceHolder;
import org.wso2.andes.server.cluster.GlobalQueueManager;
import org.wso2.andes.server.management.AMQManagedObject;
import org.wso2.andes.server.store.CassandraMessageStore;

import javax.management.NotCompliantMBeanException;
import java.util.List;

public class QueueManagementInformationMBean extends AMQManagedObject implements QueueManagementInformation {

    GlobalQueueManager globalQueueManager;
    CassandraMessageStore messageStore;

    public QueueManagementInformationMBean() throws NotCompliantMBeanException {
        super(QueueManagementInformation.class, QueueManagementInformation.TYPE);
        this.messageStore = ClusterResourceHolder.getInstance().getCassandraMessageStore();
        this.globalQueueManager = new GlobalQueueManager(messageStore);
    }

    public String getObjectInstanceName() {
        return QueueManagementInformation.TYPE;
    }

    public String[] getAllQueueNames() {

        try {
            List<String> queuesList = messageStore.getGlobalQueues();
            String[] queues= new String[queuesList.size()];
            queuesList.toArray(queues);
            return queues;
        } catch (Exception e) {
          throw new RuntimeException("Error in accessing global queues",e);
        }

    }

    public void deleteQueue(@MBeanOperationParameter(name = "queueName",
            description = "Name of the queue to be deleted") String queueName) {
    }

    public int getMessageCount(String queueName) {
       return globalQueueManager.getMessageCount(queueName);
    }

    public int getSubscriptionCount( String queueName){
        try {
            return globalQueueManager.getSubscriberCount(queueName);
        } catch (Exception e) {
            throw new RuntimeException("Error in getting subscriber count",e);
        }
    }
}
