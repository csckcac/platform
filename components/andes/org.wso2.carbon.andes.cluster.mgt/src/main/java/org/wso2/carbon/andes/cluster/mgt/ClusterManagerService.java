/*
* Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* 	http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.andes.cluster.mgt;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Admin service class for cluster management
 */
public class ClusterManagerService {


    private ArrayList<NodeDetail> nodeDetailList = new ArrayList<NodeDetail>();
    private ArrayList<Queue> queueList = new ArrayList<Queue>();
    private ArrayList<Topic> topicList = new ArrayList<Topic>();

    private static final Log log = LogFactory.getLog(ClusterManagerService.class);

    /**
     * constructor for admin service. It initializes the node list
     */
    public ClusterManagerService() {
        initialize();
    }

    /**
     * initializes nodes
     */
    public void initialize() {
        initNodes();
        initQueues();
        initTopics();
    }

    //will be replaced by nodes get method in the original
    private void initNodes() {
        nodeDetailList.clear();
        NodeDetail node1 = new NodeDetail();
        node1.setNodeId("N1");
        node1.setHostName("node1");
        node1.setIpAddress("255.255.255.255");
        node1.setZookeeperID("ZID1");
        NodeDetail node2 = new NodeDetail();
        node2.setNodeId("N2");
        node2.setHostName("node2");
        node2.setIpAddress("128.123.12.2");
        node2.setZookeeperID("ZID2");

        nodeDetailList.add(node1);
        nodeDetailList.add(node2);
    }

    //to be removed
    private void initQueues() {
        queueList.clear();
        Queue queue1 = new Queue();
        queue1.setCreatedFrom("sqsClient");
        Calendar cal = Calendar.getInstance();
        queue1.setCreatedTime(cal);
        queue1.setMessageCount(19);
        queue1.setQueueDepth(35454556);
        queue1.setQueueName("queue1");
        queue1.setUpdatedTime(cal);

        Queue queue2 = new Queue();
        queue2.setCreatedFrom("amqp");
        queue2.setCreatedTime(cal);
        queue2.setMessageCount(29);
        queue2.setQueueDepth(75764565);
        queue2.setQueueName("queue2");
        queue2.setUpdatedTime(cal);

        Queue queue3 = new Queue();
        queue3.setCreatedFrom("amqp");
        queue3.setCreatedTime(cal);
        queue3.setMessageCount(123);
        queue3.setQueueDepth(500000);
        queue3.setQueueName("queue3");
        queue3.setUpdatedTime(cal);


        queueList.add(queue1);
        queueList.add(queue2);
        queueList.add(queue3);
    }

    //to be removed
    private void initTopics() {
        try
        {
        topicList.clear();
        Topic topic1 = new Topic();
        topic1.setName("topic1");

        Topic topic2 = new Topic();
        topic2.setName("topic2");

        Topic topic3 = new Topic();
        topic3.setName("topic3");

        topicList.add(topic1);
        topicList.add(topic2);
        topicList.add(topic3);

        refreshNumOfSubscribersForTopics();
        refreshMemUsageThroughputAndMessageCount();
        }
        catch (Exception e)
        {

        }

    }

    /**
     * @return number of available nodes in the cluster
     */
    public int getNumOfNodes() throws ClusterMgtAdminException {
        try {
            //ClusterManagementBeans clusterManagementBeans = new ClusterManagementBeans();
            //ArrayList<NodeDetail> nodeDetailList = clusterManagementBeans.getNodesWithZookeeperID();
            return nodeDetailList.size();
        } catch (Exception e) {
            throw new ClusterMgtAdminException("cannot access MBean information for node detail");
        }
    }

    /**
     * gives queues whose queue manager runs on the given node
     *
     * @param hostName
     * @param startingIndex
     * @param maxQueueCount
     * @return Array of Queues
     * @throws ClusterMgtAdminException
     */
    public Queue[] getAllQueuesForNode(String hostName, int startingIndex, int maxQueueCount)
            throws ClusterMgtAdminException {

        try {
            Queue[] queueDetailsArray;
            int resultSetSize = maxQueueCount;
            ArrayList<Queue> resultList = new ArrayList<Queue>();

            if(hostName.equals("node1"))
            {
                resultList.add(queueList.get(0));
                resultList.add(queueList.get(1));
            }

            if(hostName.equals("node2"))
            {
                resultList.add(queueList.get(2));
            }


            //get all the queues populated into this node
            //QueueManager queueManager = new RegistryQueueManager();
            //List<Queue> completeQueueList = queueManager.getAllQueues();

            //get names of queues  whose queue manager runs on the given node
            //ClusterManagementBeans clusterManagementBeans = new ClusterManagementBeans();
            //Queue[] queueNamesList = clusterManagementBeans.getQueuesRunningInNode(hostName);

            //filter queues related to given host name and put them to resultList
//            for (Queue aQueue : completeQueueList) {
//
//                String currentQueueName = aQueue.getQueueName();
//
//                for (Queue matchingQueue : queueNamesList) {
//                    String matchingQueueName = matchingQueue.getQueueName();
//                    if (currentQueueName.equals(matchingQueueName)) {
//                        resultList.add(aQueue);
//                        break;
//                    }
//                }
//            }

            if ((resultList.size() - startingIndex) < maxQueueCount) {
                resultSetSize = (resultList.size() - startingIndex);
            }
            queueDetailsArray = new Queue[resultSetSize];
            int index = 0;
            int queueDetailsIndex = 0;
            for (Queue queueDetail : resultList) {
                if (startingIndex == index || startingIndex < index) {
                    queueDetailsArray[queueDetailsIndex] = new Queue();

                    queueDetailsArray[queueDetailsIndex].setQueueName(queueDetail.getQueueName());

                    queueDetailsArray[queueDetailsIndex].setMessageCount(queueDetail.getMessageCount());

                    queueDetailsArray[queueDetailsIndex].setQueueDepth(queueDetail.getQueueDepth());
                    queueDetailsArray[queueDetailsIndex].setCreatedFrom(queueDetail.getCreatedFrom());
                    queueDetailsArray[queueDetailsIndex].setUpdatedTime(queueDetail.getUpdatedTime());
                    queueDetailsArray[queueDetailsIndex].setCreatedTime(queueDetail.getCreatedTime());


                    queueDetailsIndex++;
                    if (queueDetailsIndex == maxQueueCount) {
                        break;
                    }

                }

                index++;
            }

            return queueDetailsArray;

        } catch (Exception e) {
            throw new ClusterMgtAdminException("Can not get the queue manager ", e);
        }
    }

    /**
     * gives topics whole list of topics in the cluster
     *
     * @param startingIndex
     * @param maxTopicCount
     * @return array of Topic
     */
    public Topic[] getAllTopicsForNode(int startingIndex, int maxTopicCount) throws ClusterMgtAdminException {
        try {
            Topic[] topicDetailsArray;
            //ClusterManagementBeans clusterManagementBeans = new ClusterManagementBeans();
            ArrayList<Topic> temp = topicList;

            int resultSetSize = maxTopicCount;

            if ((temp.size() - startingIndex) < maxTopicCount) {
                resultSetSize = (temp.size() - startingIndex);
            }
            topicDetailsArray = new Topic[resultSetSize];
            int index = 0;
            int topicDetailDetailsIndex = 0;
            for (Topic topicDetail : temp) {
                if (startingIndex == index || startingIndex < index) {
                    topicDetailsArray[topicDetailDetailsIndex] = new Topic();
                    topicDetailsArray[topicDetailDetailsIndex].setNumberOfSubscribers(topicDetail.getNumberOfSubscribers());
                    topicDetailsArray[topicDetailDetailsIndex].setName(topicDetail.getName());
                    topicDetailDetailsIndex++;
                    if (topicDetailDetailsIndex == maxTopicCount) {
                        break;
                    }

                }

                index++;
            }

            return topicDetailsArray;

        } catch (Exception e) {
            throw new ClusterMgtAdminException("Can not access MBean information for topics ", e);
        }
    }

    /**
     * gives complete nodes list
     *
     * @param startingIndex
     * @param maxNodesCount
     * @return Array of nodes
     */
    public NodeDetail[] getAllNodeDetail(int startingIndex, int maxNodesCount) throws ClusterMgtAdminException {

        try {
            NodeDetail[] nodeDetailArray;
            //ClusterManagementBeans clusterManagementBeans = new ClusterManagementBeans();
            ArrayList<NodeDetail> nodeDetailList = this.nodeDetailList;
            int resultSetSize = maxNodesCount;
            if ((nodeDetailList.size() - startingIndex) < maxNodesCount) {
                resultSetSize = (nodeDetailList.size() - startingIndex);
            }
            nodeDetailArray = new NodeDetail[resultSetSize];
            int index = 0;
            int nodeDetailsIndex = 0;
            for (NodeDetail nodeDetail : nodeDetailList) {
                if (startingIndex == index || startingIndex < index) {
                    nodeDetailArray[nodeDetailsIndex] = new NodeDetail();

                    nodeDetailArray[nodeDetailsIndex].setHostName(nodeDetail.getHostName());
                    nodeDetailArray[nodeDetailsIndex].setIpAddress(nodeDetail.getIpAddress());
                    nodeDetailArray[nodeDetailsIndex].setNodeId(nodeDetail.getNodeId());
                    nodeDetailArray[nodeDetailsIndex].setZookeeperID(nodeDetail.getZookeeperID());

                    //to remove
                    nodeDetailArray[nodeDetailsIndex].setMessagesReceivedLastFiveMin(nodeDetail.getMessagesReceivedLastFiveMin());
                    nodeDetailArray[nodeDetailsIndex].setMessagesReceivedLastHalfMin(nodeDetail.getMessagesReceivedLastHalfMin());
                    nodeDetailArray[nodeDetailsIndex].setMessagesReceivedLastHour(nodeDetail.getMessagesReceivedLastHour());

                    nodeDetailArray[nodeDetailsIndex].setMemoryUsage(nodeDetail.getMemoryUsage());
                    nodeDetailArray[nodeDetailsIndex].setNumOfQueues(nodeDetail.getNumOfQueues());
                    nodeDetailArray[nodeDetailsIndex].setNumOfTopics(nodeDetail.getNumOfTopics());


                    nodeDetailsIndex++;
                    if (nodeDetailsIndex == maxNodesCount) {
                        break;
                    }

                }

                index++;
            }

            return nodeDetailArray;
        } catch (Exception e) {
            throw new ClusterMgtAdminException("Can not access MBean information for nodes", e);
        }
    }

    //not used
    /*public int getMessageCount(String requestedPeriod, String hostName) throws ClusterMgtAdminException {
        int result = 0;
        for (NodeDetail aNodeDetail : nodeDetailList) {
            if (aNodeDetail.getHostName() == hostName) {
                if (requestedPeriod == Constants.MESSAGE_COUNT_FOR_LAST_THIRTY_SEC) {
                    result = aNodeDetail.getMessagesReceivedLastHalfMin();
                } else if (requestedPeriod == Constants.MESSAGE_COUNT_FOR_LAST_FIVE_MIN) {
                    result = aNodeDetail.getMessagesReceivedLastFiveMin();
                } else if (requestedPeriod == Constants.MESSAGE_COUNT_FOR_LAST_HOUR) {
                    result = aNodeDetail.getMessagesReceivedLastHour();
                }
            }
        }
        return result;
    }*/

    /**
     * get throughput for the requested node
     *
     * @param hostname
     * @return long
     */
    public long getThroughputForNode(String hostname) throws ClusterMgtAdminException{
        refreshMemUsageThroughputAndMessageCount();
        long result = 0;
        for (NodeDetail aNodeDetail : nodeDetailList) {
            if (aNodeDetail.getHostName().equals(hostname)) {
                result = aNodeDetail.getThroughput();
            }
        }
        return result;
    }

    /**
     * get memory usage for the requested node
     *
     * @param hostname
     * @return long
     */
    public long getMemoryUsage(String hostname) throws ClusterMgtAdminException{
        long result = 0;
        refreshMemUsageThroughputAndMessageCount();
        for (NodeDetail aNodeDetail : nodeDetailList) {
            if (aNodeDetail.getHostName().equals(hostname)) {
                result = aNodeDetail.getMemoryUsage();
            }
        }
        return result;
    }

    /**
     * get current number of topics those have one
     * or more subscribers subscribed to that topic on the given node
     * @return long
     */
    public long getNumberOfTopics() throws ClusterMgtAdminException {
        try {
            long result = 0;
            //ClusterManagementBeans clusterManagementBeans = new ClusterManagementBeans();
            ArrayList<Topic> topicList = this.topicList;
            result = topicList.size();
            return result;
        } catch (Exception e) {
            throw new ClusterMgtAdminException("Cannot access MBean information for topics");
        }
    }

    /**
     * gives number queues whose queue manager runs on the given node
     *
     * @param hostName
     * @return long
     * @throws ClusterMgtAdminException
     */
    public long getNumberOfQueues(String hostName) throws ClusterMgtAdminException {
        try {
            long result = 0;
            //ClusterManagementBeans clusterManagementBeans = new ClusterManagementBeans();
            Queue[] temp = this.queueList.toArray(new Queue[queueList.size()]);
            result = temp.length;
            return result;
        } catch (Exception e) {
            throw new ClusterMgtAdminException("Cannot get the queue manager ", e);
        }

    }

    /**
     * Returns number of subscriptions for the topic
     *
     * @param topicName
     * @return long
     */
    public long getNumberofSubscriptionsForTopic(String topicName) throws ClusterMgtAdminException {

        try {
            long result = 0;
            //ClusterManagementBeans clusterManagementBeans = new ClusterManagementBeans();
            //int numOfSubscribers = clusterManagementBeans.getNumOfSubscribersForTopic(topicName);
            refreshNumOfSubscribersForTopics();
            for(Topic topicDetail : topicList)
            {
                if(topicName.equals(topicDetail.getName()))
                {
                    result = topicDetail.getNumberOfSubscribers();
                }
            }
            return result;
        } catch (Exception e) {
            throw new ClusterMgtAdminException("Cannot access MBean information for topics", e);
        }
    }

    //TO REMOVE
    private void refreshNumOfSubscribersForTopics() {
        //reconsider all the topics and filter out topics that has one or more subscribers with updated details.
        int minimum = 0;
        int maximum = 20000;
        for (Topic aTopic : topicList) {
            aTopic.setNumberOfSubscribers(minimum + (int) (Math.random() * maximum));
        }
    }

    /**
     * get common cassandra connection
     *
     * @return
     */
    public String getCassandraConnection() {
        String connection = "127.0.0.1:5000";
        return connection;
    }

    /**
     * get common zookeeper connection
     *
     * @return
     */
    public String getZookeeperConnection() throws ClusterMgtAdminException {
        //ClusterManagementBeans clusterManagementBeans = new ClusterManagementBeans();
        String connection =null;
        try {
            connection =  "127.0.0.1:3234";
        } catch (Exception e) {
            throw new ClusterMgtAdminException(e.getMessage());
        }
        return connection;
    }

    /**
     * Reassign worker of a particular queue to another node
     *
     * @param queueToUpdate
     * @param newNodeToAssign
     * @return success if assign was successful
     */
    public boolean updateWorkerForQueue(String queueToUpdate, String newNodeToAssign) {
        return true;
    }

    //TO REMOVE

    /**
     * Refreshes all node details, number of queues whose worker resides,
     * number of topics who has one or more subscribers,
     * memory usage for each node,
     * throughput for each node,
     * number of messages received for different periods.
     * This should be invoked for a full refresh of nodeDetails.
     *
     * @throws ClusterMgtAdminException
     */
    public void refreshMemUsageThroughputAndMessageCount() throws ClusterMgtAdminException {
        int minimum = 0;
        int maximum = 500000;
        for (NodeDetail aNodeDetail : nodeDetailList) {
            aNodeDetail.setNumOfQueues(getNumberOfQueues(aNodeDetail.getHostName()));
            aNodeDetail.setNumOfTopics(minimum + (int) (Math.random() * maximum));
            aNodeDetail.setMemoryUsage(minimum + (int) (Math.random() * maximum));
            aNodeDetail.setThroughput(minimum + (int) (Math.random() * maximum));
            aNodeDetail.setMessagesReceivedLastFiveMin(minimum + (int) (Math.random() * maximum));
            aNodeDetail.setMessagesReceivedLastHour(minimum + (int) (Math.random() * maximum));
            aNodeDetail.setMessagesReceivedLastHalfMin(minimum + (int) (Math.random() * maximum));
        }

    }
}