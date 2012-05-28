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
package org.wso2.carbon.coordination.server.service;

import java.sql.Savepoint;
import java.util.Properties;

/**
 * This is the OSGI service interface which can be used to manage and get information about the zk server
 */
public interface CoordinationServerService {


    public static final String TICK_TIME = "tickTime";


    public static final String DATA_DIR = "dataDir";


    public static final String CLIENT_PORT = "clientPort";


    /**
     * Start the Coordination server
     */
    public void startServer();

    /**
     * Stop the Coordination server
     */
    public void stopServer();

    /**
     * Get ZkServer Configuration properties
     * @return
     */
    public Properties getZKServerConfigurationProperties();

}
