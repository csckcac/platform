/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.impl.utils;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.template.APITemplateBuilder;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.rest.api.stub.RestApiAdminStub;

import java.rmi.RemoteException;

public class RESTAPIAdminClient {

    private RestApiAdminStub restApiAdminStub;
    private APITemplateBuilder builder;
    
    public RESTAPIAdminClient(APITemplateBuilder builder) throws AxisFault {
        this.builder = builder;
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String url = config.getFirstProperty(APIConstants.API_GATEWAY_ADMIN_SERVER_URL);
        String cookie = login(config, url);
        restApiAdminStub = new RestApiAdminStub(null, url + "RestApiAdmin");
        ServiceClient client = restApiAdminStub._getServiceClient();
        Options options = client.getOptions();
        options.setTimeOutInMilliSeconds(15 * 60 * 1000);
        options.setProperty(HTTPConstants.SO_TIMEOUT, 15 * 60 * 1000);
        options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, 15 * 60 * 1000);
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public void addApi() throws AxisFault {
        try {
            String apiConfig = builder.getConfigStringForTemplate();
            restApiAdminStub.addApiFromString(apiConfig);
        } catch (Exception e) {
            throw new AxisFault("Error while adding new API", e);
        }
    }

    public void updateApi() throws AxisFault{
        try {
            String apiConfig = builder.getConfigStringForTemplate();
            restApiAdminStub.updateApiFromString(builder.getAPIName(), apiConfig);
        } catch (Exception e) {
            throw new AxisFault("Error while updating API", e);
        }
    }

    public void deleteApi() throws AxisFault{
        try {
            restApiAdminStub.deleteApi(builder.getAPIName());
        } catch (Exception e) {
            throw new AxisFault("Error while deleting API", e);
        }
    }
    
    private String login(APIManagerConfiguration config, String url) throws AxisFault {
        String user = config.getFirstProperty(APIConstants.API_GATEWAY_ADMIN_USERNAME);
        String password = config.getFirstProperty(APIConstants.API_GATEWAY_ADMIN_PASSWORD);
        String host = config.getFirstProperty(APIConstants.API_GATEWAY_ADMIN_HOST);
        if (url == null || user == null || password == null) {
            throw new AxisFault("Required API gateway admin configuration unspecified");
        }
        AuthenticationAdminStub authAdminStub = new AuthenticationAdminStub(null, url + "AuthenticationAdmin");
        ServiceClient client = authAdminStub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        try {
            authAdminStub.login(user, password, host);
            ServiceContext serviceContext = authAdminStub.
                    _getServiceClient().getLastOperationContext().getServiceContext();
            String sessionCookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
            return sessionCookie;
        } catch (RemoteException e) {
            throw new AxisFault("Error while contacting the authentication admin services", e);
        } catch (LoginAuthenticationExceptionException e) {
            throw new AxisFault("Error while authenticating against the API gateway admin", e);
        }
    }
}
