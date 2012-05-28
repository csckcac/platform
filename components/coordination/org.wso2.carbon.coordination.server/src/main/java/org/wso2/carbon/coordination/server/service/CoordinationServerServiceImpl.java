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

import org.wso2.carbon.coordination.server.CoordinationServer;
import org.wso2.carbon.coordination.server.internal.CoordinationServerConfigHolder;

import java.util.Properties;

public class CoordinationServerServiceImpl implements CoordinationServerService {
    public void startServer() {
        CoordinationServer server = CoordinationServerConfigHolder.getCoordinationServerConfigHolder().
                getCoordinationServer();

        if (server != null) {
            server.start();
        }
    }

    public void stopServer() {
        CoordinationServer server = CoordinationServerConfigHolder.getCoordinationServerConfigHolder().
                getCoordinationServer();

        if (server != null) {
            server.stop();
        }
    }

    public Properties getZKServerConfigurationProperties() {
        return CoordinationServerConfigHolder.getCoordinationServerConfigHolder().
                getConfigProperties();
    }
}
