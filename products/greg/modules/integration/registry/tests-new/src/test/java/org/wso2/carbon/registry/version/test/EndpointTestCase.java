/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.wso2.carbon.registry.version.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.governance.ListMetaDataServiceClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.endpoints.EndpointManager;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.VersionPath;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.rmi.RemoteException;

import static junit.framework.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


public class EndpointTestCase {

    private static Registry governance = null;
    private ResourceAdminServiceClient resourceAdminClient;

    private static final Log log = LogFactory.getLog(ListMetaDataServiceClient.class);
    private static WSRegistryServiceClient registry = null;


    @BeforeClass
    public void initializeRegistry()
            throws RegistryException, RemoteException, LoginAuthenticationExceptionException {
        int userId = 1;
        UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        resourceAdminClient =
                new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               userInfo.getUserName(), userInfo.getPassword());
        registry = registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        governance = registryProviderUtil.getGovernanceRegistry(registry, userId);
    }


    @Test(groups = {"wso2.greg"}, description = "Create a new endpoint")
    public void testAddEndpoint()
            throws RegistryException, ResourceAdminServiceExceptionException, RemoteException {
        String endpoint_url = "http://ws.strikeiron.com/StrikeIron/donotcall2_5/DoNotCallRegistryUnique";

        Endpoint endpoint = createEndpoint(endpoint_url);
        assertTrue(registry.resourceExists(endpoint.getPath()), "Endpoint Resource Does not exists :");
        endpoint.createVersion();
        VersionPath[] vp = resourceAdminClient.getVersion(endpoint.getPath());
        assertEquals(1, vp.length);
    }


    private Endpoint createEndpoint(String endpoint_url) throws GovernanceException {
        EndpointManager endpointManager = new EndpointManager(governance);
        Endpoint endpoint1;
        try {
            endpoint1 = endpointManager.newEndpoint(endpoint_url);
            endpoint1.associateRegistry(registry);
            endpoint1.addAttribute("status1", "QA");
            endpoint1.addAttribute("status2", "Dev");
            endpointManager.addEndpoint(endpoint1);
            log.info("Endpoint was successfully added");
        } catch (GovernanceException e) {
            log.error("Unable add Endpoint:" + e);
            throw new GovernanceException("Unable to add Endpoint:" + e);
        }
        return endpoint1;
    }

}
