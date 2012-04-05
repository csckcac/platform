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
package org.apache.qpid.server.cluster.coordination;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.*;

import java.io.IOException;

public class ZooKeeperAgent implements Watcher{


    private ZooKeeper zk;

    private static Log log = LogFactory.getLog(ZooKeeper.class);

    public ZooKeeperAgent(String address, int port) throws IOException {
        log.debug("Starting Zookeeper agent for host : " + address + " port : " + port);
        zk = new ZooKeeper(address,port,this);
        log.debug("ZooKeeper agent started successfully and connected to  " + address + ":" + port);
    }

    @Override
    public void process(WatchedEvent watchedEvent) {

    }


    public void initQueueWorkerCoordination() throws CoordinationException {

        try {
            if (zk.exists(CoordinationConstants.QUEUE_WORKER_COORDINATION_PARENT, false) == null) {
                zk.create(CoordinationConstants.QUEUE_WORKER_COORDINATION_PARENT, new byte[0],
                        ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            } else {

                if(zk.getChildren(CoordinationConstants.QUEUE_WORKER_COORDINATION_PARENT,false)
                        == null
                        || zk.getChildren(CoordinationConstants.QUEUE_WORKER_COORDINATION_PARENT,
                        false).size() == 0) {
                        zk.delete(CoordinationConstants.QUEUE_WORKER_COORDINATION_PARENT , -1);
                        zk.create(CoordinationConstants.QUEUE_WORKER_COORDINATION_PARENT,
                                new byte[0],
                        ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                }

            }
        } catch (Exception e) {
            String msg = "Error while creating Queue worker coordination parent at " +
                    CoordinationConstants.QUEUE_WORKER_COORDINATION_PARENT;
            log.error(msg ,e );
            throw new CoordinationException(msg,e);
        }

    }


    /**
     * init the zookeeper agent to handle the Queue Worker fail over scenarios.
     * @param queue
     * @throws CoordinationException
     */
    public void initQueueFailOverMCProcess(String queue) throws CoordinationException {
        try {
                  if (zk.exists(CoordinationConstants.QUEUE_FAIL_OVER_HANDLING_PARENT+"_"+queue,
                          false) == null) {
                      zk.create(CoordinationConstants.QUEUE_FAIL_OVER_HANDLING_PARENT+"_"+queue,
                              new byte[0],
                              ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                  } else {

                      if(zk.getChildren(CoordinationConstants.QUEUE_FAIL_OVER_HANDLING_PARENT+
                              "_"+queue,false)
                              == null
                              || zk.getChildren(CoordinationConstants.
                              QUEUE_FAIL_OVER_HANDLING_PARENT+"_"+queue,
                              false).size() == 0) {
                              zk.delete(CoordinationConstants.
                                      QUEUE_FAIL_OVER_HANDLING_PARENT+"_"+queue , -1);
                              zk.create(CoordinationConstants.
                                      QUEUE_FAIL_OVER_HANDLING_PARENT+"_"+queue,
                                      new byte[0],
                              ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                      }

                  }
              } catch (Exception e) {
                  String msg = "Error while creating Queue worker coordination parent at " +
                          CoordinationConstants.QUEUE_FAIL_OVER_HANDLING_PARENT+"_"+queue;
                  throw new CoordinationException(msg,e);
              }

    }

    /**
     * init the zookeeper agent to handle the Distributed Locks per queue
     * @param queue
     * @throws CoordinationException
     */
    public void initQueueResourceLockCoordination(String queue) throws CoordinationException {

        try {
            if (zk.exists(CoordinationConstants.QUEUE_RESOURCE_LOCK_PARENT + "_" + queue,
                    false) == null) {
                zk.create(CoordinationConstants.QUEUE_RESOURCE_LOCK_PARENT + "_" + queue,
                        new byte[0],
                        ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }

        } catch (Exception e) {
            String msg = "Error while creating Queue worker coordination parent at " +
                    CoordinationConstants.QUEUE_RESOURCE_LOCK_PARENT + "_" + queue;
            throw new CoordinationException(msg, e);
        }

    }


    public ZooKeeper getZooKeeper() {
        return zk;
    }
}
