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
package org.wso2.automation.common.test.greg.wsapi;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;
import org.testng.annotations.*;

import static org.testng.Assert.*;

import java.util.HashMap;
import java.util.Map;

public class QueryTest {
    private static final Log log = LogFactory.getLog(QueryTest.class);
    private static WSRegistryServiceClient registry = null;


    @BeforeClass(alwaysRun = true)
    public void init() throws RegistryException, AxisFault {
        int tenantId = 0; // only admin user can execute querires
        registry = new RegistryProvider().getRegistry(tenantId, ProductConstant.GREG_SERVER_NAME);
        removeResource();
    }

    @Test(groups = {"wso2.greg"}, description = "Put Registry Queries", priority = 1)
    private void testPutRegistryQueries() throws Exception {
        String QUERY_EPR_BY_PATH = "/Queries1/EPRByPath";
        Resource resource1 = null;
        Resource r1 = null;
        String sql = "";

        try {
            resource1 = registry.newResource();
            sql = "SELECT PATH FROM REG_RESOURCE WHERE  REG_PATH LIKE ?";
            resource1.setContent(sql);
            resource1.setMediaType(RegistryConstants.SQL_QUERY_MEDIA_TYPE);
            resource1.addProperty(RegistryConstants.RESULT_TYPE_PROPERTY_NAME,
                    RegistryConstants.RESOURCES_RESULT_TYPE);

            boolean exists = registry.resourceExists(QUERY_EPR_BY_PATH);

            if (!exists)
                registry.put(QUERY_EPR_BY_PATH, resource1);

            assertTrue(registry.resourceExists(QUERY_EPR_BY_PATH), "Resource doesn't exists");

            r1 = registry.get(QUERY_EPR_BY_PATH);
            assertEquals(sql, new String((byte[]) r1.getContent()), "File content is not matching");
            assertEquals(RegistryConstants.SQL_QUERY_MEDIA_TYPE, r1.getMediaType(), "Media type doesn't match");
            assertEquals(RegistryConstants.SQL_QUERY_MEDIA_TYPE, "application/vnd.sql.query", "Media type doesn't match");
            deleteResources(QUERY_EPR_BY_PATH);
            log.info("***********WS-API put Registry Queries test - Passed***********");
        } catch (Exception e) {
            log.error("WS-API put Registry Queries test -fail :" + e);
            throw new Exception("WS-API put Registry Queries test -fail:" + e);
        }

    }

    @Test(groups = {"wso2.greg"}, description = "Execute Registry Queries", priority = 2)
    private void testExecuteQueries() throws Exception {
        String QUERY_EPR_BY_PATH = "/Queries1/EPRByPath-new";
        Resource resource1 = null;

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
            deleteResources("/Queries1");
            log.info("*************WS-API Execute Queries test - Passed*************");
        } catch (Exception e) {
            log.error("WS-API Execute Queries test -fail:" + e);
            throw new Exception("WS-API Execute Queries test -fail:" + e);
        }
    }

    private void removeResource() throws RegistryException {
        deleteResources("/Queries1");
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


    private void storeSQLQuery(String path) throws RegistryException {
        String sql1 = "SELECT REG_PATH_ID, REG_NAME FROM REG_RESOURCE R WHERE R.REG_MEDIA_TYPE LIKE ?";
        Resource q1 = registry.newResource();
        try {
            q1.setContent(sql1);
            q1.setMediaType(RegistryConstants.SQL_QUERY_MEDIA_TYPE);
            q1.addProperty(RegistryConstants.RESULT_TYPE_PROPERTY_NAME,
                    RegistryConstants.RESOURCES_RESULT_TYPE);
            registry.put(path, q1);
        } catch (RegistryException e) {
            log.error("storeSQLQuery RegistryException thrown-fail :" + e);
            throw new RegistryException("storeSQLQuery RegistryException thrown -fail:" + e);
        }

    }

}
