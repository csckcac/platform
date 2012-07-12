/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.andes.cluster.mgt.internal.registry;

import org.wso2.carbon.andes.cluster.mgt.NodeDetail;
import org.wso2.carbon.andes.cluster.mgt.Queue;
import org.wso2.carbon.andes.cluster.mgt.Topic;
import org.wso2.carbon.andes.cluster.mgt.internal.ClusterMgtConstants;
import org.wso2.carbon.andes.cluster.mgt.internal.ClusterMgtException;


import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

public class ClusterManagementBeans {

    public String getZookeeperAddressAndPort() throws ClusterMgtException
    {
        String result = "";
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            ObjectName objectName =
                    new ObjectName("org.wso2.andes:type=ClusterManagementInformation,name=127.0.0.1");
            Object MBeanResultForPort = mBeanServer.getAttribute(objectName, ClusterMgtConstants.ZOOKEEPER_PORT_MBEAN_ATTRIB);
            Object MBeanResultForAddress = mBeanServer.getAttribute(objectName, ClusterMgtConstants.ZOOKEEPER_ADDRESS_MBEAN_ATTRIB);
            if(MBeanResultForPort!=null && MBeanResultForAddress!=null)
            {
                  int ZkPort = (Integer)MBeanResultForPort;
                  String ZkAddress = (String) MBeanResultForAddress;
                  result = ZkAddress+":"+ZkPort;
            }
        } catch (MalformedObjectNameException e) {
            throw new ClusterMgtException("Cannot find the MBean for Zookeeper Port and Address");
        } catch (InstanceNotFoundException e) {
            result = "Cannot receive information";
            return result;
        } catch (ReflectionException e) {
            throw new ClusterMgtException("Cannot find the MBean for Zookeeper Port and Address");
        } catch (AttributeNotFoundException e) {
            throw new ClusterMgtException("Cannot find the MBean for Zookeeper Port and Address");
        } catch (MBeanException e) {
            throw new ClusterMgtException("Cannot find the MBean for Zookeeper Port and Address");
        }

        return result;
    }

    public String getCassandraAddressAndPort()
    {
        return "";
    }

    public ArrayList<NodeDetail> getNodesWithZookeeperID() throws ClusterMgtException
    {
        ArrayList<NodeDetail> nodeDetailsList = new ArrayList<NodeDetail>();
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            ObjectName objectName =
                     new ObjectName("org.wso2.andes:type=ClusterManagementInformation,name=ClusterManagementInformation");
            Object result =  mBeanServer.getAttribute(objectName, ClusterMgtConstants.ZOOKEEPER_NODES_MBEAN_ATTRIB);
            if(result!=null)
            {
                List<Integer> ZkIDList = (List<Integer>)result;
                for(Integer zKID : ZkIDList)
                {
                    String zKIDString = Integer.toString(zKID);
                    NodeDetail aNodeDetail = new NodeDetail();
                    aNodeDetail.setZookeeperID(zKIDString);
                    aNodeDetail.setHostName(zKIDString);
                    aNodeDetail.setNumOfQueues(getQueuesRunningInNode(zKIDString).size());
                    aNodeDetail.setNumOfTopics(getTopicList().size());
                    nodeDetailsList.add(aNodeDetail);
                }
            }

            return nodeDetailsList;

        } catch (MalformedObjectNameException e) {
            throw new ClusterMgtException("Cannot access Zookeeper nodes");
        } catch (InstanceNotFoundException e) {
            throw new ClusterMgtException("Cannot access Zookeeper nodes");
        } catch (ReflectionException e) {
            throw new ClusterMgtException("Cannot access Zookeeper nodes");
        } catch (AttributeNotFoundException e) {
            throw new ClusterMgtException("Cannot access Zookeeper nodes");
        } catch (MBeanException e) {
            throw new ClusterMgtException("Cannot access Zookeeper nodes");
        }
    }

    public ArrayList<Topic> getTopicList() throws ClusterMgtException
    {
        ArrayList<Topic> topicDetailsList = new ArrayList<Topic>();
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            ObjectName objectName =
                     new ObjectName("org.wso2.andes:type=ClusterManagementInformation,name=ClusterManagementInformation");
            Object result = mBeanServer.getAttribute(objectName, ClusterMgtConstants.TOPICS_MBEAN_ATTRIB);

            if(result!=null)
            {
                List<String> TopicNamesList = (List<String>)result;

                for(String topicName : TopicNamesList)
                {
                    Topic aTopic = new Topic();
                    aTopic.setName(topicName);
                    aTopic.setNumberOfSubscribers(getNumOfSubscribersForTopic(topicName));
                    topicDetailsList.add(aTopic);
                }

            }

            return topicDetailsList;
        } catch (MalformedObjectNameException e) {
            throw new ClusterMgtException("Cannot access topic information");
        } catch (InstanceNotFoundException e) {
            throw new ClusterMgtException("Cannot access topic information");
        } catch (ReflectionException e) {
            throw new ClusterMgtException("Cannot access topic information");
        } catch (AttributeNotFoundException e) {
            throw new ClusterMgtException("Cannot access topic information");
        } catch (MBeanException e) {
            throw new ClusterMgtException("Cannot access topic information");
        }
    }

    public ArrayList<Queue> getQueuesRunningInNode(String nodeName) throws ClusterMgtException
    {
        int nodeId = Integer.parseInt(nodeName);
        ArrayList<Queue> queueDetailsList = new ArrayList<Queue>();
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try
        {
         ObjectName objectName =
                     new ObjectName("org.wso2.andes:type=ClusterManagementInformation,name=ClusterManagementInformation");
         String operationName = "getQueues";
         Object [] parameters = new Object[]{nodeId};
         String [] signature = new String[]{int.class.getName()};
         Object result = mBeanServer.invoke(
                                         objectName,
                                         operationName,
                                         parameters,
                                         signature);
         if(result!=null)
         {
            String[] queueNamesArray = (String[]) result;
            for(String queueName : queueNamesArray)
             {
                 Queue aQueue = new Queue();
                 aQueue.setQueueName(queueName);
                 aQueue.setMessageCount(getNumberOfAllMessagesForQueue(queueName));
                 queueDetailsList.add(aQueue);
             }
         }

         return queueDetailsList;

        } catch (MalformedObjectNameException e){
           throw new ClusterMgtException("Cannot access topic subscriber information");
        } catch (ReflectionException e) {
           throw new ClusterMgtException("Cannot access topic subscriber information");
        } catch (MBeanException e) {
            throw new ClusterMgtException("Cannot access topic subscriber information");
        } catch (InstanceNotFoundException e) {
            throw new ClusterMgtException("Cannot access topic subscriber information for node");
        }
    }

    public int getNumOfSubscribersForTopic(String topicName)  throws ClusterMgtException
    {
         MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try
        {
         ObjectName objectName =
                    new ObjectName("org.wso2.andes:type=ClusterManagementInformation,name=ClusterManagementInformation");
         String operationName = "getSubscriberCount";
         Object [] parameters = new Object[]{topicName};
         String [] signature = new String[]{String.class.getName()};

         int numberOfSubscribers =  (Integer) mBeanServer.invoke(
                                         objectName,
                                         operationName,
                                         parameters,
                                         signature);
         return numberOfSubscribers;

        } catch (MalformedObjectNameException e){
           throw new ClusterMgtException("Cannot access topic subscriber information");
        } catch (ReflectionException e) {
           throw new ClusterMgtException("Cannot access topic subscriber information");
        } catch (MBeanException e) {
            throw new ClusterMgtException("Cannot access topic subscriber information");
        } catch (InstanceNotFoundException e) {
            throw new ClusterMgtException("Cannot access topic subscriber information");
        }
    }

    public int getNumberOfAllMessagesForQueue(String queueName) throws ClusterMgtException
    {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try
        {
         ObjectName objectName =
                    new ObjectName("org.wso2.andes:type=ClusterManagementInformation,name=ClusterManagementInformation");
         String operationName = "getMessageCount";
         Object [] parameters = new Object[]{queueName};
         String [] signature = new String[]{String.class.getName()};

         int numberOfMessages =  (Integer) mBeanServer.invoke(
                                         objectName,
                                         operationName,
                                         parameters,
                                         signature);
         return numberOfMessages;

        } catch (MalformedObjectNameException e){
           throw new ClusterMgtException("Cannot access queue information");
        } catch (ReflectionException e) {
           throw new ClusterMgtException("Cannot access queue information");
        } catch (MBeanException e) {
            throw new ClusterMgtException("Cannot access queue information");
        } catch (InstanceNotFoundException e) {
            throw new ClusterMgtException("Cannot access queue information");
        }
    }

}
