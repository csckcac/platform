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

package org.wso2.carbon.automation.api.clients.identity;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.api.clients.utils.AuthenticateStub;
import org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceStub;

import java.rmi.RemoteException;

public class UserProfileMgtServiceClient {
    private static final Log log = LogFactory.getLog(UserProfileMgtServiceClient.class);


    private final String serviceName = "UserProfileMgtService";
    private UserProfileMgtServiceStub userProfileMgtServiceStub;
    private String endPoint;

    public UserProfileMgtServiceClient(String backEndUrl, String sessionCookie)
            throws RemoteException {
        this.endPoint = backEndUrl + serviceName;
        try {
            userProfileMgtServiceStub = new UserProfileMgtServiceStub(endPoint);
        } catch (AxisFault axisFault) {
            log.error("Error on initializing userProfileMgtServiceStub : " + axisFault.getMessage());
            throw new RemoteException("Error on initializing userProfileMgtServiceStub : ", axisFault);
        }
        AuthenticateStub.authenticateStub(sessionCookie, userProfileMgtServiceStub);
    }

    public UserProfileMgtServiceClient(String backEndUrl, String userName, String password)
            throws RemoteException {
        this.endPoint = backEndUrl + serviceName;
        try {
            userProfileMgtServiceStub = new UserProfileMgtServiceStub(endPoint);
        } catch (AxisFault axisFault) {
            log.error("Error on initializing userProfileMgtServiceStub : " + axisFault.getMessage());
            throw new RemoteException("Error on initializing userProfileMgtServiceStub : ", axisFault);
        }
        AuthenticateStub.authenticateStub(userName, password, userProfileMgtServiceStub);
    }
}
