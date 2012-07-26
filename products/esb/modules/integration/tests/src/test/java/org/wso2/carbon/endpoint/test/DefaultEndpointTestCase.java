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

package org.wso2.carbon.endpoint.test;

import org.apache.axiom.om.util.AXIOMUtil;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.endpoint.EndPointAdminClient;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.endpoint.stub.types.EndpointAdminEndpointAdminException;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class DefaultEndpointTestCase {

    private static final String ENDPOINT_NAME = "defaultEpTest";
    private static EndPointAdminClient endPointAdminClient;

    @Test(groups = {"wso2.esb"})
    public void testDefaultEndpoint() throws IOException, EndpointAdminEndpointAdminException,
                                             LoginAuthenticationExceptionException,
                                             XMLStreamException {
        UserInfo userInfo =  UserListCsvReader.getUserInfo(1);
        EnvironmentBuilder builder = new EnvironmentBuilder().esb(1);

        ManageEnvironment environment = builder.build();

        endPointAdminClient = new EndPointAdminClient(environment.getEsb().getBackEndUrl(), userInfo.getUserName(), userInfo.getPassword());
        cleanupEndpoints();
        endpointAdditionScenario();
        endpointStatisticsScenario();
        endpointDeletionScenario();
    }

    private void cleanupEndpoints()
            throws RemoteException, EndpointAdminEndpointAdminException {
        String[] endpointNames = endPointAdminClient.getEndpointNames();
        List endpointList;
        if (endpointNames != null && endpointNames.length > 0 && endpointNames[0] != null) {
            endpointList = Arrays.asList(endpointNames);
            if (endpointList.contains(ENDPOINT_NAME)) {
                endPointAdminClient.deleteEndpoint(ENDPOINT_NAME);
            }
        }
    }

    private void endpointAdditionScenario()
            throws IOException, EndpointAdminEndpointAdminException, XMLStreamException {
        int beforeCount = endPointAdminClient.getEndpointCount();

        endPointAdminClient.addEndPoint(AXIOMUtil.stringToOM("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                                             "<endpoint xmlns=\"http://ws.apache.org/ns/synapse\" name=\"" + ENDPOINT_NAME + "\">\n" +
                                                             "    <default/>\n" +
                                                             "</endpoint>"));
        int afterCount = endPointAdminClient.getEndpointCount();
        assertEquals(1, afterCount - beforeCount);

        String[] endpoints = endPointAdminClient.getEndpointNames();
        if (endpoints != null && endpoints.length > 0 && endpoints[0] != null) {
            List endpointList = Arrays.asList(endpoints);
            assertTrue(endpointList.contains(ENDPOINT_NAME));
        } else {
            fail("Endpoint has not been added to the system properly");
        }
    }

    private void endpointStatisticsScenario()
            throws RemoteException, EndpointAdminEndpointAdminException {
        endPointAdminClient.enableEndpointStatistics(ENDPOINT_NAME);
        String endpoint = endPointAdminClient.getEndpointConfiguration(ENDPOINT_NAME);
        assertTrue(endpoint.contains("statistics=\"enable"));
    }

    private void endpointDeletionScenario()
            throws RemoteException, EndpointAdminEndpointAdminException {
        int beforeCount = endPointAdminClient.getEndpointCount();
        endPointAdminClient.deleteEndpoint(ENDPOINT_NAME);
        int afterCount = endPointAdminClient.getEndpointCount();
        assertEquals(1, beforeCount - afterCount);
    }
}

