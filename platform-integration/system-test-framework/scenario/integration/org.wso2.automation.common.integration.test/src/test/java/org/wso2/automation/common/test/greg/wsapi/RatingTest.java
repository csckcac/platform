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
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.platform.test.core.ProductConstant;
import org.testng.annotations.*;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;

import static org.testng.Assert.*;


public class RatingTest {
    private static final Log log = LogFactory.getLog(RatingTest.class);
    private static WSRegistryServiceClient registry = null;


    @BeforeClass(alwaysRun = true)
    public void init() throws RegistryException, AxisFault {
        int tenantId = new GregUserIDEvaluator().getTenantID();
        registry = new RegistryProvider().getRegistry(tenantId, ProductConstant.GREG_SERVER_NAME);
        removeResource();
    }

    @Test(groups = {"wso2.greg"}, description = "test add a rating to a resource", priority = 1)
    private void testAddResourceRating() throws RegistryException {
        String path = "/d16/d17/r1";
        Resource r1 = registry.newResource();
        byte[] r1content = "R1 content".getBytes();

        try {
            r1.setContent(r1content);
            registry.put(path, r1);
            registry.rateResource(path, 5);

            float rating = registry.getAverageRating(path);
            assertEquals(rating, (float) 5.0, (float) 0.01,
                         "Rating of the resource /d16/d17/r1 should be 5.");
            deleteResources("/d16");
            log.info("************WS-API Add a Rating to a Resource test  - Passed***************");
        } catch (RegistryException e) {
            log.error("WS-API Add a Rating to a Resource test - Failed" + e);
            throw new RegistryException("WS-API Add a Rating to a Resource test - Failed:"
                                        + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "test add a rating to a collection", priority = 2)
    private void testAddCollectionRating() throws RegistryException {
        String path = "/d16/d18";
        Resource r1 = registry.newCollection();

        try {
            registry.put(path, r1);
            registry.rateResource(path, 4);
            float rating = registry.getAverageRating(path);
            assertEquals(rating, (float) 4.0, (float) 0.01,
                         "Rating of the resource /d16/d18 should be 5.");
            deleteResources("/d16");
            log.info("***************WS-API Add a Rating to a Collection test - Passed ***********");
        } catch (RegistryException e) {
            log.error("WS-API Add a Rating to a Collection test - Failed:" + e);
            throw new RegistryException("WS-API Add a Rating to a Collection test -Failed:"
                                        + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "test add a rating to a collection", priority = 3)
    private void testEditResourceRating() throws RegistryException {
        String path = "/d61/d17/d18/r1";
        Resource r1 = registry.newResource();
        byte[] r1content = "R1 content".getBytes();

        try {
            r1.setContent(r1content);
            registry.put(path, r1);
            registry.rateResource(path, 5);
            float rating = registry.getAverageRating(path);

            assertEquals((float) 5.0, rating, (float) 0.01,
                         "Rating of the resource /d61/d17/d18/r1 should be 5.");

            registry.rateResource(path, 3);
            float rating_edit = registry.getAverageRating(path);
            assertEquals((float) 3.0, rating_edit, (float) 0.01,
                         "Rating of the resource /d61/d17/d18/r1 should be 3.");

            deleteResources("/d61");
            log.info("**************WS-API Edit Rating of a Resource test- Passed ***************");
        } catch (RegistryException e) {
            log.error("WS-API Edit Rating of a Resource test- Failed:" + e);
            throw new RegistryException("WS-API Edit Rating of a Resource test - Failed"
                                        + e);
        }
    }


    @Test(groups = {"wso2.greg"}, description = "test apply rating to path", priority = 4)
    private void testRatingsPath() throws RegistryException {
        Resource r5 = registry.newResource();
        String r5Content = "this is r5 content";

        try {
            r5.setContent(r5Content.getBytes());
            r5.setDescription("production ready.");
            String r5Path = "/c1/r5";
            registry.put(r5Path, r5);
            registry.rateResource("/c1/r5", 3);
            String[] ratingPaths;
            Resource ratings = registry.get("/c1/r5;ratings");
            ratingPaths = (String[]) ratings.getContent();
            int rating;
            Resource c1 = registry.get(ratingPaths[0]);

            Object o = c1.getContent();
            if (o instanceof Integer) {
                rating = (Integer) o;
            } else {
                rating = Integer.parseInt(o.toString());
            }

            assertEquals(rating, 3, "Ratings are not retrieved properly as resources.");
            deleteResources("/c1");
            log.info("*************WS- API Apply Rating to a Path test - Passed *****************");
        } catch (RegistryException e) {
            log.error("WS- API Apply Rating to a Path test -Failed:" + e);
            throw new RegistryException("WS- API Apply Rating to a Path test -Failed:"
                                        + e);
        }
    }


    private void removeResource() throws RegistryException {
        deleteResources("/d16");
        deleteResources("/d61");
        deleteResources("/c1");
    }

    public void deleteResources(String resourceName) throws RegistryException {
        try {
            if (registry.resourceExists(resourceName)) {
                registry.delete(resourceName);
            }
        } catch (RegistryException e) {
            log.error("deleteResources RegistryException thrown:" + e);
            throw new RegistryException("deleteResources RegistryException thrown:"
                                        + e);
        }
    }
}
