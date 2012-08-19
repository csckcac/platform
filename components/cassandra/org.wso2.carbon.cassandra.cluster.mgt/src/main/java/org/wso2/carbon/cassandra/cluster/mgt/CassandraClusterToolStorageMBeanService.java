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

import org.apache.cassandra.config.ConfigurationException;
import org.apache.cassandra.service.StorageServiceMBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cassandra.cluster.CassandraClusterToolsMBeanDataAccess;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
public class CassandraClusterToolStorageMBeanService {
    private static Log log = LogFactory.getLog(CassandraClusterToolStorageMBeanService.class);
    private CassandraClusterToolsMBeanDataAccess cassandraClusterToolsMBeanDataAccess;
    private StorageServiceMBean storageServiceMBean;
    public static CassandraClusterToolStorageMBeanService cassandraClusterToolStorageMBeanService;
    private boolean isSuccess;
    private boolean isGossipEnable=true;
    private boolean isIncrementalBackUpEnable=false;
    public CassandraClusterToolStorageMBeanService() throws CassandraClusterToolsDataAdminException {
        createProxyConnection();
    }

    public boolean decommissionNode() throws CassandraClusterToolsDataAdminException {
        try{
            storageServiceMBean.decommission();
            return true;
        }catch (InterruptedException e)
        {
            throw new CassandraClusterToolsDataAdminException("Cannot drain the node.Cause due to interrupted exception",e,log);
        }
    }

    public boolean drainNode() throws CassandraClusterToolsDataAdminException {
        try {
            storageServiceMBean.drain();
            return true;
        } catch (IOException e) {
            throw new CassandraClusterToolsDataAdminException("Cannot drain the node.Cause due to IOException",e,log);
        } catch (InterruptedException e) {
            throw new CassandraClusterToolsDataAdminException("Cannot drain the node.Cause due to interrupted exception",e,log);
        } catch (ExecutionException e) {
            throw new CassandraClusterToolsDataAdminException("Cannot drain the node.Cause due to execution exception",e,log);
        }
    }

    public String getCommitLogLocation()
    {
        return storageServiceMBean.getCommitLogLocation();
    }

    public boolean joinRing() throws CassandraClusterToolsDataAdminException {
        try {
            storageServiceMBean.joinRing();
            return true;
        } catch (IOException e) {
            throw new CassandraClusterToolsDataAdminException("Cannot join the ring.Cause due to IOException",e,log);
        } catch (ConfigurationException e) {
            throw new CassandraClusterToolsDataAdminException("Cannot join the ring.Cause due to configuration exception",e,log);
        }
    }

    public String[] getAllDataFIleLocations()
    {
        return storageServiceMBean.getAllDataFileLocations();

    }

    public boolean clearSnapShot(String tag,String... keyspace) throws CassandraClusterToolsDataAdminException{
        try {
            storageServiceMBean.clearSnapshot(tag,keyspace);
            return true;
        } catch (IOException e) {
            throw new CassandraClusterToolsDataAdminException("Cannot clear snapshot.Cause due to IOException",e,log);
        }
    }

    public void bulkLoad(String directory)
    {
        storageServiceMBean.bulkLoad(directory);
    }

    public void shutDownNodeRPCServer () {
        storageServiceMBean.stopRPCServer();

    }

    public void startNodeRPCServer ()  {
        storageServiceMBean.startRPCServer();
    }

    public boolean getRPCServerStatus() {
        return storageServiceMBean.isRPCServerRunning();
    }

    public void stopGossipServer() {
        storageServiceMBean.stopGossiping();
        isGossipEnable=false;
    }

    public void startGossipServer() {
        storageServiceMBean.startGossiping();
        isGossipEnable=true;
    }

    public boolean isGossipEnable() {
        return isGossipEnable;
    }

    public boolean isJoined() {
        return storageServiceMBean.isJoined();
    }

    public boolean flush(String keyspace, String... columnFamily) throws CassandraClusterToolsDataAdminException {
        try {
            storageServiceMBean.forceTableFlush(keyspace,columnFamily);
            return true;
        } catch (IOException e) {
            throw new CassandraClusterToolsDataAdminException("Cannot flush the column family.Cause due to IOException",e,log);
        } catch (ExecutionException e) {
            throw new CassandraClusterToolsDataAdminException("Cannot flush the column family.Cause due to execution exception",e,log);
        } catch (InterruptedException e) {
            throw new CassandraClusterToolsDataAdminException("Cannot flush the column family.Cause due to interrupted exception",e,log);
        }
    }

    public boolean cleanUp(String keyspace, String... columnFamily) throws CassandraClusterToolsDataAdminException {
        try {
            storageServiceMBean.forceTableCleanup(keyspace,columnFamily);
            return true;
        } catch (IOException e) {
            throw new CassandraClusterToolsDataAdminException("Cannot cleanUp the column family.Cause due to IOException",e,log);
        } catch (ExecutionException e) {
            throw new CassandraClusterToolsDataAdminException("Cannot cleanUP the column family.Cause due to execution exception",e,log);
        } catch (InterruptedException e) {
            throw new CassandraClusterToolsDataAdminException("Cannot cleanUP the column family.Cause due to interrupted exception",e,log);
        }
    }
    public boolean repair(String keyspace, String... columnFamily) throws CassandraClusterToolsDataAdminException {
        isSuccess=true;
        try {
            storageServiceMBean.forceTableRepair(keyspace,true, columnFamily);
        } catch (IOException e) {
            isSuccess=false;
            throw new CassandraClusterToolsDataAdminException("Cannot repair the column family.Cause due to interrupted exception",e,log);
        }
        return isSuccess;
    }

    public boolean compact(String keyspace, String... columnFamily) throws CassandraClusterToolsDataAdminException {
        try {
            storageServiceMBean.forceTableCompaction(keyspace, columnFamily);
            return true;
        } catch (IOException e) {
            throw new CassandraClusterToolsDataAdminException("Cannot compact the column family.Cause due to IOException",e,log);
        } catch (ExecutionException e) {
            throw new CassandraClusterToolsDataAdminException("Cannot compact the column family.Cause due to execution exception",e,log);
        } catch (InterruptedException e) {
            throw new CassandraClusterToolsDataAdminException("Cannot compact the column family.Cause due to interrupted exception",e,log);
        }
    }

    private void createProxyConnection() throws CassandraClusterToolsDataAdminException {
        cassandraClusterToolsMBeanDataAccess=CassandraClusterToolsAdminComponentManager.getInstance().getCassandraClusterToolsMBeanDataAccess();
        try{
            storageServiceMBean= cassandraClusterToolsMBeanDataAccess.locateStorageServiceMBean();
        }
        catch(Exception e){
            throw new CassandraClusterToolsDataAdminException("Unable to locate storage service MBean connection",e,log);
        }
    }

    public boolean moveNode(String newToken) throws CassandraClusterToolsDataAdminException {
        try {
            storageServiceMBean.move(newToken);
            return true;
        } catch (IOException e) {
            throw new CassandraClusterToolsDataAdminException("Cannot move the node.Cause due to IOException",e,log);
        } catch (InterruptedException e) {
            throw new CassandraClusterToolsDataAdminException("Cannot move the node.Cause due to interrupted exception",e,log);
        } catch (ConfigurationException e) {
            throw new CassandraClusterToolsDataAdminException("Cannot move the node.Cause due to configuration exception",e,log);
        }
    }

    public boolean takeSnapShot(String tag,String... keyspace) throws CassandraClusterToolsDataAdminException {
        try{
            storageServiceMBean.takeSnapshot(tag,keyspace);
            return true;
        }catch (IOException e){
            throw new CassandraClusterToolsDataAdminException("Cannot take the snapshot.Cause due to IO exception",e,log);
        }
    }

    public boolean scrub(String keyspace,String... columnFamilies) throws CassandraClusterToolsDataAdminException {
        try {
            storageServiceMBean.scrub(keyspace,columnFamilies);
            return true;
        } catch (IOException e) {
            throw new CassandraClusterToolsDataAdminException("Cannot perform the scrub.Cause due to IOException",e,log);
        } catch (ExecutionException e) {
            throw new CassandraClusterToolsDataAdminException("Cannot scrub the column family.Cause due to execution exception",e,log);
        } catch (InterruptedException e) {
            throw new CassandraClusterToolsDataAdminException("Cannot scrub the column family.Cause due to interrupted exception",e,log);
        }
    }

    public boolean upgradeSSTables(String keyspace,String... columnFamilies) throws CassandraClusterToolsDataAdminException {
        try {
            storageServiceMBean.upgradeSSTables(keyspace,columnFamilies);
            return true;
        } catch (IOException e) {
            throw new CassandraClusterToolsDataAdminException("Cannot perform the upgradeSSTables.Cause due to IOException",e,log);
        } catch (ExecutionException e) {
            throw new CassandraClusterToolsDataAdminException("Cannot upgradeSSTables for the column family.Cause due to execution exception",e,log);
        } catch (InterruptedException e) {
            throw new CassandraClusterToolsDataAdminException("Cannot upgradeSSTables for the column family.Cause due to interrupted exception",e,log);
        }
    }

    public void setIncrementalBackUpStatus(boolean status) {
        storageServiceMBean.setIncrementalBackupsEnabled(status);
        isIncrementalBackUpEnable=status;
    }

    public boolean isIncrementalBackUpEnable() {
        return isIncrementalBackUpEnable;
    }
}
