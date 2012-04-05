package org.wso2.carbon.andes.cluster.mgt.ui;

import java.rmi.RemoteException;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.andes.mgt.stub.AndesManagerServiceClusterMgtAdminExceptionException;
import org.wso2.carbon.andes.mgt.stub.AndesManagerServiceStub;
import org.wso2.carbon.andes.mgt.stub.types.carbon.NodeDetail;
import org.wso2.carbon.andes.mgt.stub.types.carbon.Queue;
import org.wso2.carbon.andes.mgt.stub.types.carbon.Topic;

import java.lang.Exception;

/**
 * This class is used to call MB Cluster Manager service from client side
 */
public class ClusterManagerClient {
	
	private AndesManagerServiceStub stub;

    /**
     * Constructor for ClusterManagerClient
     * @param configCtx
     * @param backendServerURL
     * @param cookie
     * @throws Exception
     */
	public ClusterManagerClient(ConfigurationContext configCtx, String backendServerURL, String cookie) throws Exception{
		String serviceURL = backendServerURL + "AndesManagerService";
        stub = new AndesManagerServiceStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
	}

    /**
     * gives complete nodes list
     * @param startingIndex
     * @param maxMessageBoxesCount
     * @return
     * @throws RemoteException
     */
    public NodeDetail [] getAllNodeDetail(int startingIndex, int maxMessageBoxesCount)
                throws RemoteException, AndesManagerServiceClusterMgtAdminExceptionException {

       NodeDetail [] result =stub.getAllNodeDetail(startingIndex,maxMessageBoxesCount);
       return result;
    }

    /**
     * returns number of nodes in the cluster
     * @return int
     * @throws RemoteException
     */
    public int getNumOfNodes() throws RemoteException, AndesManagerServiceClusterMgtAdminExceptionException {
        return  stub.getNumOfNodes();
    }

    /**
     * gives queues whose queue manager runs on the given node
     * @param hostName
     * @param startingIndex
     * @param maxTopicCount
     * @return
     * @throws RemoteException
     * @throws AndesManagerServiceClusterMgtAdminExceptionException
     */
    public Queue[] getQueuesOfNode(String hostName,int startingIndex, int maxTopicCount)
            throws RemoteException, AndesManagerServiceClusterMgtAdminExceptionException {

       return stub.getAllQueuesForNode(hostName,startingIndex,maxTopicCount);
    }

    /**
     * gives all the topics residing in the cluster
     * @param startingIndex
     * @param maxTopicCount
     * @return
     * @throws RemoteException
     */
    public Topic[] getAllTopics(int startingIndex, int maxTopicCount) throws RemoteException, AndesManagerServiceClusterMgtAdminExceptionException {
        return stub.getAllTopicsForNode(startingIndex,maxTopicCount);
    }

    //TO DELETE
    public long updateNumOfSubscriptionsForTopic(String topicName) throws RemoteException, AndesManagerServiceClusterMgtAdminExceptionException {
        return stub.getNumberofSubscriptionsForTopic(topicName);
    }

    /**
     * initialises the nodes
     * @throws RemoteException
     */
    public void initialize() throws RemoteException {
        stub.initialize();
    }

    /**
     * Update memory usage of the node to current
     * @param hostName
     * @return long
     * @throws RemoteException
     */
    public long updateMemoryUsage(String hostName) throws RemoteException, AndesManagerServiceClusterMgtAdminExceptionException {
        return stub.getMemoryUsage(hostName);
    }

    /**
     * update number of topics in the cluster
     * @return long
     * @throws RemoteException
     */
    public long updateTopicCount() throws RemoteException, AndesManagerServiceClusterMgtAdminExceptionException {
        return stub.getNumberOfTopics();
    }

    /**
     * update number of queues whose queue manager runs on the given node
     * @param hostName
     * @return
     * @throws RemoteException
     * @throws AndesManagerServiceClusterMgtAdminExceptionException
     */
    public long updateQueueCountForNode(String hostName) throws RemoteException,
            AndesManagerServiceClusterMgtAdminExceptionException {

        return stub.getNumberOfQueues(hostName);
    }

    /**
     * update the throughput for the requested node
     * @param hostName
     * @return
     * @throws RemoteException
     */
    public long updateThroughputForNode(String hostName) throws RemoteException, AndesManagerServiceClusterMgtAdminExceptionException {
        return stub.getThroughputForNode(hostName);
    }

    /**
     *  Restart the node requested
     * @param hostName
     * @return success
     */
    public boolean restartNode(String hostName)
    {
        return true;
    }

    /**
     * Get current cassandra connection ip:port
     * @return
     * @throws RemoteException
     */
    public String getCassandraConnection() throws RemoteException {
        return stub.getCassandraConnection();
    }

    /**
     * Get current zookeeper connection ip:port
     * @return
     * @throws RemoteException
     */
    public String getZookeeperConnection() throws RemoteException, AndesManagerServiceClusterMgtAdminExceptionException {
        return stub.getZookeeperConnection();
    }

    /**
     * Refresh all the nodes in cluster
     * @throws AndesManagerServiceClusterMgtAdminExceptionException
     * @throws RemoteException
     */
    public void refreshAllNodes() throws RemoteException, AndesManagerServiceClusterMgtAdminExceptionException {
        stub.refreshMemUsageThroughputAndMessageCount();
    }

    /**
     * Reassign worker of a particular queue to another node
     * @param queueToUpdate
     * @param newNodeToAssign
     * @return
     */
    public boolean updateWorkerForQueue(String queueToUpdate, String newNodeToAssign) throws RemoteException {
        boolean  result =stub.updateWorkerForQueue(queueToUpdate, newNodeToAssign);
        return result;
    }

}
