/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.cassandra.cluster.mgt;

import org.wso2.carbon.core.AbstractAdmin;

import static org.wso2.carbon.cassandra.cluster.mgt.CassandraClusterToolStorageMBeanService.*;

public class CassandraClusterToolsAdmin extends AbstractAdmin{

    private CassandraClusterToolStorageMBeanService cassandraClusterMBeanServerConnection;

    public CassandraClusterToolsAdmin() throws CassandraClusterToolsDataAdminException {
        cassandraClusterToolStorageMBeanService= new CassandraClusterToolStorageMBeanService();
    }

    /**
     * Drain the cassandra node(Cause to stop listening to the write requests)
     * @return return true if operation success and else false
     * @throws CassandraClusterToolsDataAdminException for unable to drain node due to exception
     */
    public boolean drainNode() throws CassandraClusterToolsDataAdminException {
        return cassandraClusterToolStorageMBeanService.drainNode();
    }

    /**
     *  Decommission a cassandra node(Cause to remove node from the ring)
     * @return return true if operation success and else false
     * @throws CassandraClusterToolsDataAdminException for unable to decommission node due to exception
     */
    public  boolean decommissionNode() throws CassandraClusterToolsDataAdminException {
        return cassandraClusterToolStorageMBeanService.decommissionNode();
    }

    /**
     * Move cassandra node to a new token
     * @param newToken Value of the new token
     * @return return true if operation success and else false
     * @throws CassandraClusterToolsDataAdminException for unable to move node due to exception
     */
    public boolean moveNode(String newToken) throws CassandraClusterToolsDataAdminException {
        return cassandraClusterToolStorageMBeanService.moveNode(newToken);
    }

    /**
     * Flush a column family
     * @param keyspace Name of the keyspace where column family located
     * @param columnFamily Name of the column family
     * @return return true if operation success and else false
     * @throws CassandraClusterToolsDataAdminException for unable to flush column family due to exception
     */
    public boolean flushColumnFamily(String keyspace,String columnFamily) throws CassandraClusterToolsDataAdminException{
        return cassandraClusterToolStorageMBeanService.flush(keyspace, columnFamily);
    }

    /**
     * Repair a column family
     * @param keyspace Name of the keyspace where column family located
     * @param columnFamily Name of the column family
     * @return return true if operation success and else false
     * @throws CassandraClusterToolsDataAdminException for unable to repair column family due to exception
     */
    public boolean repairColumnFamily(String keyspace,String columnFamily) throws CassandraClusterToolsDataAdminException {
        return cassandraClusterToolStorageMBeanService.repair(keyspace, columnFamily);
    }

    /**
     * Compact a column family
     * @param keyspace Name of the keyspace where column family located
     * @param columnFamily Name of the column family
     * @return return true if operation success and else false
     * @throws CassandraClusterToolsDataAdminException for unable to compact column family due to exception
     */
    public boolean compactColumnFamily(String keyspace,String columnFamily) throws CassandraClusterToolsDataAdminException
    {
        return cassandraClusterToolStorageMBeanService.compact(keyspace, columnFamily);
    }

    /**
     * Clean up a column family
     * @param keyspace Name of the keyspace where column family located
     * @param columnFamily Name of the column family
     * @return return true if operation success and else false
     * @throws CassandraClusterToolsDataAdminException for unable to compact column family due to exception
     */
    public boolean cleanUpColumnFamily(String keyspace, String columnFamily) throws CassandraClusterToolsDataAdminException {
        return cassandraClusterToolStorageMBeanService.cleanUp(keyspace, columnFamily);
    }

    /**
     *Perform garbage collection of the node
     */
    public void performGC()
    {
        System.gc();
    }

    /**
     * Cleanup a keyspace
     * @param keyspace Name of the keyspace
     * @return return true if operation success and else false
     * @throws CassandraClusterToolsDataAdminException  for unable to cleanup keyspace due to exception
     */
    public boolean cleanUpKeyspace(String keyspace) throws CassandraClusterToolsDataAdminException {
        return cassandraClusterMBeanServerConnection.cleanUp(keyspace);
    }

    /**
     * Flush a keyspace
     * @param keyspace Name of the keyspace
     * @return return true if operation success and else false
     * @throws CassandraClusterToolsDataAdminException  for unable to flush keyspace due to exception
     */
    public boolean flushKeyspace(String keyspace) throws CassandraClusterToolsDataAdminException {
        return cassandraClusterToolStorageMBeanService.flush(keyspace);
    }

    /**
     * Compact a keyspace
     * @param keyspace Name of the keyspace
     * @return return true if operation success and else false
     * @throws CassandraClusterToolsDataAdminException  for unable to compact keyspace due to exception
     */
    public boolean compactKeyspace(String keyspace) throws CassandraClusterToolsDataAdminException{
        return cassandraClusterToolStorageMBeanService.compact(keyspace);
    }

    /**
     * Repair a keyspace
     * @param keyspace Name of the keyspace
     * @return return true if operation success and else false
     * @throws CassandraClusterToolsDataAdminException  for unable to repair keyspace due to exception
     */
    public boolean repairKeyspace(String keyspace) throws CassandraClusterToolsDataAdminException {
        return cassandraClusterToolStorageMBeanService.repair(keyspace);
    }

    /**
     * Scrub a keyspace(This cause to create a backup inside the all column families in the keyspace)
     * @param keyspace Name of the keyspace
     * @return return true if operation success and else false
     * @throws CassandraClusterToolsDataAdminException  for unable to scrub keyspace due to exception
     */
    public boolean scrubKeyspace(String keyspace) throws CassandraClusterToolsDataAdminException{
        return cassandraClusterToolStorageMBeanService.scrub(keyspace);
    }

    /**
     * Scrub up a column family(This cause to create a backup inside the column family)
     * @param keyspace Name of the keyspace where column family located
     * @param columnFamily Name of the column family
     * @return return true if operation success and else false
     * @throws CassandraClusterToolsDataAdminException for unable to scrub column family due to exception
     */
    public boolean scrubColumnFamily(String keyspace,String columnFamily) throws CassandraClusterToolsDataAdminException{
        return cassandraClusterToolStorageMBeanService.scrub(keyspace,columnFamily);
    }

    /**
     * Upgrade SSTables in keyspace
     * @param keyspace Name of the keyspace
     * @return return true if operation success and else false
     * @throws CassandraClusterToolsDataAdminException  for unable to Upgrade SSTables in keyspace due to exception
     */
    public boolean upgradeSSTablesInKeyspace(String keyspace) throws CassandraClusterToolsDataAdminException{
        return cassandraClusterToolStorageMBeanService.upgradeSSTables(keyspace);
    }

    /**
     * Upgrade SSTables in column family
     * @param keyspace Name of the keyspace where column family located
     * @param columnFamily Name of the column family
     * @return return true if operation success and else false
     * @throws CassandraClusterToolsDataAdminException for unable to Upgrade SSTables in column family due to exception
     */
    public boolean upgradeSSTablesColumnFamily(String keyspace,String columnFamily) throws CassandraClusterToolsDataAdminException{
        return cassandraClusterToolStorageMBeanService.upgradeSSTables(keyspace,columnFamily);
    }

    /**
     * This create a backup of entire node
     * @param snapShotName Name of the snapshot directory
     * @return return true if operation success and else false
     * @throws CassandraClusterToolsDataAdminException for unable to take snapshot of the node due to exception
     */
    public boolean takeSnapshotOfNode(String snapShotName) throws CassandraClusterToolsDataAdminException{
        return cassandraClusterToolStorageMBeanService.takeSnapShot(snapShotName);
    }

    /**
     * This create a backup of given keyspace
     * @param snapShotName Name of the snapshot directory
     * @param keyspace Name of the keyspace need to be taken a snapshot
     * @return  return true if operation success and else false
     * @throws CassandraClusterToolsDataAdminException for unable to take snapshot of the keyspace due to exception
     */
    public boolean takeSnapshotOfKeyspace(String snapShotName, String keyspace) throws CassandraClusterToolsDataAdminException{
        return cassandraClusterToolStorageMBeanService.takeSnapShot(snapShotName,keyspace);
    }

    /**
     * This clear the backup of node specify in the snapshot name
     * @param snapShotName Name of the snapshot need to be cleared
     * @return return true if operation success and else false
     * @throws CassandraClusterToolsDataAdminException for unable to clear the snapshot of the Node due to exception
     */
    public boolean clearSnapshotOfNode(String snapShotName) throws CassandraClusterToolsDataAdminException{
        return cassandraClusterToolStorageMBeanService.clearSnapShot(snapShotName);
    }

    /**
     * This clear the backup of the keyspace specify in the snapshot name
     * @param snapShotName Name of the snapshot need to be cleared
     * @param keyspace Name of the keyspace need to be clear the snapshot
     * @return  return true if operation success and else false
     * @throws CassandraClusterToolsDataAdminException for unable to clear the snapshot of the keyspace due to exception
     */
    public boolean clearSnapshotOfKeyspace(String snapShotName,String keyspace) throws CassandraClusterToolsDataAdminException{
        return cassandraClusterToolStorageMBeanService.clearSnapShot(snapShotName,keyspace);
    }

    /**
     * Check whether node is join in the ring or not
     * @return return true if node is join in the ring else false
     */
    public boolean isJoined(){
        return cassandraClusterToolStorageMBeanService.isJoined();
    }

    /**
     * Check whether RPC server is running
     * @return return true if RPC is running and else return false
     *
     */
    public boolean isRPCRunning()
    {
        return cassandraClusterToolStorageMBeanService.getRPCServerStatus();
    }

    /**
     * Check whether Gossip server is running
     * @return return true if Gossip is running and else return false
     */
    public boolean isGossipServerEnable(){
        return cassandraClusterToolStorageMBeanService.isGossipEnable();
    }

    /**
     * Stop the RPC server
     */
    public void stopRPCServer()
    {
        cassandraClusterToolStorageMBeanService.shutDownNodeRPCServer();
    }

    /**
     * Start the RPC server of the node
     */
    public void startRPCServer()
    {
        cassandraClusterToolStorageMBeanService.startNodeRPCServer();
    }

    /**
     * Start the gossip server of the node
     */
    public void startGossipServer(){
        cassandraClusterToolStorageMBeanService.startGossipServer();
    }

    /**
     * Stop the gossip server of the node
     */
    public void stopGossipServer(){
        cassandraClusterToolStorageMBeanService.stopGossipServer();
    }

    /**
     * Set the incremental backup status of the node
     * @param status Pass true to set the incremental backup and false to stop
     */
    public void setIncrementalBackUpStatus(boolean status)
    {
        cassandraClusterToolStorageMBeanService.setIncrementalBackUpStatus(status);
    }

    /**
     * Return whether incremental backup is enable or disable
     * @return true if it enable else false
     */
    public boolean getIncrementalBackUpStatus()
    {
        return cassandraClusterToolStorageMBeanService.isIncrementalBackUpEnable();
    }

    /**
     * Join the node to the cluster
     * @return return true if operation success and else false
     * @throws CassandraClusterToolsDataAdminException for unable to join the ring due to exception
     */
    public boolean joinCluster() throws CassandraClusterToolsDataAdminException {

        return cassandraClusterToolStorageMBeanService.joinRing();
    }
}

