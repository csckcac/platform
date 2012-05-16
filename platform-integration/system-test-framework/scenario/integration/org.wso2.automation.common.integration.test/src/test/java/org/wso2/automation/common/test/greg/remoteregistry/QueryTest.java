/*
* Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.automation.common.test.greg.remoteregistry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import static org.testng.Assert.*;

import org.testng.annotations.*;
import org.wso2.platform.test.core.utils.gregutils.GregRemoteRegistryProvider;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

public class QueryTest {
    private static final Log log = LogFactory.getLog(QueryTest.class);
    public RemoteRegistry registry;

    @BeforeClass(alwaysRun = true)
    public void init() throws MalformedURLException, RegistryException {
        int tenantId = new GregUserIDEvaluator().getTenantID();
        registry = new GregRemoteRegistryProvider().getRegistry(tenantId);
        removeResource();
    }


    @Test(groups = {"wso2.greg"}, description = "test add an query to registry", priority = 1)
    public void putRegistryQueriesTest() throws RegistryException {
        String QUERY_EPR_BY_PATH = "/_system/Queries1/EPRByPath-new";

        try {
            storeSQLQuery(QUERY_EPR_BY_PATH);
            assertTrue(registry.resourceExists(QUERY_EPR_BY_PATH), "Resource doesn't exists");

            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("1", RegistryConstants.SQL_QUERY_MEDIA_TYPE); // media type
            Collection collection = registry.executeQuery(QUERY_EPR_BY_PATH, parameters);
            String[] children = collection.getChildren();

            boolean successful = false;
            for (String path : children) {
                if (path.contains(QUERY_EPR_BY_PATH)) successful = true;
            }
            deleteResources("/_system/Queries1");
            log.info("**************Registry API Put Registry Queries Test - Passed*****************************");
            assertTrue(successful);
        } catch (Exception e) {
            log.error("*Registry API Put Registry Queries Test-Failed :" + e);
            throw new RegistryException("*Registry API Put Registry Queries Test - Failed" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "test adding a comment", priority = 2)
    public void executeQueriesTest() throws RegistryException {
        String QUERY_EPR_BY_PATH = "/_system/Queries1/EPRByPath-new";

        try {
            storeSQLQuery(QUERY_EPR_BY_PATH);
            assertTrue(registry.resourceExists(QUERY_EPR_BY_PATH), "Resource doesn't exists");

            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("1", RegistryConstants.SQL_QUERY_MEDIA_TYPE); // media type
            Collection collection = registry.executeQuery(QUERY_EPR_BY_PATH, parameters);
            String[] children = collection.getChildren();

            boolean successful = false;
            for (String path : children) {
                if (path.contains(QUERY_EPR_BY_PATH)) successful = true;
            }
            assertTrue(successful);
            deleteResources("/_system/Queries1");
            log.info("***************Registry API Put Registry Queries Test - Passed ******************");
        } catch (Exception e) {
            log.error("Registry API Put Registry Queries Test - Failed :" + e);
            throw new RegistryException("Registry API Put Registry Queries Test - Failed" + e);
        }
    }


    private void storeSQLQuery(String path) throws RegistryException, Exception {
        String sql1 = "SELECT REG_PATH_ID, REG_NAME FROM REG_RESOURCE R WHERE R.REG_MEDIA_TYPE LIKE ?";
        Resource q1 = registry.newResource();
        q1.setContent(sql1);
        q1.setMediaType(RegistryConstants.SQL_QUERY_MEDIA_TYPE);
        q1.addProperty(RegistryConstants.RESULT_TYPE_PROPERTY_NAME,
                RegistryConstants.RESOURCES_RESULT_TYPE);
        registry.put(path, q1);
    }

    private void removeResource() throws RegistryException {
        deleteResources("/_system/Queries1");

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
