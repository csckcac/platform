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
package org.wso2.carbon.rest.api.ui.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.rest.api.stub.RestApiAdminStub;
import org.wso2.carbon.rest.api.ui.client.template.APITemplateBuilder;
import org.wso2.carbon.rest.api.ui.client.template.impl.BasicTemplateBuilder;

import java.util.*;

public class RestAPITemplateClient {

    private RestApiAdminStub stub;
    private APITemplateBuilder builder;

    public RestAPITemplateClient(APITemplateBuilder builder, String cookie) throws AxisFault {
        this.builder = builder;
        initStub(cookie);
    }

    private void initStub(String cookie) throws AxisFault {
        String serviceURL = AuthAdminServiceClient.SERVICE_URL + "RestApiAdmin";
        stub = new RestApiAdminStub(null, serviceURL);
        ServiceClient client = stub._getServiceClient();
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
            System.out.println(apiConfig);
            stub.addApiFromString(apiConfig);
        } catch (Exception e) {
//			handleException(bundle.getString("could.not.add.api"), e);
        }
    }

    public void updateApi() throws AxisFault{
        try {
            String apiConfig = builder.getConfigStringForTemplate();
            System.out.println(apiConfig);
            stub.updateApiFromString(builder.getAPIName(), apiConfig);
        } catch (Exception e) {
//            handleException(bundle.getString("could.not.update.api"), e);
        }
    }

    public void deleteApi() throws AxisFault{
        try {
            stub.deleteApi(builder.getAPIName());
        } catch (Exception e) {
//            handleException(bundle.getString("could.not.delete.api"), e);
        }
    }




    public static void main(String[] args) throws Exception {
        Map<String,String> testAPIMappings = new HashMap<String,String>();
        testAPIMappings.put(APITemplateBuilder.KEY_FOR_API_NAME, "DelciousAPI3");
        testAPIMappings.put(APITemplateBuilder.KEY_FOR_API_CONTEXT, "/v3");
        testAPIMappings.put(APITemplateBuilder.KEY_FOR_API_VERSION, "1.0.0");

        Map<String,String> testResourceMappings_1 = new HashMap<String,String>();
        testResourceMappings_1.put(APITemplateBuilder.KEY_FOR_RESOURCE_URI_TEMPLATE, "/tags/get");
        testResourceMappings_1.put(APITemplateBuilder.KEY_FOR_RESOURCE_METHODS, "GET");
        testResourceMappings_1.put(APITemplateBuilder.KEY_FOR_RESOURCE_URI, "https://api.del.icio.us");

        Map<String,String> testResourceMappings_2 = new HashMap<String,String>();
        testResourceMappings_2.put(APITemplateBuilder.KEY_FOR_RESOURCE_URI_TEMPLATE, "/posts/get");
        testResourceMappings_2.put(APITemplateBuilder.KEY_FOR_RESOURCE_METHODS, "GET");
        testResourceMappings_2.put(APITemplateBuilder.KEY_FOR_RESOURCE_URI, "https://api.del.icio.us");

        Map<String,String> testResourceMappings_3 = new HashMap<String,String>();
        testResourceMappings_3.put(APITemplateBuilder.KEY_FOR_RESOURCE_URI_TEMPLATE, "/posts/delete?url={posturl}");
        testResourceMappings_3.put(APITemplateBuilder.KEY_FOR_RESOURCE_METHODS, "DELETE");
        testResourceMappings_3.put(APITemplateBuilder.KEY_FOR_RESOURCE_URI, "https://api.del.icio.us");

        Map<String,String> testResourceMappings_4 = new HashMap<String,String>();
        testResourceMappings_4.put(APITemplateBuilder.KEY_FOR_RESOURCE_URI_TEMPLATE, "/posts/add?url={posturl};description={desc}");
        testResourceMappings_4.put(APITemplateBuilder.KEY_FOR_RESOURCE_METHODS, "POST");
        testResourceMappings_4.put(APITemplateBuilder.KEY_FOR_RESOURCE_URI, "https://api.del.icio.us");

        List<Map<String,String>> resourceMappings = new ArrayList<Map<String,String>>();
        resourceMappings.add(testResourceMappings_1);
        resourceMappings.add(testResourceMappings_2);
        resourceMappings.add(testResourceMappings_3);
        resourceMappings.add(testResourceMappings_4);

        Map<String, String> testHandlerMappings_1 = new HashMap<String, String>();
        testHandlerMappings_1.put(APITemplateBuilder.KEY_FOR_HANDLER, "org.wso2.carbon.apimgt.handlers.security.APIAuthenticationHandler");

        Map<String, String> testHandlerMappings_2 = new HashMap<String, String>();
        testHandlerMappings_2.put(APITemplateBuilder.KEY_FOR_HANDLER, "org.wso2.carbon.apimgt.handlers.throttling.APIThrottleHandler");
        testHandlerMappings_2.put(APITemplateBuilder.KEY_FOR_HANDLER_POLICY_KEY, "conf:/basic-throttle-policy.xml");

        List<Map<String,String>> handlerMappings = new ArrayList<Map<String,String>>();
        handlerMappings.add(testHandlerMappings_1);
        handlerMappings.add(testHandlerMappings_2);

        String adminCookie = AuthAdminServiceClient.login(AuthAdminServiceClient.HOST_NAME,
                                                         AuthAdminServiceClient.USER_NAME,
                                                         AuthAdminServiceClient.PASSWORD);

        /* AuthAdminServiceClient.setSystemProperties(AuthAdminServiceClient.CLIENT_TRUST_STORE_PATH,
AuthAdminServiceClient.KEY_STORE_TYPE,
AuthAdminServiceClient.KEY_STORE_PASSWORD);
boolean loggedin = new AuthWrapper().login(AuthAdminServiceClient.HOST_NAME,
        AuthAdminServiceClient.USER_NAME,
        AuthAdminServiceClient.PASSWORD);*/
        if (adminCookie != null) {
            System.out.println("logged in to the back-end server successfully....");
        } else {
            throw new RuntimeException("could not login to the back-end server.... /n  aborting...");
        }

        RestAPITemplateClient restAPIClient = new RestAPITemplateClient(new BasicTemplateBuilder(
                testAPIMappings, resourceMappings, handlerMappings),
                                                  adminCookie);
        restAPIClient.addApi();
//        restAPIClient.updateApi();


    }

}
