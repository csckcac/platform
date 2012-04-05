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

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.entitlement.mediator.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.stub.EntitlementServiceStub;

import java.util.Properties;

/**
 * Implementation of Entitlement Service Client with basic authenticator
 */
public class BasicAuthServiceClient extends EntitlementServiceClient {
    
    EntitlementServiceStub stub;

    private static final Log log = LogFactory.getLog(BasicAuthServiceClient.class);

    @Override
    public void init(Properties properties) {

        String serviceURL = null;
        ServiceClient client = null;
        Options option = null;

        String backEndServerURL = properties.getProperty(EntitlementConstants.SERVICE_EPR);
        String userName = properties.getProperty(EntitlementConstants.USER);
        String password = properties.getProperty(EntitlementConstants.PASSWORD);
        ConfigurationContext configCtx = (ConfigurationContext) properties.get(EntitlementConstants.CONTEXT);

        if(backEndServerURL != null){
            backEndServerURL = backEndServerURL.trim();

            if (!backEndServerURL.endsWith("/")) {
                backEndServerURL += "/";
            }
        }
        
        serviceURL = backEndServerURL + "EntitlementService";
        try {
            stub = new EntitlementServiceStub(configCtx, serviceURL);
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }
        client = stub._getServiceClient();
        option = client.getOptions();
        option.setManageSession(true);
        HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
        auth.setUsername(userName); auth.setPassword(password);
        auth.setPreemptiveAuthentication(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, auth);
    }

    @Override
    public String getDecision(String userName, String resource, String action, String[] env) throws Exception{

        try {
            String decision = getStatus(stub.getDecisionByAttributes(userName, resource, action,
                    env));
            stub.cleanup();
            return decision;
        } catch (Exception e) {
            log.error("Error occurred while policy evaluation", e);
            throw e;
        }
    }
}
