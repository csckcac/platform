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

package org.wso2.carbon.registry.governance.api.test;

import org.apache.axis2.AxisFault;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.governance.api.endpoints.EndpointManager;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import static org.testng.Assert.assertTrue;

/**
 * Class will test all the API methods od EndPointManager
 */
public class EndpointManagerAPITestCase {

    public static EndpointManager endpointManager;
    private static Endpoint endPointObj;
    private String sampleEndPoint = "http://localhost:9763/services/TestEndPointManager";
    private Registry governanceRegistryObj;

    @BeforeClass(alwaysRun = true)
    public void initializeAPIObject() throws RegistryException, AxisFault {
        governanceRegistryObj = TestUtils.getRegistry();
        TestUtils.cleanupResources(governanceRegistryObj);
        endpointManager = new EndpointManager(governanceRegistryObj);
    }

    @Test(groups = {"wso2.greg.api"}, description = "Testing newEndpoint API method", priority = 1)
    public void testNewEndpoint() throws GovernanceException {
        try {
            endPointObj = endpointManager.newEndpoint(sampleEndPoint);
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing EndpointManager:newEndpoint method" + e);
        }
    }

    @Test(groups = {"wso2.greg.api"}, dependsOnMethods = {"testNewEndpoint"}, description = "Testing " +
                                                                                            "addEndpoint API method", priority = 2)
    public void testAddEndpoint() throws GovernanceException {
        try {
            endpointManager.addEndpoint(endPointObj);
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing EndpointManager:addEndpoint method" + e);
        }
    }

    @Test(groups = {"wso2.greg.api"}, dependsOnMethods = {"testAddEndpoint"}, description = "Testing " +
                                                                                            "getEndpointByUrl API method", priority = 3)
    public void testGetEndpointByUrl() throws GovernanceException {
        try {
            endPointObj = endpointManager.getEndpointByUrl(sampleEndPoint);
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing EndpointManager:getEndpointByUrl method" + e);
        }
        assertTrue(endPointObj.getUrl().equalsIgnoreCase(sampleEndPoint), "getEndpointByUrl method not " +
                                                                          "returning valid EndPoint object");
    }

    @Test(groups = {"wso2.greg.api"}, dependsOnMethods = {"testGetEndpointByUrl"}, description = "Testing " +
                                                                                                 "getEndpoint API method", priority = 4)
    public void testGetEndpoint() throws GovernanceException {
        Endpoint localEndpointObj;
        try {
            localEndpointObj = endpointManager.getEndpoint(endPointObj.getId());
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing EndpointManager:getEndpoint method" + e);
        }
        assertTrue(localEndpointObj.getUrl().equalsIgnoreCase(sampleEndPoint), "getEndpoint method not " +
                                                                               "returning valid EndPoint object");
    }

    @Test(groups = {"wso2.greg.api"}, dependsOnMethods = {"testNewEndpoint"}, description = "Testing " +
                                                                                            "setContent API method", priority = 5)
    public void testSetContent() throws RegistryException {
        Resource resource = governanceRegistryObj.newResource();
        try {
            endpointManager.setContent(endPointObj, resource);
            assertTrue(resource.getContent().toString().contains(sampleEndPoint), "setContent API method doesn't work");
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing EndpointManager:setContent method" + e);
        } catch (RegistryException e) {
            throw new GovernanceException("Error occurred while executing EndpointManager:setContent method" + e);
        }
    }

    //https://wso2.org/jira/browse/CARBON-13209
    @Test(groups = {"wso2.greg.api"}, description = "Testing updateEndpoint API method", priority = 5)
    public void testUpdateEndpoint() throws GovernanceException {
        Endpoint endpointObj;
        try {
            endpointObj = endpointManager.getEndpointByUrl(sampleEndPoint);
            endpointManager.updateEndpoint(endPointObj);
            endpointObj.attachLifecycle("ServiceLifeCycle");

        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing EndpointManager:updateEndpoint method" + e);
        }

        Endpoint endpointObjNew = endpointManager.getEndpointByUrl(sampleEndPoint);
        assertTrue(endpointObjNew.getLifecycleName().equalsIgnoreCase("ServiceLifeCycle"), "Error occurred " +
                                                                                           "while executing EndpointManager:updateEndpoint method");
    }

    @Test(groups = {"wso2.greg.api"}, description = "Testing removeEndpoint API method", priority = 6)
    public void testRemoveEndpoint() throws GovernanceException {
        Endpoint localEndPoint = endpointManager.getEndpointByUrl(sampleEndPoint);
        try {
            endpointManager.removeEndpoint(localEndPoint.getId());
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing EndpointManager:removeEndpoint method" + e);
        }
    }

}
