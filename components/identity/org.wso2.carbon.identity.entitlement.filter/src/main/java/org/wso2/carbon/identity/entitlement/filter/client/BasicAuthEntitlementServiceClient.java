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
package org.wso2.carbon.identity.entitlement.filter.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.identity.entitlement.filter.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.filter.exception.EntitlementFilterException;
import org.wso2.carbon.identity.entitlement.stub.EntitlementServiceStub;
import org.wso2.carbon.utils.CarbonUtils;

import java.util.Properties;

/**
 * Implementation of Entitlement Service Client with basic authenticator
 */
public class BasicAuthEntitlementServiceClient extends AbstractEntitlementServiceClient {

    EntitlementServiceStub stub;

    public void init(Properties properties) throws EntitlementFilterException{

        String entitlementServiceURL;
        ServiceClient client;
        Options option;

        String backEndServerURL = "https://" + properties.getProperty(EntitlementConstants.HOST)
                                  + ":" + properties.getProperty(EntitlementConstants.PORT) + "/";
        String userName = properties.getProperty(EntitlementConstants.USER);
        String password = properties.getProperty(EntitlementConstants.PASSWORD);
        ConfigurationContext configCtx = (ConfigurationContext) properties.get(EntitlementConstants.CONTEXT);

        entitlementServiceURL = backEndServerURL + "services/EntitlementService";
        try {
            stub = new EntitlementServiceStub(configCtx, entitlementServiceURL);
        } catch (AxisFault e) {
            throw new EntitlementFilterException("Error while initializing EntitlementServiceStub", e);
        }
        client = stub._getServiceClient();
        option = client.getOptions();
        option.setManageSession(true);
        CarbonUtils.setBasicAccessSecurityHeaders(userName, password, false, client);
    }

    public String getDecision(String userName, String resource, String action, String[] env)
            throws EntitlementFilterException {
        try {
            String decision = getStatus(stub.getDecisionByAttributes(userName, resource, action, env));
            stub.cleanup();
            return decision;
        } catch (Exception e) {
            throw new EntitlementFilterException("Error while getting decision from PDP using BasicAuthEntitlementServiceClient", e);
        }
    }
}
