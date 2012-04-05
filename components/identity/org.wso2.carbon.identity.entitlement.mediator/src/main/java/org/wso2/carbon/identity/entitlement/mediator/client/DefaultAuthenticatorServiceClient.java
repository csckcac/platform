/*
 *  Copyright (c)  WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.entitlement.mediator.client;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.identity.entitlement.mediator.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.stub.EntitlementServiceStub;

import java.util.Properties;

/**
 * Implementation of Entitlement Service Client that is authenticated
 *  with WSO2 Identity server using default authenticator
 */
public class DefaultAuthenticatorServiceClient extends EntitlementServiceClient{

    private String authCookie;

    private String backEndServerURL;

    private ConfigurationContext configCtx;

    private String userName;

    private  String password;

    private String remoteIp;

    private static final Log log = LogFactory.getLog(DefaultAuthenticatorServiceClient.class);

    @Override
    public void init(Properties properties) {

        remoteIp = properties.getProperty(EntitlementConstants.SERVER_URL);
        password = properties.getProperty(EntitlementConstants.PASSWORD);
        userName = properties.getProperty(EntitlementConstants.USER);
        configCtx = (ConfigurationContext)properties.get(EntitlementConstants.CONTEXT);
        backEndServerURL = properties.getProperty(EntitlementConstants.SERVICE_EPR);

        if(backEndServerURL != null){
            backEndServerURL = backEndServerURL.trim();
            if (!backEndServerURL.endsWith("/")) {
                backEndServerURL += "/";
            }
        }
    }

    @Override
    public String getDecision(String userName, String resource, String action, String[] env)
            throws Exception {

        String decision;
        // authenticate for every request. we need to decide this TODO  
        if(authenticate()){
            try {
                String serviceURL = backEndServerURL + "EntitlementService";
                EntitlementServiceStub stub = new EntitlementServiceStub(configCtx, serviceURL);
                ServiceClient client = stub._getServiceClient();
                Options option = client.getOptions();
                option.setManageSession(true);
                option.setProperty(HTTPConstants.COOKIE_STRING, authCookie);
                decision = getStatus(stub.getDecisionByAttributes(userName, resource, action, env));
                stub.cleanup();
            } catch (Exception e) {
                log.error("Error occurred while policy evaluation", e);
                throw e;
            }

        } else {
            log.error("User can not be authenticated to evaluate the entitlement query" );
            throw new Exception("User can not be authenticated to evaluate the entitlement query");            
        }
        return decision;
    }



    /**
     * authenticates with WSO2 Identity Server using authentication admin service
     * This must be done for each request
     *
     * @return true/false
     * @throws Exception if any error
     */
    private boolean authenticate() throws Exception {
        
        String serviceURL = null;
        ServiceClient client = null;
        Options option = null;
        boolean isAuthenticated = false;
        AuthenticationAdminStub authStub = null;

        serviceURL = backEndServerURL + "AuthenticationAdmin";
        authStub = new AuthenticationAdminStub(configCtx, serviceURL);
        client = authStub._getServiceClient();
        option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, authCookie);
        isAuthenticated = authStub.login(userName, password, remoteIp);
        authCookie = (String) authStub._getServiceClient().getServiceContext()
                .getProperty(HTTPConstants.COOKIE_STRING);
        return isAuthenticated;
    }


}
