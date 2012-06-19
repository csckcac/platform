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

package org.wso2.carbon.identity.oauth.ui.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.identity.oauth2.stub.OAuth2ServiceStub;
import org.wso2.carbon.identity.oauth2.stub.dto.*;

import java.rmi.RemoteException;

public class OAuth2ServiceClient {

    private OAuth2ServiceStub stub;

    public OAuth2ServiceClient(String backendServerURL, ConfigurationContext configCtx)
            throws AxisFault {
        String serviceURL = backendServerURL + "OAuth2Service";
        stub = new OAuth2ServiceStub(configCtx, serviceURL);
    }

    public OAuth2AuthorizeRespDTO authorize(OAuth2AuthorizeReqDTO authorizeReqDTO)
            throws RemoteException {
        return stub.authorize(authorizeReqDTO);
    }
    
    public OAuth2ClientValidationResponseDTO validateClient(String clientId, String callbackURI)
            throws RemoteException {
        return stub.validateClientInfo(clientId, callbackURI);
    }

    public OAuth2AccessTokenRespDTO issueAccessToken(OAuth2AccessTokenReqDTO tokenReqDTO)
            throws RemoteException {
        return stub.issueAccessToken(tokenReqDTO);
    }

}
