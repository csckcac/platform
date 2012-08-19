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
package org.wso2.carbon.cassandra.cluster;

import org.apache.cassandra.db.ColumnFamilyStoreMBean;
import org.apache.cassandra.db.compaction.CompactionManagerMBean;
import org.apache.cassandra.service.StorageServiceMBean;
import org.apache.cassandra.streaming.StreamingServiceMBean;

public interface CassandraClusterToolsMBeanDataAccess {
    /**
     * Access the <code>StorageServiceMBean </code> of the Cassandra
     * @return  <code>StorageServiceMBean </code>
     */
    StorageServiceMBean locateStorageServiceMBean() throws CassandraClusterToolsDataAccessException;

    /**
     * Access the <code>StreamingServiceMBean </code> of the Cassandra
     * @return  <code>StreamingServiceMBean </code>
     */
    StreamingServiceMBean locateStreamingServiceMBean() throws
                                                        CassandraClusterToolsDataAccessException;
    /**
     * Access the <code>CompactionManagerMBean </code> of the Cassandra
     * @return  <code>CompactionManagerMBean </code>
     */
    CompactionManagerMBean locateCompactionManagerMBean() throws
                                                          CassandraClusterToolsDataAccessException;

    /**
     * Access the columnFamily MBean for the provided column family within provided keyspace of the Cassandra
     * @param keyspace cassandra keyspace where the target column family locates
     * @param columnFamily Name of the column family which need to create the MBean instance of it
     * @return MBean  instance for the given column family(<code>ColumnFamilyStoreMBean </code>)
     */
    ColumnFamilyStoreMBean locateColumnFamilyStoreMBean(String keyspace, String columnFamily) throws
                                                                                              CassandraClusterToolsDataAccessException;

    /**
     *
     * @param username
     * @param password
     * @param jmxPort
     * @param host
     */
    //void createRemoteJmxConnection(String username,String password,int jmxPort,String host) throws CassandraClusterToolsDataAccessException;
}
