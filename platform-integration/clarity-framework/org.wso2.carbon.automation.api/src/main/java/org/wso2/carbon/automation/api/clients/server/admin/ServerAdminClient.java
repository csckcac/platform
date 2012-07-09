/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.automation.api.clients.server.admin;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.api.clients.utils.AuthenticateStub;
import org.wso2.carbon.server.admin.stub.ServerAdminStub;
import org.wso2.carbon.server.admin.stub.Exception;

import java.rmi.RemoteException;

public class ServerAdminClient {

    private static final Log log = LogFactory.getLog(ServerAdminClient.class);

    private ServerAdminStub serverAdminStub;

    public ServerAdminClient(String backEndUrl) throws AxisFault {
        String serviceName = "ServerAdmin";
        String endPoint = backEndUrl + serviceName;
        serverAdminStub = new ServerAdminStub(endPoint);
    }

    public void restartGracefully(String sessionCookie) throws Exception, RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, serverAdminStub);
        serverAdminStub.restartGracefully();

    }

    public void restart(String sessionCookie) throws Exception, RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, serverAdminStub);
        serverAdminStub.restart();

    }

    public void shutdown(String sessionCookie) throws Exception, RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, serverAdminStub);
        serverAdminStub.shutdown();

    }

    public void shutdownGracefully(String sessionCookie) throws Exception, RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, serverAdminStub);
        serverAdminStub.shutdownGracefully();

    }
}
