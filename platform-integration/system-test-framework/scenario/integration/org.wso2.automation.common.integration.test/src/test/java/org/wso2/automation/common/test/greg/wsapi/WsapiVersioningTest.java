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

package org.wso2.automation.common.test.greg.wsapi;

import org.apache.axis2.AxisFault;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

public class WsapiVersioningTest {
    String sessionCookie = null;
    private static final Log log = LogFactory.getLog(WsapiVersioningTest.class);
    private static WSRegistryServiceClient registry = null;
    final String versionResourcePath = "test/versioning/putResource";
    List AddedVersion = null;


    @BeforeTest(alwaysRun = true)
    public void init() throws RegistryException, AxisFault {

        int tenantId = new GregUserIDEvaluator().getTenantID();
        registry = new RegistryProvider().getRegistry(tenantId, ProductConstant.GREG_SERVER_NAME);

    }

    @BeforeClass(alwaysRun = true, groups = {"wso2.greg", "wso2.greg.WsApi"})
    public void deployArtifact() throws RegistryException {
        Resource r1 = registry.newResource();
        r1.setProperty("test", "symbolicLink");
        r1.setContent("link1");
        registry.put(versionResourcePath, r1);

    }

    @Test(groups = {"wso2.greg", "wso2.greg.GovernanceApiWsdl"}, description = "Adds version to a registry resource", priority = 1)
    public void CreateVersion() throws Exception, RemoteException {
        String[] versionsBeforeAdd = registry.getVersions(versionResourcePath);

        registry.createVersion(versionResourcePath);
        String[] versionsAfterAdd = registry.getVersions(versionResourcePath);
        Assert.assertTrue(versionsAfterAdd.length == versionsBeforeAdd.length + 1);
        AddedVersion = getAddedVersion(versionsBeforeAdd, versionsAfterAdd);
        Assert.assertTrue(AddedVersion.size() >= 0);
    }

    @Test(groups = {"wso2.greg", "wso2.greg.GovernanceApiWsdl"}, description = "remove added version", priority = 2)
    public void AddLargeSetofVersions() throws Exception, RemoteException {
        String[] versionsBeforeAdd = registry.getVersions(versionResourcePath);
        for (int index = 0; index <= 1000; index++) {
            registry.createVersion(versionResourcePath);
        }
        String[] versionsAfterAdd = registry.getVersions(versionResourcePath);
        AddedVersion = getAddedVersion(versionsBeforeAdd, versionsAfterAdd);
        Assert.assertTrue(AddedVersion.size() >= 0);
    }


    @AfterClass(alwaysRun = true)
    public void removeArtifacts() throws RegistryException {
        deleteResources("test");
    }

    private List getAddedVersion(String[] beforeAdd, String[] afteradd) {
        String newVersion = null;
        List diff = ListUtils.subtract(Arrays.asList(afteradd), Arrays.asList(beforeAdd));
        return diff;
    }

    public void deleteResources(String resourceName) throws RegistryException {
        try {
            if (registry.resourceExists(resourceName)) {
                registry.delete(resourceName);
            }
        } catch (RegistryException e) {
            log.error("deleteResources RegistryException thrown:" + e);
            throw new RegistryException("deleteResources RegistryException thrown:" + e);
        }
    }
}
