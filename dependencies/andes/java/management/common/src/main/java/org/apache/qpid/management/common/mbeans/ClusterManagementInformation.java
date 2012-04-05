package org.apache.qpid.management.common.mbeans;

import org.apache.qpid.management.common.mbeans.annotations.MBeanAttribute;
import org.apache.qpid.management.common.mbeans.annotations.MBeanOperationParameter;

import java.util.*;


/**
 * <code>ClusterManagementInformation</code>
 * Exposes the Cluster Management related information
 */
public interface ClusterManagementInformation {

    static final String TYPE = "ClusterManagementInformation";

     //Individual attribute name constants
    String ATTR_NODE_ID = "nodeId" ;
    String ATTR_ADDRESS = "Address";
    String ATTR_PORT = "Port";

     //All attribute names constant
    static final List<String> CLUSTER_ATTRIBUTES
            = Collections.unmodifiableList(
             new ArrayList<String>(
                     new HashSet<String>(
                             Arrays.asList(
                                     ATTR_NODE_ID,
                                     ATTR_ADDRESS,
                                     ATTR_PORT))));


    @MBeanAttribute(name = "Address", description = "zookeeper host Name")
    String getZkServerAddress();

    @MBeanAttribute(name = "Port", description = "zookeeper port")
    int getZkServerPort();

    @MBeanAttribute(name = "Queues", description = "Existing queues in the node")
    String[] getQueues(int nodeId);

    @MBeanAttribute(name = "zooKeeperNodes" , description = "Existing zookeeper nodes")
    List<Integer> getZkNodes();

    @MBeanAttribute(name = "MessageCount" , description = "Message Count in the queue")
    int getMessageCount(@MBeanOperationParameter(name = "queueName",description = "Name of the queue which message count is required")String queueName);

    @MBeanAttribute(name = "Topics" ,description = "Topics where subscribers are available")
    List<String> getTopics();

    @MBeanAttribute(name = "Subscribers",description = "Subscribers for a given topic")
    List<String> getSubscribers(@MBeanOperationParameter(name="Topic",description = "Topic name") String topic);

    @MBeanAttribute(name = "Subscriber Count",description = "Number of subscribers for a given topic")
    int getSubscriberCount(@MBeanOperationParameter(name="Topic",description = "Topic name") String topic);
}
