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
package org.wso2.carbon.identity.scim.provider.impl;

import org.apache.axiom.om.util.Base64;
import org.wso2.charon.core.exceptions.CharonException;
import org.wso2.charon.core.extensions.AuthenticationHandler;
import org.wso2.charon.core.extensions.AuthenticationInfo;
import org.wso2.charon.core.extensions.CharonManager;
import org.wso2.charon.core.schema.SCIMConstants;

import java.util.Map;

public class BasicAuthHandler implements AuthenticationHandler{

    private BasicAuthInfo basicAuthInfo = null;
    public boolean isAuthenticated(Map<String, String> authHeaderMap) throws CharonException {
        String authHeader = authHeaderMap.get(SCIMConstants.AUTHORIZATION_HEADER);
        basicAuthInfo = decodeBasicAuthHeader(authHeader);
        //TODO:do what ever authentication
        //for the moment do not do any authentication at this level since transport
        // level auth will be enforced.
        
        //set only user name in basic auth info which might be retrieved externally.
        basicAuthInfo.setPassword(null);
        return true;  
    }

    public AuthenticationInfo getAuthenticationToken(AuthenticationInfo authenticationInfo) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setCharonManager(CharonManager charonManager) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public AuthenticationInfo getAuthenticationInfo() {
        return basicAuthInfo;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private BasicAuthInfo decodeBasicAuthHeader(String authorizationHeader) {
        byte[] decodedAuthHeader = Base64.decode(authorizationHeader.split(" ")[1]);
        String authHeader = new String(decodedAuthHeader);
        String userName = authHeader.split(":")[0];
        String password = authHeader.split(":")[1];
        BasicAuthInfo basicAuthInfo = new BasicAuthInfo();
        basicAuthInfo.setUserName(userName);
        basicAuthInfo.setPassword(password);
        return basicAuthInfo;

    }
}
